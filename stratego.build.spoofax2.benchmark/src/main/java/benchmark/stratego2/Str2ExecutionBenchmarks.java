package benchmark.stratego2;

import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import benchmark.stratego2.problems.ExecutableStr2Problem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
public class Str2ExecutionBenchmarks extends Str2Benchmarks {
    @Param({
            "Benchexpr10",
//            "Benchexpr11",
//            "Benchexpr12",
//            "Benchexpr13",
//            "Benchexpr14",
//            "Benchexpr15",
//            "Benchexpr16",
//            "Benchexpr17",
//
//            "Benchsym10",
//            "Benchsym11",
//            "Benchsym12",
//            "Benchsym13",
//            "Benchsym14",
//            "Benchsym15",
//            "Benchsym16",
//            "Benchsym17",
//            "Benchsym18",
//
//            "Benchtree2",
//            "Benchtree4",
//            "Benchtree6",
//
//            "Bubblesort10",
//            "Bubblesort20",
//            "Bubblesort50",
//            "Bubblesort100",
//            "Bubblesort200",
//
//            "Calls",
//
//            "Factorial4",
//            "Factorial5",
//            "Factorial6",
//            "Factorial7",
//
//            "Fibonacci18",
//            "Fibonacci19",
//            "Fibonacci20",
//            "Fibonacci21",
//
//            "GarbageCollection",
//
//            "Hanoi4",
//            "Hanoi5",
//            "Hanoi6",
//            "Hanoi7",
//            "Hanoi8",
//            "Hanoi9",
//            "Hanoi10",
//            "Hanoi11",
//
//            "Mergesort10",
//            "Mergesort20",
//            "Mergesort30",
//            "Mergesort40",
//
//            "Quicksort10",
//            "Quicksort12",
//            "Quicksort14",
//            "Quicksort16",
//            "Quicksort18",
//            "Quicksort20",
//
//            "Sieve20",
//            "Sieve40",
//            "Sieve60",
//            "Sieve80",
//            "Sieve100",
    })
    public ExecutableStr2Problem problem;

    @Param({"3"})
    public int optimisationLevel = -1;

    @Setup(Level.Trial)
    public final void setup() throws MetaborgException, IOException {
        initProgram();
        program.compileStratego();
        program.compileJava();
    }

    @TearDown(Level.Trial)
    public final void teardown() throws MetaborgException, IOException {
        program.cleanup();
    }

    @Benchmark
    public final String executionBenchmark() throws IOException, InterruptedException {
        return program.run(problem.input);
    }
}
