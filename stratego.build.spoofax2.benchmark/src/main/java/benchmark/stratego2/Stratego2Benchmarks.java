package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import benchmark.exception.SkipException;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;
import benchmark.stratego2.problems.ExecutableProblem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Stratego2Benchmarks {
    public ExecutableProblem problem;
    public Stratego2Program program;
    public int optimisationLevel;

    public abstract void setup() throws MetaborgException, IOException;
    public abstract void teardown() throws MetaborgException, IOException;

    public final void initProgram() {
        Path sourcePath = Paths.get("src", "main", "resources", "stratego2", problem.name + ".str2");
        String MetaborgVersion = "2.6.0-SNAPSHOT";
        Arguments args = new Arguments();
        args.add("-O", optimisationLevel);
        args.add("-sc", "on");
        try {
            program = new Stratego2Program(sourcePath, args, MetaborgVersion);
        } catch (Exception e) {
            System.out.println("****ERROR INITIALIZING PROGRAM****");
            if (e instanceof FileNotFoundException) {
                throw new SkipException(String.format("Benchmark problem file %s does not exist! Skipping.", sourcePath), e);
            } else if (e instanceof IOException) {
                throw new SkipException("Exception while creating temporary intermediate directory! Skipping.", e);
            } else if (e instanceof MetaborgException) {
                throw new SkipException("Exception in build system! Skipping.", e);
            }
        }
    }
}
