package mb.stratego.build.strincr.data;

import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.util.TermUtils;

public class StrategyType extends StrategoAppl {
    // either TP/0 or FunNoArgsType/2
    public final IStrategoTerm functionType;
    public final ArrayList<IStrategoTerm> strategyArguments;
    public final ArrayList<IStrategoTerm> termArguments;

    public StrategyType(IStrategoTermBuilder tf, IStrategoTerm functionType,
        ArrayList<IStrategoTerm> strategyArguments, ArrayList<IStrategoTerm> termArguments) {
        super(tf.makeConstructor("FunTType", 3),
            new IStrategoTerm[] { tf.makeList(strategyArguments), tf.makeList(termArguments),
                functionType }, null);
        this.functionType = functionType;
        this.strategyArguments = strategyArguments;
        this.termArguments = termArguments;
    }

    public static @Nullable StrategyType fromDefinition(IStrategoTermBuilder tf,
        IStrategoTerm strategyDef) {
        if(!TermUtils.isAppl(strategyDef)) {
            return null;
        }
        final ArrayList<IStrategoTerm> strategyArguments;
        final ArrayList<IStrategoTerm> termArguments;
        final IStrategoTerm sSimpleFunType;
        switch(TermUtils.toAppl(strategyDef).getName()) {
            case "DefHasTypeNoArgs":
                strategyArguments = termArguments = new ArrayList<>(0);
                sSimpleFunType = strategyDef.getSubterm(1);
                break;
            case "DefHasType":
                if(!TermUtils.isListAt(strategyDef, 1)) {
                    return null;
                }
                strategyArguments = new ArrayList<>(strategyDef.getSubterm(1).getSubterms());
                termArguments = new ArrayList<>(0);

                sSimpleFunType = strategyDef.getSubterm(2);
                break;
            case "DefHasTType":
                if(!TermUtils.isListAt(strategyDef, 1)) {
                    return null;
                }
                strategyArguments = new ArrayList<>(strategyDef.getSubterm(0).getSubterms());
                if(!TermUtils.isListAt(strategyDef, 2)) {
                    return null;
                }
                termArguments = new ArrayList<>(strategyDef.getSubterm(1).getSubterms());

                sSimpleFunType = strategyDef.getSubterm(3);
                break;
            default:
                return null;
        }
        return new StrategyType(tf, sSimpleFunType, strategyArguments, termArguments);
    }

    public StrategySignature withName(IStrategoString name) {
        return new StrategySignature(name, strategyArguments.size(), termArguments.size());
    }

    public static class Standard extends StrategyType {
        private Standard(IStrategoTermBuilder tf, IStrategoTerm sSimpleFunType,
            ArrayList<IStrategoTerm> strategyArguments, ArrayList<IStrategoTerm> termArguments) {
            super(tf, sSimpleFunType, strategyArguments, termArguments);
        }

        public static Standard fromArity(IStrategoTermBuilder tf, int noStrategyArgs,
            int noTermArgs) {
            final IStrategoAppl sdyn = tf.makeAppl("SDyn");
            final IStrategoAppl dyn = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
            return new Standard(tf, tf.makeAppl("FunNoArgsType", dyn, dyn),
                new ArrayList<>(Collections.nCopies(noStrategyArgs, sdyn)),
                new ArrayList<>(Collections.nCopies(noTermArgs, dyn)));
        }
    }
}
