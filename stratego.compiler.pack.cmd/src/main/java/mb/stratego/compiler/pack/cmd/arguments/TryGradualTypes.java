package mb.stratego.compiler.pack.cmd.arguments;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.nio.file.Path;

@Parameters(separators = "=", commandDescription = "Type-check a CTree file according to the gradual type system")
public class TryGradualTypes {
    @Parameter(names = { "--input", "-i" }, description = "The CTree file to type-check", required = true) public Path
        inputFile;

    @Parameter(names = { "--output", "-o" }, description = "The resulting CTree with casts inserted", required = true)
    public Path outputFile;
}
