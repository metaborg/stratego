package mb.stratego.build.strincr.task.input;

import java.io.Serializable;

import javax.annotation.Nullable;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;

public class CheckModuleInput implements Serializable {
    public final FrontInput frontInput;
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final ResourcePath projectPath;

    public CheckModuleInput(FrontInput frontInput,
        IModuleImportService.ModuleIdentifier mainModuleIdentifier, ResourcePath projectPath) {
        this.frontInput = frontInput;
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.projectPath = projectPath;
    }

    public ResolveInput resolveInput() {
        final @Nullable FrontInput.FileOpenInEditor fileOpenInEditor;
        if(frontInput instanceof FrontInput.FileOpenInEditor) {
            fileOpenInEditor = (FrontInput.FileOpenInEditor) frontInput;
        } else {
            fileOpenInEditor = null;
        }
        return new ResolveInput(mainModuleIdentifier, frontInput.strFileGeneratingTasks,
            frontInput.includeDirs, frontInput.linkedLibraries, fileOpenInEditor);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckModuleInput that = (CheckModuleInput) o;

        if(!frontInput.equals(that.frontInput))
            return false;
        if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
            return false;
        return projectPath.equals(that.projectPath);
    }

    @Override public int hashCode() {
        int result = frontInput.hashCode();
        result = 31 * result + mainModuleIdentifier.hashCode();
        result = 31 * result + projectPath.hashCode();
        return result;
    }

    @Override public String toString() {
        return "CheckModuleInput(" + frontInput + ", " + mainModuleIdentifier + ')';
    }
}
