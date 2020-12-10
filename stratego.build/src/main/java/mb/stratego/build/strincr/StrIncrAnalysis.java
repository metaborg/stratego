package mb.stratego.build.strincr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.util.B;

import com.google.common.collect.Sets;

import mb.pie.api.ExecContext;
import mb.pie.api.Logger;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.SplitResult.ConstructorSignature;
import mb.stratego.build.strincr.SplitResult.StrategySignature;
import mb.stratego.build.strincr.StaticChecks.Data;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrategoGradualSetting;
import mb.stratego.build.util.StrategyEnvironment;
import mb.stratego.build.util.StringSetWithPositions;
import mb.stratego.build.util.TermEqWithAttachments;

public class StrIncrAnalysis implements TaskDef<StrIncrAnalysis.Input, StrIncrAnalysis.Output> {
    public static class Input implements Serializable {
        protected final ResourcePath inputFile;
        protected final Collection<ResourcePath> includeDirs;
        protected final Collection<String> builtinLibs;
        protected final Collection<STask<?>> originTasks;
        protected final ResourcePath projectLocation;
        protected final StrategoGradualSetting strGradualSetting;
        public final @Nullable String moduleName;
        public final @Nullable IStrategoAppl ast;

        public Input(ResourcePath inputFile, Collection<ResourcePath> includeDirs, Collection<String> builtinLibs,
            Collection<STask<?>> originTasks, ResourcePath projectLocation, StrategoGradualSetting strGradualSetting) {
            this.inputFile = inputFile;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.originTasks = originTasks;
            this.projectLocation = projectLocation;
            this.strGradualSetting = strGradualSetting;
            this.moduleName = null;
            this.ast = null;
        }

        public Input(ResourcePath inputFile, Collection<ResourcePath> includeDirs, Collection<String> builtinLibs,
            Collection<STask<?>> originTasks, ResourcePath projectLocation, StrategoGradualSetting strGradualSetting,
            String moduleName, IStrategoAppl ast) {
            this.inputFile = inputFile;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.originTasks = originTasks;
            this.projectLocation = projectLocation;
            this.strGradualSetting = strGradualSetting;
            this.moduleName = moduleName;
            this.ast = ast;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(getClass() != o.getClass())
                return false;
            Input input = (Input) o;
            return inputFile.equals(input.inputFile) && includeDirs.equals(input.includeDirs)
                && builtinLibs.equals(input.builtinLibs) && originTasks.equals(input.originTasks)
                && projectLocation.equals(input.projectLocation) && strGradualSetting == input.strGradualSetting
                && Objects.equals(moduleName, input.moduleName) && Objects.equals(ast, input.ast);
        }

        @Override public int hashCode() {
            return Objects.hash(inputFile, includeDirs, builtinLibs, originTasks, projectLocation, strGradualSetting,
                moduleName, ast);
        }

        @Override public String toString() {
            return "FrontEnds$Input(" + "inputFile=" + inputFile + ", includeDirs=" + includeDirs + ", builtinLibs="
                + builtinLibs + ", originTasks=" + originTasks + ", projectLocation=" + projectLocation
                + ", strGradualSetting=" + strGradualSetting + ", moduleName=" + moduleName + ", ast="
                + (ast == null ? "null" : ast.toString(5)) + ')';
        }
    }

    public static class Output implements Serializable {
        public final StaticChecks.Data staticData;
        public final BackendData backendData;
        public final Map<String, SplitResult> splitModules;
        public final List<Message<?>> messages;

        public Output(Data staticData, BackendData backendData, Map<String, SplitResult> splitModules,
            List<Message<?>> messages) {
            this.staticData = staticData;
            this.backendData = backendData;
            this.splitModules = splitModules;
            this.messages = messages;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(getClass() != o.getClass())
                return false;
            Output output = (Output) o;
            return staticData.equals(output.staticData) && backendData.equals(output.backendData)
                && splitModules.equals(output.splitModules) && messages.equals(output.messages);
        }

        @Override public int hashCode() {
            return Objects.hash(staticData, backendData, splitModules, messages);
        }
    }

    public static final String id = StrIncrAnalysis.class.getCanonicalName();
    protected final LibFrontend strIncrFrontLib;
    private final StaticChecks staticChecks;
    private final SubFrontend strIncrSubFront;
    private final ParseStratego parseStratego;

    @Inject public StrIncrAnalysis(LibFrontend strIncrFrontLib, StaticChecks staticChecks,
        ParseStratego parseStratego, SubFrontend strIncrSubFront) {
        this.strIncrFrontLib = strIncrFrontLib;
        this.staticChecks = staticChecks;
        this.strIncrSubFront = strIncrSubFront;
        this.parseStratego = parseStratego;
    }

    @Override public StrIncrAnalysis.Output exec(ExecContext execContext, StrIncrAnalysis.Input input)
        throws Exception {
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

        final Module inputModule = Module.source(input.inputFile);

        final StrIncrAnalysis.Output output = frontends(execContext, input, inputModule);

        long preCheckTime = System.nanoTime();

        // CHECK: constructor/strategy uses have definition which is imported
        staticChecks.insertCasts(execContext, inputModule.path, output,
            input.projectLocation, input.strGradualSetting);

        BuildStats.checkTime = System.nanoTime() - preCheckTime;
        return output;
    }

    public StrIncrAnalysis.Output frontends(ExecContext execContext, StrIncrAnalysis.Input input, Module inputModule)
        throws Exception {
        // FRONTEND
        final java.util.Set<Module> seen = new HashSet<>();
        final Deque<Module> workList = new ArrayDeque<>();
        workList.add(inputModule);
        seen.add(inputModule);

        final List<Import> defaultImports = new ArrayList<>(input.builtinLibs.size());
        for(String builtinLib : input.builtinLibs) {
            defaultImports.add(Import.library(B.string(builtinLib)));
        }
        // depend on the include directories in which we search for str and rtree files
        for(ResourcePath includeDir : input.includeDirs) {
            execContext.require(includeDir);
        }

        final StaticChecks.Data staticData = new StaticChecks.Data();

        final BackendData backendData = new BackendData();
        final List<Message<?>> messages = new ArrayList<>();
        final Map<String, SplitResult> splitModules = new HashMap<>();

        long shuffleStartTime;
        do {
            final Module module = workList.remove();

            if(module.type == Module.Type.library) {
                final LibFrontend.Input frontLibInput =
                    new LibFrontend.Input(Library.fromString(execContext.getResourceService(), module.path));
                final LibFrontend.Output frontLibOutput = execContext.require(strIncrFrontLib, frontLibInput);

                shuffleStartTime = System.nanoTime();

                final StrategyEnvironment strategies = new StrategyEnvironment();
                for(StrategySignature strategy : frontLibOutput.strategies.keySet()) {
                    strategies.add(new StrategoString(strategy.cifiedName(), AbstractTermFactory.EMPTY_LIST));
                }
                staticData.definedStrategies.put(module.path, strategies);
                staticData.libraryStrategies.put(module.path, frontLibOutput.strategies);
                staticData.registerConstructorDefinitions(module.path, frontLibOutput.constrs);
                staticData.libraryInjections.put(module.path, frontLibOutput.injs);

                reportOverlappingStrategies(staticData.libraryExternalStrategies, strategies,
                    execContext.logger());

                staticData.libraryExternalStrategies.addAll(strategies);
                final StringSetWithPositions constrs = new StringSetWithPositions();
                for(ConstructorSignature constr : frontLibOutput.constrs.keySet()) {
                    constrs.add(new StrategoString(constr.cifiedName(), AbstractTermFactory.EMPTY_LIST));
                }
                staticData.externalConstructors.addAll(constrs);

                BuildStats.shuffleLibTime += System.nanoTime() - shuffleStartTime;

                continue;
            }

            final ResourceService resourceService = execContext.getResourceService();
            final HierarchicalResource inputFile = resourceService.getHierarchicalResource(ResourceKeyString.parse(module.path));
            final IStrategoTerm ast;
            if(input.moduleName != null && module.path.endsWith(input.moduleName)) {
                execContext.require(inputFile);
                execContext.logger().debug("File open in editor: " + module.path);
                ast = input.ast;
            } else {
                // File existence:
                if(!inputFile.exists()) {
                    execContext.logger().trace("File deletion detected: " + module.path);
                    continue;
                }
                // Parse file:
                execContext.require(inputFile);
                try(final InputStream inputStream = new BufferedInputStream(inputFile.openRead())) {
                    if("rtree".equals(inputFile.getLeafExtension())) {
                        ast = parseStratego.parseRtree(inputStream);
                    } else {
                        // TODO: The metaborg resource service uses strings like java##file:///Users/jeff/... for paths
                        //       but this is not recognised as a path by the VFS2 used in Spoofax 2, which results in
                        //       debug messages in the following method:
                        ast = parseStratego.parse(inputStream, StandardCharsets.UTF_8, module.path);
                    }
                }
            }
            execContext.logger().trace("File parsed: " + module.path);
            staticData.sugarASTs.put(module.path, ast);
            // Split file up with PIE task:
            final SubFrontend.Input splitInput =
                SubFrontend.Input.split(input.originTasks, module.path, module.path, new TermEqWithAttachments(ast));
            final IStrategoTerm splitTerm = execContext.require(strIncrSubFront, splitInput).result;
            final SplitResult splitResult = SplitResult.fromTerm(splitTerm, module.path);
            // Save results for use in different order during type checking
            splitModules.put(module.path, splitResult);
            // Resolve imports
            final Set<Module> expandedImports =
                resolveImports(input, defaultImports, messages, module, splitResult,
                    execContext);
            for(Module m : expandedImports) {
                Relation.getOrInitialize(staticData.imports, module.path, HashSet::new).add(m.path);
            }
            expandedImports.removeAll(seen);
            workList.addAll(expandedImports);
            seen.addAll(expandedImports);
        } while(!workList.isEmpty());
        return new StrIncrAnalysis.Output(staticData, backendData, splitModules, messages);
    }

    public static Set<Module> resolveImports(StrIncrAnalysis.Input input, List<Import> defaultImports,
        List<Message<?>> messages, Module module, SplitResult splitResult,
        ExecContext execContext) throws IOException {
        final List<Import> theImports = new ArrayList<>(splitResult.imports.size());
        for(IStrategoTerm importTerm : splitResult.imports) {
            theImports.add(Import.fromTerm(importTerm));
        }
        theImports.addAll(defaultImports);

        return Module
            .resolveWildcards(module.path, theImports, input.includeDirs, messages, execContext);
    }

    public static void reportOverlappingStrategies(StrategyEnvironment externalStrategies,
        StrategyEnvironment newExternalStrategies, Logger logger) {
        final java.util.Set<String> overlappingStrategies = Sets.difference(
            Sets.intersection(externalStrategies.readSet(), newExternalStrategies.readSet()),
            StaticChecks.ALWAYS_DEFINED);
        if(!overlappingStrategies.isEmpty()) {
            logger.warn("Overlapping external strategy definitions: " + overlappingStrategies, null);
        }
    }

    @Override public String getId() {
        return id;
    }
}
