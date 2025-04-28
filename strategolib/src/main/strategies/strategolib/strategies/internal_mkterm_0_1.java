package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_mkterm_0_1 extends Strategy {
    public static final internal_mkterm_0_1 instance = new internal_mkterm_0_1();

    /**
     * Stratego 2 type: {@code internal-mkterm :: (|List(?)) ? -> ?}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm cons, IStrategoTerm children) {
        return callStatic(context, cons, children);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm cons, IStrategoTerm children) {
        switch(cons.getType()) {
            case STRING:
                final ITermFactory factory = context.getFactory();
                final String consString = TermUtils.toJavaString(cons);
                if(consString.startsWith("\"")) {
                    return factory.parseFromString(consString + "\"");
                } else {
                    return makeAppl(factory, consString, children);
                }
            case INT:
            case REAL:
                return cons;
            case LIST:
                return children;
            default:
                return null;
        }
    }

    private static IStrategoTerm makeAppl(ITermFactory factory, String name, IStrategoTerm argsTerm) {
        // Cut constructor short to valid name
        for(int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if(!(Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '+' || c == '*' || c == '$')) {
                name = name.substring(0, i);
                break;
            }
        }

        final IStrategoList args = (IStrategoList) argsTerm;

        if(name.length() == 0) { // tuple
            return factory.makeTuple(args.getAllSubterms());
        } else {
            IStrategoConstructor cons = factory.makeConstructor(name, args.size());
            return factory.makeAppl(cons, args.getAllSubterms());
        }
    }
}
