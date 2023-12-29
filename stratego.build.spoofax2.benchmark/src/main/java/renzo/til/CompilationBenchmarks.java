package renzo.til;

import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import org.spoofax.interpreter.terms.IStrategoTerm;
import renzo.til.problems.ExecutableProblem;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class CompilationBenchmarks extends TILBenchmarks {
    @Param({
            "Add100",
//            "Add200",
//            "Add500",
//            "Add1000",
//            "EBlock",
//            "Factorial4",
    })
    public ExecutableProblem problem;

    @Param({"3"})
    public int optimisationLevel = -1;

    @Setup(Level.Iteration)
    public void prepareCompilation() throws MetaborgException {
        program.compiler.setupBuild();
    }

    @Benchmark
    public final IStrategoTerm run() throws MetaborgException {
        return program.compileTIL();
    }

    @Override
    @TearDown(Level.Iteration)
    public final void teardown() {
        if (program != null) {
            program.cleanup();
        }
    }
}
