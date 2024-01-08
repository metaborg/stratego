package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import benchmark.stratego2.problems.ExecutableStr2Problem;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static benchmark.stratego2.Str2BenchmarkUtil.initProgram;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class Str2JavaCompilationBenchmark {

    @Param({
            "Benchexpr_10",
//            "Benchsym_10",
//            "Benchtree_2",
//            "Bubblesort_10",
//            "Calls",
//            "Factorial_4",
//            "Fibonacci_18",
//            "GarbageCollection",
//            "Hanoi_4",
//            "Mergesort_10",
//            "Quicksort_10",
//            "Sieve_20",
    })
    public ExecutableStr2Problem problem;

    @Param({"2"})
    public int optimisationLevel = -1;

    Stratego2Program program;

    @Setup(Level.Trial)
    public final void setup() throws MetaborgException, IOException {
        program = initProgram(problem, optimisationLevel);
        program.compileStratego();
    }

    @TearDown(Level.Trial)
    public final void teardown() throws MetaborgException, IOException {
        program.cleanup();
    }

//    @Benchmark
    public final File compileJava() throws MetaborgException, IOException {
        return program.compileJava();
    }
}
