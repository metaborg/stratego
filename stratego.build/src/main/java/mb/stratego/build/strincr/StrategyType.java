package mb.stratego.build.strincr;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.terms.util.TermUtils;

public class StrategyType {
    public final IStrategoTerm from;
    public final IStrategoTerm to;
    private final List<IStrategoTerm> strategyArguments;
    private final List<IStrategoTerm> termArguments;

    public StrategyType(IStrategoTerm from, IStrategoTerm to, List<IStrategoTerm> strategyArguments,
        List<IStrategoTerm> termArguments) {
        this.from = from;
        this.to = to;
        this.strategyArguments = strategyArguments;
        this.termArguments = termArguments;
    }

    public List<IStrategoTerm> getStrategyArguments() {
        return Collections.unmodifiableList(strategyArguments);
    }

    public List<IStrategoTerm> getTermArguments() {
        return Collections.unmodifiableList(termArguments);
    }

    public IStrategoTerm toTerm(IStrategoTermBuilder tf) {
        return tf.makeAppl("FunTType", tf.makeList(strategyArguments), tf.makeList(termArguments),
            tf.makeAppl("FunNoArgsType", from, to));
    }

    public static @Nullable StrategyType fromTerm(IStrategoTerm funTType) {
        if(!TermUtils.isAppl(funTType)) {
            return null;
        }
        final List<IStrategoTerm> strategyArguments;
        final List<IStrategoTerm> termArguments;
        final IStrategoTerm from;
        final IStrategoTerm to;
        switch(TermUtils.toAppl(funTType).getName()) {
            case "FunNoArgsType":
                strategyArguments = Collections.emptyList();
                termArguments = Collections.emptyList();
                from = funTType.getSubterm(0);
                to = funTType.getSubterm(1);
                break;
            case "FunType":
                if(!TermUtils.isListAt(funTType, 0)) {
                    return null;
                }
                strategyArguments = funTType.getSubterm(0).getSubterms();
                termArguments = Collections.emptyList();

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
                strategyArguments = funTType.getSubterm(0).getSubterms();
                if(!TermUtils.isListAt(funTType, 1)) {
                    return null;
                }
                termArguments = funTType.getSubterm(1).getSubterms();

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
        return new StrategyType(from, to, strategyArguments, termArguments);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        StrategyType that = (StrategyType) o;

        if(!from.equals(that.from))
            return false;
        if(!to.equals(that.to))
            return false;
        if(!strategyArguments.equals(that.strategyArguments))
            return false;
        return termArguments.equals(that.termArguments);
    }

    @Override public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + strategyArguments.hashCode();
        result = 31 * result + termArguments.hashCode();
        return result;
    }
}
