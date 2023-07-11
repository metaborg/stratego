package benchmark.til;

import api.til.TILProgram;
import org.metaborg.core.MetaborgException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

class Compile {
    final String filename;
    TILProgram program;

    final Path p = Paths.get("src", "main", "resources");

    private static final int optimisationLevel = 2;

    public Compile(String filename) {
        this.filename = filename;
    }

    public static void main(String... args) throws Exception {
        String filename = args[0];

        long start, elapsed;

        Compile c = new Compile(filename);
        System.out.println("Loading program...");
        c.loadProgram();

        for (int i = 0; i < 5; i++) {
            System.out.print("Compiling TIL...");
            c.program.compiler.setupBuild();
            start = System.currentTimeMillis();
            c.program.compileTIL();
            elapsed = System.currentTimeMillis() - start;
            c.program.compiler.cleanup();
            System.out.printf(" (%f s)%n", elapsed / 1000.f);
        }

        start = System.currentTimeMillis();
        String res = c.program.run(Collections.emptyList());
        elapsed = System.currentTimeMillis() - start;

        System.out.println(res);
        System.out.printf("Time elapsed: %f s%n", elapsed / 1000.f);

        c.program.cleanup();
    }

    private void loadProgram() throws IOException, MetaborgException {
        program = new TILProgram(p.resolve(filename), optimisationLevel, "2.6.0-SNAPSHOT");
    }
}
