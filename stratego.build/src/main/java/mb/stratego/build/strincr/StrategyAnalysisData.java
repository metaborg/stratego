package mb.stratego.build.strincr;

import java.io.Serializable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.util.WithLastModified;

public class StrategyAnalysisData implements Serializable, WithLastModified {
    public final IStrategoAppl analyzedAst;
    public final long lastModified;


    public StrategyAnalysisData(IStrategoAppl analyzedAst, long lastModified) {
        this.analyzedAst = analyzedAst;
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
        return analyzedAst.equals(that.analyzedAst);
    }

    @Override public int hashCode() {
        int result = analyzedAst.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }
}
