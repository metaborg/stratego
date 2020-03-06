package mb.stratego.compiler.pack.cmd;

import mb.pie.api.Logger;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.compiler.pack.Packer;
import mb.stratego.compiler.pack.cmd.arguments.Arguments;
import mb.stratego.compiler.pack.cmd.arguments.Boilerplate;
import mb.stratego.compiler.pack.cmd.arguments.SingleStrategy;
import mb.stratego.compiler.pack.cmd.arguments.TryGradualTypes;

import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Main {
    private static final ILogger logger = LoggerUtils.logger(Main.class);
    private static final Logger pieLogger = new Logger() {
        @Override
        public void error(String message, Throwable throwable) {
            logger.error(message, throwable);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            logger.warn(message, throwable);
        }

        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void debug(String message) {
            logger.debug(message);
        }

        @Override
        public void trace(String message) {
            logger.trace(message);
        }
    };
    private static final OpenOption[] openOptions =
        new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };

    public static void main(String[] args) {
        final Arguments arguments = Arguments.parse(args);

        try {
            Object commandArgs = arguments.getParsedCommandArguments();
            if(commandArgs instanceof SingleStrategy) {
                final SingleStrategy ssArgs = (SingleStrategy) commandArgs;
                final Path inputDir = ssArgs.inputDir.toAbsolutePath();
                final String strategyName;

                if(ssArgs.strategyName != null) {
                    strategyName = ssArgs.strategyName;
                } else {
                    strategyName = inputDir.getName(inputDir.getNameCount() - 1).toString();
                }

                Packer.packStrategy(inputDir, ssArgs.outputFile, strategyName);
            } else if(commandArgs instanceof Boilerplate) {
                final Boilerplate bArgs = (Boilerplate) commandArgs;
                final Path inputDir = bArgs.inputDir.toAbsolutePath();

                Packer.packBoilerplate(inputDir, bArgs.outputFile);
            } else if(commandArgs instanceof TryGradualTypes) {
                final TryGradualTypes tgtArgs = (TryGradualTypes) commandArgs;

                try(Spoofax s = new Spoofax()) {
                    final StrIncrContext strIncrContext = s.injector.getInstance(StrIncrContext.class);
                    final IResourceService resourceService = s.injector.getInstance(IResourceService.class);
                    final GradualTypesRunner gtr = new GradualTypesRunner(strIncrContext, resourceService);

                    final ITermFactory factory = strIncrContext.getFactory();
                    final IStrategoTerm ast =
                        factory.parseFromString(new String(Files.readAllBytes(tgtArgs.inputFile)));

                    final IStrategoTerm result = gtr.exec(pieLogger, ast);

                    result.writeAsString(Files.newBufferedWriter(tgtArgs.outputFile.toAbsolutePath(), openOptions),
                        Integer.MAX_VALUE);
                }
            } else {
                throw new IllegalArgumentException("Unknown command parsed");
            }
        } catch(Exception e) {
            logger.error("Error during packing of strategy", e);
            System.exit(1);
        }
    }
}
