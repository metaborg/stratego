package benchmark.stratego2.template;

import api.Stratego2Program;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.net.URISyntaxException;

@State(Scope.Benchmark)
public class OptimisationBenchmark {

    public Stratego2Program program;
    public Arguments str2Args = new Arguments();

    @Param({"2", "4"})
    public int optimisationLevel;

    @Setup(Level.Trial)
    public void setup() throws URISyntaxException, IOException, MetaborgException { }

    @TearDown(Level.Trial)
    public void teardown() throws IOException { }
}
