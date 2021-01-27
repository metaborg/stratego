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
}
