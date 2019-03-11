package mb.stratego.build;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.Algorithms;
import mb.stratego.build.util.CommonPaths;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.cmd.Arguments;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class StrIncr implements TaskDef<StrIncr.Input, None> {
    public static final String id = "StrIncr";

    public static final class Input implements Serializable {
        final URL inputFile;
        final @Nullable String javaPackageName;
        final Collection<File> includeDirs;
        final Collection<String> builtinLibs;
        final @Nullable File cacheDir;
        final Arguments extraArgs;
        final File outputPath;
        final Collection<STask<?>> originTasks;
        final File projectLocation;

        public Input(URL inputFile, @Nullable String javaPackageName, Collection<File> includeDirs,
            Collection<String> builtinLibs, @Nullable File cacheDir, Arguments extraArgs, File outputPath,
            Collection<STask<?>> originTasks, File projectLocation) {
            this.inputFile = inputFile;
            this.javaPackageName = javaPackageName;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.cacheDir = cacheDir;
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
    }

    public static final class Module implements Serializable {
        public enum Type {
            library, source
        }

        public final String path;
        public final Type type;

        private Module(String path, Type type) {
            this.path = path;
            this.type = type;
        }

        static Module library(String path) {
            return new Module(path, Type.library);
        }

        public static Module source(Path path) {
            return new Module(path.normalize().toString(), Type.source);
        }

        static Set<Module> resolveWildcards(ExecContext execContext, Collection<StrIncrFront.Import> imports,
            Collection<File> includeDirs, File projectLocation) throws ExecException {
            final Function<Path, Module> module = p -> Module.source(projectLocation.toPath().relativize(p));
            final Set<Module> result = new HashSet<>(imports.size() * 2);
            for(StrIncrFront.Import anImport : imports) {
                switch(anImport.type) {
                    case normal: {
                        boolean foundSomethingToImport = false;
                        for(File dir : includeDirs) {
                            final Path strPath = dir.toPath().resolve(anImport.path + ".str");
                            final Path rtreePath = dir.toPath().resolve(anImport.path + ".rtree");
                            if(Files.exists(rtreePath)) {
                                foundSomethingToImport = true;
                                result.add(module.apply(rtreePath));
                            } else if(Files.exists(strPath)) {
                                foundSomethingToImport = true;
                                result.add(module.apply(strPath));
                            }
                        }
                        if(!foundSomethingToImport) {
                            execContext.getLogger()
                                .warn("Could not find any module corresponding to import " + anImport.path, null);
                        }
                        break;
                    }
                    case wildcard: {
                        boolean foundSomethingToImport = false;
                        for(File dir : includeDirs) {
                            final Path path = dir.toPath().resolve(anImport.path);
                            if(Files.exists(path)) {
                                final @Nullable File[] strFiles = path.toFile()
                                    .listFiles((FilenameFilter) new SuffixFileFilter(Arrays.asList(".str", ".rtree")));
                                if(strFiles == null) {
                                    throw new ExecException(
                                        "Reading file list in directory failed for directory: " + path);
                                }
                                for(File strFile : strFiles) {
                                    foundSomethingToImport = true;
                                    result.add(module.apply(strFile.toPath()));
                                }
                            }
                        }
                        if(!foundSomethingToImport) {
                            execContext.getLogger()
                                .warn("Could not find any module corresponding to import " + anImport.path + "/-",
                                    null);
                        }
                        break;
                    }
                    case library: {
                        result.add(Module.library(anImport.path));
                        break;
                    }
                }
            }
            return result;
        }

        public URL resolve(File projectLocation) {
            try {
                return projectLocation.toPath().resolve(path).toUri().toURL();
            } catch(MalformedURLException e) {
                // Shouldn't happen because any path can be expressed as a URL
                throw new RuntimeException(e);
            }
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Module module = (Module) o;

            //noinspection SimplifiableIfStatement
            if(!path.equals(module.path))
                return false;
            return type == module.type;
        }

        @Override public int hashCode() {
            int result = path.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }

    private final IResourceService resourceService;

    private final StrIncrFront strIncrFront;
    private final StrIncrFrontLib strIncrFrontLib;
    private final StrIncrBack strIncrBack;

    @Inject public StrIncr(IResourceService resourceService, StrIncrFront strIncrFront, StrIncrFrontLib strIncrFrontLib,
        StrIncrBack strIncrBack) {
        this.resourceService = resourceService;
        this.strIncrFront = strIncrFront;
        this.strIncrFrontLib = strIncrFrontLib;
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

        final FileObject location = resourceService.resolve(input.projectLocation);

        // FRONTEND
        final Set<Module> seen = new HashSet<>();
        final Deque<Module> workList = new ArrayDeque<>();
        final Module inputModule;
        try {
            inputModule =
                Module.source(input.projectLocation.toPath().relativize(Paths.get(input.inputFile.toURI())));
        } catch(URISyntaxException e) {
            throw new ExecException(e);
        }
        workList.add(inputModule);
        seen.add(inputModule);

        final List<File> boilerplateFiles = new ArrayList<>();
        final List<STask<?>> frontSourceTasks = new ArrayList<>();
        final List<StrIncrFront.Import> defaultImports = new ArrayList<>(input.builtinLibs.size());
        for(String builtinLib : input.builtinLibs) {
            defaultImports.add(StrIncrFront.Import.library(builtinLib));
        }

        final String mainFileModulePath = inputModule.path;
        // Module-path to module-path
        final Map<String, Set<String>> imports = new HashMap<>();
        // Module-path to strategy-names used
        final Map<String, Set<String>> usedStrategies = new HashMap<>();
        // Module-path to strategy-names-without-arity used in ambiguous call position
        final Map<String, Set<String>> usedAmbStrategies = new HashMap<>();
        // Module-path to constructor_arity names used
        final Map<String, Set<String>> usedConstructors = new HashMap<>();
        // Module-path to  visible when imported (transitive closure of strategy definitions)
        final Map<String, Set<String>> visibleStrategies = new HashMap<>();
        // Module-path to constructor_arity names visible when imported (transitive closure of constructor definitions)
        final Map<String, Set<String>> visibleConstructors = new HashMap<>();

        // Strategy-name to file with CTree definition of that strategy
        final Map<String, Set<File>> strategyFiles = new HashMap<>();
        // Strategy-name to constructor_arity names that were used in the body
        final Map<String, Set<String>> strategyConstrs = new HashMap<>();
        // Overlay_arity name to file with CTree definition of that overlay
        final Map<String, Set<File>> overlayFiles = new HashMap<>();
        // Strategy-name to set of tasks that contributed strategy definitions
        final Map<String, List<STask<?>>> strategyOrigins = new HashMap<>();
        // Overlay_arity names to set of tasks that contributed overlay definitions
        final Map<String, List<STask<?>>> overlayOrigins = new HashMap<>();

        do {
            final Module module = workList.remove();

            if(module.type == Module.Type.library) {
                final StrIncrFrontLib.Input frontLibInput =
                    new StrIncrFrontLib.Input(Objects.requireNonNull(StrIncrFrontLib.BuiltinLibrary.fromString(module.path)));
                Task<StrIncrFrontLib.Input, StrIncrFrontLib.Output> task = strIncrFrontLib.createTask(frontLibInput);
                StrIncrFrontLib.Output frontLibOutput = execContext.require(task);
                registerStrategyDefinitions(visibleStrategies, module.path, frontLibOutput.strategies);
                registerConstructorDefinitions(visibleConstructors, module, frontLibOutput.constrs,
                    Collections.emptySet());
                continue;
            }

            final String projectName = projectName(module.path);
            final StrIncrFront.Input frontInput =
                new StrIncrFront.Input(input.projectLocation, module.resolve(input.projectLocation), projectName,
                    input.originTasks);
            final Task<?, StrIncrFront.Output> task = strIncrFront.createTask(frontInput);
            frontSourceTasks.add(task.toSTask());
            final StrIncrFront.Output frontOutput = execContext.require(task);
            boilerplateFiles.add(resourceService
                .localPath(CommonPaths.strSepCompBoilerplateFile(location, projectName, frontOutput.moduleName)));
            final List<StrIncrFront.Import> theImports = new ArrayList<>(frontOutput.imports);
            theImports.addAll(defaultImports);

            // combining output for check
            for(Set<String> usedConstrs : frontOutput.strategyConstrs.values()) {
                getOrInitialize(usedConstructors, module.path, HashSet::new).addAll(usedConstrs);
            }
            usedStrategies.put(module.path, frontOutput.usedStrategies);
            usedAmbStrategies.put(module.path, frontOutput.ambStratUsed);
            registerStrategyDefinitions(visibleStrategies, module.path, frontOutput.strategies);
            registerConstructorDefinitions(visibleConstructors, module, frontOutput.constrs,
                frontOutput.overlayFiles.keySet());


            // shuffling output for backend
            for(Map.Entry<String, File> gen : frontOutput.strategyFiles.entrySet()) {
                String strategyName = gen.getKey();
                getOrInitialize(strategyFiles, strategyName, HashSet::new).add(gen.getValue());
                getOrInitialize(strategyConstrs, strategyName, HashSet::new)
                    .addAll(frontOutput.strategyConstrs.get(strategyName));
                getOrInitialize(strategyOrigins, strategyName, ArrayList::new).add(task.toSTask());
            }
            for(Map.Entry<String, File> gen : frontOutput.overlayFiles.entrySet()) {
                final String overlayName = gen.getKey();
                getOrInitialize(overlayFiles, overlayName, HashSet::new).add(gen.getValue());
                getOrInitialize(overlayOrigins, overlayName, ArrayList::new).add(task.toSTask());
            }

            // resolving imports
            final Set<Module> expandedImports =
                Module.resolveWildcards(execContext, theImports, input.includeDirs, input.projectLocation);
            for(Module m : expandedImports) {
                getOrInitialize(imports, module.path, HashSet::new).add(m.path);
            }
            expandedImports.removeAll(seen);
            workList.addAll(expandedImports);
            seen.addAll(expandedImports);
        } while(!workList.isEmpty());

        // CHECK: constructor/strategy uses have definition which is imported
        staticCheck(execContext, mainFileModulePath, imports, usedStrategies, usedAmbStrategies, usedConstructors,
            visibleStrategies, visibleConstructors);

        // BACKEND
        for(String strategyName : strategyFiles.keySet()) {
            final List<STask<?>> backEndOrigin = new ArrayList<>(strategyOrigins.size());
            backEndOrigin.addAll(strategyOrigins.get(strategyName));
            final @Nullable File strategyDir =
                resourceService.localPath(CommonPaths.strSepCompStrategyDir(location, strategyName));
            assert strategyDir
                != null : "Bug in strSepCompStrategyDir or the arguments thereof: returned path is not a directory";
            final List<File> strategyOverlayFiles = new ArrayList<>();
            for(String overlayName : strategyConstrs.get(strategyName)) {
                final Set<File> theOverlayFiles = overlayFiles.get(overlayName);
                if(theOverlayFiles != null) {
                    strategyOverlayFiles.addAll(theOverlayFiles);
                }
                final List<STask<?>> overlayOriginBuilder = overlayOrigins.get(overlayName);
                if(overlayOriginBuilder != null) {
                    backEndOrigin.addAll(overlayOriginBuilder);
                }
            }
            final Arguments args = new Arguments();
            args.addAll(input.extraArgs);
            for(String builtinLib : input.builtinLibs) {
                args.add("-la", builtinLib);
            }
            StrIncrBack.Input backEndInput =
                new StrIncrBack.Input(backEndOrigin, input.projectLocation, strategyName, strategyDir,
                    Arrays.asList(strategyFiles.get(strategyName).toArray(new File[0])), strategyOverlayFiles,
                    input.javaPackageName, input.outputPath, input.cacheDir, args, false);
            execContext.require(strIncrBack.createTask(backEndInput));
        }
        // boilerplate task
        final @Nullable File strSrcGenDir = resourceService.localPath(CommonPaths.strSepCompSrcGenDir(location));
        assert strSrcGenDir
            != null : "Bug in strSepCompSrcGenDir or the arguments thereof: returned path is not a directory";
        StrIncrBack.Input backEndInput =
            new StrIncrBack.Input(frontSourceTasks, input.projectLocation, null, strSrcGenDir, boilerplateFiles,
                Collections.emptyList(), input.javaPackageName, input.outputPath, input.cacheDir, input.extraArgs,
                true);
        execContext.require(strIncrBack.createTask(backEndInput));

        return None.getInstance();
    }

    private static void registerConstructorDefinitions(Map<String, Set<String>> visibleConstructors, Module module,
        Set<String> constrs, Set<String> overlays) {
        Set<String> visConstrs = new HashSet<>(constrs);
        visConstrs.addAll(overlays);
        visibleConstructors.put(module.path, visConstrs);
    }

    private static void registerStrategyDefinitions(Map<String, Set<String>> visibleStrategies, String path,
        Set<String> strategies) {
        visibleStrategies.put(path, strategies);
    }

    private static void staticCheck(ExecContext execContext, String mainFileModulePath,
        Map<String, Set<String>> imports, Map<String, Set<String>> usedStrategies,
        Map<String, Set<String>> usedAmbStrategies, Map<String, Set<String>> usedConstructors,
        Map<String, Set<String>> visibleStrategies, Map<String, Set<String>> visibleConstructors) throws ExecException {
        boolean checkOk = true;
        final Deque<Set<String>> sccs = Algorithms
            .topoSCCs(Collections.singleton(mainFileModulePath), k -> imports.getOrDefault(k, Collections.emptySet()));
        for(Iterator<Set<String>> iterator = sccs.descendingIterator(); iterator.hasNext(); ) {
            Set<String> scc = iterator.next();
            Set<String> theVisibleStrategies = new HashSet<>();
            Set<String> theVisibleConstructors = new HashSet<>();
            for(String moduleName : scc) {
                theVisibleConstructors.addAll(visibleConstructors.getOrDefault(moduleName, Collections.emptySet()));
                theVisibleStrategies.addAll(visibleStrategies.getOrDefault(moduleName, Collections.emptySet()));
                for(String mod : imports.getOrDefault(moduleName, Collections.emptySet())) {
                    theVisibleConstructors.addAll(visibleConstructors.getOrDefault(mod, Collections.emptySet()));
                    theVisibleStrategies.addAll(visibleStrategies.getOrDefault(mod, Collections.emptySet()));
                }
            }
            for(String moduleName : scc) {
                visibleConstructors.put(moduleName, theVisibleConstructors);
                visibleStrategies.put(moduleName, theVisibleStrategies);
                Set<String> unresolvedConstructors =
                    Sets.difference(usedConstructors.getOrDefault(moduleName, Collections.emptySet()),
                        theVisibleConstructors);
                if(!unresolvedConstructors.isEmpty()) {
                    checkOk = false;
                    execContext.getLogger()
                        .error("In module " + moduleName + ": Cannot find constructors " + unresolvedConstructors,
                            null);
                }
                Set<String> unresolvedStrategies =
                    Sets.difference(usedStrategies.getOrDefault(moduleName, Collections.emptySet()),
                        theVisibleStrategies);
                if(!unresolvedStrategies.isEmpty()) {
                    checkOk = false;
                    execContext.getLogger()
                        .error("In module " + moduleName + ": Cannot find strategies " + unresolvedStrategies, null);
                }
                Set<String> theUsedAmbStrategies =
                    new HashSet<>(usedAmbStrategies.getOrDefault(moduleName, Collections.emptySet()));
                theUsedAmbStrategies
                    .removeIf(theUsedAmbStrategy -> theVisibleStrategies.contains(theUsedAmbStrategy + "_0_0"));
                if(!theUsedAmbStrategies.isEmpty()) {
                    Map<String, Integer> differentArityDefinitions = new HashMap<>(theVisibleStrategies.size());
                    for(String theVisibleStrategy : theVisibleStrategies) {
                        String stripped = StrIncrFront.stripArity(theVisibleStrategy);
                        differentArityDefinitions
                            .put(stripped, differentArityDefinitions.getOrDefault(stripped, 0) + 1);
                    }
                    for(String usedAmbStrategy : theUsedAmbStrategies) {
                        switch(differentArityDefinitions.getOrDefault(usedAmbStrategy, 0)) {
                            case 0:
                                execContext.getLogger().error(
                                    "In module " + moduleName + ": Cannot find strategy " + usedAmbStrategy
                                        + " in ambiguous call position", null);
                                break;
                            case 1:
                                break;
                            default:
                                execContext.getLogger().error(
                                    "In module " + moduleName + ": Call to strategy " + usedAmbStrategy
                                        + " is ambiguous, multiple arities possible. ", null);
                        }
                    }
                }
            }
        }
        if(!checkOk) {
            throw new ExecException("Name resolution check failed. ");
        }
    }

    private static String projectName(String inputFile) {
        // TODO: *can* we get the project name somehow?
        return Integer.toString(inputFile.hashCode());
    }

    private static <K, V> V getOrInitialize(Map<K, V> map, K key, Supplier<V> initialize) {
        map.computeIfAbsent(key, ignore -> initialize.get());
        return map.get(key);
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.inputFile;
    }

    @Override public String desc(Input input) {
        return TaskDef.DefaultImpls.desc(this, input);
    }

    @Override public String desc(Input input, int maxLength) {
        return TaskDef.DefaultImpls.desc(this, input, maxLength);
    }

    @Override public Task<Input, None> createTask(Input input) {
        return TaskDef.DefaultImpls.createTask(this, input);
    }

    @Override public STask<Input> createSerializableTask(Input input) {
        return TaskDef.DefaultImpls.createSerializableTask(this, input);
    }
}
