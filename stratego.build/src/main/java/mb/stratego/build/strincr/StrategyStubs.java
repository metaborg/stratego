package mb.stratego.build.strincr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.B;
import org.strategoxt.strj.strj;

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

    static List<IStrategoAppl> declStubs(Map<String, List<IStrategoAppl>> strategyASTs) {
        final List<IStrategoAppl> decls = new ArrayList<>(strategyASTs.size());
        final B b = new B(strj.init().getFactory());
        for(String strategyName : strategyASTs.keySet()) {
            final @Nullable StrategySignature sig;
            sig = StrategySignature.fromCified(strategyName);
            if(sig != null) {
                decls.add(sdefStub(b, strategyName, sig.noStrategyArgs, sig.noTermArgs));
            } else {
                decls.add(sdefStub(b, strategyName, 0, 0));
            }
        }
        return decls;
    }

    private static IStrategoAppl sdefStub(B b, String strategyName, int svars, int tvars) {
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
