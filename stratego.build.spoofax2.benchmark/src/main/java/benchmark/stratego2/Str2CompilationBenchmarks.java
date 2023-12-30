package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import benchmark.stratego2.problems.ExecutableStr2Problem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static benchmark.stratego2.Str2Benchmarks.initProgram;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(value = 10, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class Str2CompilationBenchmarks {

    @Param({
            "Benchexpr10",
            "Benchsym10",
            "Benchtree2",
            "Bubblesort10",
            "Calls",
            "Factorial4",
            "Fibonacci18",
            "GarbageCollection",
            "Hanoi4",
            "Mergesort10",
            "Quicksort10",
            "Sieve20",
    })
    public ExecutableStr2Problem problem;

    @Param({"3"})
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
    public final CompileOutput compileStratego() throws MetaborgException {
        return program.compileStratego();
    }
}
