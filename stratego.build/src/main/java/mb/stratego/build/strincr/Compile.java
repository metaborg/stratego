package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.metaborg.util.cmd.Arguments;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.Resource;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;

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
        public final List<Resource> resultFiles;

        public Output(List<Resource> resultFiles) {
            this.resultFiles = resultFiles;
        }
    }

    public final Resolve resolve;

    @Inject public Compile(Resolve resolve) {
        this.resolve = resolve;
    }

    @Override public Output exec(ExecContext context, Input input) {
        final List<Resource> resultFiles = new ArrayList<>();
        final GlobalData gd = context.require(resolve,
            new Check.Input(input.mainModuleIdentifier, input.moduleImportService));
        return new Output(resultFiles);
    }

    @Override public String getId() {
        return id;
    }
}