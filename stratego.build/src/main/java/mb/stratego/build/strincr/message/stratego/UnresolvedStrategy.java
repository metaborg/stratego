package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class UnresolvedStrategy extends StrategoMessage {
    public final int strategyArity;
    public final int termArity;

    public UnresolvedStrategy(String module, IStrategoTerm locationTerm, int strategyArity, int termArity,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.strategyArity = strategyArity;
        this.termArity = termArity;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Unresolved strategy with arity " + strategyArity + "/" + termArity + ".";
    }
}
