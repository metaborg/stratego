package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class concat_0_0 extends Strategy {
    public static final concat_0_0 instance = new concat_0_0();

    /**
     * Stratego 2 type: {@code flatten-list :: (|) List(List(a)) -> List(a)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();
        final IStrategoList list = (IStrategoList) current;
        final IStrategoList annos = list.getAnnotations();

        final IStrategoList.Builder b = factory.arrayListBuilder();
        for(IStrategoTerm t : list) {
            for(IStrategoTerm lt : t) {
                b.add(lt);
            }
        }

        // NOTE: The Stratego implementation of `concat` preserves the
        // wrong annotation. See: <http://yellowgrass.org/issue/StrategoXT/905>
        // This is fixed in this Java implementation, making the two
        // implementations not entirely equal.
        return factory.annotateTerm(factory.makeList(b), annos);
    }
}
