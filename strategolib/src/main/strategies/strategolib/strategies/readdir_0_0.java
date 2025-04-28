package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class readdir_0_0 extends Strategy {
    public static final readdir_0_0 instance = new readdir_0_0();

    /**
     * Stratego 2 type: {@code readdir :: (|) string -> List(string)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        String dir = TermUtils.toJavaString(current);

        String[] entries = context.getIOAgent().readdir(dir);
        if(entries == null) {
            return null;
        }

        final ITermFactory f = context.getFactory();
        final IStrategoList.Builder b = f.arrayListBuilder(entries.length);
        for(String entry : entries) {
            b.add(f.makeString(entry));
        }

        return f.makeList(b);
    }
}
