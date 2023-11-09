package strategolib.strategies;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_relation_values_set_0_0 extends Strategy {
    public static internal_immutable_relation_values_set_0_0 instance = new internal_immutable_relation_values_set_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final StrategoImmutableRelation map = (StrategoImmutableRelation) current;
        final Set.Transient<IStrategoTerm> result = CapsuleUtil.transientSet();
        result.__insertAll(map.backingRelation.inverse().keySet());

        return new StrategoImmutableSet(result.freeze());
    }
}
