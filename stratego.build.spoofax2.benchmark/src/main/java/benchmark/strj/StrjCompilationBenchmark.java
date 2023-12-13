package benchmark.strj;

import benchmark.exception.SkipException;
import benchmark.stratego2.compilation.stratego.StrategoCompilationBenchmark;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import java.io.IOException;
import java.nio.file.Paths;

public abstract class StrjCompilationBenchmark extends StrategoCompilationBenchmark {

    @Param({"on"})
    public String sharedConstructors;

    /**
     * @throws SkipException
     */
    @Override
    @Setup(Level.Trial)
    public final void setup() {
        sourcePath = Paths.get("src", "main", "resources", sourceFileName());

        args.add("-O", optimisationLevel);
        args.add("-sc", sharedConstructors);
//        args.add("--fusion", fusion);
//        args.add("--statistics", 1);
//        args.add("--verbose", 3);

        try {
            instantiateProgram();
        } catch (IOException | MetaborgException e) {
            handleException(e);
        }
    }

    @Benchmark
    public final boolean compileStrj() throws IOException {
        return getProgram().compileStrj();
    }

}
