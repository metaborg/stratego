import api.Stratego2Program;
import org.apache.commons.io.FileUtils;
import org.metaborg.util.cmd.Arguments;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Compile {
    String filename;
    Stratego2Program program;

    Path p = Paths.get("src", "main", "resources");

    private final static int optimisationLevel = 2;

    public Compile(String filename) {
        this.filename = filename;
    }

    public static void main(String... args) throws Exception {
        String filename = args[0];

        Compile c = new Compile(filename);
        System.out.println("Loading program...");
        c.loadProgram();

        System.out.println("Compiling Stratego...");
        c.program.compileStratego();
        System.out.println("Compiling Java...");
        c.program.compileJava();

        System.out.println("Done!");

        BufferedReader br = c.program.run();
        String line;
        while (null != (line = br.readLine())) {
            System.out.println(line);
        }

        System.out.printf("Size of Java directory: %d kB%n", FileUtils.sizeOfDirectory(c.program.javaDir) / 1000);
        System.out.printf("Size of classes directory: %d kB%n", FileUtils.sizeOfDirectory(c.program.classDir) / 1000);
        System.out.printf("Java bytes: %d%n", Files.walk(c.program.javaDir.toPath()).mapToLong(f -> f.toFile().length()).sum() / 1000);
        System.out.printf("Class bytes: %d%n", Files.walk(c.program.classDir.toPath()).mapToLong(f -> f.toFile().length()).sum() / 1000);

        c.program.cleanup();
    }

    private void loadProgram() throws IOException {
        Arguments args = new Arguments();
        args.add("-O", optimisationLevel);
        args.add("--statistics", 1);
        args.add("--verbose", 10);

        program = new Stratego2Program(p.resolve(filename), "2.6.0-SNAPSHOT", args);
    }
}
