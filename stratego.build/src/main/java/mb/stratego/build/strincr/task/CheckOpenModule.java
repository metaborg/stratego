package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import javax.annotation.Nullable;
import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.data.GTEnvironment;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.output.CheckOpenModuleOutput;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.util.InsertCastsInput;
import mb.stratego.build.util.InsertCastsOutput;

public class CheckOpenModule implements TaskDef<CheckModuleInput, CheckOpenModuleOutput> {
    public static final String id = "stratego." + CheckOpenModule.class.getSimpleName();

    private final CheckModule checkModule;
    private final Front front;
    private final ResourcePathConverter resourcePathConverter;

    @Inject public CheckOpenModule(CheckModule checkModule, Front front,
        ResourcePathConverter resourcePathConverter) {
        this.checkModule = checkModule;
        this.front = front;
        this.resourcePathConverter = resourcePathConverter;
    }

    @Override public CheckOpenModuleOutput exec(ExecContext context, CheckModuleInput input)
        throws Exception {
        if(input.frontInput.moduleIdentifier.isLibrary()) {
            return new CheckOpenModuleOutput(null, new ArrayList<>(0));
        }

        final @Nullable ModuleData moduleData = context.require(front, input.frontInput);
        assert moduleData != null;

        final IModuleImportService.ModuleIdentifier moduleIdentifier =
            input.frontInput.moduleIdentifier;

        final LinkedHashSet<StrategySignature> moduleDefinitions = new LinkedHashSet<>();
        final GTEnvironment environment =
            checkModule.prepareGTEnvironment(context, moduleData, input.frontInput,
                moduleDefinitions);
        final InsertCastsInput insertCastsInput =
            new InsertCastsInput(moduleIdentifier, input.projectPath, environment);
        final String projectPath = resourcePathConverter.toString(input.projectPath);
        final InsertCastsOutput output = checkModule.insertCasts(insertCastsInput, projectPath, context.logger());

        final ArrayList<Message> messages =
            new ArrayList<>(moduleData.messages.size() + output.messages.size());
        messages.addAll(moduleData.messages);
        messages.addAll(output.messages);

        checkModule
            .otherChecks(context, input.resolveInput(), moduleData, messages,
                projectPath);

        return new CheckOpenModuleOutput(output.astWithCasts, messages);
    }

    @Override public String getId() {
        return id;
    }
}
