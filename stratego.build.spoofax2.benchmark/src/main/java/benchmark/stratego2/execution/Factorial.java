package benchmark.stratego2.execution;

import benchmark.stratego2.input.NatSD0NumInput;
import benchmark.stratego2.problem.FactorialProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.concurrent.TimeUnit;

public class Factorial extends StrategoExecutionBenchmark implements FactorialProblem, NatSD0NumInput {

    @Param({"4", "5", "6", "7", /*"8", "9"*/})
    int problemSize;

    @Override
    public IStrategoTerm constructInput(ITermFactory termFactory) {
        return constructInput(termFactory, problemSize);
    }

}
