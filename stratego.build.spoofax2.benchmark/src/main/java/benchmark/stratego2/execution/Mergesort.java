package benchmark.stratego2.execution;

import benchmark.stratego2.input.NatSD0NumInput;
import benchmark.stratego2.problem.MergesortProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.concurrent.TimeUnit;

public class Mergesort extends StrategoExecutionBenchmark implements MergesortProblem, NatSD0NumInput {

    @Param({"10", "20", "30", "40", /*"50", "100", "200", "300", "500", "720", "1000"*/})
    int problemSize;

    @Override
    protected IStrategoTerm constructInput(ITermFactory termFactory) {
        return constructInput(termFactory, problemSize);
    }
}
