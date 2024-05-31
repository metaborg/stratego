package benchmark.stratego2.execution;

import benchmark.stratego2.input.SNatNumInput;
import benchmark.stratego2.problem.BenchtreeProblem;
import org.openjdk.jmh.annotations.Param;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class Benchtree extends StrategoExecutionBenchmark implements BenchtreeProblem, SNatNumInput {

    @Param({"2", "4", "6", /*"7", /*"8", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "22"*/})
    int problemSize;

    @Override
    public IStrategoTerm constructInput(ITermFactory termFactory) {
        return constructInput(termFactory, problemSize);
    }

}
