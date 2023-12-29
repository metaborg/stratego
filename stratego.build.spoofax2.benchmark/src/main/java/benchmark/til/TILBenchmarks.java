package benchmark.til;

import api.til.TILProgram;
import benchmark.exception.SkipException;
import org.metaborg.core.MetaborgException;
import benchmark.til.problems.ExecutableTILProblem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class TILBenchmarks {
    public ExecutableTILProblem problem;
    public TILProgram program;
    public int optimisationLevel;

    public abstract void teardown() throws MetaborgException, IOException;

    public final void initProgram() {
        Path sourcePath = Paths.get("src", "main", "resources", "til", problem.name + ".til");
        String MetaborgVersion = "2.6.0-SNAPSHOT";
        try {
            program = new TILProgram(sourcePath, optimisationLevel, MetaborgVersion);
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
