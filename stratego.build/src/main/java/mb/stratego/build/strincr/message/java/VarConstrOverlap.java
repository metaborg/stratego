package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class VarConstrOverlap extends JavaMessage<IStrategoString> {
    public VarConstrOverlap(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Nullary constructor '" + locationTerm.stringValue() + "' should be followed by round brackets ().";
    }
}
