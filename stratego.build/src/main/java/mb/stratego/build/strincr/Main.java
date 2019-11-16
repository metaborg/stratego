package mb.stratego.build.strincr;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.strj.strj;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import mb.flowspec.terms.B;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.CommonPaths;
import mb.stratego.build.util.Relation;

public class Main implements TaskDef<Main.Input, None> {
    public static final String id = Main.class.getCanonicalName();

    private static final IStrategoAppl A_TERM;
    private static final IStrategoTerm newSVar;
    private static final IStrategoTerm newTVar;

    static {
        final B b = new B(strj.init().getFactory());
        A_TERM = b.applShared("Sort", B.string("ATerm"), B.list());
        newSVar = b.applShared("VarDec", B.string("a"), b.applShared("FunType", A_TERM, A_TERM));
        newTVar = b.applShared("VarDec", B.string("a"), b.applShared("ConstType", A_TERM));
    }

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

        static Set<Module> resolveWildcards(ExecContext execContext, Collection<Frontend.Import> imports,
            Collection<File> includeDirs, Path projectLocation) throws ExecException, IOException {
            final Set<Module> result = new HashSet<>(imports.size() * 2);
            for(Frontend.Import anImport : imports) {
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
                            execContext.require(path);
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

    private final IResourceService resourceService;

    private final Frontend strIncrFront;
    private final LibFrontend strIncrFrontLib;
    private final Backend strIncrBack;

    @Inject public Main(IResourceService resourceService, Frontend strIncrFront, LibFrontend strIncrFrontLib,
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

        // FRONTEND
        final Set<Module> seen = new HashSet<>();
        final Deque<Module> workList = new ArrayDeque<>();
        final Module inputModule = Module.source(projectLocationPath, Paths.get(input.inputFile.toURI()));
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
        
        final StaticChecks.Data staticData = new StaticChecks.Data();

        // External constructors that will be imported in Java
        final Set<String> externalConstructors = new HashSet<>();
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
                externalConstructors.addAll(frontLibOutput.constrs);

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
                    Relation.getOrInitialize(BuildStats.modulesDefiningStrategy, strategyNoOfDefs.getKey(), ArrayList::new)
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
                    Relation.getOrInitialize(strategyASTs, strategyName, ArrayList::new).add(gen.getValue());
                    Relation.getOrInitialize(strategyConstrs, strategyName, HashSet::new)
                        .addAll(frontOutput.strategyConstrs.get(strategyName));
                }
                for(Map.Entry<String, IStrategoAppl> gen : frontOutput.congrASTs.entrySet()) {
                    final String congrName = gen.getKey();
                    congrASTs.put(congrName, gen.getValue());
                    Relation.getOrInitialize(strategyConstrs, congrName, HashSet::new)
                        .addAll(frontOutput.strategyConstrs.get(congrName));
                }
                for(Map.Entry<String, List<IStrategoAppl>> gen : frontOutput.overlayASTs.entrySet()) {
                    final String overlayName = gen.getKey();
                    Relation.getOrInitialize(overlayASTs, overlayName, ArrayList::new).addAll(gen.getValue());
                }
                for(Map.Entry<String, Set<String>> gen : frontOutput.overlayConstrs.entrySet()) {
                    final String overlayName = gen.getKey();
                    Relation.getOrInitialize(overlayConstrs, overlayName, HashSet::new).addAll(gen.getValue());
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

        long preCheckTime = System.nanoTime();

        // CHECK: constructor/strategy uses have definition which is imported
        final StaticChecks.Output staticCheckOutput =
            StaticChecks.check(execContext, inputModule.path, staticData, overlayConstrs);


        if(!staticCheckOutput.staticNameCheck) {
            // Commented during benchmarking, too many missing local imports to automatically fix.
//            throw new ExecException("One of the static checks failed. See above for error messages in the log. ");
        }

        BuildStats.checkTime = System.nanoTime() - preCheckTime;

        // BACKEND
        long backendStart = System.nanoTime();
        final Arguments args = new Arguments();
        args.addAll(input.extraArgs);
        for(String builtinLib : input.builtinLibs) {
            args.add("-la", builtinLib);
        }
        BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
        for(String strategyName : strategyASTs.keySet()) {
            backendStart = System.nanoTime();
            /* TODO: don't use a set, it could in a very strange case affect the semantics of the Stratego problem
                (side-effect, then failure, purposefully defined multiple times).
               This set is used right now to eliminate overhead in the generated helper strategies of dynamic rules,
               which should be defined once per rule-name but are defined once per rule-name per module. Fix that in a
               principled way, then this can go back to a list.
             */
            final Set<IStrategoAppl> strategyContributions;
            final List<IStrategoAppl> strategyOverlayFiles = new ArrayList<>();
            strategyContributions = new HashSet<>(strategyASTs.get(strategyName));
            for(String overlayName : requiredOverlays(strategyName, strategyConstrs, overlayConstrs)) {
                strategyOverlayFiles.addAll(overlayASTs.getOrDefault(overlayName, Collections.emptyList()));
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
        for(Map.Entry<String, IStrategoAppl> entry : congrASTs.entrySet()) {
            backendStart = System.nanoTime();
            String congrName = entry.getKey();
            IStrategoAppl congrAST = entry.getValue();
            if(!strategyASTs.getOrDefault(congrName + "_0", Collections.emptyList()).isEmpty()) {
                continue;
            }
            if(externalConstructors.contains(congrName)) {
                execContext.logger()
                    .info("Dropping congruence for " + congrName + " in favour of external definition in library. ");
                continue;
            }
            final List<IStrategoAppl> strategyContributions;
            final List<IStrategoAppl> strategyOverlayFiles = new ArrayList<>();
            strategyContributions = Collections.singletonList(congrAST);
            for(String overlayName : requiredOverlays(congrName, strategyConstrs, overlayConstrs)) {
                strategyOverlayFiles.addAll(overlayASTs.getOrDefault(overlayName, Collections.emptyList()));
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
            final List<IStrategoAppl> decls = declStubs(strategyASTs);
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

        return None.instance;
    }

    private List<IStrategoAppl> declStubs(Map<String, List<IStrategoAppl>> strategyASTs) throws ExecException {
        final List<IStrategoAppl> decls = new ArrayList<>(strategyASTs.size());
        final B b = new B(strj.init().getFactory());
        for(String strategyName : strategyASTs.keySet()) {
            final Matcher m = StaticChecks.stripArityPattern.matcher(strategyName);
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
