package mb.stratego.build.strincr;

import java.io.Serializable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.util.LastModified;

public class StrategyAnalysisData implements Serializable {
    public final LastModified<IStrategoTerm> analyzedAst;


    public StrategyAnalysisData(LastModified<IStrategoTerm> analyzedAst) {
        this.analyzedAst = analyzedAst;
    }
}
