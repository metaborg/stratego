package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.util.LastModified;

public abstract class FrontInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final ArrayList<STask<?>> strFileGeneratingTasks;
    public final ArrayList<? extends ResourcePath> includeDirs;
    public final ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries;

    public FrontInput(IModuleImportService.ModuleIdentifier moduleIdentifier,
        ArrayList<STask<?>> strFileGeneratingTasks, ArrayList<? extends ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries) {
        this.moduleIdentifier = moduleIdentifier;
        this.strFileGeneratingTasks = strFileGeneratingTasks;
        this.includeDirs = includeDirs;
        this.linkedLibraries = linkedLibraries;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        FrontInput that = (FrontInput) o;

        if(!moduleIdentifier.equals(that.moduleIdentifier))
            return false;
        if(!strFileGeneratingTasks.equals(that.strFileGeneratingTasks))
            return false;
        if(!includeDirs.equals(that.includeDirs))
            return false;
        return linkedLibraries.equals(that.linkedLibraries);
    }

    @Override public int hashCode() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + strFileGeneratingTasks.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + linkedLibraries.hashCode();
        return result;
    }

    @Override public String toString() {
        return "FrontInput." + this.getClass().getSimpleName() + "(" + moduleIdentifier + ")";
    }

    public static class Normal extends FrontInput {
        public Normal(IModuleImportService.ModuleIdentifier moduleIdentifier,
            ArrayList<STask<?>> strFileGeneratingTasks, ArrayList<? extends ResourcePath> includeDirs,
            ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries) {
            super(moduleIdentifier, strFileGeneratingTasks, includeDirs, linkedLibraries);
        }
    }

    public static class FileOpenInEditor extends FrontInput {
        public final LastModified<IStrategoTerm> ast;

        public FileOpenInEditor(IModuleImportService.ModuleIdentifier moduleIdentifier,
            ArrayList<STask<?>> strFileGeneratingTasks, ArrayList<? extends ResourcePath> includeDirs,
            ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
            LastModified<IStrategoTerm> ast) {
            super(moduleIdentifier, strFileGeneratingTasks, includeDirs, linkedLibraries);
            this.ast = ast;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            FileOpenInEditor that = (FileOpenInEditor) o;

            return ast.equals(that.ast);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + ast.hashCode();
            return result;
        }
    }
}
