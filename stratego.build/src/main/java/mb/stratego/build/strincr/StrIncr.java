package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
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

import javax.annotation.Nullable;

import mb.resource.hierarchical.ResourcePath;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.fs.FSPath;
import mb.stratego.build.strincr.Analysis.Output;
import mb.stratego.build.util.CommonPaths;
import mb.stratego.build.util.Relation;

public class StrIncr implements TaskDef<StrIncr.Input, None> {
    public static final String id = StrIncr.class.getCanonicalName();

    public static final class Input extends Analysis.Input {
        final @Nullable String javaPackageName;
        final @Nullable ResourcePath cacheDir;
        final List<String> constants;
        final Arguments extraArgs;
        final ResourcePath outputPath;

        public Input(ResourcePath inputFile, @Nullable String javaPackageName, Collection<ResourcePath> includeDirs,
            Collection<String> builtinLibs, @Nullable ResourcePath cacheDir, List<String> constants, Arguments extraArgs,
            ResourcePath outputPath, Collection<STask> originTasks, ResourcePath projectLocation) {
            super(inputFile, includeDirs, builtinLibs, originTasks, projectLocation);
            this.javaPackageName = javaPackageName;
            this.cacheDir = cacheDir;
            this.constants = constants;
            this.extraArgs = extraArgs;
            this.outputPath = outputPath;
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

    private final Backend strIncrBack;
    private final StrIncrAnalysis strIncrAnalysis;
    static ArrayList<Long> timestamps = new ArrayList<>();

    @Inject public StrIncr(Backend strIncrBack, StrIncrAnalysis analysis) {
        this.strIncrBack = strIncrBack;
        this.strIncrAnalysis = analysis;
    }

    @Override public None exec(ExecContext execContext, Input input) throws Exception {
        timestamps.add(System.nanoTime());

        timestamps.add(System.nanoTime());
        final Output result = execContext.require(strIncrAnalysis, input);
        timestamps.add(System.nanoTime());

        if(!result.messages.isEmpty()) {
            boolean error = false;
            for(Message<?> message : result.messages) {
                switch(message.severity) {
                    case NOTE:
                        execContext.logger().info(message.toString());
                        break;
                    case WARNING:
                        execContext.logger().warn(message.toString(), null);
                        break;
                    case ERROR:
                        execContext.logger().error(message.toString(), null);
                        error = true;
                        break;
                }
            }
            if(error) {
                timestamps.add(System.nanoTime());
                throw new ExecException("One of the static checks failed. See above for error messages in the log. ");
            }
        }

        // BACKEND
        backends(execContext, input, input.projectLocation, result.staticData, result.backendData,
            result.staticCheckOutput);

        timestamps.add(System.nanoTime());
        return None.instance;
    }

    private void backends(ExecContext execContext, Input input, ResourcePath projectLocation,
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
            /* This set is used right now to eliminate overhead in the generated helper strategies of dynamic rules,
               which should be defined once per rule-name but are defined once per rule-name per module.
               If that can be fixed in a principled way, then this can go back to a list.
             */
            final Set<IStrategoAppl> strategyContributions;
            final List<IStrategoAppl> strategyOverlayFiles = new ArrayList<>();
            strategyContributions = new HashSet<>(backendData.strategyASTs.get(strategyName));
            for(String overlayName : requiredOverlays(strategyName, backendData.strategyConstrs, backendData.overlayConstrs)) {
                strategyOverlayFiles.addAll(backendData.overlayASTs.getOrDefault(overlayName, Collections.emptyList()));
            }

            // GK: commented dead code
//            final @Nullable File strategyDir =
//                execContext.getResourceService().toLocalFile(CommonPaths.strSepCompStrategyDir(projectLocation, strategyName));
//            assert strategyDir
//                != null : "Bug in strSepCompStrategyDir or the arguments thereof: returned path is not a directory";
            final SortedMap<String, String> ambStrategyResolution =
                staticCheckOutput.ambStratResolution.getOrDefault(strategyName, Collections.emptySortedMap());
            Backend.Input backEndInput =
                new Backend.Input(projectLocation, strategyName, strategyContributions,
                    strategyOverlayFiles, ambStrategyResolution, input.javaPackageName, input.outputPath,
                    input.cacheDir, input.constants, input.includeDirs, args, false);
            BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
            timestamps.add(System.nanoTime());
            execContext.require(strIncrBack.createTask(backEndInput));
            timestamps.add(System.nanoTime());
        }
        ArrayList<String> droppedCongruences = new ArrayList<>();
        for(Map.Entry<String, IStrategoAppl> entry : backendData.congrASTs.entrySet()) {
            backendStart = System.nanoTime();
            String congrName = entry.getKey();
            IStrategoAppl congrAST = entry.getValue();
            if(!backendData.strategyASTs.getOrDefault(congrName + "_0", Collections.emptyList()).isEmpty()) {
                continue;
            }
            if(staticData.externalConstructors.contains(congrName)) {
                droppedCongruences.add(congrName);
                continue;
            }
            final List<IStrategoAppl> strategyContributions;
            final List<IStrategoAppl> strategyOverlayFiles = new ArrayList<>();
            strategyContributions = Collections.singletonList(congrAST);
            for(String overlayName : requiredOverlays(congrName, backendData.strategyConstrs, backendData.overlayConstrs)) {
                strategyOverlayFiles.addAll(backendData.overlayASTs.getOrDefault(overlayName, Collections.emptyList()));
            }

            final @Nullable File strategyDir =
                execContext.getResourceService().toLocalFile(CommonPaths.strSepCompStrategyDir(projectLocation, congrName));
            assert strategyDir
                != null : "Bug in strSepCompStrategyDir or the arguments thereof: returned path is not a directory";
            Backend.Input backEndInput =
                new Backend.Input(projectLocation, congrName, strategyContributions,
                    strategyOverlayFiles, Collections.emptySortedMap(), input.javaPackageName, input.outputPath,
                    input.cacheDir, input.constants, input.includeDirs, args, false);
            BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
            timestamps.add(System.nanoTime());
            execContext.require(strIncrBack.createTask(backEndInput));
            timestamps.add(System.nanoTime());

            Relation.getOrInitialize(BuildStats.modulesDefiningStrategy, congrName, ArrayList::new).add(1);
        }
        if(!droppedCongruences.isEmpty()) {
            execContext.logger().info(
                "The following congruences were not generated as an external definition for that name was found in a library: "
                    + droppedCongruences);
        }
        // boilerplate task
        {
            backendStart = System.nanoTime();
            final List<IStrategoAppl> decls = StrategyStubs.declStubs(backendData.strategyASTs);
            // GK: commented out dead code
//            final @Nullable File strSrcGenDir =
//                execContext.getResourceService().toLocalFile(CommonPaths.strSepCompSrcGenDir(projectLocation));
//            assert strSrcGenDir
//                != null : "Bug in strSepCompSrcGenDir or the arguments thereof: returned path is not a directory";
            Backend.Input backEndInput =
                new Backend.Input(projectLocation, null, decls, Collections.emptyList(),
                    Collections.emptySortedMap(), input.javaPackageName, input.outputPath, input.cacheDir,
                    input.constants, input.includeDirs, args, true);
            BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
            timestamps.add(System.nanoTime());
            execContext.require(strIncrBack.createTask(backEndInput));
            timestamps.add(System.nanoTime());
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

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.inputFile;
    }
}
