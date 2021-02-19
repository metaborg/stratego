package mb.stratego.build.strincr.message.java;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class CyclicOverlay extends JavaMessage<IStrategoTerm> {
    public final Set<String> cycle;

    public CyclicOverlay(String module, IStrategoTerm name, Set<String> cycle) {
        super(module, name, MessageSeverity.ERROR);
        this.cycle = cycle;
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }
}
