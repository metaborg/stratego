package mb.stratego.build.strincr.task.input;

import java.io.Serializable;

import mb.stratego.build.strincr.IModuleImportService;

public class CheckModuleInput implements Serializable {
    public final FrontInput frontInput;
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;

    public CheckModuleInput(FrontInput frontInput,
        IModuleImportService.ModuleIdentifier mainModuleIdentifier) {
        this.frontInput = frontInput;
        this.mainModuleIdentifier = mainModuleIdentifier;
    }

    public ResolveInput resolveInput() {
        return new ResolveInput(mainModuleIdentifier, frontInput.strFileGeneratingTasks,
            frontInput.includeDirs, frontInput.linkedLibraries);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckModuleInput that = (CheckModuleInput) o;

        if(!frontInput.equals(that.frontInput))
            return false;
        return mainModuleIdentifier.equals(that.mainModuleIdentifier);
    }

    @Override public int hashCode() {
        int result = frontInput.hashCode();
        result = 31 * result + mainModuleIdentifier.hashCode();
        return result;
    }

    @Override public String toString() {
        return "CheckModuleInput(" + frontInput + ", " + mainModuleIdentifier + ')';
    }
}
