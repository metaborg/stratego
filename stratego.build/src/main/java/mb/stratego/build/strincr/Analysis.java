package mb.stratego.build.strincr;

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
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.metaborg.core.resource.IResourceService;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.stratego.build.strincr.StaticChecks.Data;
import mb.stratego.build.strincr.StaticChecks;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StringSetWithPositions;

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

        @Override public boolean equals(Object o) {
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

        @Override public int hashCode() {
            int result = inputFile.hashCode();
            result = 31 * result + includeDirs.hashCode();
            result = 31 * result + builtinLibs.hashCode();
            result = 31 * result + originTasks.hashCode();
            result = 31 * result + projectLocation.hashCode();
            return result;
        }
    }

    public static class Output implements Serializable {
        public StaticChecks.Data staticData;
        public BackendData backendData;
        public StaticChecks.Output staticCheckOutput;

        public Output(Data staticData, BackendData backendData) {
            this.staticData = staticData;
            this.backendData = backendData;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((backendData == null) ? 0 : backendData.hashCode());
            result = prime * result + ((staticCheckOutput == null) ? 0 : staticCheckOutput.hashCode());
            result = prime * result + ((staticData == null) ? 0 : staticData.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
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
                if(other.staticData != null)
                    return false;
            } else if(!staticData.equals(other.staticData))
                return false;
            return true;
        }
    }

    protected final IResourceService resourceService;

    protected final Frontend strIncrFront;
    protected final LibFrontend strIncrFrontLib;

    @Inject public Analysis(IResourceService resourceService, Frontend strIncrFront, LibFrontend strIncrFrontLib) {
        this.resourceService = resourceService;
        this.strIncrFront = strIncrFront;
        this.strIncrFrontLib = strIncrFrontLib;
    }

    protected Output collectInformation(ExecContext execContext, Input input, Path projectLocationPath)
        throws IOException, ExecException, InterruptedException {
        final Module inputModule = Module.source(projectLocationPath, Paths.get(input.inputFile.toURI()));

        final Output output = frontends(execContext, input, projectLocationPath, inputModule);

        long preCheckTime = System.nanoTime();

        // CHECK: constructor/strategy uses have definition which is imported
        output.staticCheckOutput = StaticChecks.check(execContext.logger(), inputModule.path, output.staticData,
            output.backendData.overlayConstrs);

        if(!output.staticCheckOutput.messages.isEmpty()) {
            // Commented during benchmarking, too many missing local imports to automatically fix.
            for(ErrorMessage message : output.staticCheckOutput.messages) {
                execContext.logger().error(message.toString(), null);
            }
            // throw new ExecException("One of the static checks failed. See above for error messages in the log. ");
        }

        BuildStats.checkTime = System.nanoTime() - preCheckTime;
        return output;
    }

    public Output frontends(ExecContext execContext, Input input, Path projectLocationPath, Module inputModule)
        throws IOException, mb.pie.api.ExecException, InterruptedException {
        // FRONTEND
        final Set<Module> seen = new HashSet<>();
        final Deque<Module> workList = new ArrayDeque<>();
        workList.add(inputModule);
        seen.add(inputModule);

        final List<Import> defaultImports = new ArrayList<>(input.builtinLibs.size());
        for(String builtinLib : input.builtinLibs) {
            defaultImports.add(Import.library(builtinLib));
        }
        // depend on the include directories in which we search for str and rtree files
        for(File includeDir : input.includeDirs) {
            execContext.require(includeDir);
        }

        StaticChecks.Data staticData = new StaticChecks.Data();

        BackendData backendData = new BackendData();

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

                final Set<String> overlappingStrategies = Sets.difference(
                    Sets.intersection(staticData.externalStrategies.readSet(), frontLibOutput.strategies.readSet()),
                    StaticChecks.ALWAYS_DEFINED);
                if(!overlappingStrategies.isEmpty()) {
                    execContext.logger().warn("Overlapping external strategy definitions: " + overlappingStrategies,
                        null);
                }

                staticData.externalStrategies.addAll(frontLibOutput.strategies);
                staticData.externalConstructors.addAll(frontLibOutput.constrs);

                BuildStats.shuffleLibTime += System.nanoTime() - shuffleStartTime;

                continue;
            }

            final String projectName = projectName(module.path);
            final Frontend.Input frontInput = new Frontend.Input(projectLocationPath.toFile(),
                module.resolveFrom(projectLocationPath), projectName, input.originTasks);
            final @Nullable Frontend.NormalOutput frontOutput =
                execContext.require(strIncrFront, frontInput).normalOutput();
            if(frontOutput != null) {
                for(Map.Entry<String, Integer> strategyNoOfDefs : frontOutput.noOfDefinitions.entrySet()) {
                    Relation
                        .getOrInitialize(BuildStats.modulesDefiningStrategy, strategyNoOfDefs.getKey(), ArrayList::new)
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
                    Relation.getOrInitialize(backendData.strategyASTs, strategyName, ArrayList::new)
                        .add(gen.getValue());
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

                    Relation.getOrInitialize(backendData.overlayASTs, overlayName, ArrayList::new)
                        .addAll(gen.getValue());
                }
                for(Map.Entry<String, StringSetWithPositions> gen : frontOutput.overlayConstrs.entrySet()) {
                    final String overlayName = gen.getKey();
                    Relation.getOrInitialize(backendData.overlayConstrs, overlayName, HashSet::new)
                        .addAll(gen.getValue().readSet());
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
        return new Output(staticData, backendData);
    }

    private static String projectName(String inputFile) {
        // *can* we get the project name somehow? This is probably more portable for non-project based compilation
        return Integer.toString(inputFile.hashCode());
    }
}
