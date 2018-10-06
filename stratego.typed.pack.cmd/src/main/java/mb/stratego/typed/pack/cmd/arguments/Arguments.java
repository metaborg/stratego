package mb.stratego.typed.pack.cmd.arguments;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class Arguments {
    private static final ILogger logger = LoggerUtils.logger(Arguments.class);

    public final SingleStrategy singleStrategy;
    public final Boilerplate boilerplate;
    private SharedArguments parsedCommandArguments;

    public Arguments(SingleStrategy singleStrategy, Boilerplate boilerplate) {
        this.singleStrategy = singleStrategy;
        this.boilerplate = boilerplate;
    }

    void setParsedCommandArguments(String command) {
        switch(command) {
            case "single":
                parsedCommandArguments = singleStrategy;
                break;
            case "boilerplate":
                parsedCommandArguments = boilerplate;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public SharedArguments getParsedCommandArguments() {
        return parsedCommandArguments;
    }

    public static Arguments parse(String... args) {
        JCommander jc = new JCommander();

        SingleStrategy singleStrategy = new SingleStrategy();
        Boilerplate boilerplate = new Boilerplate();
        Arguments arguments = new Arguments(singleStrategy, boilerplate);

        jc.addObject(arguments);
        jc.addCommand("single", singleStrategy);
        jc.addCommand("boilerplate", boilerplate);

        try {
            jc.parse(args);
        } catch(ParameterException e) {
            logger.error("Could not parse parameters", e);
            jc.usage();
            System.exit(1);
        }

        if(arguments.help) {
            jc.usage();
            System.exit(0);
        }

        if(arguments.exit) {
            logger.info("Exiting immediately for testing purposes");
            System.exit(0);
        }

        String command = jc.getParsedCommand();
        if(command == null) {
            jc.usage();
            System.exit(1);
        }

        arguments.setParsedCommandArguments(command);

        return arguments;
    }

    @Parameter(names = { "--help", "-h" }, description = "Shows usage help", required = false, help = true)
    public boolean help;

    @Parameter(names = { "--exit" }, description = "Immediately exit, used for testing purposes", hidden = true)
    public boolean exit;
}
