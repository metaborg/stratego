package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrategoGradualSetting;

public class StrIncr implements TaskDef<StrIncr.Input, None> {
    public static final String id = StrIncr.class.getCanonicalName();

    public static final class Input implements Serializable {
        final @Nullable String javaPackageName;
        final @Nullable ResourcePath cacheDir;
        final List<String> constants;
        final Arguments extraArgs;
        final ResourcePath outputPath;
        final StrIncrAnalysis.Input frontendsInput;

        public Input(ResourcePath inputFile, @Nullable String javaPackageName, Collection<ResourcePath> includeDirs,
            Collection<String> builtinLibs, @Nullable ResourcePath cacheDir, List<String> constants, Arguments extraArgs,
            ResourcePath outputPath, Collection<STask<?>> originTasks, ResourcePath projectLocation, StrategoGradualSetting strGradualSetting) {
            this.javaPackageName = javaPackageName;
            this.cacheDir = cacheDir;
            this.constants = constants;
            this.extraArgs = extraArgs;
            this.outputPath = outputPath;
            this.frontendsInput = new StrIncrAnalysis.Input(inputFile, includeDirs, builtinLibs, originTasks, projectLocation, strGradualSetting);
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;
            Input input = (Input) o;
            return Objects.equals(javaPackageName, input.javaPackageName) && Objects.equals(cacheDir, input.cacheDir)
                && constants.equals(input.constants) && extraArgs.equals(input.extraArgs) && outputPath
                .equals(input.outputPath) && frontendsInput.equals(input.frontendsInput);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), javaPackageName, cacheDir, constants, extraArgs, outputPath, frontendsInput);
        }

        @Override public String toString() {
            return "StrIncr$Input(" +
                "javaPackageName='" + javaPackageName + '\'' +
                ", cacheDir=" + cacheDir +
                ", constants=" + constants +
                ", extraArgs=" + extraArgs +
                ", outputPath=" + outputPath +
                ", frontendsInput=" + frontendsInput +
                ')';
        }
    }

    private final Backend strIncrBack;
    private final StrIncrAnalysis strIncrAnalysis;

    @Inject public StrIncr(Backend strIncrBack, StrIncrAnalysis analysis) {
        this.strIncrBack = strIncrBack;
        this.strIncrAnalysis = analysis;
    }

    @Override public None exec(ExecContext execContext, Input input) throws Exception {
        final StrIncrAnalysis.Output result = execContext.require(strIncrAnalysis, input.frontendsInput);

        if(!result.messages.isEmpty()) {
            boolean error = false;
            for(Message<?> message : result.messages) {
                switch(message.severity) {
                    case NOTE:
                        execContext.logger().info(message.toString());
                        break;
                    case WARNING:
                        execContext.logger().warn(message.toString());
                        break;
                    case ERROR:
                        execContext.logger().error(message.toString());
                        error = true;
                        break;
                }
            }
            if(error) {
                throw new ExecException("One of the static checks failed. See above for error messages in the log. ");
            }
        }

        // BACKEND
        backends(execContext, input, input.frontendsInput.projectLocation, result.staticData, result.backendData);
        return None.instance;
    }

    private void backends(ExecContext execContext, Input input, ResourcePath projectLocation,
        StaticChecks.Data staticData, BackendData backendData) {
        long backendStart = System.nanoTime();
        final Arguments args = new Arguments();
        args.addAll(input.extraArgs);
        for(String builtinLib : input.frontendsInput.builtinLibs) {
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

            Backend.Input backEndInput =
                new Backend.Input(projectLocation, strategyName, Collections.emptyList(), strategyContributions,
                    strategyOverlayFiles, input.javaPackageName, input.outputPath,
                    input.cacheDir, input.constants, input.frontendsInput.includeDirs, args, false);
            BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
            execContext.require(strIncrBack.createTask(backEndInput));
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

            Backend.Input backEndInput =
                new Backend.Input(projectLocation, congrName, Collections.emptyList(), strategyContributions,
                    strategyOverlayFiles, input.javaPackageName, input.outputPath,
                    input.cacheDir, input.constants, input.frontendsInput.includeDirs, args, false);
            BuildStats.shuffleBackendTime += System.nanoTime() - backendStart;
            execContext.require(strIncrBack.createTask(backEndInput));

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
            Backend.Input backEndInput =
                new Backend.Input(projectLocation, null, backendData.consDefs, decls, Collections.emptyList(),
                    input.javaPackageName, input.outputPath, input.cacheDir,
                    input.constants, input.frontendsInput.includeDirs, args, true);
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

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.frontendsInput.inputFile;
    }
}
