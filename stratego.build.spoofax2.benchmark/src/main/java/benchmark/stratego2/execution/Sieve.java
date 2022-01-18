package benchmark.stratego2.execution;

import benchmark.stratego2.input.NatSZNumInput;
import benchmark.stratego2.problem.SieveProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.concurrent.TimeUnit;

public class Sieve extends StrategoExecutionBenchmark implements SieveProblem, NatSZNumInput {

    @Param({"20", "40", "60", "80", "100", /*"1000", "2000", "100000"*/})
    int problemSize;

    @Override
    protected IStrategoTerm constructInput(ITermFactory termFactory) {
        return constructInput(termFactory, problemSize);
    }
}
