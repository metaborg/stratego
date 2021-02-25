package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.AllModulesIdentifiers;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.type.TypeMessage;
import mb.stratego.build.strincr.task.input.CheckInput;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.task.output.CheckOutput;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;

/**
 * Runs {@link Resolve}, gets the list of all modules, then runs {@link CheckModule} on each module.
 * This task can be used to get all error messages for a project.
 */
public class Check implements TaskDef<CheckInput, CheckOutput> {
    public static final String id = "stratego." + Check.class.getSimpleName();

    public final Resolve resolve;
    public final CheckModule checkModule;

    @Inject public Check(Resolve resolve, CheckModule checkModule) {
        this.resolve = resolve;
        this.checkModule = checkModule;
    }

    @Override public CheckOutput exec(ExecContext context, CheckInput input) {
        final HashMap<IModuleImportService.ModuleIdentifier, STask<CheckModuleOutput>>
            moduleCheckTasks = new HashMap<>();
        final HashMap<StrategySignature, HashSet<IModuleImportService.ModuleIdentifier>>
            strategyIndex = new HashMap<>();
        final HashMap<StrategySignature, HashSet<IModuleImportService.ModuleIdentifier>>
            dynamicRuleIndex = new HashMap<>();
        final ArrayList<Message> messages = new ArrayList<>();
        boolean containsErrors = false;
        final HashSet<IModuleImportService.ModuleIdentifier> allModulesIdentifiers = PieUtils
            .requirePartial(context, resolve, input.resolveInput(), AllModulesIdentifiers.INSTANCE);

        for(IModuleImportService.ModuleIdentifier moduleIdentifier : allModulesIdentifiers) {
            if(moduleIdentifier.isLibrary()) {
                continue;
            }
            final STask<CheckModuleOutput> sTask = checkModule.createSupplier(
                new CheckModuleInput.Normal(input.mainModuleIdentifier, moduleIdentifier,
                    input.strFileGeneratingTasks, input.includeDirs));
            moduleCheckTasks.put(moduleIdentifier, sTask);
            final CheckModuleOutput output = context.require(sTask);
            for(StrategySignature strategySignature : output.strategyDataWithCasts.keySet()) {
                Relation.getOrInitialize(strategyIndex, strategySignature, HashSet::new)
                    .add(moduleIdentifier);
            }
            for(StrategySignature strategySignature : output.dynamicRules.keySet()) {
                Relation.getOrInitialize(dynamicRuleIndex, strategySignature, HashSet::new)
                    .add(moduleIdentifier);
            }
            if(input.ignoreTypeMessages) {
                for(Message message : output.messages) {
                    if(!(message instanceof TypeMessage)) {
                        messages.add(message);
                        containsErrors |= message.severity == MessageSeverity.ERROR;
                    }
                }
            } else {
                for(Message message : output.messages) {
                    messages.add(message);
                    containsErrors |= message.severity == MessageSeverity.ERROR;
                }
            }
        }

        return new CheckOutput(moduleCheckTasks, strategyIndex, dynamicRuleIndex, messages, containsErrors);
    }

    @Override public String getId() {
        return id;
    }
}
