package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class access_0_1 extends Strategy {
    public static final access_0_1 instance = new access_0_1();

    /**
     * Stratego 2 type: {@code access :: (|List(AccessPermission)) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm perms) {
        return callStatic(context, current, perms);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm perms) {
        final IOAgent agent = context.getIOAgent();

        final String path = TermUtils.toJavaString(current);
        final int permissions = permissions_from_term(agent, perms);

        if((permissions & R_OK) != 0) {
            if(!agent.readable(path)) {
                return null;
            }
        } else if((permissions & W_OK) != 0) {
            if(!agent.writable(path)) {
                return null;
            }
        } else if((permissions & X_OK) != 0) {
            if(!agent.openFile(path).canExecute()) {
                return null;
            }
        } else if(permissions == F_OK) {
            if(!agent.exists(path)) {
                return null;
            }
        }

        return current;
    }

    private static int permissions_from_term(IOAgent agent, IStrategoTerm perms) {
        int res = 0;
        for(IStrategoTerm t : perms) {
            if(TermUtils.isAppl(t, "W_OK"))
                res |= W_OK;
            else if(TermUtils.isAppl(t, "R_OK"))
                res |= R_OK;
            else if(TermUtils.isAppl(t, "X_OK"))
                res |= X_OK;
            else if(TermUtils.isAppl(t, "F_OK"))
                res |= F_OK;
            else
                agent.printError("*** ERROR: not an access mode: " + t);
        }
        return res;
    }

    public static final int R_OK = 4;
    public static final int W_OK = 2;
    public static final int X_OK = 1;
    public static final int F_OK = 0;
}
