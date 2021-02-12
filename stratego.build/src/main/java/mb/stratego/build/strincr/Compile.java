package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.metaborg.util.cmd.Arguments;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message2;
import mb.stratego.build.util.PieUtils;

public class Compile implements TaskDef<Compile.Input, Compile.Output> {
    public static final String id = Compile.class.getCanonicalName();

    public static class Input implements Serializable {
        public final ModuleIdentifier mainModuleIdentifier;
        public final IModuleImportService moduleImportService;
        public final ResourcePath outputDir;
        public final @Nullable String packageName;
        public final @Nullable ResourcePath cacheDir;
        public final List<String> constants;
        public final Collection<ResourcePath> includeDirs;
        public final Arguments extraArgs;
        public final Collection<STask<?>> strFileGeneratingTasks;

        public Input(ModuleIdentifier mainModuleIdentifier,
            IModuleImportService moduleImportService, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs,
            Collection<STask<?>> strFileGeneratingTasks) {
            this.mainModuleIdentifier = mainModuleIdentifier;
            this.moduleImportService = moduleImportService;
            this.outputDir = outputDir;
            this.packageName = packageName;
            this.cacheDir = cacheDir;
            this.constants = constants;
            this.includeDirs = includeDirs;
            this.extraArgs = extraArgs;
            this.strFileGeneratingTasks = strFileGeneratingTasks;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!mainModuleIdentifier.equals(input.mainModuleIdentifier))
                return false;
            if(!moduleImportService.equals(input.moduleImportService))
                return false;
            if(!outputDir.equals(input.outputDir))
                return false;
            if(packageName != null ? !packageName.equals(input.packageName) :
                input.packageName != null)
                return false;
            if(cacheDir != null ? !cacheDir.equals(input.cacheDir) : input.cacheDir != null)
                return false;
            if(!constants.equals(input.constants))
                return false;
            if(!includeDirs.equals(input.includeDirs))
                return false;
            if(!extraArgs.equals(input.extraArgs))
                return false;
            return strFileGeneratingTasks.equals(input.strFileGeneratingTasks);
        }

        @Override public int hashCode() {
            int result = mainModuleIdentifier.hashCode();
            result = 31 * result + moduleImportService.hashCode();
            result = 31 * result + outputDir.hashCode();
            result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
            result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
            result = 31 * result + constants.hashCode();
            result = 31 * result + includeDirs.hashCode();
            result = 31 * result + extraArgs.hashCode();
            result = 31 * result + strFileGeneratingTasks.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Compile.Input(" + mainModuleIdentifier + ", " + moduleImportService + ")";
        }
    }

    public interface Output extends Serializable {
        String toString();
    }

    public static class Success implements Output {
        public final Set<ResourcePath> resultFiles;

        public Success(Set<ResourcePath> resultFiles) {
            this.resultFiles = resultFiles;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Success output = (Success) o;

            return resultFiles.equals(output.resultFiles);
        }

        @Override public int hashCode() {
            return resultFiles.hashCode();
        }

        @Override public String toString() {
            return "Compile.Success(" + resultFiles.size() + ")";
        }
    }

    public static class Failure implements Output {
        public final List<Message2<?>> messages;

        public Failure(List<Message2<?>> messages) {
            this.messages = messages;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Failure output = (Failure) o;

            return messages.equals(output.messages);
        }

        @Override public int hashCode() {
            return messages.hashCode();
        }

        @Override public String toString() {
            return "Compile.Failure(" + messages.size() + ")";
        }
    }

    public final Resolve resolve;
    public final Check check;
    public final Back back;

    @Inject public Compile(Resolve resolve, Check check, Back back) {
        this.resolve = resolve;
        this.check = check;
        this.back = back;
    }

    @Override public Output exec(ExecContext context, Input input) {
        final Check.Input checkInput =
            new Check.Input(input.mainModuleIdentifier, input.moduleImportService);
        final Check.Output.Messages checkOutput =
            PieUtils.requirePartial(context, check, checkInput, Check.Output.GetMessages.INSTANCE);
        if(checkOutput.containsErrors) {
            return new Compile.Failure(checkOutput.messages);
        }

        final Set<ResourcePath> resultFiles = new HashSet<>();
        final STask<GlobalData> resolveTask = resolve.createSupplier(checkInput);
        final GlobalIndex globalIndex =
            PieUtils.requirePartial(context, resolveTask, GlobalData.ToGlobalIndex.INSTANCE);

        for(StrategySignature strategySignature : globalIndex.nonExternalStrategies) {
            final Back.Output output = context.require(back,
                new Back.NormalInput(strategySignature, input.outputDir, input.packageName,
                    input.cacheDir, input.constants, input.includeDirs, input.extraArgs,
                    resolveTask, input.mainModuleIdentifier, input.moduleImportService));
            assert output != null;
            resultFiles.addAll(output.resultFiles);
        }
        final Back.Output boilerplateOutput = context.require(back,
            new Back.BoilerplateInput(resolveTask, input.outputDir, input.packageName,
                input.cacheDir, input.constants, input.includeDirs, input.extraArgs));
        assert boilerplateOutput != null;
        resultFiles.addAll(boilerplateOutput.resultFiles);

        final Back.Output congruenceOutput = context.require(back,
            new Back.CongruenceInput(resolveTask, input.outputDir, input.packageName,
                input.cacheDir, input.constants, input.includeDirs, input.extraArgs));
        assert congruenceOutput != null;
        resultFiles.addAll(congruenceOutput.resultFiles);

        return new Success(resultFiles);
    }

    @Override public String getId() {
        return id;
    }
}
