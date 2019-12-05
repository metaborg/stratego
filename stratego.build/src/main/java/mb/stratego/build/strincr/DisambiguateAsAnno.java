package mb.stratego.build.strincr;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgRuntimeException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.SRTS_all;
import org.strategoxt.lang.Strategy;

/**
 * Resolve ambiguity NoAnnoList/As in Term/PreTerm for Stratego code like `x@[]`.
 * See also https://github.com/metaborg/jsglr/pull/44
 */
public class DisambiguateAsAnno {
    private final Strategy visitor;
    private final Context context;

    public DisambiguateAsAnno(Context context) {
        this.context = context;
        visitor = new Strategy() {
            @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
                final IStrategoTerm ambiguityResolved = resolveAmbiguity(current);
                if(ambiguityResolved == null) {
                    return visit2(current);
                } else {
                    return ambiguityResolved;
                }
            }
        };
    }

    public IStrategoTerm visit(IStrategoTerm term) {
        return visit2(term);
    }

    public IStrategoTerm visit2(IStrategoTerm term) {
        SRTS_all.instance.invoke(context, term, visitor);
        return term;
    }

    public @Nullable IStrategoTerm resolveAmbiguity(IStrategoTerm current) {
        if(Tools.isTermAppl(current) && ((IStrategoAppl) current).getName().equals("amb") && Tools.isTermList(current.getSubterm(0))) {
            IStrategoList ambs = Tools.listAt(current, 0);
            if(ambs.size() == 2) {
                IStrategoTerm left = ambs.getSubterm(0);
                IStrategoTerm right = ambs.getSubterm(1);
                return resolveAmbiguity(left, right);
            }
            throw new MetaborgRuntimeException("Ambiguity found: " + current.toString(7));
        }
        return null;
    }

    public IStrategoTerm resolveAmbiguity(IStrategoTerm left, IStrategoTerm right) {
        if(left == null || right == null) {
            return null;
        }
        if(left.getClass() != right.getClass()) {
            return null;
        }
        if(left == right || left.equals(right)) {
            return left;
        }
        if(Tools.isTermAppl(left)) {
            IStrategoAppl leftA = (IStrategoAppl) left;
            IStrategoAppl rightA = (IStrategoAppl) right;

            if(leftA.getConstructor().equals(rightA.getConstructor())) {
                final IStrategoTerm[] newChildren = new IStrategoTerm[leftA.getSubtermCount()];
                for(int i = 0; i < leftA.getSubtermCount(); i++) {
                    newChildren[i] = resolveAmbiguity(leftA.getSubterm(i), rightA.getSubterm(i));
                    if(newChildren[i] == null) {
                        return null;
                    }
                }
                return context.getFactory().replaceAppl(leftA.getConstructor(), newChildren, leftA);
            } else if(leftA.getName().equals("As") && rightA.getName().equals("NoAnnoList")) {
                return leftA;
            } else if(rightA.getName().equals("As") && leftA.getName().equals("NoAnnoList")) {
                return rightA;
            }
        }
        return null;
    }
}
