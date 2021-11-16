package benchmark.chocopy;

import api.chocopy.ChocoPyProgram;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class Compile {
    final String filename;
    ChocoPyProgram program;

    final Path p = Paths.get("src", "main", "resources");

    private static final int optimisationLevel = 2;

    public Compile(String filename) {
        this.filename = filename;
    }

    public static void main(String... args) throws Exception {
        String filename = args[0];

        Compile c = new Compile(filename);
        System.out.println("Loading program...");
        c.loadProgram();

        System.out.println("Compiling Stratego...");
        c.program.compileChocoPy();

        long start = System.currentTimeMillis();
        String res = c.program.run();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println(res);
        System.out.printf("Time elapsed: %f s%n", elapsed / 1000.f);

        c.program.cleanup();
    }

    private void loadProgram() throws IOException, MetaborgException {
        program = new ChocoPyProgram(p.resolve(filename), optimisationLevel, "2.6.0-SNAPSHOT");
    }
}
