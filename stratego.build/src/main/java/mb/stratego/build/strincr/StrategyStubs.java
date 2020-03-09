package mb.stratego.build.strincr;

import org.spoofax.terms.util.B;
import mb.pie.api.ExecException;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.strj.strj;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class StrategyStubs {
    private StrategyStubs() {}

    private static final IStrategoAppl A_TERM;
    private static final IStrategoTerm newSVar;
    private static final IStrategoTerm newTVar;

    static {
        final B b = new B(strj.init().getFactory());
        A_TERM = b.applShared("Sort", B.string("ATerm"), B.list());
        newSVar = b.applShared("VarDec", B.string("a"), b.applShared("FunType", A_TERM, A_TERM));
        newTVar = b.applShared("VarDec", B.string("a"), b.applShared("ConstType", A_TERM));
    }

    static List<IStrategoAppl> declStubs(Map<String, List<IStrategoAppl>> strategyASTs) throws ExecException {
        final List<IStrategoAppl> decls = new ArrayList<>(strategyASTs.size());
        final B b = new B(strj.init().getFactory());
        for(String strategyName : strategyASTs.keySet()) {
            final Matcher m = StaticChecks.stripArityPattern.matcher(strategyName);
            if(!m.matches()) {
                throw new ExecException(
                    "Frontend returned stratego strategy name that does not conform to cified name: '" + strategyName
                        + "'");
            }
            final int svars = Integer.parseInt(m.group(2));
            final int tvars = Integer.parseInt(m.group(3));
            decls.add(sdefStub(b, strategyName, svars, tvars));
        }
        return decls;
    }

    private static IStrategoAppl sdefStub(B b, String strategyName, int svars, int tvars) throws ExecException {
        final IStrategoAppl newBody = b.applShared("Id");
        final IStrategoTerm name = b.stringShared(strategyName);

        final IStrategoTerm[] newSVarArray = new IStrategoTerm[svars];
        Arrays.fill(newSVarArray, newSVar);
        final IStrategoTerm newSVars = B.list(newSVarArray);

        final IStrategoTerm[] newTVarArray = new IStrategoTerm[tvars];
        Arrays.fill(newTVarArray, newTVar);
        final IStrategoTerm newTVars = B.list(newTVarArray);

        return b.applShared("SDefT", name, newSVars, newTVars, newBody);
    }
}
