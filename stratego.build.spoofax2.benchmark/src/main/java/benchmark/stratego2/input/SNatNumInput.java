package benchmark.stratego2.input;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public interface SNatNumInput {
    default IStrategoTerm constructInput(ITermFactory termFactory, int size) {
        IStrategoTerm inputTerm = termFactory.makeAppl("exz");
        for (int i = 0; i < size; i++) {
            inputTerm = termFactory.makeAppl("exs", inputTerm);
        }
        return inputTerm;
    }
}
