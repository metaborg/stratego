package benchmark.chocopy.compilation;

import api.chocopy.ChocoPyProgram;
import benchmark.chocopy.ChocoPyBenchmark;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.openjdk.jmh.annotations.*;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public abstract class ChocoPyCompilationBenchmark extends ChocoPyBenchmark {

    @Setup(Level.Iteration)
    public void prepareCompilation() throws MetaborgException {
        getProgram().compiler.setupBuild();
    }

    @Benchmark
    public final Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> compileChocoPy() throws MetaborgException {
        return getProgram().compileChocoPy();
    }

    @Override
    @TearDown(Level.Iteration)
    public void teardown() {
        ChocoPyProgram p = getProgram();
        if (null != p)
            p.cleanup();
    }

}
