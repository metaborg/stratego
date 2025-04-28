package strategolib.strategies;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_set_map_2_0 extends Strategy {
    public static final internal_immutable_set_map_2_0 instance = new internal_immutable_set_map_2_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy mapping, Strategy dupMapping) {
        return callStatic(context, current, mapping, dupMapping);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy mapping, Strategy dupMapping) {
        final Set.Immutable<IStrategoTerm> set = ((StrategoImmutableSet) current).backingSet;
        final Set.Transient<IStrategoTerm> resultSet = CapsuleUtil.transientSet();
        for(IStrategoTerm value : set) {
            current = mapping.invoke(context, value);
            if(current == null) {
                return null;
            }
            if(resultSet.contains(current)) {
                current = dupMapping.invoke(context, current);
                if(current == null) {
                    return null;
                }
            }
            resultSet.__insert(current);
        }

        return new StrategoImmutableSet(resultSet.freeze());
    }
}
