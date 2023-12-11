package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class Compile {
    final String filename;
    Stratego2Program program;

    final Path p = Paths.get("src", "main", "resources");

    private static final int optimisationLevel = 2;
    private final Arguments compilerArgs;

    public Compile(String filename) {
        this.filename = filename;

        compilerArgs = new Arguments();
        compilerArgs.add("-O", optimisationLevel);
        compilerArgs.add("--statistics", 1);
        compilerArgs.add("--verbose", 10);
    }

    public static void main(String... args) throws Exception {
        String filename = args[0];

        Compile c = new Compile(filename);
        System.out.println("Loading program...");
        c.loadProgram();

        System.out.println("Compiling Stratego...");
        c.program.compileStratego();
        c.program.compileJava();

        long start = System.currentTimeMillis();
        String res = c.program.run("");
        long elapsed = System.currentTimeMillis() - start;

        System.out.println(res);
        System.out.printf("Time elapsed: %f s%n", elapsed / 1000.f);

//        System.out.printf("Size of Java directory: %d kB%n", FileUtils.sizeOfDirectory(c.program.javaDir) / 1000);
//        System.out.printf("Size of classes directory: %d kB%n", FileUtils.sizeOfDirectory(c.program.classDir) / 1000);
//        System.out.printf("Java bytes: %d%n", Files.walk(c.program.javaDir.toPath()).mapToLong(f -> f.toFile().length()).sum() / 1000);
//        System.out.printf("Class bytes: %d%n", Files.walk(c.program.classDir.toPath()).mapToLong(f -> f.toFile().length()).sum() / 1000);

        c.program.cleanup();
    }

    private void loadProgram() throws IOException, MetaborgException {
        program = new Stratego2Program(p.resolve(filename), compilerArgs, false, true, "2.6.0-SNAPSHOT", false);
    }
}
