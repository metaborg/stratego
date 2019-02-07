package mb.stratego.build;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.CommonPaths;

import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class StrIncr implements TaskDef<StrIncr.Input, None> {
    public static final class Input implements Serializable {
        final File inputFile;
        final String javaPackageName;
        final Collection<File> includeDirs;
        final File cacheDir;
        final Arguments extraArgs;
        final File outputPath;
        final Collection<STask<?>> originTasks;
        final File projectLocation;

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!inputFile.equals(input.inputFile))
                return false;
            if(!javaPackageName.equals(input.javaPackageName))
                return false;
            if(!includeDirs.equals(input.includeDirs))
                return false;
            if(!cacheDir.equals(input.cacheDir))
                return false;
            if(!extraArgs.equals(input.extraArgs))
                return false;
            if(!outputPath.equals(input.outputPath))
                return false;
            //noinspection SimplifiableIfStatement
            if(!originTasks.equals(input.originTasks))
                return false;
            return projectLocation.equals(input.projectLocation);
        }

        @Override public int hashCode() {
            int result = inputFile.hashCode();
            result = 31 * result + javaPackageName.hashCode();
            result = 31 * result + includeDirs.hashCode();
            result = 31 * result + cacheDir.hashCode();
            result = 31 * result + extraArgs.hashCode();
            result = 31 * result + outputPath.hashCode();
            result = 31 * result + originTasks.hashCode();
            result = 31 * result + projectLocation.hashCode();
            return result;
        }

        public Input(File inputFile, String javaPackageName, Collection<File> includeDirs, File cacheDir,
            Arguments extraArgs, File outputPath, Collection<STask<?>> originTasks, File projectLocation) {
            this.inputFile = inputFile;
            this.javaPackageName = javaPackageName;
            this.includeDirs = includeDirs;
            this.cacheDir = cacheDir;
            this.extraArgs = extraArgs;
            this.outputPath = outputPath;
            this.originTasks = originTasks;
            this.projectLocation = projectLocation;

        }

    }

    private static final ILogger logger = LoggerUtils.logger(StrIncr.class);


    private final IResourceService resourceService;

    private final StrIncrFront strIncrFront;
    private final StrIncrBack strIncrBack;

    @Inject public StrIncr(IResourceService resourceService, StrIncrFront strIncrFront, StrIncrBack strIncrBack) {
        this.resourceService = resourceService;
        this.strIncrFront = strIncrFront;
        this.strIncrBack = strIncrBack;
    }

    @Override public None exec(ExecContext execContext, Input input) throws ExecException, InterruptedException {
        /*
         * Note that we require the sdf tasks here to force it to generated needed str files. We then discover those in
         * this method with a directory search, and start a front-end task for each. Every front-end task also depends
         * on the sdf tasks so there is no hidden dep. To make sure that front-end tasks only run when their input
         * _files_ change, we need the front-end to depend on the sdf tasks with a simple stamper that allows the
         * execution of the sdf task to be ignored.
         */
        for(final STask<?> t : input.originTasks) {
            execContext.require(t);
        }

        logger.debug("Starting time measurement");
        long startTime = System.nanoTime();

        final FileObject location = resourceService.resolve(input.projectLocation);

        // FRONTEND
        final Set<File> seen = new HashSet<>();
        final Deque<File> workList = new ArrayDeque<>();
        workList.add(input.inputFile);
        seen.add(input.inputFile);

        final List<File> boilerplateFiles = new ArrayList<>();
        final List<STask<?>> allFrontEndTasks = new ArrayList<>();
        final Map<String, Set<File>> strategyFiles = new HashMap<>();
        final Map<String, Set<String>> strategyConstrFiles = new HashMap<>();
        final Map<String, Set<File>> overlayFiles = new HashMap<>();
        final Map<String, List<STask<?>>> strategyOrigins = new HashMap<>();
        final Map<String, List<STask<?>>> overlayOrigins = new HashMap<>();

        long frontEndStartTime;
        long frontEndTime = 0;
        long shuffleStartTime;
        long shuffleTime = 0;
        do {
            frontEndStartTime = System.nanoTime();
            final File strFile = workList.remove();
            final String projectName = projectName(strFile);
            final StrIncrFront.Input frontEndInput =
                new StrIncrFront.Input(input.projectLocation, strFile, projectName, input.originTasks);
            final Task<StrIncrFront.Input, StrIncrFront.Output> task = strIncrFront.createTask(frontEndInput);
            final StrIncrFront.Output frontEndOutput = execContext.require(task);
            shuffleStartTime = System.nanoTime();
            frontEndTime += shuffleStartTime - frontEndStartTime;

            // shuffling output for backend
            allFrontEndTasks.add(task.toSTask());
            boilerplateFiles
                .add(resourceService.localPath(CommonPaths.strSepCompBoilerplateFile(location, projectName, frontEndOutput.moduleName)));
            for(Map.Entry<String, File> gen : frontEndOutput.strategyFiles.entrySet()) {
                String strategyName = gen.getKey();
                getOrInitialize(strategyFiles, strategyName, HashSet::new).add(gen.getValue());
                getOrInitialize(strategyConstrFiles, strategyName, HashSet::new)
                    .addAll(frontEndOutput.strategyConstrFiles.get(strategyName));
                getOrInitialize(strategyOrigins, strategyName, ArrayList::new).add(task.toSTask());
            }
            for(Map.Entry<String, File> gen : frontEndOutput.overlayFiles.entrySet()) {
                final String overlayName = gen.getKey();
                getOrInitialize(overlayFiles, overlayName, HashSet::new).add(gen.getValue());
                getOrInitialize(overlayOrigins, overlayName, ArrayList::new).add(task.toSTask());
            }

            // resolving imports
            for(StrIncrFront.Import i : frontEndOutput.imports) {
                final Set<File> resolvedImport;
                try {
                    resolvedImport = i.resolveImport(input.includeDirs);
                } catch(IOException e) {
                    throw new ExecException(e);
                }
                resolvedImport.removeAll(seen);
                workList.addAll(resolvedImport);
                seen.addAll(resolvedImport);
            }

            shuffleTime += System.nanoTime() - shuffleStartTime;
        } while(!workList.isEmpty());

        long betweenFrontAndBack = System.nanoTime();
        logger.debug("Frontends overall took: {} ns", betweenFrontAndBack - startTime);
        logger.debug("Purely frontend tasks took: {} ns", frontEndTime);
        logger.debug("While shuffling information and tracking imports took: {} ns", shuffleTime);

        // BACKEND
        for(String strategyName : strategyFiles.keySet()) {
            List<STask<?>> backEndOrigin = new ArrayList<>(strategyOrigins.size());
            backEndOrigin.addAll(strategyOrigins.get(strategyName));
            @Nullable File strategyDir = resourceService.localPath(CommonPaths.strSepCompStrategyDir(location, strategyName));
            assert strategyDir != null : "Bug in strSepCompStrategyDir or the arguments thereof: returned path is not a directory";
            List<File> strategyOverlayFiles = new ArrayList<>();
            for(String overlayName : strategyConstrFiles.get(strategyName)) {
                final Set<File> theOverlayFiles = overlayFiles.get(overlayName);
                if(theOverlayFiles != null) {
                    strategyOverlayFiles.addAll(theOverlayFiles);
                }
                final List<STask<?>> overlayOriginBuilder = overlayOrigins.get(overlayName);
                if(overlayOriginBuilder != null) {
                    backEndOrigin.addAll(overlayOriginBuilder);
                }
            }
            StrIncrBack.Input backEndInput = new StrIncrBack.Input(backEndOrigin, input.projectLocation, strategyName,
                strategyDir, Arrays.asList(strategyFiles.get(strategyName).toArray(new File[0])),
                strategyOverlayFiles, input.javaPackageName,
                input.outputPath, input.cacheDir, input.extraArgs, false);
            execContext.require(strIncrBack.createTask(backEndInput));
        }
        // boilerplate task
        @Nullable File strSrcGenDir = resourceService.localPath(CommonPaths.strSepCompSrcGenDir(location));
        assert strSrcGenDir != null : "Bug in strSepCompSrcGenDir or the arguments thereof: returned path is not a directory";
        StrIncrBack.Input backEndInput = new StrIncrBack.Input(allFrontEndTasks, input.projectLocation, null,
            strSrcGenDir, boilerplateFiles, Collections.emptyList(), input.javaPackageName, input.outputPath,
            input.cacheDir, input.extraArgs, true);
        execContext.require(strIncrBack.createTask(backEndInput));

        long finishTime = System.nanoTime();
        logger.debug("Backends overall took: {} ns", finishTime - betweenFrontAndBack);

        logger.debug("Full Stratego incremental build took: {} ns", finishTime - startTime);
        return None.getInstance();
    }

    private static String projectName(File inputFile) {
        // TODO: *can* we get the project name somehow?
        return Integer.toString(inputFile.toString().hashCode());
    }

    private static <K, V> V getOrInitialize(Map<K, V> map, K key, Supplier<V> initialize) {
        map.computeIfAbsent(key, ignore -> initialize.get());
        return map.get(key);
    }
    
    @Override public String getId() {
        return StrIncr.class.getCanonicalName();
    }

    @Override public Serializable key(Input input) {
        return input.inputFile;
    }

    @Override public String desc(Input input) {
        return this.getId() + "(" + input + ")";
    }

    @Override public String desc(Input input, int maxLength) {
        return desc(input);
    }

    @Override public Task<Input, None> createTask(Input input) {
        return new Task<>(this, input);
    }

    @Override public STask<Input> createSerializableTask(Input input) {
        return new STask<>(this.getId(), input);
    }
}
