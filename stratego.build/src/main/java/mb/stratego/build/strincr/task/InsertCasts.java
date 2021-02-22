package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.strc.insert_casts_0_0;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.task.input.InsertCastsInput;
import mb.stratego.build.strincr.task.output.InsertCastsOutput;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoExecutor;

// TODO: should this be a separate task or be inlined in CheckModule?
public class InsertCasts implements TaskDef<InsertCastsInput, InsertCastsOutput> {
    public static final String id = "stratego." + InsertCasts.class.getSimpleName();

    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;

    @Inject
    public InsertCasts(IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext) {
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
    }


    @Override public InsertCastsOutput exec(ExecContext execContext, InsertCastsInput input)
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

        final long lastModified = input.environment.lastModified;
        List<Message<?>> messages = new ArrayList<>(errors.size() + warnings.size() + notes.size());
        for(IStrategoTerm errorTerm : errors) {
            messages.add(
                Message.from(execContext.logger(), errorTerm, MessageSeverity.ERROR, lastModified));
        }
        for(IStrategoTerm warningTerm : warnings) {
            messages.add(Message
                .from(execContext.logger(), warningTerm, MessageSeverity.WARNING, lastModified));
        }
        for(IStrategoTerm noteTerm : notes) {
            messages.add(
                Message.from(execContext.logger(), noteTerm, MessageSeverity.NOTE, lastModified));
        }
        return new InsertCastsOutput(astWithCasts, messages);
    }

    @Override public String getId() {
        return id;
    }

}
