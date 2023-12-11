package benchmark.stratego2.input;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public interface NatNumInput {
    default IStrategoTerm constructInput(String zero, String succ, ITermFactory termFactory, int size) {
        IStrategoTerm inputTerm = termFactory.makeAppl(zero);
        for (int i = 0; i < size; i++) {
            inputTerm = termFactory.makeAppl(succ, inputTerm);
        }
        return inputTerm;
    }
}
