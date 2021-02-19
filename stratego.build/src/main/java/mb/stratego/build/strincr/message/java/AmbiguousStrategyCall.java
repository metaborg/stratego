package mb.stratego.build.strincr.message.java;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class AmbiguousStrategyCall extends JavaMessage<IStrategoString> {
    public final Set<String> defs;

    public AmbiguousStrategyCall(String module, IStrategoString name, Set<String> defs) {
        super(module, name, MessageSeverity.ERROR);
        this.defs = defs;
    }

    @Override public String getMessage() {
        return "The call to '" + locationTerm.stringValue() + "' is ambiguous, it may resolve to " + defs;
    }
}
