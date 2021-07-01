package benchmark.stratego2.template.benchmark;

import api.Stratego2Program;
import benchmark.exception.SkipException;
import benchmark.stratego2.template.problem.Problem;
import org.metaborg.util.cmd.Arguments;
import org.openjdk.jmh.annotations.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("JmhInspections")
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
public abstract class BaseBenchmark implements Problem {

    private Path sourcePath;

    private Stratego2Program program;
    private Arguments str2Args = new Arguments();

    @Param({"2.6.0-SNAPSHOT"})
    public String metaborgVersion;

    @Param({"2"})
    public int optimisationLevel;

    @Param({"-1"})
    public int problemSize;

    @Setup(Level.Iteration)
    public void setup() throws SkipException {
        sourcePath = Paths.get("src", "main", "resources", sourceFileName());

        str2Args.add("-O", optimisationLevel);
//        str2Args.add("--statistics", 1);
//        str2Args.add("--verbose", 3);

        try {
            program = new Stratego2Program(sourcePath, metaborgVersion, str2Args);
        } catch (FileNotFoundException e) {
            throw new SkipException(String.format("Benchmark problem file %s does not exist! Skipping.", sourcePath), e);
        } catch (IOException e) {
            throw new SkipException("Exception while creating temporary intermediate directory! Skipping.", e);
        }
    }

    public Stratego2Program getProgram() {
        return program;
    }

    public String sourceFileName() {
        return String.format(problemFileNamePattern(), problemSize);
    }

}
