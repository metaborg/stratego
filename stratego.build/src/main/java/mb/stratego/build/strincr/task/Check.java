package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;


import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.ToModuleIdentifiersAndMessages;
import mb.stratego.build.strincr.function.output.ModuleIndentifiersAndMessages;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.task.input.CheckInput;
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

    @jakarta.inject.Inject public Check(Resolve resolve, CheckModule checkModule) {
        this.resolve = resolve;
        this.checkModule = checkModule;
    }

    @Override public CheckOutput exec(ExecContext context, CheckInput input) {
        final LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
            dynamicRuleIndex = new LinkedHashMap<>();
        final ArrayList<Message> messages = new ArrayList<>();
        final ModuleIndentifiersAndMessages moduleIndentifiersAndMessages = PieUtils
            .requirePartial(context, resolve, input.resolveInput(),
                ToModuleIdentifiersAndMessages.INSTANCE);

        for(IModuleImportService.ModuleIdentifier moduleIdentifier : moduleIndentifiersAndMessages.allModuleIdentifiers) {
            if(moduleIdentifier.isLibrary()) {
                continue;
            }
            final STask<CheckModuleOutput> sTask = checkModule.createSupplier(input.checkModuleInput(moduleIdentifier));
            final CheckModuleOutput output = context.require(sTask);
            for(StrategySignature strategySignature : output.dynamicRules.keySet()) {
                Relation.getOrInitialize(dynamicRuleIndex, strategySignature, LinkedHashSet::new)
                    .add(moduleIdentifier);
            }
            messages.addAll(output.messages);
        }

        messages.addAll(moduleIndentifiersAndMessages.messages);

        return new CheckOutput(dynamicRuleIndex, moduleIndentifiersAndMessages.allModuleIdentifiers, messages);
    }

    @Override public String getId() {
        return id;
    }
}
