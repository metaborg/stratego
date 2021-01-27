package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.metaborg.util.cmd.Arguments;

import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.util.PieUtils;

public class Compile implements TaskDef<Compile.Input, Compile.Output> {
    public static final String id = Compile.class.getCanonicalName();

    public static class Input implements Serializable {
        public final ModuleIdentifier mainModuleIdentifier;
        public final IModuleImportService moduleImportService;
        public final ResourcePath outputDir;
        public final String packageName;
        public final @Nullable ResourcePath cacheDir;
        public final List<String> constants;
        public final Collection<ResourcePath> includeDirs;
        public final Arguments extraArgs;

        public Input(ModuleIdentifier mainModuleIdentifier,
            IModuleImportService moduleImportService, ResourcePath outputDir, String packageName,
            @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs) {
            this.mainModuleIdentifier = mainModuleIdentifier;
            this.moduleImportService = moduleImportService;
            this.outputDir = outputDir;
            this.packageName = packageName;
            this.cacheDir = cacheDir;
            this.constants = constants;
            this.includeDirs = includeDirs;
            this.extraArgs = extraArgs;
        }
    }

    public static class Output implements Serializable {
        public final List<ResourcePath> resultFiles;

        public Output(List<ResourcePath> resultFiles) {
            this.resultFiles = resultFiles;
        }
    }

    public final Resolve resolve;
    public final Back back;

    @Inject public Compile(Resolve resolve, Back back) {
        this.resolve = resolve;
        this.back = back;
    }

    @Override public Output exec(ExecContext context, Input input) {
        final List<ResourcePath> resultFiles = new ArrayList<>();
        final Task<GlobalData> resolveTask = resolve
            .createTask(new Check.Input(input.mainModuleIdentifier, input.moduleImportService));
        final GlobalIndex globalIndex =
            PieUtils.requirePartial(context, resolveTask, GlobalData.ToGlobalIndex.Instance);

        for(StrategySignature strategySignature : globalIndex.strategies) {
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

        return new Output(resultFiles);
    }

    @Override public String getId() {
        return id;
    }
}
