package strategolib.strategies;

import org.metaborg.core.context.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;
import strategolib.terms.StrategoImmutableSet;
import strategolib.terms.StrategyEqualityComparator;

public class internal_immutable_set_insert_eq_1_1 extends Strategy {
    public static internal_immutable_set_insert_eq_1_1 instance = new internal_immutable_set_insert_eq_1_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare,
        IStrategoTerm value) {
        return insert(context, current, value, new StrategyEqualityComparator(context, compare));
    }

    protected IStrategoTerm insert(Context context, IStrategoTerm current, IStrategoTerm value,
        EqualityComparator<Object> cmp) {
        final StrategoImmutableSet set = (StrategoImmutableSet) current;

        return new StrategoImmutableSet(set.backingSet.__insertEquivalent(value, cmp));
    }
}
