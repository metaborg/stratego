package benchmark.stratego2.execution;

import benchmark.exception.SkipException;
import benchmark.stratego2.StrategoBenchmark;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public abstract class StrategoExecutionBenchmark extends StrategoBenchmark {

    /**
     * @throws MetaborgException
     * @throws IOException
     * @throws SkipException
     */
    @Setup(Level.Trial)
    public final void compile() throws MetaborgException, IOException {
        getProgram().compileStratego();
        getProgram().compileJava();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public final String run() throws Exception {
        return getProgram().run();
    }
}
