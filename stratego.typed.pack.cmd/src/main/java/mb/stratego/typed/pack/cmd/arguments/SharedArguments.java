package mb.stratego.typed.pack.cmd.arguments;

import java.nio.file.Path;

import com.beust.jcommander.Parameter;

public abstract class SharedArguments {
    @Parameter(names = { "--input-dir",
    "-i" }, description = "Directory with strategies to pack together", required = true)
    public Path inputDir;

    @Parameter(names = { "--output", "-o" }, description = "File to put the output into", required = false)
    public Path outputFile;
}
