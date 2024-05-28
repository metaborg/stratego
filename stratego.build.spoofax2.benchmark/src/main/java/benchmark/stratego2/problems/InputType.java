package benchmark.stratego2.problems;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

public enum InputType {
    NatSD0Num,
    NatSZNum,
    SNatNum,
    DNatNum,
    None;

    private static final ITermFactory termFactory = new TermFactory();

    public static IStrategoTerm constructInput(InputType input, int size) {
        switch (input) {
            case NatSD0Num:
                return natNum("d0", "s", size);
            case NatSZNum:
                return natNum("z", "s", size);
            case None:
                return termFactory.makeString("");
            case SNatNum:
                return natNum("exz", "exs", size);
            case DNatNum:
                return termFactory.makeAppl(String.format("d%d", size));
            default: throw new RuntimeException("Unexpected InputType: " + input);
        }
    }

    /**
     * Helper function used in the term construction of 3 of the InputTypes.
     * @return the constructed input term.
     */
    private static IStrategoTerm natNum(String zero, String succ, int size) {
        IStrategoTerm inputTerm = termFactory.makeAppl(zero);
        for (int i = 0; i < size; i++) {
            inputTerm = termFactory.makeAppl(succ, inputTerm);
        }
        return inputTerm;
    }
}
