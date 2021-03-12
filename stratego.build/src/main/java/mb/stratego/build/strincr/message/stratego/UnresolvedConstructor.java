package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class UnresolvedConstructor extends StrategoMessage {
    public final int arity;
    public final IStrategoTerm sort;

    public UnresolvedConstructor(String module, IStrategoTerm locationTerm, int arity, IStrategoTerm sort,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.arity = arity;
        this.sort = sort;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Undefined constructor with arity " + arity + " and type " + sort + ".";
    }
}
