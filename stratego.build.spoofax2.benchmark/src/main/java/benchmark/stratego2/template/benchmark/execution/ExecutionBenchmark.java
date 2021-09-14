package benchmark.stratego2.template.benchmark.execution;

import benchmark.exception.SkipException;
import benchmark.stratego2.template.benchmark.base.OptimisationBenchmark;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.SingleShotTime)
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public abstract class ExecutionBenchmark extends OptimisationBenchmark {

    @Setup(Level.Trial)
    public final void compile() throws MetaborgException, IOException, SkipException {
        getProgram().compileStratego();
        getProgram().compileJava();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public final BufferedReader run() throws Exception {
        return getProgram().run();
    }
}
