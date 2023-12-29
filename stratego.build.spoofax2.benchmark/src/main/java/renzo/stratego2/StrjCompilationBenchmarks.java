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
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class StrjCompilationBenchmarks extends Stratego2Benchmarks { //TODO: figure out wtf this is

    @Param({
            "Factorial4",
//            "Factorial5",
//            "Factorial6",
//            "Factorial7",
    })
    public ExecutableProblem problem;

    @Param({"3"})
    public int optimisationLevel = -1;

    @Setup(Level.Trial)
    public final void setup() throws MetaborgException, IOException {
        initProgram();
    }

    @TearDown(Level.Trial)
    public final void teardown() throws MetaborgException, IOException {
        if (program != null) {
            program.cleanup();
        }
    }

    @Benchmark
    public final void compileStrj() throws IOException {
        program.compileStrj();
    }
}
