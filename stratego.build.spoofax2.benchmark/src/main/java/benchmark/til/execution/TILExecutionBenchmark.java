package benchmark.til.execution;

import api.til.TILProgram;
import benchmark.til.TILBenchmark;

import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public abstract class TILExecutionBenchmark extends TILBenchmark {

    private Collection<String> inputStrings;

    @Setup(Level.Trial)
    public final void setInput() {
        inputStrings = input();
    }

    protected abstract Collection<String> input();

    @Benchmark
    public final String run() throws MetaborgException {
        return getProgram().run(inputStrings);
    }

    @Override
    @TearDown(Level.Trial)
    public final void teardown() {
        TILProgram p = getProgram();
        if (null != p)
            p.cleanup();
    }

}
