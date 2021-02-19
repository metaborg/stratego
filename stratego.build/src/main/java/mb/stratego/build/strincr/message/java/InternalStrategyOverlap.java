package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class InternalStrategyOverlap extends JavaMessage<IStrategoString> {
    public InternalStrategyOverlap(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Strategy '" + locationTerm.stringValue() + "' overlaps with a strategy defined to be internal";
    }
}
