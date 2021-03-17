package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;

public class ResolveInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final ArrayList<STask<?>> strFileGeneratingTasks;
    public final ArrayList<? extends ResourcePath> includeDirs;
    public final ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries;
    public final @Nullable FrontInput.FileOpenInEditor fileOpenInEditor;

    public ResolveInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ArrayList<STask<?>> strFileGeneratingTasks, ArrayList<? extends ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
        @Nullable FrontInput.FileOpenInEditor fileOpenInEditor) {
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.strFileGeneratingTasks = strFileGeneratingTasks;
        this.includeDirs = includeDirs;
        this.linkedLibraries = linkedLibraries;
        this.fileOpenInEditor = fileOpenInEditor;
    }

    public ResolveInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ArrayList<STask<?>> strFileGeneratingTasks, ArrayList<? extends ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries) {
        this(mainModuleIdentifier, strFileGeneratingTasks, includeDirs, linkedLibraries, null);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ResolveInput that = (ResolveInput) o;

        if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
            return false;
        if(!strFileGeneratingTasks.equals(that.strFileGeneratingTasks))
            return false;
        if(!includeDirs.equals(that.includeDirs))
            return false;
        if(!linkedLibraries.equals(that.linkedLibraries))
            return false;
        return fileOpenInEditor != null ? fileOpenInEditor.equals(that.fileOpenInEditor) :
            that.fileOpenInEditor == null;
    }

    @Override public int hashCode() {
        int result = mainModuleIdentifier.hashCode();
        result = 31 * result + strFileGeneratingTasks.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + linkedLibraries.hashCode();
        result = 31 * result + (fileOpenInEditor != null ? fileOpenInEditor.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "Resolve.Input(" + mainModuleIdentifier + ")";
    }
}
