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
    public final boolean autoImportStd;

    public CheckInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ResourcePath projectPath, StrategoGradualSetting strategoGradualSetting,
        ArrayList<STask<?>> strFileGeneratingTasks, ArrayList<? extends ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
        boolean autoImportStd) {
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.projectPath = projectPath;
        this.strategoGradualSetting = strategoGradualSetting;
        this.strFileGeneratingTasks = strFileGeneratingTasks;
        this.includeDirs = includeDirs;
        this.linkedLibraries = linkedLibraries;
        this.autoImportStd = autoImportStd;
    }

    public ResolveInput resolveInput() {
        return new ResolveInput(mainModuleIdentifier, strFileGeneratingTasks, includeDirs,
            linkedLibraries, autoImportStd);
    }

    public CheckModuleInput checkModuleInput(
        IModuleImportService.ModuleIdentifier moduleIdentifier) {
        return new CheckModuleInput(
            new FrontInput.Normal(moduleIdentifier, strFileGeneratingTasks, includeDirs,
                linkedLibraries, autoImportStd), mainModuleIdentifier, projectPath);
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
        if(!linkedLibraries.equals(that.linkedLibraries))
            return false;
        return autoImportStd == that.autoImportStd;
    }

    @Override public int hashCode() {
        int result = mainModuleIdentifier.hashCode();
        result = 31 * result + projectPath.hashCode();
        result = 31 * result + strategoGradualSetting.hashCode();
        result = 31 * result + strFileGeneratingTasks.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + linkedLibraries.hashCode();
        result = 31 * result + (autoImportStd ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "Check.Input(" + mainModuleIdentifier + ")";
    }
}
