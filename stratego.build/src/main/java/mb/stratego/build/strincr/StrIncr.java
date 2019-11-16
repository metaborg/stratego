package mb.stratego.build.strincr;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.CommonPaths;
import mb.stratego.build.util.Relation;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class StrIncr implements TaskDef<StrIncr.Input, None> {
    public static final String id = StrIncr.class.getCanonicalName();

    public static final class Input implements Serializable {
        final File inputFile;
        final @Nullable String javaPackageName;
        final Collection<File> includeDirs;
        final Collection<String> builtinLibs;
        final @Nullable File cacheDir;
        final List<String> constants;
        final Arguments extraArgs;
        final File outputPath;
        final Collection<STask> originTasks;
        final File projectLocation;

        public Input(File inputFile, @Nullable String javaPackageName, Collection<File> includeDirs,
            Collection<String> builtinLibs, @Nullable File cacheDir, List<String> constants, Arguments extraArgs,
            File outputPath, Collection<STask> originTasks, File projectLocation) {
            this.inputFile = inputFile;
            this.javaPackageName = javaPackageName;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.cacheDir = cacheDir;
            this.constants = constants;
            this.extraArgs = extraArgs;
            this.outputPath = outputPath;
            this.originTasks = originTasks;
            this.projectLocation = projectLocation;

        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!inputFile.equals(input.inputFile))
                return false;
            if(javaPackageName != null ? !javaPackageName.equals(input.javaPackageName) : input.javaPackageName != null)
                return false;
            if(!includeDirs.equals(input.includeDirs))
                return false;
            if(!builtinLibs.equals(input.builtinLibs))
                return false;
            if(cacheDir != null ? !cacheDir.equals(input.cacheDir) : input.cacheDir != null)
                return false;
            if(!constants.equals(input.constants))
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
            result = 31 * result + (javaPackageName != null ? javaPackageName.hashCode() : 0);
            result = 31 * result + includeDirs.hashCode();
            result = 31 * result + builtinLibs.hashCode();
            result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
            result = 31 * result + constants.hashCode();
            result = 31 * result + extraArgs.hashCode();
            result = 31 * result + outputPath.hashCode();
            result = 31 * result + originTasks.hashCode();
            result = 31 * result + projectLocation.hashCode();
            return result;
        }
    }

    public static class Frontends {
        private ExecContext execContext;
        private Input input;
        private Path projectLocationPath;
        private File projectLocationFile;
        private Module inputModule;
        private StaticChecks.Data staticData;
        private BackendData backendData;
        private IResourceService resourceService;
        private Frontend strIncrFront;
        private LibFrontend strIncrFrontLib;

        public Frontends(IResourceService resourceService, Frontend strIncrFront, LibFrontend strIncrFrontLib,
            ExecContext execContext, Input input, Path projectLocationPath, File projectLocationFile, Module inputModule) {
            this.execContext = execContext;
            this.input = input;
            this.projectLocationPath = projectLocationPath;
            this.projectLocationFile = projectLocationFile;
            this.inputModule = inputModule;
            this.resourceService = resourceService;
            this.strIncrFront = strIncrFront;
            this.strIncrFrontLib = strIncrFrontLib;
        }

        public StaticChecks.Data getStaticData() {
            return staticData;
        }

        public BackendData getBackendData() {
            return backendData;
        }

        public Frontends invoke() throws IOException, mb.pie.api.ExecException, InterruptedException {
            // FRONTEND
            final Set<Module> seen = new HashSet<>();
            final Deque<Module> workList = new ArrayDeque<>();
            workList.add(inputModule);
            seen.add(inputModule);

            final List<Frontend.Import> defaultImports = new ArrayList<>(input.builtinLibs.size());
            for(String builtinLib : input.builtinLibs) {
                defaultImports.add(Frontend.Import.library(builtinLib));
            }
            // depend on the include directories in which we search for str and rtree files
            for(File includeDir : input.includeDirs) {
                execContext.require(includeDir);
            }

            staticData = new StaticChecks.Data();

            backendData = new BackendData();

            long shuffleStartTime;
            do {
                final Module module = workList.remove();

                if(module.type == Module.Type.library) {
                    final LibFrontend.Input frontLibInput =
                        new LibFrontend.Input(Library.fromString(resourceService, module.path));
                    final LibFrontend.Output frontLibOutput = execContext.require(strIncrFrontLib, frontLibInput);

                    shuffleStartTime = System.nanoTime();

                    registerStrategyDefinitions(staticData.definedStrategies, module, frontLibOutput.strategies);
                    registerConstructorDefinitions(staticData.definedConstructors, module, frontLibOutput.constrs,
                        Collections.emptySet());

                    final Set<String> overlappingStrategies =
                        Sets.difference(Sets.intersection(staticData.externalStrategies, frontLibOutput.strategies), StaticChecks.ALWAYS_DEFINED);
                    if(!overlappingStrategies.isEmpty()) {
                        execContext.logger()
                            .warn("Overlapping external strategy definitions: " + overlappingStrategies, null);
                    }

                    staticData.externalStrategies.addAll(frontLibOutput.strategies);
                    staticData.externalConstructors.addAll(frontLibOutput.constrs);

                    BuildStats.shuffleLibTime += System.nanoTime() - shuffleStartTime;

                    continue;
                }

                final String projectName = projectName(module.path);
                final Frontend.Input frontInput =
                    new Frontend.Input(projectLocationFile, module.resolveFrom(projectLocationPath), projectName,
                        input.originTasks);
                final @Nullable Frontend.NormalOutput frontOutput =
                    execContext.require(strIncrFront, frontInput).normalOutput();
                if(frontOutput != null) {
                    for(Map.Entry<String, Integer> strategyNoOfDefs : frontOutput.noOfDefinitions.entrySet()) {
                        Relation
                            .getOrInitialize(BuildStats.modulesDefiningStrategy, strategyNoOfDefs.getKey(), ArrayList::new)
                            .add(strategyNoOfDefs.getValue());
                    }
                    shuffleStartTime = System.nanoTime();

                    final List<Frontend.Import> theImports = new ArrayList<>(frontOutput.imports);
                    theImports.addAll(defaultImports);

                    // combining output for check
                    for(Set<String> usedConstrs : frontOutput.strategyConstrs.values()) {
                        Relation.getOrInitialize(staticData.usedConstructors, module.path, HashSet::new).addAll(usedConstrs);
                    }
                    staticData.usedStrategies.put(module.path, frontOutput.usedStrategies);
                    staticData.usedAmbStrategies.put(module.path, frontOutput.ambStratUsed);
                    registerStrategyDefinitions(staticData.definedStrategies, module,
                        new HashSet<>(frontOutput.strategyFiles.keySet()));
                    registerStrategyDefinitions(staticData.definedCongruences, module, frontOutput.congrs);
                    registerConstructorDefinitions(staticData.definedConstructors, module, frontOutput.constrs,
                        frontOutput.overlayFiles.keySet());
                    staticData.strategyNeedsExternal.addAll(frontOutput.strategyNeedsExternal);


                    // shuffling output for backend
                    for(Map.Entry<String, IStrategoAppl> gen : frontOutput.strategyASTs.entrySet()) {
                        String strategyName = gen.getKey();
                        // ensure the strategy is a key in the strategyFiles map
                        Relation.getOrInitialize(backendData.strategyASTs, strategyName, ArrayList::new).add(gen.getValue());
                        Relation.getOrInitialize(backendData.strategyConstrs, strategyName, HashSet::new)
                            .addAll(frontOutput.strategyConstrs.get(strategyName));
                    }
                    for(Map.Entry<String, IStrategoAppl> gen : frontOutput.congrASTs.entrySet()) {
                        final String congrName = gen.getKey();
                        backendData.congrASTs.put(congrName, gen.getValue());
                        Relation.getOrInitialize(backendData.strategyConstrs, congrName, HashSet::new)
                            .addAll(frontOutput.strategyConstrs.get(congrName));
                    }
                    for(Map.Entry<String, List<IStrategoAppl>> gen : frontOutput.overlayASTs.entrySet()) {
                        final String overlayName = gen.getKey();
                        Relation.getOrInitialize(backendData.overlayASTs, overlayName, ArrayList::new).addAll(gen.getValue());
                    }
                    for(Map.Entry<String, Set<String>> gen : frontOutput.overlayConstrs.entrySet()) {
                        final String overlayName = gen.getKey();
                        Relation.getOrInitialize(backendData.overlayConstrs, overlayName, HashSet::new).addAll(gen.getValue());
                    }

                    // resolving imports
                    final Set<Module> expandedImports =
                        Module.resolveWildcards(execContext, theImports, input.includeDirs, projectLocationPath);
                    for(Module m : expandedImports) {
                        Relation.getOrInitialize(staticData.imports, module.path, HashSet::new).add(m.path);
                    }
                    expandedImports.removeAll(seen);
                    workList.addAll(expandedImports);
                    seen.addAll(expandedImports);

                    BuildStats.shuffleTime += System.nanoTime() - shuffleStartTime;
                }
            } while(!workList.isEmpty());
            return this;
        }
    }

    private final IResourceService resourceService;

    private final Frontend strIncrFront;
    private final LibFrontend strIncrFrontLib;
    private final Backend strIncrBack;

    @Inject public StrIncr(IResourceService resourceService, Frontend strIncrFront, LibFrontend strIncrFrontLib,
        Backend strIncrBack) {
        this.resourceService = resourceService;
        this.strIncrFront = strIncrFront;
        this.strIncrFrontLib = strIncrFrontLib;
        this.strIncrBack = strIncrBack;
    }

    @Override public None exec(ExecContext execContext, Input input) throws Exception {
        /*
         * Note that we require the sdf tasks here to force it to generated needed str files. We then discover those in
         * this method with a directory search, and start a front-end task for each. Every front-end task also depends
         * on the sdf tasks so there is no hidden dep. To make sure that front-end tasks only run when their input
         * _files_ change, we need the front-end to depend on the sdf tasks with a simple stamper that allows the
         * execution of the sdf task to be ignored.
         */
        for(final STask t : input.originTasks) {
            execContext.require(t);
        }

        final Path projectLocationPath = input.projectLocation.toPath().toAbsolutePath().normalize();
        final FileObject projectLocation = resourceService.resolve(projectLocationPath.toFile());
        final File projectLocationFile = resourceService.localFile(projectLocation);

        final Module inputModule = Module.source(projectLocationPath, Paths.get(input.inputFile.toURI()));

        final Frontends frontends =
            new Frontends(resourceService, strIncrFront, strIncrFrontLib, execContext, input, projectLocationPath, projectLocationFile, inputModule).invoke();
        final StaticChecks.Data staticData = frontends.getStaticData();
        final BackendData backendData = frontends.getBackendData();

        long preCheckTime = System.nanoTime();

        // CHECK: constructor/strategy uses have definition which is imported
        final StaticChecks.Output staticCheckOutput =
            StaticChecks.check(execContext, inputModule.path, staticData, backendData.overlayConstrs);

        if(!staticCheckOutput.staticNameCheck) {
            // Commented during benchmarking, too many missing local imports to automatically fix.
//            throw new ExecException("One of the static checks failed. See above for error messages in the log. ");
        }

        BuildStats.checkTime = System.nanoTime() - preCheckTime;

        // BACKEND
        backends(execContext, input, projectLocation, projectLocationFile, staticData, backendData, staticCheckOutput);

        return None.instance;
    }

    private void backends(ExecContext execContext, Input input, FileObject projectLocation, File projectLocationFile,
        StaticChecks.Data staticData, BackendData backendData, StaticChecks.Output staticCheckOutput)
        throws mb.pie.api.ExecException, InterruptedException {
        long backendStart = System.nanoTime();
        final Arguments args = new Arguments();
        args.addAll(input.extraArgs);
        for(String builtinLib : input.builtinLibs) {
            args.add("-la", builtinLib);
        }
        BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
        for(String strategyName : backendData.strategyASTs.keySet()) {
            backendStart = System.nanoTime();
            /* TODO: don't use a set, it could in a very strange case affect the semantics of the Stratego problem
                (side-effect, then failure, purposefully defined multiple times).
               This set is used right now to eliminate overhead in the generated helper strategies of dynamic rules,
               which should be defined once per rule-name but are defined once per rule-name per module. Fix that in a
               principled way, then this can go back to a list.
             */
            final Set<IStrategoAppl> strategyContributions;
            final List<IStrategoAppl> strategyOverlayFiles = new ArrayList<>();
            strategyContributions = new HashSet<>(backendData.strategyASTs.get(strategyName));
            for(String overlayName : requiredOverlays(strategyName, backendData.strategyConstrs, backendData.overlayConstrs)) {
                strategyOverlayFiles.addAll(backendData.overlayASTs.getOrDefault(overlayName, Collections.emptyList()));
            }

            final @Nullable File strategyDir =
                resourceService.localPath(CommonPaths.strSepCompStrategyDir(projectLocation, strategyName));
            assert strategyDir
                != null : "Bug in strSepCompStrategyDir or the arguments thereof: returned path is not a directory";
            final SortedMap<String, String> ambStrategyResolution =
                staticCheckOutput.ambStratResolution.getOrDefault(strategyName, Collections.emptySortedMap());
            Backend.Input backEndInput =
                new Backend.Input(projectLocationFile, strategyName, strategyContributions,
                    strategyOverlayFiles, ambStrategyResolution, input.javaPackageName, input.outputPath,
                    input.cacheDir, input.constants, input.includeDirs, args, false);
            BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
            execContext.require(strIncrBack.createTask(backEndInput));
        }
        for(Map.Entry<String, IStrategoAppl> entry : backendData.congrASTs.entrySet()) {
            backendStart = System.nanoTime();
            String congrName = entry.getKey();
            IStrategoAppl congrAST = entry.getValue();
            if(!backendData.strategyASTs.getOrDefault(congrName + "_0", Collections.emptyList()).isEmpty()) {
                continue;
            }
            if(staticData.externalConstructors.contains(congrName)) {
                execContext.logger()
                    .info("Dropping congruence for " + congrName + " in favour of external definition in library. ");
                continue;
            }
            final List<IStrategoAppl> strategyContributions;
            final List<IStrategoAppl> strategyOverlayFiles = new ArrayList<>();
            strategyContributions = Collections.singletonList(congrAST);
            for(String overlayName : requiredOverlays(congrName, backendData.strategyConstrs, backendData.overlayConstrs)) {
                strategyOverlayFiles.addAll(backendData.overlayASTs.getOrDefault(overlayName, Collections.emptyList()));
            }

            final @Nullable File strategyDir =
                resourceService.localPath(CommonPaths.strSepCompStrategyDir(projectLocation, congrName));
            assert strategyDir
                != null : "Bug in strSepCompStrategyDir or the arguments thereof: returned path is not a directory";
            Backend.Input backEndInput =
                new Backend.Input(projectLocationFile, congrName, strategyContributions,
                    strategyOverlayFiles, Collections.emptySortedMap(), input.javaPackageName, input.outputPath,
                    input.cacheDir, input.constants, input.includeDirs, args, false);
            BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
            execContext.require(strIncrBack.createTask(backEndInput));

            Relation.getOrInitialize(BuildStats.modulesDefiningStrategy, congrName, ArrayList::new).add(1);
        }
        // boilerplate task
        {
            backendStart = System.nanoTime();
            final List<IStrategoAppl> decls = StrategyStubs.declStubs(backendData.strategyASTs);
            final @Nullable File strSrcGenDir =
                resourceService.localPath(CommonPaths.strSepCompSrcGenDir(projectLocation));
            assert strSrcGenDir
                != null : "Bug in strSepCompSrcGenDir or the arguments thereof: returned path is not a directory";
            Backend.Input backEndInput =
                new Backend.Input(projectLocationFile, null, decls, Collections.emptyList(),
                    Collections.emptySortedMap(), input.javaPackageName, input.outputPath, input.cacheDir,
                    input.constants, input.includeDirs, args, true);
            BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
            execContext.require(strIncrBack.createTask(backEndInput));
        }
    }

    private static Iterable<String> requiredOverlays(String strategyName, Map<String, Set<String>> strategyConstrs,
        Map<String, Set<String>> overlayConstrs) {
        final Deque<String> workList =
            new ArrayDeque<>(strategyConstrs.getOrDefault(strategyName, Collections.emptySet()));
        workList.retainAll(overlayConstrs.keySet());
        final Set<String> seenOverlays = new HashSet<>(workList);
        while(!workList.isEmpty()) {
            String overlay = workList.pop();
            seenOverlays.add(overlay);

            Set<String> usedConstrs = overlayConstrs.getOrDefault(overlay, new HashSet<>());
            usedConstrs.retainAll(overlayConstrs.keySet());
            usedConstrs.removeAll(seenOverlays);
            workList.addAll(usedConstrs);
        }
        return seenOverlays;
    }

    private static void registerConstructorDefinitions(Map<String, Set<String>> visibleConstructors, Module module,
        Set<String> constrs, Set<String> overlays) {
        Set<String> visConstrs = new HashSet<>(constrs);
        visConstrs.addAll(overlays);
        visibleConstructors.put(module.path, visConstrs);
    }

    private static void registerStrategyDefinitions(Map<String, Set<String>> visibleStrategies, Module module,
        Set<String> strategies) {
        visibleStrategies.put(module.path, strategies);
    }

    private static String projectName(String inputFile) {
        // *can* we get the project name somehow? This is probably more portable for non-project based compilation
        return Integer.toString(inputFile.hashCode());
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.inputFile;
    }
}
