package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;


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

/**
 * This is an in-place replacement task for {@link CheckModule}, in case the gradual type system is
 * turned off. This task is used as a replacement in the {@link Back} task so that the asts with
 * inserted casts are not used.
 * This does *not* mean {@link CheckModule} is not run. The static checks in {@link CheckModule} are
 * not all type system matters, so the checks are run and the messages about types are filtered out.
 */
public class FrontSplit implements TaskDef<CheckModuleInput, CheckModuleOutput> {
    public static final String id = "stratego." + FrontSplit.class.getSimpleName();

    public final Front front;

    @jakarta.inject.Inject public FrontSplit(Front front) {
        this.front = front;
    }

    @Override public String getId() {
        return id;
    }

    @Override public CheckModuleOutput exec(ExecContext context, CheckModuleInput input)
        throws Exception {
        if(input.frontInput.moduleIdentifier.isLibrary()) {
            return new CheckModuleOutput(new LinkedHashMap<>(0), new LinkedHashMap<>(0),
                new LinkedHashSet<>(0), new ArrayList<>(0));
        }

        final LastModified<IStrategoTerm> astWLM =
            PieUtils.requirePartial(context, front, input.frontInput,
                GetASTWithLastModified.INSTANCE);

        LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>>
            strategyDataWithCasts = CheckModule
            .extractStrategyDefs(input.frontInput.moduleIdentifier, astWLM.wrapped, dynamicRules);

        return new CheckModuleOutput(strategyDataWithCasts, dynamicRules, new LinkedHashSet<>(0),
            new ArrayList<>(0));
    }
}
