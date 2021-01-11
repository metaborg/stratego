package mb.stratego.build.strincr;

import java.io.Serializable;

import mb.stratego.build.util.TermWithLastModified;

public class StrategyAnalysisData implements Serializable {
    public final TermWithLastModified analyzedAst;


    public StrategyAnalysisData(TermWithLastModified analyzedAst) {
        this.analyzedAst = analyzedAst;
    }
}
