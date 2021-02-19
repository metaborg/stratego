package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class ConstantCongruence extends JavaMessage<IStrategoTerm> {
    public ConstantCongruence(String module, IStrategoTerm congruence) {
        super(module, congruence, MessageSeverity.WARNING);
    }

    @Override public String getMessage() {
        return "Simple matching congruence: prefix with '?'. Or with '!' if you meant to build.";
    }
}
