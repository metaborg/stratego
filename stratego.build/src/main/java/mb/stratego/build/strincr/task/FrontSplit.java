package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.GetASTWithLastModified;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.PieUtils;

public class FrontSplit implements TaskDef<CheckModuleInput, CheckModuleOutput> {
    public static final String id = "stratego." + FrontSplit.class.getSimpleName();

    public final Front front;

    @Inject public FrontSplit(Front front) {
        this.front = front;
    }

    @Override public String getId() {
        return id;
    }

    @Override public CheckModuleOutput exec(ExecContext context, CheckModuleInput input)
        throws Exception {
        final LastModified<IStrategoTerm> astWLM =
            PieUtils.requirePartial(context, front, input.frontInput, GetASTWithLastModified.INSTANCE);

        LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>>
            strategyDataWithCasts = CheckModule
            .extractStrategyDefs(input.frontInput.moduleIdentifier, astWLM.lastModified,
                astWLM.wrapped, dynamicRules);

        return new CheckModuleOutput(strategyDataWithCasts, dynamicRules, new ArrayList<>(0));
    }
}
