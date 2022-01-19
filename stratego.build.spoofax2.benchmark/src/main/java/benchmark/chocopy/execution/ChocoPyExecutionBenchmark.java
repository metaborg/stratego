package benchmark.chocopy.execution;

import api.chocopy.ChocoPyProgram;
import benchmark.chocopy.ChocoPyBenchmark;
import benchmark.exception.SkipException;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public abstract class ChocoPyExecutionBenchmark extends ChocoPyBenchmark {

    @Setup(Level.Iteration)
    public final void compile() {
        try {
            getProgram().compileChocoPy();
        } catch (MetaborgException e) {
            throw new SkipException("Setup failed!", e);
        }
    }

    @Benchmark
    public final String runPrettyPrintRISCV() throws MetaborgException {
        return getProgram().run();
    }

    @Override
    @TearDown(Level.Trial)
    public final void teardown() {
        ChocoPyProgram p = getProgram();
        if (null != p)
            p.cleanup();
    }

}
