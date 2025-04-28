package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoPlaceholder;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class get_arguments_0_0 extends Strategy {
    public static final get_arguments_0_0 instance = new get_arguments_0_0();

    /**
     * Stratego 2 type: {@code get-arguments :: (|) ? -> List(?)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();
        switch(current.getType()) {
            case APPL:
                IStrategoAppl appl = (IStrategoAppl) current;
                return factory.makeList(appl.getAllSubterms());
            case INT:
            case STRING:
            case REAL:
            case BLOB:
                return factory.makeList();
            case LIST:
                return current;
            case TUPLE:
                IStrategoTuple tuple = (IStrategoTuple) current;
                return factory.makeList(tuple.getAllSubterms());
            case PLACEHOLDER:
                IStrategoPlaceholder placeholder = (IStrategoPlaceholder) current;
                return factory.makeList(placeholder.getTemplate());
            case REF:
            case CTOR:
            default:
                throw new IllegalStateException("SSL_get_arguments failed for " + current);
        }
    }
}
