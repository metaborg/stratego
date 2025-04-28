package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.BinaryRelation;
import io.usethesource.capsule.Set;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_set_cartesian_product_0_1 extends Strategy {
    public static final internal_immutable_set_cartesian_product_0_1 instance = new internal_immutable_set_cartesian_product_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {
        return callStatic(context, current, otherTerm);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {

        final StrategoImmutableSet left = (StrategoImmutableSet) current;
        final StrategoImmutableSet right = (StrategoImmutableSet) otherTerm;

        return new StrategoImmutableRelation(cartesianProduct(left.backingSet, right.backingSet));
    }

    public static BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> cartesianProduct(
        Set.Immutable<IStrategoTerm> left, Set.Immutable<IStrategoTerm> right) {
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = BinaryRelation.Transient.of();

        for(IStrategoTerm key : left) {
            result.__insert(key, right);
        }

        return result.freeze();
    }
}
