package mb.stratego.build;

import mb.flowspec.terms.B;
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
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.strj.strj;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrIncr implements TaskDef<StrIncr.Input, None> {
    public static final String id = StrIncr.class.getCanonicalName();
    private static final HashSet<String> ALWAYS_DEFINED =
        new HashSet<>(Arrays.asList("DR__DUMMY_0_0", "Anno__Cong_____2_0", "DR__UNDEFINE_1_0"));
    static final Pattern stripArityPattern = Pattern.compile("([A-Za-z$_][A-Za-z0-9_$]*)_(\\d+)_(\\d+)");
    private static final IStrategoConstructor constType = strj.init().getFactory().makeConstructor("ConstType", 1);
    private static final IStrategoConstructor sort = strj.init().getFactory().makeConstructor("Sort", 2);
    private static final IStrategoAppl A_TERM = B.appl(sort, B.string("ATerm"), B.list());
    private static final IStrategoConstructor varDec = strj.init().getFactory().makeConstructor("VarDec", 2);
    private static final IStrategoConstructor funType = strj.init().getFactory().makeConstructor("FunType", 2);
    private static final IStrategoTerm newSVar = B.appl(varDec, B.string("a"), B.appl(funType, A_TERM, A_TERM));
    private static final IStrategoTerm newTVar = B.appl(varDec, B.string("a"), B.appl(constType, A_TERM));

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

        /**
         * Create source module with a normalized, relative path from the projectLocation to the module file. This
         * should give us a unique string to use to identify the module file within this pipeline.
         */
        public static Module source(Path projectLocationPath, Path path) {
            return new Module(projectLocationPath.relativize(path.toAbsolutePath().normalize()).toString(),
                Type.source);
        }

        static Set<Module> resolveWildcards(ExecContext execContext, Collection<StrIncrFront.Import> imports,
            Collection<File> includeDirs, Path projectLocation) throws ExecException, IOException {
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
                                if(isLibraryRTree(rtreePath)) {
                                    result.add(Module.library(rtreePath.toString()));
                                } else {
                                    result.add(Module.source(projectLocation, rtreePath));
                                }
                            } else if(Files.exists(strPath)) {
                                foundSomethingToImport = true;
                                result.add(Module.source(projectLocation, strPath));
                            }
                        }
                        if(!foundSomethingToImport) {
                            execContext.logger()
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
                                    Path p = strFile.toPath();
                                    result.add(Module.source(projectLocation, p));
                                }
                            }
                        }
                        if(!foundSomethingToImport) {
                            execContext.logger()
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

        /**
         * Check if file starts with Specification/1 instead of Module/2
         *
         * @param rtreePath Path to the file
         * @return if file starts with Specification/1
         * @throws IOException on file system trouble
         */
        private static boolean isLibraryRTree(Path rtreePath) throws IOException {
            char[] chars = new char[4];
            BufferedReader r = Files.newBufferedReader(rtreePath);
            return r.read(chars) != -1 && Arrays.equals(chars, "Spec".toCharArray());
        }

        String resolveFrom(Path projectLocation) throws MalformedURLException {
            return projectLocation.resolve(path).normalize().toAbsolutePath().toString();
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

        @Override public String toString() {
            return "Module(" + path + ", " + type + ')';
        }
    }

    public static final class StaticCheckOutput {
        // Cified-strategy-name (where the call occurs) to cified-strategy-name (amb call) to cified-strategy-name (amb call resolves to)
        final Map<String, SortedMap<String, String>> ambStratResolution;

        StaticCheckOutput(Map<String, SortedMap<String, String>> ambStratResolution) {
            this.ambStratResolution = ambStratResolution;
        }

        @Override public String toString() {
            if(!ambStratResolution.isEmpty()) {
                final StringBuilder b = new StringBuilder();
                for(Map.Entry<String, SortedMap<String, String>> stringSortedMapEntry : ambStratResolution.entrySet()) {
                    b.append("  In strategy ").append(stringSortedMapEntry.getKey()).append(":\n");
                    for(Map.Entry<String, String> stringStringEntry : stringSortedMapEntry.getValue().entrySet()) {
                        b.append("    ").append(stringStringEntry.getKey()).append(" -> ")
                            .append(stringStringEntry.getValue()).append("\n");
                    }
                }
                return b.toString();
            } else {
                return "  (none)";
            }
        }
    }

    private final IResourceService resourceService;

    private final StrIncrFront strIncrFront;
    private final StrIncrFrontLib strIncrFrontLib;
    private final StrIncrBack strIncrBack;

    @Inject
    public StrIncr(IResourceService resourceService, StrIncrFront strIncrFront, StrIncrFrontLib strIncrFrontLib,
        StrIncrBack strIncrBack) {
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

        //        execContext.logger().debug("Starting time measurement");
        long startTime = System.nanoTime();

        final Path projectLocationPath = input.projectLocation.toPath().toAbsolutePath().normalize();
        final FileObject projectLocation = resourceService.resolve(projectLocationPath.toFile());
        final File projectLocationFile = resourceService.localFile(projectLocation);

        // FRONTEND
        final Set<Module> seen = new HashSet<>();
        final Deque<Module> workList = new ArrayDeque<>();
        final Module inputModule = Module.source(projectLocationPath, Paths.get(input.inputFile.toURI()));
        workList.add(inputModule);
        seen.add(inputModule);

        final List<STask> frontSourceTasks = new ArrayList<>();
        final List<StrIncrFront.Import> defaultImports = new ArrayList<>(input.builtinLibs.size());
        for(String builtinLib : input.builtinLibs) {
            defaultImports.add(StrIncrFront.Import.library(builtinLib));
        }
        // depend on the include directories in which we search for str and rtree files
        for(File includeDir : input.includeDirs) {
            execContext.require(includeDir);
        }

        // Module-path to module-path
        final Map<String, Set<String>> imports = new HashMap<>();
        // Module-path to cified-strategy-names used
        final Map<String, Set<String>> usedStrategies = new HashMap<>();
        // Module-path to cified-strategy-name used in ambiguous call position to cified-strategy-names where the calls occur
        final Map<String, Map<String, Set<String>>> usedAmbStrategies = new HashMap<>();
        // Module-path to constructor_arity names used
        final Map<String, Set<String>> usedConstructors = new HashMap<>();
        // Module-path to cified-strategy-names defined here
        final Map<String, Set<String>> definedStrategies = new HashMap<>();
        // Module-path to cified-strategy-names defined here as congruences
        final Map<String, Set<String>> definedCongruences = new HashMap<>();
        // External cified-strategy-names that will be imported in Java
        final Set<String> externalStrategies = new HashSet<>();
        // Module-path to constructor_arity names defined there
        final Map<String, Set<String>> definedConstructors = new HashMap<>();
        // External constructors that will be imported in Java
        final Set<String> externalConstructors = new HashSet<>();
        // Cified-strategy-names that need a corresponding name in a library because it overrides or extends it.
        final Set<String> strategyNeedsExternal = new HashSet<>();

        // Cified-strategy-name to definitions of that strategy
        final Map<String, List<IStrategoAppl>> strategyASTs = new HashMap<>();
        // Constructor_arity of congruence to definition of that strategy
        final Map<String, IStrategoAppl> congrASTs = new HashMap<>();
        // Cified-strategy-name to constructor_arity names that were used in the body
        final Map<String, Set<String>> strategyConstrs = new HashMap<>();
        // Overlay_arity names to constructor_arity names used
        final Map<String, Set<String>> overlayConstrs = new HashMap<>();
        // Overlay_arity name to definition of that overlay
        final Map<String, List<IStrategoAppl>> overlayASTs = new HashMap<>();
        // Cified-strategy-name to set of tasks that contributed strategy definitions
        final Map<String, List<STask>> strategyOrigins = new HashMap<>();
        // Cified-strategy-name to task that created strategy definitions
        final Map<String, STask> congrOrigin = new HashMap<>();
        // Overlay_arity names to set of tasks that contributed overlay definitions
        final Map<String, List<STask>> overlayOrigins = new HashMap<>();

        boolean checkOk = true;

        long frontEndStartTime;
        long frontEndTime = 0;
        long frontEndLibTime = 0;
        long shuffleStartTime;
        long shuffleTime = 0;
        long shuffleLibTime = 0;
        long numberOfFETasks = 0;
        long numberOfFELibTasks = 0;
        do {
            frontEndStartTime = System.nanoTime();

            final Module module = workList.remove();

            if(module.type == Module.Type.library) {
                final StrIncrFrontLib.Input frontLibInput =
                    new StrIncrFrontLib.Input(Library.fromString(resourceService, module.path));
                final Task<StrIncrFrontLib.Output> task = strIncrFrontLib.createTask(frontLibInput);
                final StrIncrFrontLib.Output frontLibOutput = execContext.require(task);

                shuffleStartTime = System.nanoTime();
                frontEndLibTime += shuffleStartTime - frontEndStartTime;

                registerStrategyDefinitions(definedStrategies, module, frontLibOutput.strategies);
                registerConstructorDefinitions(definedConstructors, module, frontLibOutput.constrs,
                    Collections.emptySet());

                final Set<String> overlappingStrategies =
                    Sets.difference(Sets.intersection(externalStrategies, frontLibOutput.strategies), ALWAYS_DEFINED);
                if(!overlappingStrategies.isEmpty()) {
                    checkOk = false;
                    execContext.logger()
                        .error("Overlapping external strategy definitions: " + overlappingStrategies, null);
                }

                externalStrategies.addAll(frontLibOutput.strategies);
                externalConstructors.addAll(frontLibOutput.constrs);

                shuffleLibTime += System.nanoTime() - shuffleStartTime;

                numberOfFELibTasks++;
                continue;
            }
            numberOfFETasks++;

            final String projectName = projectName(module.path);
            final StrIncrFront.Input frontInput =
                new StrIncrFront.Input(projectLocationFile, module.resolveFrom(projectLocationPath), projectName,
                    input.originTasks);
            final Task<StrIncrFront.Output> task = strIncrFront.createTask(frontInput);
            frontSourceTasks.add(task.toSerializableTask());
            final StrIncrFront.Output frontOutput = execContext.require(task);
            //            boilerplateFiles.add(resourceService.localPath(
            //                CommonPaths.strSepCompBoilerplateFile(projectLocation, projectName, frontOutput.moduleName)));

            shuffleStartTime = System.nanoTime();
            frontEndTime += shuffleStartTime - frontEndStartTime;

            final List<StrIncrFront.Import> theImports = new ArrayList<>(frontOutput.imports);
            theImports.addAll(defaultImports);

            // combining output for check
            for(Set<String> usedConstrs : frontOutput.strategyConstrs.values()) {
                getOrInitialize(usedConstructors, module.path, HashSet::new).addAll(usedConstrs);
            }
            usedStrategies.put(module.path, frontOutput.usedStrategies);
            usedAmbStrategies.put(module.path, frontOutput.ambStratUsed);
            registerStrategyDefinitions(definedStrategies, module, new HashSet<>(frontOutput.strategyFiles.keySet()));
            registerStrategyDefinitions(definedCongruences, module, frontOutput.congrs);
            registerConstructorDefinitions(definedConstructors, module, frontOutput.constrs,
                frontOutput.overlayFiles.keySet());
            strategyNeedsExternal.addAll(frontOutput.strategyNeedsExternal);


            // shuffling output for backend
            for(Map.Entry<String, IStrategoAppl> gen : frontOutput.strategyASTs.entrySet()) {
                String strategyName = gen.getKey();
                // ensure the strategy is a key in the strategyFiles map
                getOrInitialize(strategyASTs, strategyName, ArrayList::new).add(gen.getValue());
                getOrInitialize(strategyOrigins, strategyName, ArrayList::new).add(task.toSerializableTask());
                getOrInitialize(strategyConstrs, strategyName, HashSet::new)
                    .addAll(frontOutput.strategyConstrs.get(strategyName));
            }
            for(Map.Entry<String, IStrategoAppl> gen : frontOutput.congrASTs.entrySet()) {
                final String congrName = gen.getKey();
                congrASTs.put(congrName, gen.getValue());
                congrOrigin.put(congrName, task.toSerializableTask());
                getOrInitialize(strategyConstrs, congrName, HashSet::new)
                    .addAll(frontOutput.strategyConstrs.get(congrName));
            }
            for(Map.Entry<String, List<IStrategoAppl>> gen : frontOutput.overlayASTs.entrySet()) {
                final String overlayName = gen.getKey();
                getOrInitialize(overlayASTs, overlayName, ArrayList::new).addAll(gen.getValue());
                getOrInitialize(overlayOrigins, overlayName, ArrayList::new).add(task.toSerializableTask());
            }
            for(Map.Entry<String, Set<String>> gen : frontOutput.overlayConstrs.entrySet()) {
                final String overlayName = gen.getKey();
                getOrInitialize(overlayConstrs, overlayName, HashSet::new).addAll(gen.getValue());
            }

            // resolving imports
            final Set<Module> expandedImports =
                Module.resolveWildcards(execContext, theImports, input.includeDirs, projectLocationPath);
            for(Module m : expandedImports) {
                getOrInitialize(imports, module.path, HashSet::new).add(m.path);
            }
            expandedImports.removeAll(seen);
            workList.addAll(expandedImports);
            seen.addAll(expandedImports);

            shuffleTime += System.nanoTime() - shuffleStartTime;
        } while(!workList.isEmpty());

        long betweenFrontAndCheck = System.nanoTime();
        //        execContext.logger().debug("\"Frontends overall took\", " + (betweenFrontAndCheck - startTime));
        //        execContext.logger().debug("\"Purely str file frontend tasks took\", " + frontEndTime);
        //        execContext.logger().debug("\"Purely libs took\", " + frontEndLibTime);
        //        execContext.logger().debug("\"Shuffling information and tracking imports took\", " + shuffleTime);
        //        execContext.logger().debug("\"Shuffling information in libs took\", " + shuffleLibTime);
        //        execContext.logger().debug("\"Number of FrontEnd tasks\", " + numberOfFETasks);
        //        execContext.logger().debug("\"Number of FrontEndLib tasks\", " + numberOfFELibTasks);

        // CHECK: constructor/strategy uses have definition which is imported
        final StaticCheckOutput staticCheckOutput =
            staticCheck(checkOk, execContext, inputModule.path, imports, usedStrategies, usedAmbStrategies,
                usedConstructors, definedStrategies, definedConstructors, definedCongruences, externalStrategies,
                strategyNeedsExternal, overlayConstrs);

        long betweenCheckAndBack = System.nanoTime();
        //        execContext.logger().debug("\"Static check overall took\", " + (betweenCheckAndBack - betweenFrontAndCheck));

        //        execContext.logger().debug("Renaming:\n" + staticCheckOutput);

        // BACKEND
        long numberOfBETasks = 0;
        long backEndTime = 0;
        final Arguments args = new Arguments();
        args.addAll(input.extraArgs);
        for(String builtinLib : input.builtinLibs) {
            args.add("-la", builtinLib);
        }
        for(String strategyName : strategyASTs.keySet()) {
            final List<IStrategoAppl> strategyContributions;
            final List<STask> backEndOrigin;
            final List<IStrategoAppl> strategyOverlayFiles = new ArrayList<>();
            strategyContributions = strategyASTs.get(strategyName);
            backEndOrigin = new ArrayList<>(strategyOrigins.get(strategyName));
            for(String overlayName : requiredOverlays(strategyName, strategyConstrs, overlayConstrs)) {
                strategyOverlayFiles.addAll(overlayASTs.getOrDefault(overlayName, Collections.emptyList()));
                backEndOrigin.addAll(overlayOrigins.getOrDefault(overlayName, Collections.emptyList()));
            }

            final @Nullable File strategyDir =
                resourceService.localPath(CommonPaths.strSepCompStrategyDir(projectLocation, strategyName));
            assert strategyDir
                != null : "Bug in strSepCompStrategyDir or the arguments thereof: returned path is not a directory";
            long backendStartTime = System.nanoTime();
            final SortedMap<String, String> ambStrategyResolution =
                staticCheckOutput.ambStratResolution.getOrDefault(strategyName, Collections.emptySortedMap());
            StrIncrBack.Input backEndInput =
                new StrIncrBack.Input(backEndOrigin, projectLocationFile, strategyName, strategyDir,
                    strategyContributions, strategyOverlayFiles, ambStrategyResolution, input.javaPackageName,
                    input.outputPath, input.cacheDir, input.constants, input.includeDirs, args, false);
            execContext.require(strIncrBack.createTask(backEndInput));
            backEndTime += backendStartTime - System.nanoTime();
            numberOfBETasks++;
        }
        for(Map.Entry<String, IStrategoAppl> entry : congrASTs.entrySet()) {
            String congrName = entry.getKey();
            IStrategoAppl congrAST = entry.getValue();
            if(!strategyASTs.getOrDefault(congrName + "_0", Collections.emptyList()).isEmpty()) {
                continue;
            }
            if(externalConstructors.contains(congrName)) {
                //                execContext.logger()
                //                    .debug("Dropping congruence for " + congrName + " in favour of external definition in library. ");
                continue;
            }
            final List<IStrategoAppl> strategyContributions;
            final List<STask> backEndOrigin;
            final List<IStrategoAppl> strategyOverlayFiles = new ArrayList<>();
            strategyContributions = Collections.singletonList(congrAST);
            backEndOrigin = new ArrayList<>();
            backEndOrigin.add(Objects.requireNonNull(congrOrigin.get(congrName)));
            for(String overlayName : requiredOverlays(congrName, strategyConstrs, overlayConstrs)) {
                strategyOverlayFiles.addAll(overlayASTs.getOrDefault(overlayName, Collections.emptyList()));
                backEndOrigin.addAll(overlayOrigins.getOrDefault(overlayName, Collections.emptyList()));
            }

            final @Nullable File strategyDir =
                resourceService.localPath(CommonPaths.strSepCompStrategyDir(projectLocation, congrName));
            assert strategyDir
                != null : "Bug in strSepCompStrategyDir or the arguments thereof: returned path is not a directory";
            long backendStartTime = System.nanoTime();
            StrIncrBack.Input backEndInput =
                new StrIncrBack.Input(backEndOrigin, projectLocationFile, congrName, strategyDir, strategyContributions,
                    strategyOverlayFiles, Collections.emptySortedMap(), input.javaPackageName, input.outputPath,
                    input.cacheDir, input.constants, input.includeDirs, args, false);
            execContext.require(strIncrBack.createTask(backEndInput));
            backEndTime += backendStartTime - System.nanoTime();
            numberOfBETasks++;
        }
        // boilerplate task
        final List<IStrategoAppl> decls = declStubs(strategyASTs);
        final @Nullable File strSrcGenDir = resourceService.localPath(CommonPaths.strSepCompSrcGenDir(projectLocation));
        assert strSrcGenDir
            != null : "Bug in strSepCompSrcGenDir or the arguments thereof: returned path is not a directory";
        long backendStartTime = System.nanoTime();
        StrIncrBack.Input backEndInput =
            new StrIncrBack.Input(frontSourceTasks, projectLocationFile, null, strSrcGenDir, decls,
                Collections.emptyList(), Collections.emptySortedMap(), input.javaPackageName, input.outputPath,
                input.cacheDir, input.constants, input.includeDirs, args, true);
        execContext.require(strIncrBack.createTask(backEndInput));
        backEndTime += backendStartTime - System.nanoTime();
        numberOfBETasks++;

        long finishTime = System.nanoTime();
        //        execContext.logger().debug("\"Backends overall took\", " + (finishTime - betweenCheckAndBack));
        //        execContext.logger().debug("\"Number of BackEnd tasks\", " + numberOfBETasks);
        execContext.logger().debug(
            "\"Full Stratego incremental build took\", " + (finishTime - startTime - frontEndTime - frontEndLibTime
                - backEndTime));

        return None.instance;
    }

    private List<IStrategoAppl> declStubs(Map<String, List<IStrategoAppl>> strategyASTs) throws ExecException {
        final List<IStrategoAppl> decls = new ArrayList<>(strategyASTs.size());
        final B b = new B(strj.init().getFactory());
        for(String strategyName : strategyASTs.keySet()) {
            final Matcher m = stripArityPattern.matcher(strategyName);
            if(!m.matches()) {
                throw new ExecException(
                    "Frontend returned stratego strategy name that does not conform to cified name: '" + strategyName
                        + "'");
            }
            final int svars = Integer.parseInt(m.group(2));
            final int tvars = Integer.parseInt(m.group(3));
            decls.add(sdefStub(b, strategyName, svars, tvars));
        }
        return decls;
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

    private static StaticCheckOutput staticCheck(boolean checkOk, ExecContext execContext, String mainFileModulePath,
        Map<String, Set<String>> imports, Map<String, Set<String>> usedStrategies,
        Map<String, Map<String, Set<String>>> usedAmbStrategies, Map<String, Set<String>> usedConstructors,
        Map<String, Set<String>> definedStrategies, Map<String, Set<String>> definedConstructors,
        Map<String, Set<String>> definedCongruences, Set<String> externalStrategies, Set<String> strategyNeedsExternal,
        Map<String, Set<String>> overlayConstrs) throws ExecException {
        // Cified-strategy-name (where the call occurs) to cified-strategy-name (amb call) to cified-strategy-name (amb call resolves to)
        final Map<String, SortedMap<String, String>> ambStratResolution = new HashMap<>();
        // Module-path to  visible when imported (transitive closure of strategy definitions)
        final Map<String, Set<String>> visibleStrategies = new HashMap<>(definedStrategies);
        for(Map.Entry<String, Set<String>> entry : visibleStrategies.entrySet()) {
            entry.setValue(new HashSet<>(entry.getValue()));
        }
        for(Map.Entry<String, Set<String>> entry : definedCongruences.entrySet()) {
            getOrInitialize(visibleStrategies, entry.getKey(), HashSet::new).addAll(entry.getValue());
        }
        // Module-path to constructor_arity names visible when imported (transitive closure of constructor definitions)
        final Map<String, Set<String>> visibleConstructors = new HashMap<>(definedConstructors);
        for(Map.Entry<String, Set<String>> entry : visibleConstructors.entrySet()) {
            entry.setValue(new HashSet<>(entry.getValue()));
        }

        // CHECK that extending and/or overriding strategies have an external strategy to extend and/or override
        Set<String> strategyNeedsExternalNonOverlap = Sets.difference(strategyNeedsExternal, externalStrategies);
        if(!strategyNeedsExternalNonOverlap.isEmpty()) {
            checkOk = false;
            execContext.logger()
                .error("Cannot find external strategies for override/extend " + strategyNeedsExternalNonOverlap, null);
        }

        // CHECK that overlays do not cyclically use each other
        final Deque<Set<String>> overlaySccs =
            Algorithms.topoSCCs(overlayConstrs.keySet(), k -> overlayConstrs.getOrDefault(k, Collections.emptySet()));
        overlaySccs.removeIf(s -> {
            String overlayName = s.iterator().next();
            return s.size() == 1 && !(overlayConstrs.getOrDefault(overlayName, Collections.emptySet())
                .contains(overlayName));
        });
        if(!overlaySccs.isEmpty()) {
            checkOk = false;
            for(Set<String> overlayScc : overlaySccs) {
                execContext.logger().error("Overlays have a cyclic dependency " + overlayScc, null);
            }
        }

        // CHECK that names can be resolved
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
                if(Library.Builtin.isBuiltinLibrary(moduleName)) {
                    continue;
                }
                visibleConstructors.put(moduleName, theVisibleConstructors);
                visibleStrategies.put(moduleName, theVisibleStrategies);
                Set<String> unresolvedConstructors =
                    Sets.difference(usedConstructors.getOrDefault(moduleName, Collections.emptySet()),
                        theVisibleConstructors);
                if(!unresolvedConstructors.isEmpty()) {
                    checkOk = false;
                    execContext.logger()
                        .error("In module " + moduleName + ": Cannot find constructors " + unresolvedConstructors,
                            null);
                }
                Set<String> unresolvedStrategies =
                    Sets.difference(usedStrategies.getOrDefault(moduleName, Collections.emptySet()),
                        theVisibleStrategies);
                if(!unresolvedStrategies.isEmpty()) {
                    checkOk = false;
                    execContext.logger()
                        .error("In module " + moduleName + ": Cannot find strategies " + unresolvedStrategies, null);
                }
                Set<String> strategiesOverlapWithExternal = Sets.difference(
                    Sets.intersection(definedStrategies.getOrDefault(moduleName, Collections.emptySet()),
                        externalStrategies), ALWAYS_DEFINED);
                if(!strategiesOverlapWithExternal.isEmpty()) {
                    checkOk = false;
                    execContext.logger().error("In module " + moduleName + ": Illegal overlap with external strategies "
                        + strategiesOverlapWithExternal, null);
                }
                Map<String, Set<String>> theUsedAmbStrategies =
                    new HashMap<>(usedAmbStrategies.getOrDefault(moduleName, Collections.emptyMap()));
                // By default a _0_0 strategy is used in the ambiguous call situation if one is defined.
                theUsedAmbStrategies.keySet().removeIf(theVisibleStrategies::contains);
                if(!theUsedAmbStrategies.isEmpty()) {
                    Map<String, Set<String>> differentArityDefinitions = new HashMap<>(theVisibleStrategies.size());
                    for(String theVisibleStrategy : theVisibleStrategies) {
                        String ambCallVersion = stripArity(theVisibleStrategy) + "_0_0";
                        getOrInitialize(differentArityDefinitions, ambCallVersion, HashSet::new)
                            .add(theVisibleStrategy);
                    }
                    for(Map.Entry<String, Set<String>> entry : theUsedAmbStrategies.entrySet()) {
                        final String usedAmbStrategy = entry.getKey();
                        final Set<String> defs =
                            differentArityDefinitions.getOrDefault(usedAmbStrategy, Collections.emptySet());
                        switch(defs.size()) {
                            case 0:
                                execContext.logger().error(
                                    "In module " + moduleName + ": Cannot find strategy " + usedAmbStrategy
                                        + " in ambiguous call position", null);
                                break;
                            case 1:
                                final String resolvedDef = defs.iterator().next();
                                for(String useSite : entry.getValue()) {
                                    getOrInitialize(ambStratResolution, useSite, TreeMap::new)
                                        .put(usedAmbStrategy, resolvedDef);
                                }
                                break;
                            default:
                                execContext.logger().error(
                                    "In module " + moduleName + ": Call to strategy " + usedAmbStrategy
                                        + " is ambiguous, multiple arities possible. ", null);
                        }
                    }
                }
            }
        }
//        if(!checkOk) {
//            throw new ExecException("One of the static checks failed. See above for error messages in the log. ");
//        }
        return new StaticCheckOutput(ambStratResolution);
    }

    private static IStrategoAppl sdefStub(B b, String strategyName, int svars, int tvars) throws ExecException {
        final IStrategoAppl newBody = b.applShared("Id");
        final IStrategoTerm name = b.stringShared(strategyName);

        final IStrategoTerm[] newSVarArray = new IStrategoTerm[svars];
        Arrays.fill(newSVarArray, newSVar);
        final IStrategoTerm newSVars = B.list(newSVarArray);

        final IStrategoTerm[] newTVarArray = new IStrategoTerm[tvars];
        Arrays.fill(newTVarArray, newTVar);
        final IStrategoTerm newTVars = B.list(newTVarArray);

        return b.applShared("SDefT", name, newSVars, newTVars, newBody);
    }

    private static String stripArity(String s) throws ExecException {
        if(s.substring(s.length() - 4, s.length()).matches("_\\d_\\d")) {
            return s.substring(0, s.length() - 4);
        }
        if(s.substring(s.length() - 5, s.length()).matches("_\\d+_\\d+")) {
            return s.substring(0, s.length() - 5);
        }
        Matcher m = stripArityPattern.matcher(s);
        if(!m.matches()) {
            throw new ExecException(
                "Frontend returned stratego strategy name that does not conform to cified name: '" + s + "'");
        }
        return m.group(1);
    }

    private static String projectName(String inputFile) {
        // *can* we get the project name somehow? This is probably more portable for non-project based compilation
        return Integer.toString(inputFile.hashCode());
    }

    static <K, V> V getOrInitialize(Map<K, V> map, K key, Supplier<V> initialize) {
        map.computeIfAbsent(key, ignore -> initialize.get());
        return map.get(key);
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.inputFile;
    }
}
