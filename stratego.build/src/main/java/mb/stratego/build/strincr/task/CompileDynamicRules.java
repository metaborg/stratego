package mb.stratego.build.strincr.task;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;


import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.ToCompileGlobalIndex;
import mb.stratego.build.strincr.function.output.CompileGlobalIndex;
import mb.stratego.build.strincr.task.input.BackInput;
import mb.stratego.build.strincr.task.input.CompileDynamicRulesInput;
import mb.stratego.build.strincr.task.output.BackOutput;
import mb.stratego.build.strincr.task.output.CompileDynamicRulesOutput;
import mb.stratego.build.util.PieUtils;

public class CompileDynamicRules implements TaskDef<CompileDynamicRulesInput, CompileDynamicRulesOutput>  {
    public static final String id = "stratego." + CompileDynamicRules.class.getSimpleName();

    public final Resolve resolve;
    public final Back back;

    @jakarta.inject.Inject public CompileDynamicRules(Resolve resolve, Back back) {
        this.resolve = resolve;
        this.back = back;
    }

    @Override
    public CompileDynamicRulesOutput exec(ExecContext context, CompileDynamicRulesInput input)
        throws Exception {
        LinkedHashSet<ResourcePath> resultFiles = new LinkedHashSet<>();

        final CompileGlobalIndex compileGlobalIndex = PieUtils
            .requirePartial(context, resolve, input.checkInput.resolveInput(),
                ToCompileGlobalIndex.INSTANCE);
        final TreeSet<StrategySignature> compiledThroughDynamicRule = new TreeSet<>();
        for(StrategySignature dynamicRule : compileGlobalIndex.dynamicRules.keySet()) {
            final BackInput.DynamicRule dynamicRuleInput =
                new BackInput.DynamicRule(input.outputDirWithPackage, input.packageNames, input.cacheDir,
                    input.constants, input.extraArgs, input.checkInput, dynamicRule,
                    input.strategyAnalysisDataTask, input.usingLegacyStrategoStdLib);
            final BackOutput output = context.require(back, dynamicRuleInput);
            assert output != null;
            assert !output.depTasksHaveErrorMessages : "Previous code should have already returned on checkOutput.containsErrors";
            compiledThroughDynamicRule.addAll(output.compiledStrategies);
            resultFiles.addAll(output.resultFiles);
        }

        return new CompileDynamicRulesOutput(compiledThroughDynamicRule, compileGlobalIndex.dynamicRules.keySet(),
            resultFiles);
    }

    @Override public boolean shouldExecWhenAffected(CompileDynamicRulesInput input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public String getId() {
        return id;
    }
}
