package mb.stratego.build.strincr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;

import mb.stratego.build.util.StrIncrContext;

public class StrategyStubs {
    private final ITermFactory tf;
    private final IStrategoTerm newSVar;
    private final IStrategoTerm newTVar;

    @Inject public StrategyStubs(StrIncrContext context) {
        this.tf = context.getFactory();
        final IStrategoAppl aTerm = tf.makeAppl("Sort", B.string("ATerm"), B.list());
        newSVar = tf.makeAppl("VarDec", B.string("a"), tf.makeAppl("FunType", aTerm, aTerm));
        newTVar = tf.makeAppl("VarDec", B.string("a"), tf.makeAppl("ConstType", aTerm));
    }

    List<IStrategoAppl> declStubs(Map<String, List<IStrategoAppl>> strategyASTs) {
        final List<IStrategoAppl> decls = new ArrayList<>(strategyASTs.size());
        for(String strategyName : strategyASTs.keySet()) {
            final @Nullable StrategySignature sig;
            sig = StrategySignature.fromCified(strategyName);
            if(sig != null) {
                decls.add(sdefStub(tf, strategyName, sig.noStrategyArgs, sig.noTermArgs));
            } else {
                decls.add(sdefStub(tf, strategyName, 0, 0));
            }
        }
        return decls;
    }

    List<IStrategoAppl> declStubs(Collection<StrategySignature> strategySignatures) {
        final List<IStrategoAppl> decls = new ArrayList<>(strategySignatures.size());
        for(StrategySignature sig : strategySignatures) {
            decls.add(sdefStub(tf, sig.name, sig.noStrategyArgs, sig.noTermArgs));
        }
        return decls;
    }

    private IStrategoAppl sdefStub(ITermFactory tf, String strategyName, int svars, int tvars) {
        final IStrategoAppl newBody = tf.makeAppl("Id");
        final IStrategoTerm name = tf.makeAppl(strategyName);

        final IStrategoTerm[] newSVarArray = new IStrategoTerm[svars];
        Arrays.fill(newSVarArray, newSVar);
        final IStrategoTerm newSVars = B.list(newSVarArray);

        final IStrategoTerm[] newTVarArray = new IStrategoTerm[tvars];
        Arrays.fill(newTVarArray, newTVar);
        final IStrategoTerm newTVars = B.list(newTVarArray);

        return tf.makeAppl("SDefT", name, newSVars, newTVars, newBody);
    }
}
