package benchmark.stratego2.template.benchmark;

import benchmark.exception.SkipException;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.SingleShotTime)
public abstract class ExecutionBenchmark extends OptimisationBenchmark {

    @Setup(Level.Trial)
    public void compile() throws MetaborgException, IOException, SkipException {
        setup();
        getProgram().compileStratego();
        getProgram().compileJava();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    final public BufferedReader run() throws Exception {
        return getProgram().run();
    }
}
