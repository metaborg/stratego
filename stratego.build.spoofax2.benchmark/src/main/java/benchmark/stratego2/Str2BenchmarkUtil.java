package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import benchmark.exception.SkipException;
import benchmark.stratego2.problems.ExecutableStr2Problem;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Str2BenchmarkUtil {
    public static Stratego2Program initProgram(ExecutableStr2Problem problem, int optimisationLevel) {
        Path sourcePath = Paths.get("src", "main", "resources", "stratego2", problem.name + ".str2");
        String MetaborgVersion = "2.6.0-SNAPSHOT";
        Arguments args = new Arguments();
        args.add("-O", optimisationLevel);
        args.add("-sc", "on");
        try {
            return new Stratego2Program(sourcePath, args, MetaborgVersion);
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
        throw new SkipException("Failed to init program! Skipping.");
    }
}
