package mb.stratego.build.strincr.message.java;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class CyclicOverlay extends JavaMessage<IStrategoString> {
    public final Set<String> cycle;

    public CyclicOverlay(String module, IStrategoString name, Set<String> cycle) {
        super(module, name, MessageSeverity.ERROR);
        this.cycle = cycle;
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }
}
