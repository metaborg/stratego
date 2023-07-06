package mb.stratego.build.strincr.data;

import java.io.Serializable;
import java.util.TreeSet;

import org.spoofax.interpreter.terms.IStrategoAppl;

public class StrategyAnalysisData implements Serializable {
    public final StrategySignature signature;
    public final IStrategoAppl analyzedAst;
    public final TreeSet<StrategySignature> definedDynamicRules;

    public StrategyAnalysisData(StrategySignature signature, IStrategoAppl analyzedAst,
        TreeSet<StrategySignature> definedDynamicRules) {
        this.signature = signature;
        this.analyzedAst = analyzedAst;
        this.definedDynamicRules = definedDynamicRules;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        StrategyAnalysisData that = (StrategyAnalysisData) o;

        if(!signature.equals(that.signature))
            return false;
        if(!analyzedAst.equals(that.analyzedAst))
            return false;
        return definedDynamicRules.equals(that.definedDynamicRules);
    }

    @Override public int hashCode() {
        int result = signature.hashCode();
        result = 31 * result + analyzedAst.hashCode();
        result = 31 * result + definedDynamicRules.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StrategyAnalysisData(" + signature + ", " + analyzedAst + ", " + definedDynamicRules
            + ')';
    }
}
