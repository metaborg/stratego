package benchmark.stratego2.execution;

import benchmark.exception.SkipException;
import benchmark.stratego2.StrategoBenchmark;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
public abstract class StrategoExecutionBenchmark extends StrategoBenchmark {

    private ITermFactory termFactory = new TermFactory();
    private IStrategoTerm inputTerm;
    private String inputString;

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

    @Setup(Level.Trial)
    public final void setInput() {
        inputTerm = constructInput(termFactory);
        inputString = inputTerm.toString(Integer.MAX_VALUE);
    }

    protected abstract IStrategoTerm constructInput(ITermFactory termFactory);

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public final String run() throws IOException, InterruptedException {
        return getProgram().run(inputString);
    }

    @Override
    protected final String sourceFileName() {
        return problemFileName();
    }
}
