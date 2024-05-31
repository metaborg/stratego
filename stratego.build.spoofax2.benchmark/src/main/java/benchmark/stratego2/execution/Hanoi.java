package benchmark.stratego2.execution;

import benchmark.stratego2.problem.HanoiProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.concurrent.TimeUnit;

public class Hanoi extends StrategoExecutionBenchmark implements HanoiProblem {

    @Param({"4", "5", "6", "7", "8", "9", "10", "11"/*, "12", "16", "20"*/})
    int problemSize;

    @Override
    public IStrategoTerm constructInput(ITermFactory termFactory) {
        return termFactory.makeAppl(String.format("d%d", problemSize));
    }

}
