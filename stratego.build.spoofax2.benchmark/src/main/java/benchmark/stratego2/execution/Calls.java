package benchmark.stratego2.execution;

import benchmark.stratego2.input.NoInput;
import benchmark.stratego2.problem.CallsProblem;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class Calls extends StrategoExecutionBenchmark implements CallsProblem, NoInput {

    @Override
    public IStrategoTerm constructInput(ITermFactory termFactory) {
        return NoInput.super.constructInput(termFactory);
    }

}
