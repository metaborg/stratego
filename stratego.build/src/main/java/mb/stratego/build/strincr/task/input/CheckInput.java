package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;

public class CheckInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final boolean ignoreTypeMessages;
    public final ArrayList<STask<?>> strFileGeneratingTasks;
    public final ArrayList<? extends ResourcePath> includeDirs;

    public CheckInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        boolean ignoreTypeMessages, ArrayList<STask<?>> strFileGeneratingTasks,
        ArrayList<? extends ResourcePath> includeDirs) {
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.ignoreTypeMessages = ignoreTypeMessages;
        this.strFileGeneratingTasks = strFileGeneratingTasks;
        this.includeDirs = includeDirs;
    }

    public ResolveInput resolveInput() {
        return new ResolveInput(mainModuleIdentifier, strFileGeneratingTasks,
            includeDirs);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckInput that = (CheckInput) o;

        if(ignoreTypeMessages != that.ignoreTypeMessages)
            return false;
        if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
            return false;
        if(!strFileGeneratingTasks.equals(that.strFileGeneratingTasks))
            return false;
        return includeDirs.equals(that.includeDirs);
    }

    @Override public int hashCode() {
        int result = mainModuleIdentifier.hashCode();
        result = 31 * result + (ignoreTypeMessages ? 1 : 0);
        result = 31 * result + strFileGeneratingTasks.hashCode();
        result = 31 * result + includeDirs.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Check.Input(" + mainModuleIdentifier + ")";
    }
}
