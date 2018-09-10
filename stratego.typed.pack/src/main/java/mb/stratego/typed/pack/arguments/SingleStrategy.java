package mb.stratego.typed.pack.arguments;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Pack strategy bodies into a CTree with one strategy definition")
public class SingleStrategy extends SharedArguments {
    @Parameter(names = { "--strategy-name",
            "-n" }, description = "The strategy name to use when packing the definition", required = false)
    public String strategyName;
}