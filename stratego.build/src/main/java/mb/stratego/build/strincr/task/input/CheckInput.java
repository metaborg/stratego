package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.util.StrategoGradualSetting;

public class CheckInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final ResourcePath projectPath;
    public final StrategoGradualSetting strategoGradualSetting;
    public final ArrayList<STask<?>> strFileGeneratingTasks;
    public final ArrayList<? extends ResourcePath> includeDirs;
    public final ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries;

    public CheckInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ResourcePath projectPath, StrategoGradualSetting strategoGradualSetting, ArrayList<STask<?>> strFileGeneratingTasks,
        ArrayList<? extends ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries) {
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.projectPath = projectPath;
        this.strategoGradualSetting = strategoGradualSetting;
        this.strFileGeneratingTasks = strFileGeneratingTasks;
        this.includeDirs = includeDirs;
        this.linkedLibraries = linkedLibraries;
    }

    public ResolveInput resolveInput() {
        return new ResolveInput(mainModuleIdentifier, strFileGeneratingTasks,
            includeDirs, linkedLibraries);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckInput that = (CheckInput) o;

        if(strategoGradualSetting != that.strategoGradualSetting)
            return false;
        if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
            return false;
        if(!projectPath.equals(that.projectPath))
            return false;
        if(!strFileGeneratingTasks.equals(that.strFileGeneratingTasks))
            return false;
        if(!includeDirs.equals(that.includeDirs))
            return false;
        return linkedLibraries.equals(that.linkedLibraries);
    }

    @Override public int hashCode() {
        int result = mainModuleIdentifier.hashCode();
        result = 31 * result + projectPath.hashCode();
        result = 31 * result + strategoGradualSetting.hashCode();
        result = 31 * result + strFileGeneratingTasks.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + linkedLibraries.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Check.Input(" + mainModuleIdentifier + ")";
    }
}
