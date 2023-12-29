package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import benchmark.stratego2.problems.ExecutableStr2Problem;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static benchmark.stratego2.Str2Benchmarks.initProgram;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class StrjCompilationBenchmarks { //TODO: figure out wtf this is

    @Param({
            "Factorial4",
//            "Factorial5",
//            "Factorial6",
//            "Factorial7",
    })
    public ExecutableStr2Problem problem;

    @Param({"3"})
    public int optimisationLevel = -1;

    Stratego2Program program;

    @Setup(Level.Trial)
    public final void setup() throws MetaborgException, IOException {
        program = initProgram(problem, optimisationLevel);
    }

    @TearDown(Level.Trial)
    public final void teardown() throws MetaborgException, IOException {
        if (program != null) {
            program.cleanup();
        }
    }

    @Benchmark
    public final boolean compileStrj() throws IOException {
        return program.compileStrj();
    }
}
