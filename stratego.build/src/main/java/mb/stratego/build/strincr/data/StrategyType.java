package mb.stratego.build.strincr.data;

import java.util.ArrayList;
import java.util.Collections;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.termvisitors.DesugarType;

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

    public static @Nullable StrategyType fromDefinition(ITermFactory tf,
        IStrategoTerm strategyDef) {
        if(!TermUtils.isAppl(strategyDef)) {
            return null;
        }
        final ArrayList<IStrategoTerm> strategyArguments;
        final ArrayList<IStrategoTerm> termArguments;
        final IStrategoTerm sSimpleFunType;
        @Nullable IStrategoTerm funttype = null;
        switch(TermUtils.toAppl(strategyDef).getName()) {
            case "DefHasTypeNoArgs":
                strategyArguments = termArguments = new ArrayList<>(0);
                sSimpleFunType = strategyDef.getSubterm(1);
                break;
            case "DefHasType":
                if(!TermUtils.isListAt(strategyDef, 1)) {
                    return null;
                }
                strategyArguments = new ArrayList<>(strategyDef.getSubterm(1).getSubtermCount());
                for(IStrategoTerm strategyArgument : strategyDef.getSubterm(1)) {
                    strategyArguments.add(DesugarType.tryDesugarSType(tf, strategyArgument));
                }
                termArguments = new ArrayList<>(0);

                sSimpleFunType = strategyDef.getSubterm(2);
                break;
            case "DefHasTType":
                if(!TermUtils.isListAt(strategyDef, 1)) {
                    return null;
                }
                strategyArguments = new ArrayList<>(strategyDef.getSubterm(1).getSubtermCount());
                for(IStrategoTerm strategyArgument : strategyDef.getSubterm(1)) {
                    strategyArguments.add(DesugarType.tryDesugarSType(tf, strategyArgument));
                }
                if(!TermUtils.isListAt(strategyDef, 2)) {
                    return null;
                }
                termArguments = new ArrayList<>(strategyDef.getSubterm(2).getSubtermCount());
                for(IStrategoTerm termArgument : strategyDef.getSubterm(2)) {
                    termArguments.add(DesugarType.tryDesugarType(tf, termArgument));
                }

                sSimpleFunType = strategyDef.getSubterm(3);
                break;
            case "ExtTypedDef":
                funttype = strategyDef.getSubterm(1);
                // fall-through
            case "ExtTypedDefInl":
                if(funttype == null) {
                    funttype = strategyDef.getSubterm(3);
                }

                if(!TermUtils.isListAt(funttype, 0)) {
                    return null;
                }
                strategyArguments = new ArrayList<>(funttype.getSubterm(0).getSubtermCount());
                for(IStrategoTerm strategyArgument : funttype.getSubterm(0)) {
                    strategyArguments.add(DesugarType.tryDesugarSType(tf, strategyArgument));
                }
                if(!TermUtils.isListAt(funttype, 1)) {
                    return null;
                }
                termArguments = new ArrayList<>(funttype.getSubterm(1).getSubtermCount());
                for(IStrategoTerm termArgument : funttype.getSubterm(1)) {
                    termArguments.add(DesugarType.tryDesugarType(tf, termArgument));
                }

                sSimpleFunType = funttype.getSubterm(2);
                break;
            default:
                return null;
        }
        final StrategyType result =
            new StrategyType(tf, desugarSSimpleFunType(tf, sSimpleFunType), strategyArguments,
                termArguments);
        tf.replaceTerm(result, strategyDef);
        return result;
    }

    private static IStrategoTerm desugarSSimpleFunType(ITermFactory tf,
        IStrategoTerm sSimpleFunType) {
        if(TermUtils.isAppl(sSimpleFunType, "TP", 0)) {
            return sSimpleFunType;
        }
        return tf.replaceTerm(
            tf.makeAppl("FunNoArgsType", DesugarType.tryDesugarType(tf, sSimpleFunType.getSubterm(0)),
                DesugarType.tryDesugarType(tf, sSimpleFunType.getSubterm(1))), sSimpleFunType);
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

    // equals/hashcode/toString inherited from StrategoAppl
}
