package mb.stratego.build.termvisitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiFunction;

import jakarta.annotation.Nullable;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.SRTS_all;
import org.strategoxt.lang.Strategy;

/**
 * Resolve ambiguity NoAnnoList/As in Term/PreTerm for Stratego code like `x@[]`.
 * See also https://github.com/metaborg/jsglr/pull/44
 */
public class DisambiguateAsAnno {
    private static final ILogger logger = LoggerUtils.logger(DisambiguateAsAnno.class);
    private final Strategy visitor;
    private final Context context;

    private static class DisambiguationResult {
        private final boolean ambiguityFound;
        private @Nullable IStrategoTerm resolution;

        private DisambiguationResult(boolean ambiguityFound, @Nullable IStrategoTerm resolution) {
            this.ambiguityFound = ambiguityFound;
            this.resolution = resolution;
        }

        @Nullable IStrategoTerm resolution() {
            return resolution;
        }
    }

    public DisambiguateAsAnno(Context context) {
        this.context = context;
        visitor = new Strategy() {
            @Override
            public @Nullable IStrategoTerm invoke(Context context, IStrategoTerm current) {
                final IStrategoTerm visited = visit(current);
                final DisambiguationResult ambiguityResolved = resolveAmbiguity(visited);
                if(ambiguityResolved.ambiguityFound) {
                    return ambiguityResolved.resolution();
                }
                return visited;
            }
        };
    }

    public IStrategoTerm visit(IStrategoTerm term) {
        final IStrategoTerm result = SRTS_all.instance.invoke(context, term, visitor);
        // Flatten lists, workaround for JSGLR2 + Stratego parse table from the old sdf2table (in C)
        // See also: https://github.com/metaborg/jsglr/pull/44#issuecomment-589648434
        if(TermUtils.isList(result)) {
            final ArrayList<IStrategoTerm> flatList = new ArrayList<>();
            boolean nestedListFound = false;
            for(IStrategoTerm child : result) {
                if(TermUtils.isList(child)) {
                    nestedListFound = true;
                    Collections.addAll(flatList, child.getAllSubterms());
                } else {
                    flatList.add(child);
                }
            }
            if(nestedListFound) {
                // TermFactory#replaceList apparently requires the lists to be equal length, or it will throw an exception
                final ITermFactory factory = context.getFactory();
                final IStrategoList newList =
                    factory.makeList(flatList.toArray(AbstractTermFactory.EMPTY_TERM_ARRAY), result.getAnnotations());
                return factory.replaceTerm(newList, result);
            }
        }
        return result;
    }

    public DisambiguationResult resolveAmbiguity(IStrategoTerm current) {
        if(TermUtils.isAppl(current) && ((IStrategoAppl) current).getName().equals("amb") && TermUtils
            .isList(current.getSubterm(0))) {
            final IStrategoList ambs = TermUtils.toListAt(current, 0);
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

    public @Nullable IStrategoTerm resolveAmbiguity(IStrategoTerm left, IStrategoTerm right) {
        if(left.getClass() != right.getClass()) {
            return null;
        }
        if(left == right || left.equals(right)) {
            return left;
        }
        if(TermUtils.isList(left)) {
            return resolveChildAmbiguity((IStrategoList) left, (IStrategoList) right,
                context.getFactory()::replaceList);
        }
        if(TermUtils.isTuple(left)) {
            return resolveChildAmbiguity((IStrategoTuple) left, (IStrategoTuple) right,
                context.getFactory()::replaceTuple);
        }
        if(TermUtils.isAppl(left)) {
            final IStrategoAppl leftA = (IStrategoAppl) left;
            final IStrategoAppl rightA = (IStrategoAppl) right;

            if(leftA.getConstructor().equals(rightA.getConstructor())) {
                return resolveChildAmbiguity(leftA, rightA, this::replaceAppl);
            }
            switch(leftA.getName()) {
                case "As":
                case "App":
                    switch(rightA.getName()) {
                        case "AnnoList":
                        case "NoAnnoList":
                        case "Explode":
                            return leftA;
                    }
                    break;
                case "AnnoList":
                case "NoAnnoList":
                case "Explode":
                    switch(rightA.getName()) {
                        case "As":
                        case "App":
                            return rightA;
                    }
                    break;
            }
        }
        return null;
    }

    private IStrategoAppl replaceAppl(IStrategoTerm[] nc, IStrategoAppl l) {
        return context.getFactory().replaceAppl(l.getConstructor(), nc, l);
    }

    public <T extends IStrategoTerm> T resolveChildAmbiguity(T left, T right,
        BiFunction<IStrategoTerm[], T, T> replaceT) {
        final IStrategoTerm[] newChildren = new IStrategoTerm[left.getSubtermCount()];
        for(int i = 0; i < left.getSubtermCount(); i++) {
            newChildren[i] = resolveAmbiguity(left.getSubterm(i), right.getSubterm(i));
            if(newChildren[i] == null) {
                //noinspection ConstantConditions
                return null;
            }
        }
        return replaceT.apply(newChildren, left);
    }
}
