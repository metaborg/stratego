package strategolib.strategies;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_set_from_list_0_0 extends Strategy {
    public static final internal_immutable_set_from_list_0_0 instance = new internal_immutable_set_from_list_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IStrategoList list = (IStrategoList) current;
        final Set.Transient<IStrategoTerm> map = CapsuleUtil.transientSet();
        for(IStrategoTerm t : list) {
            if(!map.contains(t)) {
                map.__insert(t);
            }
        }

        return new StrategoImmutableSet(map.freeze());
    }
}
