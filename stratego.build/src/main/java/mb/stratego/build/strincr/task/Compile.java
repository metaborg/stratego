package mb.stratego.build.strincr.task;

import java.util.HashSet;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.GetMessages;
import mb.stratego.build.strincr.function.ToGlobalIndex;
import mb.stratego.build.strincr.function.output.CheckOutputMessages;
import mb.stratego.build.strincr.function.output.GlobalIndex;
import mb.stratego.build.strincr.task.input.BackInput;
import mb.stratego.build.strincr.task.input.CheckInput;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.BackOutput;
import mb.stratego.build.strincr.task.output.CheckOutput;
import mb.stratego.build.strincr.task.output.CompileOutput;
import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.StrategoGradualSetting;

/**
 * The one task to rule them all, this task runs {@link Check}, stops if there are errors, and
 * otherwise continues to run all the Back tasks. It returns a list of the Java files that were
 * written to, as well as non-error messages from Check.
 */
public class Compile implements TaskDef<CompileInput, CompileOutput> {
    public static final String id = "stratego." + Compile.class.getSimpleName();

    public final Resolve resolve;
    public final Check check;
    public final Back back;

    @Inject public Compile(Resolve resolve, Check check, Back back) {
        this.resolve = resolve;
        this.check = check;
        this.back = back;
    }

    @Override public CompileOutput exec(ExecContext context, CompileInput input) {
        final CheckInput checkInput = new CheckInput(input.mainModuleIdentifier,
            input.strategoGradualSetting == StrategoGradualSetting.NONE,
            input.strFileGeneratingTasks, input.includeDirs, input.linkedLibraries);
        final STask<CheckOutput> checkTask = check.createSupplier(checkInput);
        final CheckOutputMessages checkOutput =
            PieUtils.requirePartial(context, check, checkInput, GetMessages.INSTANCE);
        if(checkOutput.containsErrors) {
            return new CompileOutput.Failure(checkOutput.messages);
        }

        final HashSet<ResourcePath> resultFiles = new HashSet<>();
        final STask<GlobalData> resolveTask = resolve.createSupplier(checkInput.resolveInput());
        final GlobalIndex globalIndex =
            PieUtils.requirePartial(context, resolveTask, ToGlobalIndex.INSTANCE);
        final HashSet<StrategySignature> compiledThroughDynamicRule = new HashSet<>();

        final HashSet<String> dynamicRuleNewGenerated = new HashSet<>();
        final HashSet<String> dynamicRuleUndefineGenerated = new HashSet<>();

        for(StrategySignature dynamicRule : globalIndex.dynamicRules) {
            if(compiledThroughDynamicRule.contains(dynamicRule)) {
                continue;
            }
            final BackInput.DynamicRule dynamicRuleInput =
                new BackInput.DynamicRule(dynamicRule, input.outputDir, input.packageName,
                    input.cacheDir, input.constants, input.includeDirs, input.extraArgs,
                    resolveTask, input.mainModuleIdentifier, input.strFileGeneratingTasks,
                    input.linkedLibraries, checkTask);
            final BackOutput output = context.require(back, dynamicRuleInput);
            assert output != null;
            resultFiles.addAll(output.resultFiles);
            compiledThroughDynamicRule.addAll(output.compiledStrategies);
        }
        for(StrategySignature dynamicRule : globalIndex.dynamicRules) {
            final StrategySignature dynamicRuleNew =
                new StrategySignature("new-" + dynamicRule.name, 0, 2);
            if(compiledThroughDynamicRule.contains(dynamicRuleNew)) {
                dynamicRuleNewGenerated.add(dynamicRule.name);
            }
            final StrategySignature dynamicRuleUndefine =
                new StrategySignature("undefine-" + dynamicRule.name, 0, 1);
            if(compiledThroughDynamicRule.contains(dynamicRuleUndefine)) {
                dynamicRuleUndefineGenerated.add(dynamicRule.name);
            }
        }
        for(StrategySignature strategySignature : globalIndex.nonExternalStrategies) {
            if(compiledThroughDynamicRule.contains(strategySignature)) {
                continue;
            }
            final BackInput.Normal normalInput =
                new BackInput.Normal(strategySignature, input.outputDir, input.packageName,
                    input.cacheDir, input.constants, input.includeDirs, input.extraArgs,
                    resolveTask, input.mainModuleIdentifier, input.strFileGeneratingTasks,
                    input.linkedLibraries);
            final BackOutput output = context.require(back, normalInput);
            assert output != null;
            resultFiles.addAll(output.resultFiles);
        }
        final boolean dynamicCallsDefined =
            !dynamicRuleNewGenerated.isEmpty() || !dynamicRuleUndefineGenerated.isEmpty();
        final BackInput.Boilerplate boilerplateInput =
            new BackInput.Boilerplate(resolveTask, input.outputDir, input.packageName,
                input.cacheDir, input.constants, input.includeDirs, input.extraArgs,
                dynamicCallsDefined, input.strFileGeneratingTasks, input.linkedLibraries);
        final BackOutput boilerplateOutput = context.require(back, boilerplateInput);
        assert boilerplateOutput != null;
        resultFiles.addAll(boilerplateOutput.resultFiles);

        final BackInput.Congruence congruenceInput =
            new BackInput.Congruence(resolveTask, input.outputDir, input.packageName,
                input.cacheDir, input.constants, input.includeDirs, input.extraArgs,
                dynamicRuleNewGenerated, dynamicRuleUndefineGenerated, input.linkedLibraries);
        final BackOutput congruenceOutput = context.require(back, congruenceInput);
        assert congruenceOutput != null;
        resultFiles.addAll(congruenceOutput.resultFiles);

        return new CompileOutput.Success(resultFiles);
    }

    @Override public String getId() {
        return id;
    }
}
