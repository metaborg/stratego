package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class get_constructor_0_0 extends Strategy {
    public static final get_constructor_0_0 instance = new get_constructor_0_0();

    /**
     * Stratego 2 type: {@code get_constructor :: (|) ? -> ?}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        ITermFactory factory = context.getFactory();
        switch(current.getType()) {
            case APPL:
                IStrategoAppl appl = (IStrategoAppl) current;
                return factory.makeString(appl.getConstructor().getName());
            case INT:
                return factory.makeInt(((IStrategoInt) current).intValue());
            case REAL:
                return factory.makeReal(((IStrategoReal) current).realValue());
            case BLOB:
                return factory.makeString("BLOB_" + current.toString());
            case LIST:
                return factory.makeList();
            case STRING:
                IStrategoString string = (IStrategoString) factory.annotateTerm(current, factory.makeList());
                return factory.makeString(string.toString());
            case TUPLE:
                return factory.makeString("");
            case PLACEHOLDER:
                return factory.makePlaceholder(factory.makeList());
            case REF:
            case CTOR:
            default:
                throw new IllegalStateException("SSL_get_constructor failed for " + current);
        }
    }
}
