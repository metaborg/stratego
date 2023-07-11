package benchmark.stratego2.input;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public interface NoInput {
    default IStrategoTerm constructInput(ITermFactory termFactory) {
        return termFactory.makeString("");
    }
}
