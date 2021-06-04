package mb.stratego.build.termvisitors;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

public class HasDynamicRuleDefinitions {
    public static boolean visit(IStrategoTerm term) {
        if(TermUtils.isAppl(term)) {
            if("GenDynRules".equals(TermUtils.toAppl(term).getName())
                && term.getSubtermCount() == 1) {
                return true;
            }
        }
        for(IStrategoTerm child : term) {
            if(visit(child)) {
                return true;
            }
        }
        return false;
    }

//    public IStrategoList filterRules(IStrategoTerm term) {
////          AddScopeLabel/2
////          UndefineDynRule/2
////          SetDynRule/2
////          AddDynRule/2
////          SetDynRuleMatch/2
////          DynRuleAssign/2
////          DynRuleAssignAdd/2
////          SetDynRuleDepends/3
//        if(TermUtils.isAppl(term)) {
//            switch(TermUtils.toAppl(term).getName()) {
//                case "AddScopeLabel":
//                    // fall-through
//                case "UndefineDynRule":
//                    // fall-through
//                case "SetDynRule":
//                    // fall-through
//                case "AddDynRule":
//                    // fall-through
//                case "SetDynRuleMatch":
//                    // fall-through
//                case "DynRuleAssign":
//                    // fall-through
//                case "DynRuleAssignAdd":
//                    if(term.getSubtermCount() == 2 && filter(term.getSubterm(0).getSubterm(0))) {
//                        result.add(term);
//                    }
//                    break;
//                case "SetDynRuleDepends":
//                    if(term.getSubtermCount() == 3 && filter(term.getSubterm(0).getSubterm(0))) {
//                        result.add(term);
//                    }
//                    break;
//            }
//        }
//    }
//
//    private boolean filter(IStrategoTerm term) {
////        dyn-rule-sig: RDecNoArgs(name) -> (name, 0, 0)
////        dyn-rule-sig: RDec(name, sarg*) -> (name, <length> sarg*, 0)
////        dyn-rule-sig: RDecT(name, sarg*, targ*) -> (name, <length> sarg*, <length> targ*)
//        if(TermUtils.isAppl(term)) {
//            switch(TermUtils.toAppl(term).getName()) {
//                case "RDecNoArgs":
//                    if(term.getSubtermCount() == 1 && TermUtils.isStringAt(term, 0)) {
//                        return dynamicRule
//                            .equals(new StrategySignature(TermUtils.toStringAt(term, 0), 0, 0));
//                    }
//                    break;
//                case "RDec":
//                    if(term.getSubtermCount() == 2 && TermUtils.isStringAt(term, 0) && TermUtils
//                        .isListAt(term, 1)) {
//                        return dynamicRule.equals(
//                            new StrategySignature(TermUtils.toStringAt(term, 0),
//                                TermUtils.toListAt(term, 1).size(), 0));
//                    }
//                    break;
//                case "RDecT":
//                    if(term.getSubtermCount() == 3 && TermUtils.isStringAt(term, 0) && TermUtils
//                        .isListAt(term, 1) && TermUtils.isListAt(term, 2)) {
//                        return dynamicRule.equals(
//                            new StrategySignature(TermUtils.toStringAt(term, 0),
//                                TermUtils.toListAt(term, 1).size(),
//                                TermUtils.toListAt(term, 2).size()));
//                    }
//                    break;
//            }
//        }
//        return false;
//    }
}
