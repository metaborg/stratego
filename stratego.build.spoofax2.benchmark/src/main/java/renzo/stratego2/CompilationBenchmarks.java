package renzo.stratego2;

import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import renzo.stratego2.problems.ExecutableProblem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
public class CompilationBenchmarks extends Stratego2Benchmarks {

    @Param({
            "Benchexpr10",
//            "Benchsym10",
//            "Benchtree2",
//            "Bubblesort10",
//            "Calls",
//            "Factorial4",
//            "Fibonacci18",
//            "GarbageCollection",
//            "Hanoi4",
//            "Mergesort10",
//            "Quicksort10",
//            "Sieve20",
    })
    public ExecutableProblem problem;

    @Param({"3"})
    public int optimisationLevel = -1;

    @Setup(Level.Trial)
    public final void setup() throws MetaborgException, IOException {
        initProgram();
    }

    @TearDown(Level.Iteration)
    public final void teardown() throws MetaborgException, IOException {
        if (program != null) {
            program.cleanup();
        }
    }

    @Benchmark
    public final void compilationBenchmark() throws MetaborgException {
        program.compileStratego();
    }
}
