package mb.stratego.typed.pack;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
    private static final ILogger logger = LoggerUtils.logger(Main.class);

    public static void main(String[] args) {
        final Arguments arguments = new Arguments();
        final JCommander jc = new JCommander(arguments);

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

        try {
            Path dir = Paths.get(arguments.dir).toAbsolutePath();
            String strategyName = (arguments.strategyName != null) ? arguments.strategyName : dir.getName(dir.getNameCount()-1).toString();

            Packer.pack(dir, strategyName);
        } catch(Exception e) {
            logger.error("Error during packing of strategy", e);
            System.exit(1);
        }
    }
}
