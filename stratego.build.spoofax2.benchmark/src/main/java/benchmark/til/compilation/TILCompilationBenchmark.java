package benchmark.til.compilation;

import api.til.TILProgram;
import benchmark.til.TILBenchmark;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.openjdk.jmh.annotations.*;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public abstract class TILCompilationBenchmark extends TILBenchmark {

    @Setup(Level.Iteration)
    public void prepareCompilation() throws MetaborgException {
        getProgram().compiler.setupBuild();
    }

    @Benchmark
    public final IStrategoTerm runTILCompiler() throws MetaborgException {
        return getProgram().compileTIL();
    }

    @Override
    @TearDown(Level.Iteration)
    public void teardown() {
        TILProgram p = getProgram();
        if (null != p)
            p.cleanup();
    }

}
