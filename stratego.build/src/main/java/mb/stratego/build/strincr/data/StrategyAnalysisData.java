package mb.stratego.build.strincr.data;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoAppl;

import mb.stratego.build.util.WithLastModified;

public class StrategyAnalysisData implements Serializable, WithLastModified {
    public final StrategySignature signature;
    public final IStrategoAppl analyzedAst;
    public final LinkedHashSet<StrategySignature> definedDynamicRules;
    public final long lastModified;

    public StrategyAnalysisData(StrategySignature signature, IStrategoAppl analyzedAst,
        LinkedHashSet<StrategySignature> definedDynamicRules, long lastModified) {
        this.signature = signature;
        this.analyzedAst = analyzedAst;
        this.definedDynamicRules = definedDynamicRules;
        this.lastModified = lastModified;
    }

    @Override public long lastModified() {
        return lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        StrategyAnalysisData that = (StrategyAnalysisData) o;

        if(lastModified != that.lastModified)
            return false;
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
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "StrategyAnalysisData(" + signature + ", " + analyzedAst
            + ", " + definedDynamicRules + ", " + lastModified
            + ')';
    }
}
