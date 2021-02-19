package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.strc.insert_casts_0_0;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoExecutor;

// TODO: should this be a separate task or be inlined in CheckModule?
public class InsertCasts implements TaskDef<InsertCasts.Input, InsertCasts.Output> {
    public static final String id = "stratego." + InsertCasts.class.getSimpleName();

    public static final class Input implements Serializable {
        public final ModuleIdentifier moduleIdentifier;
        public final GTEnvironment environment;

        public Input(ModuleIdentifier moduleIdentifier, GTEnvironment environment) {
            this.moduleIdentifier = moduleIdentifier;
            this.environment = environment;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            Input input = (Input) o;

            if(!moduleIdentifier.equals(input.moduleIdentifier))
                return false;
            return environment.equals(input.environment);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + moduleIdentifier.hashCode();
            result = 31 * result + environment.hashCode();
            return result;
        }

        @Override public String toString() {
            return "InsertCasts.Input2(" + moduleIdentifier + ")";
        }
    }

    public static final class Output implements Serializable {
        public final IStrategoTerm astWithCasts;
        public final List<Message<?>> messages;

        public Output(IStrategoTerm astWithCasts, List<Message<?>> messages) {
            this.astWithCasts = astWithCasts;
            this.messages = messages;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(getClass() != o.getClass())
                return false;
            Output output = (Output) o;
            return astWithCasts.equals(output.astWithCasts) && messages.equals(output.messages);
        }

        @Override public int hashCode() {
            return Objects.hash(astWithCasts, messages);
        }
    }

    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;

    @Inject
    public InsertCasts(IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext) {
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
    }


    @Override public Output exec(ExecContext execContext, Input input)
        throws ExecException {
        final String moduleName = input.moduleIdentifier.moduleString();

        final StrategoExecutor.ExecutionResult output = StrategoExecutor
            .runLocallyUniqueStringStrategy(ioAgentTrackerFactory, execContext.logger(), true,
                insert_casts_0_0.instance, input.environment, strContext);
        if(!output.success) {
            throw new ExecException(
                "Call to insert_casts failed on " + moduleName + ": \n" + output.exception);
        }
        assert output.result != null;

        final IStrategoTerm astWithCasts = output.result.getSubterm(0);
        final IStrategoList errors = TermUtils.toListAt(output.result, 1);
        final IStrategoList warnings = TermUtils.toListAt(output.result, 2);
        final IStrategoList notes = TermUtils.toListAt(output.result, 3);

        List<Message<?>> messages = new ArrayList<>(errors.size() + warnings.size() + notes.size());
        for(IStrategoTerm errorTerm : errors) {
            messages.add(Message
                .from(execContext.logger(), moduleName, errorTerm, MessageSeverity.ERROR));
        }
        for(IStrategoTerm warningTerm : warnings) {
            messages.add(Message.from(execContext.logger(), moduleName, warningTerm,
                MessageSeverity.WARNING));
        }
        for(IStrategoTerm noteTerm : notes) {
            messages.add(Message
                .from(execContext.logger(), moduleName, noteTerm, MessageSeverity.NOTE));
        }
        return new Output(astWithCasts, messages);
    }

    @Override public String getId() {
        return id;
    }

}
