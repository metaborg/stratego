package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_set_filter_1_0 extends Strategy {
    public static internal_immutable_set_filter_1_0 instance = new internal_immutable_set_filter_1_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy filter) {
        final Set.Immutable<IStrategoTerm> set = ((StrategoImmutableSet) current).backingSet;
        final Set.Transient<IStrategoTerm> resultSet = Set.Transient.of();
        for(IStrategoTerm value : set) {
            current = filter.invoke(context, value);
            if(current == null) {
                continue;
            }
            resultSet.__insert(value);
        }

        return new StrategoImmutableSet(resultSet.freeze());
    }
}
