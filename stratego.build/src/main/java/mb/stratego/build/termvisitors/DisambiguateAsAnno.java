package mb.stratego.build.termvisitors;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.SRTS_all;
import org.strategoxt.lang.Strategy;
import javax.annotation.Nullable;

/**
 * Resolve ambiguity NoAnnoList/As in Term/PreTerm for Stratego code like `x@[]`.
 * See also https://github.com/metaborg/jsglr/pull/44
 */
public class DisambiguateAsAnno {
    private static final ILogger logger = LoggerUtils.logger(DisambiguateAsAnno.class);
    private final Strategy visitor;
    private final Context context;

    private static class DisambiguationResult {
        private boolean ambiguityFound;
        private @Nullable IStrategoTerm resolution;

        private DisambiguationResult(boolean ambiguityFound, @Nullable IStrategoTerm resolution) {
            this.ambiguityFound = ambiguityFound;
            this.resolution = resolution;
        }

        boolean ambiguityFound() {
            return ambiguityFound;
        }
        IStrategoTerm resolution() {
            return resolution;
        }
    }

    public DisambiguateAsAnno(Context context) {
        this.context = context;
        visitor = new Strategy() {
            @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
                final DisambiguationResult ambiguityResolved = resolveAmbiguity(current);
                if(ambiguityResolved.ambiguityFound()) {
                    return ambiguityResolved.resolution();
                } else {
                    return visit(current);
                }
            }
        };
    }

    public IStrategoTerm visit(IStrategoTerm term) {
        return SRTS_all.instance.invoke(context, term, visitor);
    }

    public DisambiguationResult resolveAmbiguity(IStrategoTerm current) {
        if(Tools.isTermAppl(current) && ((IStrategoAppl) current).getName().equals("amb") && Tools.isTermList(current.getSubterm(0))) {
            final IStrategoList ambs = Tools.listAt(current, 0);
            assert ambs != null;
            final DisambiguationResult ambiguityResolved = new DisambiguationResult(true, null);
            if(ambs.size() == 2) {
                final IStrategoTerm left = ambs.getSubterm(0);
                final IStrategoTerm right = ambs.getSubterm(1);
                ambiguityResolved.resolution = resolveAmbiguity(left, right);
            }
            if(ambs.size() != 2 || ambiguityResolved.resolution() == null) {
                logger.error("Ambiguity found: " + current.toString());
//                throw new MetaborgRuntimeException("Ambiguity found: " + current.toString(7));
                ambiguityResolved.resolution = ambs.getSubterm(0);
            }
            return ambiguityResolved;
        }
        return new DisambiguationResult(false, null);
    }

    public IStrategoTerm resolveAmbiguity(IStrategoTerm left, IStrategoTerm right) {
        if(left == null || right == null) {
            return null;
        }
        if(left.getClass() != right.getClass()) {
            return null;
        }
        if(left.getSubtermCount() != right.getSubtermCount()) {
            return null;
        }
        if(left == right || left.equals(right)) {
            return left;
        }
        final IStrategoTerm[] newChildren = resolveChildAmbiguity(left, right);
        if(newChildren == null) {
            return null;
        }
        if(Tools.isTermAppl(left)) {
            final IStrategoAppl leftA = (IStrategoAppl) left;
            final IStrategoAppl rightA = (IStrategoAppl) right;

            if(leftA.getConstructor().equals(rightA.getConstructor())) {
                return context.getFactory().replaceAppl(leftA.getConstructor(), newChildren, leftA);
            } else if(leftA.getName().equals("As") && rightA.getName().equals("NoAnnoList")) {
                return leftA;
            } else if(rightA.getName().equals("As") && leftA.getName().equals("NoAnnoList")) {
                return rightA;
            }
        }
        if(Tools.isTermList(left)) {
            return context.getFactory().replaceList(newChildren, (IStrategoList) left);
        }
        if(Tools.isTermTuple(left)) {
            return context.getFactory().replaceTuple(newChildren, (IStrategoTuple) left);
        }
        return null;
    }

    public <T extends IStrategoTerm> IStrategoTerm[] resolveChildAmbiguity(T leftL, T rightL) {
        final IStrategoTerm[] newChildren = new IStrategoTerm[leftL.getSubtermCount()];
        for(int i = 0; i < leftL.getSubtermCount(); i++) {
            newChildren[i] = resolveAmbiguity(leftL.getSubterm(i), rightL.getSubterm(i));
            if(newChildren[i] == null) {
                return null;
            }
        }
        return newChildren;
    }
}
