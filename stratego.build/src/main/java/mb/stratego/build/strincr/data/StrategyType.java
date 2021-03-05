package mb.stratego.build.strincr.data;

import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.util.TermUtils;

public class StrategyType extends StrategoAppl {
    public final IStrategoTerm from;
    public final IStrategoTerm to;
    private final ArrayList<IStrategoTerm> strategyArguments;
    private final ArrayList<IStrategoTerm> termArguments;

    public StrategyType(IStrategoTermBuilder tf, IStrategoTerm from, IStrategoTerm to,
        ArrayList<IStrategoTerm> strategyArguments, ArrayList<IStrategoTerm> termArguments) {
        super(tf.makeConstructor("FunTType", 3),
            new IStrategoTerm[] { tf.makeList(strategyArguments), tf.makeList(termArguments),
                tf.makeAppl("FunNoArgsType", from, to) }, null);
        this.from = from;
        this.to = to;
        this.strategyArguments = strategyArguments;
        this.termArguments = termArguments;
    }

    public ArrayList<IStrategoTerm> getStrategyArguments() {
        return strategyArguments;
    }

    public ArrayList<IStrategoTerm> getTermArguments() {
        return termArguments;
    }

    public static @Nullable StrategyType fromTerm(IStrategoTermBuilder tf, IStrategoTerm funTType) {
        if(!TermUtils.isAppl(funTType)) {
            return null;
        }
        final ArrayList<IStrategoTerm> strategyArguments;
        final ArrayList<IStrategoTerm> termArguments;
        final IStrategoTerm from;
        final IStrategoTerm to;
        switch(TermUtils.toAppl(funTType).getName()) {
            case "FunNoArgsType":
                strategyArguments = new ArrayList<>(0);
                termArguments = new ArrayList<>(0);
                from = funTType.getSubterm(0);
                to = funTType.getSubterm(1);
                break;
            case "FunType":
                if(!TermUtils.isListAt(funTType, 0)) {
                    return null;
                }
                strategyArguments = new ArrayList<>(funTType.getSubterm(0).getSubterms());
                termArguments = new ArrayList<>(0);

                funTType = funTType.getSubterm(1);
                if(!TermUtils.isAppl(funTType, "FunNoArgsType", 2)) {
                    return null;
                }
                from = funTType.getSubterm(0);
                to = funTType.getSubterm(1);
                break;
            case "FunTType":
                if(!TermUtils.isListAt(funTType, 0)) {
                    return null;
                }
                strategyArguments = new ArrayList<>(funTType.getSubterm(0).getSubterms());
                if(!TermUtils.isListAt(funTType, 1)) {
                    return null;
                }
                termArguments = new ArrayList<>(funTType.getSubterm(1).getSubterms());

                funTType = funTType.getSubterm(2);
                if(!TermUtils.isAppl(funTType, "FunNoArgsType", 2)) {
                    return null;
                }
                from = funTType.getSubterm(0);
                to = funTType.getSubterm(1);
                break;
            default:
                return null;
        }
        return new StrategyType(tf, from, to, strategyArguments, termArguments);
    }

    public static class Standard extends StrategyType {
        private Standard(IStrategoTermBuilder tf, IStrategoTerm from, IStrategoTerm to,
            ArrayList<IStrategoTerm> strategyArguments, ArrayList<IStrategoTerm> termArguments) {
            super(tf, from, to, strategyArguments, termArguments);
        }

        public static Standard fromArity(IStrategoTermBuilder tf, int noStrategyArgs,
            int noTermArgs) {
            final IStrategoAppl sdyn = tf.makeAppl("SDyn");
            final IStrategoAppl dyn = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
            return new Standard(tf, dyn, dyn,
                new ArrayList<>(Collections.nCopies(noStrategyArgs, sdyn)),
                new ArrayList<>(Collections.nCopies(noTermArgs, dyn)));
        }
    }
}
