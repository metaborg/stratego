package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import benchmark.stratego2.problems.ExecutableStr2Problem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static benchmark.stratego2.Str2BenchmarkUtil.initProgram;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(value = 10, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class Str2CompilationBenchmark {

    @Param({
            "Benchexpr_10",
            "Benchsym_10",
            "Benchtree_2",
            "Bubblesort_10",
            "Calls",
            "Factorial_4",
            "Fibonacci_18",
            "GarbageCollection",
            "Hanoi_4",
            "Mergesort_10",
            "Quicksort_10",
            "Sieve_20",
    })
    public ExecutableStr2Problem problem;

    @Param({"2"})
    public int optimisationLevel = -1;

    Stratego2Program program;

    @Setup(Level.Trial)
    public final void setup() throws MetaborgException, IOException {
        program = initProgram(problem, optimisationLevel);
    }

    @TearDown(Level.Iteration)
    public final void teardown() throws MetaborgException, IOException {
        if (program != null) {
            program.cleanup();
        }
    }

    @Benchmark
    public final CompileOutput compileStratego() throws MetaborgException, IOException {
        return program.compileStratego();
    }
}
