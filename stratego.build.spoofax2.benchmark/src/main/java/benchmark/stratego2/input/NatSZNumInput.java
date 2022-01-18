package benchmark.stratego2.input;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public interface NatSZNumInput extends NatNumInput {
    default IStrategoTerm constructInput(ITermFactory termFactory, int size) {
        return constructInput("z", "s", termFactory, size);
    }
}
