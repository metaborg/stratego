package benchmark.stratego2.execution;

import benchmark.stratego2.input.NatSD0NumInput;
import benchmark.stratego2.problem.BubblesortProblem;
import org.openjdk.jmh.annotations.Param;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class Bubblesort extends StrategoExecutionBenchmark implements BubblesortProblem, NatSD0NumInput {

    @Param({"10", "20", "50", "100", "200", /*"300", "500", "720", "1000"*/})
    int problemSize;

    @Override
    public IStrategoTerm constructInput(ITermFactory termFactory) {
        return constructInput(termFactory, problemSize);
    }

}
