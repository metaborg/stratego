package mb.stratego.build.strincr;

import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import mb.flowspec.terms.B;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.stratego.build.strincr.StaticChecks.Data;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StringSetWithPositions;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.metaborg.core.resource.IResourceService;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;

public class Analysis {
    public static class Input implements Serializable {
        protected final File inputFile;
        protected final Collection<File> includeDirs;
        protected final Collection<String> builtinLibs;
        protected final Collection<STask> originTasks;
        protected final File projectLocation;

        public Input(File inputFile, Collection<File> includeDirs, Collection<String> builtinLibs,
            Collection<STask> originTasks, File projectLocation) {
            this.inputFile = inputFile;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.originTasks = originTasks;
            this.projectLocation = projectLocation;

        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!inputFile.equals(input.inputFile))
                return false;
            if(!includeDirs.equals(input.includeDirs))
                return false;
            if(!builtinLibs.equals(input.builtinLibs))
                return false;
            // noinspection SimplifiableIfStatement
            if(!originTasks.equals(input.originTasks))
                return false;
            return projectLocation.equals(input.projectLocation);
        }

        @Override
        public int hashCode() {
            int result = inputFile.hashCode();
            result = 31 * result + includeDirs.hashCode();
            result = 31 * result + builtinLibs.hashCode();
            result = 31 * result + originTasks.hashCode();
            result = 31 * result + projectLocation.hashCode();
            return result;
        }
    }

    public static class Output implements Serializable {
        public final StaticChecks.Data staticData;
        public final BackendData backendData;
        public StaticChecks.Output staticCheckOutput;
        public final List<Message<?>> messages;

        public Output(Data staticData, BackendData backendData, List<Message<?>> messages) {
            this.staticData = staticData;
            this.backendData = backendData;
            this.messages = messages;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((backendData == null) ? 0 : backendData.hashCode());
            result = prime * result + ((staticCheckOutput == null) ? 0 : staticCheckOutput.hashCode());
            result = prime * result + ((staticData == null) ? 0 : staticData.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Output other = (Output) obj;
            if(backendData == null) {
                if(other.backendData != null)
                    return false;
            } else if(!backendData.equals(other.backendData))
                return false;
            if(staticCheckOutput == null) {
                if(other.staticCheckOutput != null)
                    return false;
            } else if(!staticCheckOutput.equals(other.staticCheckOutput))
                return false;
            if(staticData == null) {
                return other.staticData == null;
            } else
                return staticData.equals(other.staticData);
        }
    }

    protected final IResourceService resourceService;

    protected final Frontend strIncrFront;
    protected final InsertCasts strIncrInsertCasts;
    protected final LibFrontend strIncrFrontLib;

    private final StrIncrContext strContext;

    @Inject
    public Analysis(IResourceService resourceService, Frontend strIncrFront, InsertCasts strIncrInsertCasts,
        LibFrontend strIncrFrontLib, StrIncrContext strContext) {
        this.resourceService = resourceService;
        this.strIncrFront = strIncrFront;
        this.strIncrInsertCasts = strIncrInsertCasts;
        this.strIncrFrontLib = strIncrFrontLib;
        this.strContext = strContext;
    }

    protected Output collectInformation(ExecContext execContext, Input input, Path projectLocationPath)
        throws IOException, ExecException, InterruptedException {
        final Module inputModule = Module.source(projectLocationPath, Paths.get(input.inputFile.toURI()));

        final Output output = frontends(execContext, input, projectLocationPath, inputModule);

        insertCasts(execContext, output);

        long preCheckTime = System.nanoTime();

        // CHECK: constructor/strategy uses have definition which is imported
        output.staticCheckOutput = StaticChecks
            .check(execContext.logger(), inputModule.path, output.staticData, output.backendData.overlayConstrs,
                output.messages);

        BuildStats.checkTime = System.nanoTime() - preCheckTime;
        return output;
    }

    private void insertCasts(ExecContext execContext, Output output) throws ExecException, InterruptedException {
        final Map.Transient<IStrategoTerm, IStrategoTerm> sEnv = Map.Transient.of();
        final Map.Transient<IStrategoTerm, IStrategoTerm> constrs = Map.Transient.of();

        final Set.Transient<IStrategoTerm> injClos = Set.Transient.of();
        final Map.Transient<IStrategoTerm, IStrategoTerm> lub = Map.Transient.of();
        final Map.Transient<IStrategoTerm, IStrategoTerm> lvl = Map.Transient.of();

        final IStrategoTerm err = strContext.getFactory().makeAppl("Err");
        final IStrategoTerm warn = strContext.getFactory().makeAppl("Warn");

        for(java.util.Map.Entry<String, Boolean> e : output.staticData.strictnessLevel.entrySet()) {
            if(e.getValue()) {
                lvl.__put(strContext.getFactory().makeString(e.getKey()), err);
            } else {
                lvl.__put(strContext.getFactory().makeString(e.getKey()), warn);
            }
        }

        final StrategoImmutableMap strategyEnvironment = new StrategoImmutableMap(sEnv.freeze());
        final StrategoImmutableMap constructors = new StrategoImmutableMap(constrs.freeze());
        final StrategoImmutableSet injectionClosure = new StrategoImmutableSet(injClos.freeze());
        final StrategoImmutableMap lubMap = new StrategoImmutableMap(lub.freeze());
        final StrategoImmutableMap strictnessLevel = new StrategoImmutableMap(lvl.freeze());

        final java.util.Map<String, List<IStrategoAppl>> asts = output.backendData.strategyASTs;

        InsertCasts.Input input = new InsertCasts.Input(strategyEnvironment, constructors, injectionClosure, lubMap, strictnessLevel, asts);
        InsertCasts.Output icOutput = execContext.require(strIncrInsertCasts.createTask(input));
        output.backendData.strategyASTs = icOutput.astsWithCasts;

    }

    public Output frontends(ExecContext execContext, Input input, Path projectLocationPath, Module inputModule)
        throws IOException, mb.pie.api.ExecException, InterruptedException {
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
        for(File includeDir : input.includeDirs) {
            execContext.require(includeDir);
        }

        final StaticChecks.Data staticData = new StaticChecks.Data();

        final BackendData backendData = new BackendData();
        final List<Message<?>> messages = new ArrayList<>();

        long shuffleStartTime;
        do {
            final Module module = workList.remove();

            if(module.type == Module.Type.library) {
                final LibFrontend.Input frontLibInput =
                    new LibFrontend.Input(Library.fromString(resourceService, module.path));
                final LibFrontend.Output frontLibOutput = execContext.require(strIncrFrontLib, frontLibInput);

                shuffleStartTime = System.nanoTime();

                staticData.registerStrategyDefinitions(module, frontLibOutput.strategies);
                staticData.registerConstructorDefinitions(module, frontLibOutput.constrs, new StringSetWithPositions());

                final java.util.Set<String> overlappingStrategies = Sets.difference(
                    Sets.intersection(staticData.externalStrategies.readSet(), frontLibOutput.strategies.readSet()),
                    StaticChecks.ALWAYS_DEFINED);
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
                new Frontend.Input(projectLocationPath.toFile(), module.resolveFrom(projectLocationPath), projectName,
                    input.originTasks);
            final @Nullable Frontend.NormalOutput frontOutput =
                execContext.require(strIncrFront, frontInput).normalOutput();

            if(frontOutput == null) {
                execContext.logger().debug("File deletion detected: " + module.resolveFrom(projectLocationPath));
                continue;
            }
            execContext.logger().debug("File parsed: " + module.resolveFrom(projectLocationPath));

            for(Map.Entry<String, Integer> strategyNoOfDefs : frontOutput.noOfDefinitions.entrySet()) {
                Relation.getOrInitialize(BuildStats.modulesDefiningStrategy, strategyNoOfDefs.getKey(), ArrayList::new)
                    .add(strategyNoOfDefs.getValue());
            }
            shuffleStartTime = System.nanoTime();

            final List<Import> theImports = new ArrayList<>(frontOutput.imports);
            theImports.addAll(defaultImports);

            // combining output for check
            for(StringSetWithPositions usedConstrs : frontOutput.strategyConstrs.values()) {
                Relation.getOrInitialize(staticData.usedConstructors, module.path, StringSetWithPositions::new)
                    .addAll(usedConstrs);
            }
            staticData.sugarASTs.put(module.path, frontOutput.sugarAST);
            staticData.usedStrategies.put(module.path, frontOutput.usedStrategies);
            staticData.usedAmbStrategies.put(module.path, frontOutput.ambStratUsed);
            staticData.ambStratPositions.put(module.path, frontOutput.ambStratPositions);
            staticData.registerStrategyDefinitions(module, frontOutput.strats);
            staticData.registerCongruenceDefinitions(module, frontOutput.congrs);
            staticData.registerConstructorDefinitions(module, frontOutput.constrs, frontOutput.overlays);

            staticData.strategyNeedsExternal.addAll(frontOutput.strategyNeedsExternal);


            // shuffling output for backend
            for(Map.Entry<String, IStrategoAppl> gen : frontOutput.strategyASTs.entrySet()) {
                String strategyName = gen.getKey();
                // ensure the strategy is a key in the strategyFiles map
                Relation.getOrInitialize(backendData.strategyASTs, strategyName, ArrayList::new).add(gen.getValue());
                Relation.getOrInitialize(backendData.strategyConstrs, strategyName, HashSet::new)
                    .addAll(frontOutput.strategyConstrs.get(strategyName).readSet());
            }
            for(Map.Entry<String, IStrategoAppl> gen : frontOutput.congrASTs.entrySet()) {
                final String congrName = gen.getKey();
                backendData.congrASTs.put(congrName, gen.getValue());
                Relation.getOrInitialize(backendData.strategyConstrs, congrName, HashSet::new)
                    .addAll(frontOutput.strategyConstrs.get(congrName).readSet());
            }
            for(Map.Entry<String, List<IStrategoAppl>> gen : frontOutput.overlayASTs.entrySet()) {
                final String overlayName = gen.getKey();

                Relation.getOrInitialize(backendData.overlayASTs, overlayName, ArrayList::new).addAll(gen.getValue());
            }
            for(Map.Entry<String, StringSetWithPositions> gen : frontOutput.overlayConstrs.entrySet()) {
                final String overlayName = gen.getKey();
                Relation.getOrInitialize(backendData.overlayConstrs, overlayName, HashSet::new)
                    .addAll(gen.getValue().readSet());
            }

            // resolving imports
            final java.util.Set<Module> expandedImports = Module
                .resolveWildcards(execContext, module.path, theImports, input.includeDirs, projectLocationPath,
                    messages);
            for(Module m : expandedImports) {
                Relation.getOrInitialize(staticData.imports, module.path, HashSet::new).add(m.path);
            }
            expandedImports.removeAll(seen);
            workList.addAll(expandedImports);
            seen.addAll(expandedImports);

            BuildStats.shuffleTime += System.nanoTime() - shuffleStartTime;
        } while(!workList.isEmpty());
        return new Output(staticData, backendData, messages);
    }

    private static String projectName(String inputFile) {
        // *can* we get the project name somehow? This is probably more portable for non-project based compilation
        return Integer.toString(inputFile.hashCode());
    }
}
