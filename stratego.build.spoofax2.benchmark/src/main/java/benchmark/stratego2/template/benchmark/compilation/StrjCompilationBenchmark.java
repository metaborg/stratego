package benchmark.stratego2.template.benchmark.compilation;

import api.Stratego2Program;
import benchmark.exception.SkipException;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

public abstract class StrjCompilationBenchmark extends CompilationBenchmark {

    @Param({""})
    public String switchImplementation;

    @Param({""})
    public String switchImplementationOrder;

    @Param({"on"})
    public String sharedConstructors;

    @Override
    @Setup(Level.Trial)
    public final void setup() throws SkipException {
        sourcePath = Paths.get("src", "main", "resources", sourceFileName());

        args.add("-O", optimisationLevel);
        args.add("-sc", sharedConstructors);
//        args.add("--fusion", fusion);
//        args.add("--statistics", 1);
//        args.add("--verbose", 3);

        try {
            program = new Stratego2Program(sourcePath, args, metaborgVersion);
        } catch (FileNotFoundException e) {
            throw new SkipException(String.format("Benchmark problem file %s does not exist! Skipping.", sourcePath), e);
        } catch (IOException e) {
            throw new SkipException("Exception while creating temporary intermediate directory! Skipping.", e);
        } catch (MetaborgException e) {
            throw new SkipException("Exception in build system! Skipping.", e);
        }
    }

    @Benchmark
    public final boolean compileStrj() throws IOException {
        return getProgram().compileStrj();
    }

}
