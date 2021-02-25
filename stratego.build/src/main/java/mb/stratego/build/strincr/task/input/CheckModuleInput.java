package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.util.LastModified;

public interface CheckModuleInput extends Serializable {
    ResolveInput resolveInput();

    IModuleImportService.ModuleIdentifier moduleIdentifier();

    ArrayList<STask<?>> strFileGeneratingTasks();

    ArrayList<? extends ResourcePath> includeDirs();

    ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries();

    FrontInput frontInput();

    class Normal extends FrontInput.Normal implements CheckModuleInput {
        public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;

        public Normal(IModuleImportService.ModuleIdentifier moduleIdentifier,
            ArrayList<STask<?>> strFileGeneratingTasks,
            ArrayList<? extends ResourcePath> includeDirs,
            ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
            IModuleImportService.ModuleIdentifier mainModuleIdentifier) {
            super(moduleIdentifier, strFileGeneratingTasks, includeDirs, linkedLibraries);
            this.mainModuleIdentifier = mainModuleIdentifier;
        }

        @Override public ResolveInput resolveInput() {
            return new ResolveInput(mainModuleIdentifier, strFileGeneratingTasks, includeDirs,
                linkedLibraries);
        }

        @Override public IModuleImportService.ModuleIdentifier moduleIdentifier() {
            return moduleIdentifier;
        }

        @Override public ArrayList<STask<?>> strFileGeneratingTasks() {
            return strFileGeneratingTasks;
        }

        @Override public ArrayList<? extends ResourcePath> includeDirs() {
            return includeDirs;
        }

        @Override
        public ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries() {
            return linkedLibraries;
        }

        @Override public FrontInput frontInput() {
            return this;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            CheckModuleInput.Normal normal = (CheckModuleInput.Normal) o;

            return mainModuleIdentifier.equals(normal.mainModuleIdentifier);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + mainModuleIdentifier.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CheckModuleInput.Normal(" + moduleIdentifier + ")";
        }
    }

    class FileOpenInEditor extends FrontInput.FileOpenInEditor implements CheckModuleInput {
        public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;

        public FileOpenInEditor(IModuleImportService.ModuleIdentifier moduleIdentifier,
            ArrayList<STask<?>> strFileGeneratingTasks,
            ArrayList<? extends ResourcePath> includeDirs,
            ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
            LastModified<IStrategoTerm> ast,
            IModuleImportService.ModuleIdentifier mainModuleIdentifier) {
            super(moduleIdentifier, strFileGeneratingTasks, includeDirs, linkedLibraries, ast);
            this.mainModuleIdentifier = mainModuleIdentifier;
        }

        @Override public ResolveInput resolveInput() {
            return new ResolveInput(mainModuleIdentifier, strFileGeneratingTasks, includeDirs,
                linkedLibraries);
        }

        @Override public IModuleImportService.ModuleIdentifier moduleIdentifier() {
            return moduleIdentifier;
        }

        @Override public ArrayList<STask<?>> strFileGeneratingTasks() {
            return strFileGeneratingTasks;
        }

        @Override public ArrayList<? extends ResourcePath> includeDirs() {
            return includeDirs;
        }

        @Override
        public ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries() {
            return linkedLibraries;
        }

        @Override public FrontInput frontInput() {
            return this;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            CheckModuleInput.FileOpenInEditor that = (CheckModuleInput.FileOpenInEditor) o;

            return mainModuleIdentifier.equals(that.mainModuleIdentifier);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + mainModuleIdentifier.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CheckModuleInput.FileOpenInEditor(" + moduleIdentifier + ")";
        }
    }
}
