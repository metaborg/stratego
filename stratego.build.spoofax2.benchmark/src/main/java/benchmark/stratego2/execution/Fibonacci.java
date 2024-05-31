package benchmark.stratego2.execution;

import benchmark.stratego2.input.NatSD0NumInput;
import benchmark.stratego2.problem.FibonacciProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.concurrent.TimeUnit;

public class Fibonacci extends StrategoExecutionBenchmark implements FibonacciProblem, NatSD0NumInput {

    @Param({"18", "19", "20", "21"})
    int problemSize;

    @Override
    public IStrategoTerm constructInput(ITermFactory termFactory) {
        return constructInput(termFactory, problemSize);
    }
}
