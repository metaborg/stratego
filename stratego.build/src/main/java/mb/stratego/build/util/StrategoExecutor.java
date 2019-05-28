package mb.stratego.build.util;

import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;
import javax.annotation.Nullable;
import java.util.List;

public class StrategoExecutor {
    public static class ExecutionResult {
        public final boolean success;
        public final String outLog;
        public final String errLog;
        public final @Nullable IStrategoTerm result;
        public final @Nullable Exception exception;

        public ExecutionResult(boolean success, String outLog, String errLog, @Nullable Exception exception) {
            this.success = success;
            this.outLog = outLog;
            this.errLog = errLog;
            this.exception = exception;
            this.result = null;
        }
    }


    private static final ILogger log = LoggerUtils.logger("Build log");

    private @Nullable Context context;
    private @Nullable Strategy strategy;
    private @Nullable ResourceAgentTracker tracker;
    private @Nullable String name;
    private boolean silent;


    private void withContext(Context context) {
        this.context = context;
    }

    public StrategoExecutor withStrjContext() {
        // strj requires a fresh context each time.
        withContext(org.strategoxt.strj.strj.init());
        return this;
    }

    public StrategoExecutor withStrategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public StrategoExecutor withTracker(ResourceAgentTracker tracker) {
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

        ExecutionResult result;
        ret:
        {
            try {
                if(!silent) {
                    log.info("Execute {} {}", name, arguments);
                }
                context.setIOAgent(tracker.agent());
                dr_scope_all_start_0_0.instance.invoke(context, context.getFactory().makeTuple());
                final String[] args = getArgumentStrings(arguments);
                context.invokeStrategyCLI(strategy, name, args);
                result = new ExecutionResult(true, tracker.stdout(), tracker.stderr(), null);
            } catch(StrategoExit e) {
                if(e.getValue() == 0) {
                    result = new ExecutionResult(true, tracker.stdout(), tracker.stderr(), e);
                    break ret;
                }
                context.popOnExit(false);
                if(!silent) {
                    log.error("Executing {} failed: {}", name, e);
                }
                result = new ExecutionResult(false, tracker.stdout(), tracker.stderr(), e);
            } finally {
                dr_scope_all_end_0_0.instance.invoke(context, context.getFactory().makeTuple());
            }
        }
        return result;
    }


    private String[] getArgumentStrings(Arguments arguments) {
        List<String> strings = arguments.asStrings(null);
        return strings.toArray(new String[strings.size()]);
    }
}
