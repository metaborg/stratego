import api.Stratego2Program;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Compile {
    String filename;
    Stratego2Program program;

    Path p = Paths.get("src", "main", "resources");

    public Compile(String filename) {
        this.filename = filename;
    }

    public static void main(String... args) throws Exception {
        String filename = "bubblesort10.str2";

        Compile c = new Compile(filename);
        c.loadProgram();

        c.program.compileStratego();
        c.program.compileJava();

        c.program.run();

//        c.program.cleanup();
    }

    private void loadProgram() throws FileNotFoundException {
        program = new Stratego2Program(p.resolve(filename), "2.6.0-SNAPSHOT");
    }
}
