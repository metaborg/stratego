package mb.stratego.build.termvisitors;

import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.data.StrategySignature;

public class CollectDynRuleSigs {
    private final LinkedHashSet<StrategySignature> signatures = new LinkedHashSet<>();

    public static LinkedHashSet<StrategySignature> collect(IStrategoTerm ast) {
        CollectDynRuleSigs instance = new CollectDynRuleSigs();
        instance.visit(ast);
        return instance.signatures;
    }

    private void visit(IStrategoTerm term) {
        /*
        dyn-rule-sig: RDecNoArgs(name) -> (name, 0, 0)
        dyn-rule-sig: RDec(name, sarg*) -> (name, <length> sarg*, 0)
        dyn-rule-sig: RDecT(name, sarg*, targ*) -> (name, <length> sarg*, <length> targ*)
         */
        if(TermUtils.isAppl(term)) {
            switch(TermUtils.toAppl(term).getName()) {
                case "RDecNoArgs":
                    if(term.getSubtermCount() == 1 && TermUtils.isStringAt(term, 0)) {
                        signatures.add(new StrategySignature(TermUtils.toStringAt(term, 0), 0, 0));
                        return;
                    }
                    break;
                case "RDec":
                    if(term.getSubtermCount() == 2 && TermUtils.isStringAt(term, 0) && TermUtils
                        .isListAt(term, 1)) {
                        signatures.add(new StrategySignature(TermUtils.toStringAt(term, 0),
                            TermUtils.toListAt(term, 1).size(), 0));
                        return;
                    }
                    break;
                case "RDecT":
                    if(term.getSubtermCount() == 3 && TermUtils.isStringAt(term, 0) && TermUtils
                        .isListAt(term, 1) && TermUtils.isListAt(term, 2)) {
                        signatures.add(new StrategySignature(TermUtils.toStringAt(term, 0),
                            TermUtils.toListAt(term, 1).size(),
                            TermUtils.toListAt(term, 2).size()));
                        return;
                    }
                    break;
            }
        }
        /*
          DynRuleDef.AddScopeLabel =
            <<Id> + <Term>>
          DynRuleDef.UndefineDynRule =
            <<DynRuleId> :- <Term>> // doesn't generate aux- rule
          DynRuleDef.SetDynRule =
            <<DynRuleId> : <Rule>>
          DynRuleDef.AddDynRule =
            <<DynRuleId> :+ <Rule>>
          DynRuleDef.SetDynRuleMatch =
            <<DynRuleId> : <Term>> // Wld() become Var(<newname> "wld")
          DynRuleDef.DynRuleAssign =
            <<DynRuleId> := <Term>> // becomes SetDynRule with rule _ -> y, no vars in lhs
          DynRuleDef.DynRuleAssignAdd =
            <<DynRuleId> :+= <Term>> // becomes AddDynRule with rule _ -> y, no vars in lhs
          DynRuleDef.SetDynRuleDepends =
            <<DynRuleId> : <Rule> depends on <Term>>
          DynRuleId.LabeledDynRuleId =
            <<RuleDec> . <Term>>
          DynRuleId.AddLabelDynRuleId =
            <<RuleDec> + <Term>>
          DynRuleId.DynRuleId = RuleDec
        */
        // TODO: create aux- rule signature for every lhs, based on its free variables.
        for(IStrategoTerm child : term) {
            visit(child);
        }
    }
}
