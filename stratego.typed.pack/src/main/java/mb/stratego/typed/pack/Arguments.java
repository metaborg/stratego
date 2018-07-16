package mb.stratego.typed.pack;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class Arguments {
    @Parameter(names = { "--help", "-h" }, description = "Shows usage help", required = false,
            help = true) public boolean help;


    @Parameter(names = { "--input-dir", "-i" }, description = "Directory with strategies to pack together", 
            required = true) public String inputDir;


    @Parameter(names = { "--output", "-o" }, description = "File to put the output into", 
            required = false) public String outputFile;


    @Parameter(names = { "--strategy-name", "-n" }, description = "The strategy name to use when packing the definition", 
            required = false) public String strategyName;


    @Parameter(names = { "--exit" }, description = "Immediately exit, used for testing purposes",
        hidden = true) public boolean exit;
}
