package benchmark.stratego2.execution;

import benchmark.stratego2.input.NatSD0NumInput;
import benchmark.stratego2.problem.QuicksortProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.concurrent.TimeUnit;

public class Quicksort extends StrategoExecutionBenchmark implements QuicksortProblem, NatSD0NumInput {

    @Param({"10", "12", "14", "16", "18", "20", /*"100", "1000"*/})
    int problemSize;

    @Override
    protected IStrategoTerm constructInput(ITermFactory termFactory) {
        return constructInput(termFactory, problemSize);
    }
}