package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.util.StrategoGradualSetting;

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
    public final StrategoGradualSetting strategoGradualSetting;

    public CompileInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        IModuleImportService moduleImportService, ResourcePath outputDir,
        @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
        Collection<ResourcePath> includeDirs, Arguments extraArgs,
        Collection<STask<?>> strFileGeneratingTasks, StrategoGradualSetting strategoGradualSetting) {
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.moduleImportService = moduleImportService;
        this.outputDir = outputDir;
        this.packageName = packageName;
        this.cacheDir = cacheDir;
        this.constants = constants;
        this.includeDirs = includeDirs;
        this.extraArgs = extraArgs;
        this.strFileGeneratingTasks = strFileGeneratingTasks;
        this.strategoGradualSetting = strategoGradualSetting;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CompileInput that = (CompileInput) o;

        if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
            return false;
        if(!moduleImportService.equals(that.moduleImportService))
            return false;
        if(!outputDir.equals(that.outputDir))
            return false;
        if(packageName != null ? !packageName.equals(that.packageName) : that.packageName != null)
            return false;
        if(cacheDir != null ? !cacheDir.equals(that.cacheDir) : that.cacheDir != null)
            return false;
        if(!constants.equals(that.constants))
            return false;
        if(!includeDirs.equals(that.includeDirs))
            return false;
        if(!extraArgs.equals(that.extraArgs))
            return false;
        if(!strFileGeneratingTasks.equals(that.strFileGeneratingTasks))
            return false;
        return strategoGradualSetting == that.strategoGradualSetting;
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
        result = 31 * result + strategoGradualSetting.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Compile.Input(" + mainModuleIdentifier + ", " + moduleImportService + ")";
    }
}
