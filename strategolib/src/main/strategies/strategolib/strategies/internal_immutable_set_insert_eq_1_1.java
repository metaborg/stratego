package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import strategolib.terms.CompiledStrategyEqualityComparator;

public class internal_immutable_set_insert_eq_1_1 extends Strategy {
    public static final internal_immutable_set_insert_eq_1_1 instance = new internal_immutable_set_insert_eq_1_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm value) {
        return callStatic(context, current, compare, value);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm value) {
        return insert(context, current, value, new CompiledStrategyEqualityComparator(context, compare));
    }

    protected static IStrategoTerm insert(Context context, IStrategoTerm current, IStrategoTerm value,
        EqualityComparator<Object> cmp) {
        final StrategoImmutableSet set = (StrategoImmutableSet) current;

        return new StrategoImmutableSet(set.backingSet.__insertEquivalent(value, cmp));
    }
}
