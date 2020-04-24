package mb.stratego.build.termvisitors;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.HybridInterpreter;

import mb.stratego.build.strincr.Message;
import mb.stratego.build.util.StringSetWithPositions;

/**
 * Static analysis which requires the non-desugared AST
 */
public class SugarAnalysis {
    private final String module;
    private final List<Message<?>> sugarAnalysisMessages;
    private final Set<String> definedConstructors;

    public SugarAnalysis(String module, List<Message<?>> sugarAnalysisMessages, Map<String, StringSetWithPositions> allDefinedConstructors) {
        this.module = module;
        this.sugarAnalysisMessages = sugarAnalysisMessages;
        this.definedConstructors = new HashSet<>();
        for(StringSetWithPositions sswp : allDefinedConstructors.values()) {
            for(String constr : sswp.readSet()) {
                this.definedConstructors.add(constr);
            }
        }
    }

    /**
     * Visit topdown as long as it isn't a congruence.
     * The recursive calls inside the visitCongruence method are tracked and will call back into this visit method for non-congruence terms
     * @param term
     */
    public void visit(IStrategoTerm term) {
        boolean isCongruence = visitCongruence(term);
        if(!isCongruence) {
            visitVar(term);
            for(IStrategoTerm child : term) {
                visit(child);
            }
        }
    }

    /**
     * Test whether a local variable overlaps with a nullary constructor
     */
    private void visitVar(IStrategoTerm term) {
        if(TermUtils.isAppl(term) && term.getSubtermCount() == 1) {
            IStrategoAppl appl = (IStrategoAppl) term;
            if(appl.getName().equals("Var") && TermUtils.isString(appl.getSubterm(0))) {
                final IStrategoString varName = TermUtils.toStringAt(appl, 0);
                if(isConstructor(varName.stringValue(), 0)) {
                    sugarAnalysisMessages.add(Message.varConstrOverlap(module, varName));
                }
            }
        }
    }

    /**
     * @param term
     * @return true if term is a congruence
     */
    private boolean visitCongruence(IStrategoTerm term) {
        return visitCongruence(term, false);
    }

    /**
     * When this is a top-level call (returnConst == false) then if a constant congruence is found, it's registered
     * When this is not a top-level call (returnConst == true), and the term isn't a congruence, go back to the visit method to make sure the entire AST is visited
     * @param term
     * @param returnConst tracks if this is a recursive call and we already know we're inside a congruence
     * @return if returnConst is true, only return true when constCongruence, otherwise return true if term is a congruence
     */
    private boolean visitCongruence(IStrategoTerm term, boolean returnConst) {
        boolean isCongruence = false;
        boolean constCongruence = false;
        if(TermUtils.isAppl(term)) {
            IStrategoAppl appl = (IStrategoAppl) term;
            switch(appl.getName()) {
                case "StrCong":
                case "IntCong":
                case "RealCong":
                case "CharCong":
                    if(appl.getSubtermCount() == 1) {
                        constCongruence = true;
                        isCongruence = true;
                    }
                    break;
                case "CongQ":
                    if(appl.getSubtermCount() == 2 && TermUtils.isList(appl.getSubterm(1))) {
                        IStrategoList children = TermUtils.toListAt(appl, 1);
                        if(constantCongruence(children)) {
                            constCongruence = true;
                        }
                        isCongruence = true;
                    }
                    break;
                case "AnnoCong":
                    if(appl.getSubtermCount() == 2 && TermUtils.isAppl(appl.getSubterm(1))) {
                        IStrategoAppl secondChild = TermUtils.toApplAt(term, 1);
                        if(secondChild.getName().equals("StrategyCurly") && secondChild.getSubtermCount() == 1) {
                            if(constantCongruence(Arrays.asList(appl.getSubterm(0), secondChild.getSubterm(0)))) {
                                constCongruence = true;
                            }
                            isCongruence = true;
                        }
                    }
                    break;
                case "EmptyTupleCong":
                    if(appl.getSubtermCount() == 0) {
                        constCongruence = true;
                        isCongruence = true;
                    }
                    break;
                case "TupleCong":
                case "ListCongNoTail":
                    if(appl.getSubtermCount() == 1 && TermUtils.isList(appl.getSubterm(0))) {
                        IStrategoList children = TermUtils.toListAt(appl, 0);
                        if(constantCongruence(children)) {
                            constCongruence = true;
                        }
                        isCongruence = true;
                    }
                    break;
                case "ListCong":
                    if(appl.getSubtermCount() == 2 && TermUtils.isList(appl.getSubterm(0))) {
                        IStrategoList children = TermUtils.toListAt(appl, 0);
                        if(constantCongruence(children) && constantCongruence(appl.getSubterm(1))) {
                            constCongruence = true;
                        }
                        isCongruence = true;
                    }
                    break;
                case "ExplodeCong":
                    if(appl.getSubtermCount() == 2 && TermUtils.isAppl(appl.getSubterm(1))) {
                        IStrategoAppl secondChild = TermUtils.toApplAt(term, 1);
                        if(secondChild.getName().equals("ParenStrat") && secondChild.getSubtermCount() == 1) {
                            if(constantCongruence(Arrays.asList(appl.getSubterm(0), secondChild.getSubterm(0)))) {
                                constCongruence = true;
                            }
                            isCongruence = true;
                        }
                    }
                    break;
                case "CallT":
                    if(appl.getSubtermCount() == 3 && TermUtils.isAppl(appl.getSubterm(0))
                        && TermUtils.isList(appl.getSubterm(1)) && TermUtils.isList(appl.getSubterm(2))) {
                        IStrategoAppl firstChild = TermUtils.toApplAt(term, 0);
                        IStrategoList secondChild = TermUtils.toListAt(term, 1);
                        IStrategoList thirdChild = TermUtils.toListAt(term, 1);
                        visit(thirdChild);
                        if(firstChild.getName().equals("SVar") && firstChild.getSubtermCount() == 1
                            && TermUtils.isString(firstChild.getSubterm(0))
                            && isConstructor(TermUtils.toJavaStringAt(firstChild, 0), secondChild.size())) {
                            if(constantCongruence(secondChild)) {
                                constCongruence = true;
                            }
                            isCongruence = true;
                        }
                    }
                    break;
            }
        }
        if(returnConst) {
            if(!isCongruence) {
                visit(term);
            }
            return constCongruence;
        } else {
            if(constCongruence) {
                registerConstantCongruence(term);
            }
            return isCongruence;
        }
    }

    /**
     * @param cong
     * @return true if congruence is constant
     */
    private boolean constantCongruence(IStrategoTerm cong) {
        return visitCongruence(cong, true);
    }

    /**
     * If not all children are congruences the ones that are are registered as such.
     * @param children
     * @return true if all children are constant congruences
     */
    private boolean constantCongruence(Collection<IStrategoTerm> children) {
        boolean[] isConstantCong = new boolean[children.size()];
        boolean constantChildren = true;
        {
            int i = 0;
            for(IStrategoTerm child : children) {
                isConstantCong[i] = constantCongruence(child);
                constantChildren &= isConstantCong[i];
                i++;
            }
        }
        if(!constantChildren) {
            {
                int i = 0;
                for(IStrategoTerm child : children) {
                    if(isConstantCong[i]) {
                        registerConstantCongruence(child);
                    }
                    i++;
                }
            }
        }
        return constantChildren;
    }

    private void registerConstantCongruence(final IStrategoTerm child) {
        sugarAnalysisMessages.add(Message.constantCongruence(module, child));
    }

    private boolean isConstructor(String name, int arity) {
        return definedConstructors.contains(HybridInterpreter.cify(name) + "_" + arity);
    }
}
