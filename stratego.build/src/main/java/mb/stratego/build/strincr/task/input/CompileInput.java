package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;

public class CompileInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final IModuleImportService moduleImportService;
    public final ResourcePath outputDir;
    public final @Nullable String packageName;
    public final @Nullable ResourcePath cacheDir;
    public final List<String> constants;
    public final Collection<ResourcePath> includeDirs;
    public final Arguments extraArgs;
    public final Collection<STask<?>> strFileGeneratingTasks;

    public CompileInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
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

        CompileInput input = (CompileInput) o;

        if(!mainModuleIdentifier.equals(input.mainModuleIdentifier))
            return false;
        if(!moduleImportService.equals(input.moduleImportService))
            return false;
        if(!outputDir.equals(input.outputDir))
            return false;
        if(packageName != null ? !packageName.equals(input.packageName) : input.packageName != null)
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
