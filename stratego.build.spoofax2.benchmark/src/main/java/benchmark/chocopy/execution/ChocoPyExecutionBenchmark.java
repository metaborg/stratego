package benchmark.chocopy.execution;

import api.chocopy.ChocoPyProgram;
import benchmark.chocopy.ChocoPyBenchmark;
import benchmark.exception.SkipException;
import benchmark.generic.Program;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.openjdk.jmh.annotations.*;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
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
    public final String prettyPrint() throws MetaborgException {
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
