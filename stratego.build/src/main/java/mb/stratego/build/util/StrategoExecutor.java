package mb.stratego.build.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.output.NullOutputStream;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;

import mb.log.api.Logger;

public class StrategoExecutor {
    public static class ExecutionResult {
        public final boolean success;
        public final String outLog;
        public final String errLog;
        public final @Nullable IStrategoTerm result;
        public final @Nullable Exception exception;
        public final @Nullable List<String> strategoTrace;
        public final long time;

        public ExecutionResult(boolean success, String outLog, String errLog, @Nullable Exception exception,
            long time) {
            this(success, outLog, errLog, exception, time, null, null);
        }

        public ExecutionResult(boolean success, String outLog, String errLog, @Nullable Exception exception,
            long time, @Nullable List<String> strategoTrace) {
            this(success, outLog, errLog, exception, time, null, strategoTrace);
        }

        public ExecutionResult(boolean success, String outLog, String errLog, @Nullable Exception exception,
            long time, @Nullable IStrategoTerm result) {
            this(success, outLog, errLog, exception, time, result, null);
        }

        public ExecutionResult(boolean success, String outLog, String errLog, @Nullable Exception exception,
            long time, @Nullable IStrategoTerm result, @Nullable List<String> strategoTrace) {
            this.success = success;
            this.outLog = outLog;
            this.errLog = errLog;
            this.exception = exception;
            this.time = time;
            this.result = result;
            this.strategoTrace = strategoTrace;
        }
    }


    private static final ILogger log = LoggerUtils.logger("Build log");

    private @Nullable Context context;
    private @Nullable Strategy strategy;
    private @Nullable IOAgentTracker tracker;
    private @Nullable String name;
    private boolean silent;


    private void withContext(Context context) {
        this.context = context;
    }

    public StrategoExecutor withStrjContext(StrIncrContext c) {
        // strj requires a fresh context each time.
        withContext(org.strategoxt.strj.strj.init(c));
        return this;
    }

    public StrategoExecutor withStrategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public StrategoExecutor withTracker(IOAgentTracker tracker) {
        this.tracker = tracker;
        return this;
    }

    public StrategoExecutor withName(String name) {
        this.name = name;
        return this;
    }

    public StrategoExecutor setSilent(boolean silent) {
        this.silent = silent;
        return this;
    }


    public ExecutionResult executeCLI(Arguments arguments) {
        if(context == null) {
            throw new RuntimeException("Cannot execute Stratego strategy; context was not set");
        }
        if(strategy == null) {
            throw new RuntimeException("Cannot execute Stratego strategy; strategy or strategy name was not set");
        }
        if(tracker == null) {
            throw new RuntimeException("Cannot execute Stratego strategy; tracker was not set");
        }
        if(name == null) {
            name = strategy.getName();
        }

        if(!silent) {
            log.info("Execute {} {}", name, arguments);
        }
        context.setIOAgent(tracker.agent());
        final String[] args = getArgumentStrings(arguments);
        final long start = System.nanoTime();
        try {
            context.invokeStrategyCLI(strategy, name, args);
            final long time = System.nanoTime() - start;
            return new ExecutionResult(true, tracker.stdout(), tracker.stderr(), null, time);
        } catch(StrategoExit e) {
            final long time = System.nanoTime() - start;
            if(e.getValue() == 0) {
                return new ExecutionResult(true, tracker.stdout(), tracker.stderr(), e, time);
            }
            context.popOnExit(false);
            if(!silent) {
                log.error("Executing {} failed: {}", name, e);
            }
            return new ExecutionResult(false, tracker.stdout(), tracker.stderr(), e, time);
        } finally {
            final @Nullable IOperatorRegistry registry = context.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
            if(registry != null) {
                final SSLLibrary sslLibrary = (SSLLibrary) registry;
                sslLibrary.getDynamicRuleTable().clear();
                sslLibrary.getTableTable().clear();
            }
        }
    }


    private String[] getArgumentStrings(Arguments arguments) {
        List<String> strings = arguments.asStrings(null);
        return strings.toArray(new String[0]);
    }

    public static ExecutionResult runLocallyUniqueStringStrategy(IOAgentTrackerFactory agentTrackerFactory,
        Logger logger, boolean silent, Strategy strategy, IStrategoTerm input, StrIncrContext strContext) {
        return runLocallyUniqueStringStrategy(logger, silent,
            newResourceTracker(agentTrackerFactory, new File(System.getProperty("user.dir")), silent), strategy, input, strContext);
    }

    public static IOAgentTracker newResourceTracker(IOAgentTrackerFactory agentTrackerFactory, File baseFile, boolean silent, String... excludePatterns) {
        final IOAgentTracker tracker;
        if(silent) {
            tracker = agentTrackerFactory.create(baseFile, new NullOutputStream(), new NullOutputStream());
        } else {
            tracker = agentTrackerFactory.create(baseFile, excludePatterns);
        }
        return tracker;
    }

    public static ExecutionResult runLocallyUniqueStringStrategy(Logger logger, boolean silent,
        @Nullable IOAgentTracker tracker, Strategy strategy, IStrategoTerm input, StrIncrContext strContext) {
        strContext.resetUsedStringsInFactory();

        final String name = strategy.getName();

        final ITermFactory factory = strContext.getFactory();
        if(tracker != null) {
            strContext.setIOAgent(tracker.agent());
        }
        dr_scope_all_start_0_0.instance.invoke(strContext, factory.makeTuple());

        final long start = System.nanoTime();
        try {
            // We don't use StackSaver because we do not expect that the strategy invoked here will be more recursive
            //  than the already generous stack limit.
            final IStrategoTerm result = strategy.invoke(strContext, input);
            final long time = System.nanoTime() - start;
            if(!silent && result == null) {
                logger.error("Executing " + name + " failed with normal Stratego failure. ");
            } else if(result == null) {
                logger.debug("Executing " + name + " failed with normal Stratego failure. ");
            }
            final String stdout;
            final String stderr;
            if(tracker != null) {
                stdout = tracker.stdout();
                stderr = tracker.stderr();
            } else {
                stdout = "";
                stderr = "";
            }
            return new ExecutionResult(result != null, stdout, stderr, null, time, result, Arrays.asList(strContext.getTrace()));
        } catch(StrategoExit e) {
            final long time = System.nanoTime() - start;
            if(e.getValue() == 0) {
                final String stdout;
                final String stderr;
                if(tracker != null) {
                    stdout = tracker.stdout();
                    stderr = tracker.stderr();
                } else {
                    stdout = "";
                    stderr = "";
                }
                return new ExecutionResult(true, stdout, stderr, e, time, Arrays.asList(strContext.getTrace()));
            }
            strContext.popOnExit(false);
            if(!silent) {
                logger.error("Executing " + name + " failed: ", e);
            } else {
                logger.debug("Executing " + name + " failed: " + e);
            }
            final String stdout;
            final String stderr;
            if(tracker != null) {
                stdout = tracker.stdout();
                stderr = tracker.stderr();
            } else {
                stdout = "";
                stderr = "";
            }
            return new ExecutionResult(false, stdout, stderr, e, time, Arrays.asList(strContext.getTrace()));
        } finally {
            dr_scope_all_end_0_0.instance.invoke(strContext, factory.makeTuple());
        }
    }
}
