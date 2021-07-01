import api.Stratego2Program;
import org.metaborg.util.cmd.Arguments;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Compile {
    String filename;
    Stratego2Program program;

    Path p = Paths.get("src", "main", "resources");

    private int optimisationLevel = 2;

    public Compile(String filename) {
        this.filename = filename;
    }

    public static void main(String... args) throws Exception {
        String filename = args[0];

        Compile c = new Compile(filename);
        c.loadProgram();

        c.program.compileStratego();
        c.program.compileJava();

        BufferedReader br = c.program.run();
        String line;
        while (null != (line = br.readLine())) {
            System.out.println(line);
        }

        c.program.cleanup();
    }

    private void loadProgram() throws IOException {
        Arguments str2Args = new Arguments();
        str2Args.add("-O", optimisationLevel);
        str2Args.add("--statistics", 1);
        str2Args.add("--verbose", 10);

        program = new Stratego2Program(p.resolve(filename), "2.6.0-SNAPSHOT", str2Args);
    }
}
