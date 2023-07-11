package benchmark.stratego2.input;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public interface NatSD0NumInput extends NatNumInput {
    default IStrategoTerm constructInput(ITermFactory termFactory, int size) {
        return constructInput("d0", "s", termFactory, size);
    }
}
