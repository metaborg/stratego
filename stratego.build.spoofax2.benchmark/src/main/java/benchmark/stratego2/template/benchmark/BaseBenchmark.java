package benchmark.stratego2.template.benchmark;

import api.Stratego2Program;
import benchmark.exception.SkipException;
import benchmark.stratego2.template.problem.Problem;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;
import org.openjdk.jmh.annotations.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("JmhInspections")
@State(Scope.Thread)
@Fork(jvmArgsPrepend = {"--add-opens=java.base/java.lang=ALL-UNNAMED", "-Xss16M", "-Xms2G", "-Xmx2G"})
@OutputTimeUnit(TimeUnit.SECONDS)
public abstract class BaseBenchmark implements Problem {

    private Path sourcePath;

    private Stratego2Program program;
    protected Arguments args = new Arguments();

    @Param({"2.6.0-SNAPSHOT"})
    public String metaborgVersion;

    @Param({"2"})
    public int optimisationLevel;

    @Param({"-1"})
    public int problemSize;

//    @Param({"on", "off"})
//    public String fusion;

    @Setup(Level.Trial)
    public final void setup() throws SkipException {
        sourcePath = Paths.get("src", "main", "resources", sourceFileName());

        args.add("-O", optimisationLevel);
//        args.add("--fusion", fusion);
        args.add("--statistics", 1);
        args.add("--verbose", 3);

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

    @TearDown(Level.Trial)
    public final void teardown() throws IOException {
        getProgram().cleanup();
    }

    public Stratego2Program getProgram() {
        return program;
    }

    public String sourceFileName() {
        return String.format(problemFileNamePattern(), problemSize);
    }

}
