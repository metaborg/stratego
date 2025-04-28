package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class explode_string_0_0 extends Strategy {
    public static final explode_string_0_0 instance = new explode_string_0_0();

    /**
     * Stratego 2 type: {@code explode-string :: (|) string -> List(Char)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();

        final String s = TermUtils.toJavaString(current);
        final IStrategoList.Builder b = factory.arrayListBuilder();

        s.codePoints().forEach(c -> b.add(factory.makeInt(c)));

        return factory.makeList(b);
    }
}
