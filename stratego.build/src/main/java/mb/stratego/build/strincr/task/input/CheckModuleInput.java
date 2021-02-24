package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.util.LastModified;

public interface CheckModuleInput extends Serializable {
    ResolveInput resolveInput();
    ModuleIdentifier moduleIdentifier();
    Collection<STask<?>> strFileGeneratingTasks();
    Collection<? extends ResourcePath> includeDirs();
    FrontInput frontInput();

    class Normal extends FrontInput.Normal implements CheckModuleInput {
        public final ModuleIdentifier mainModuleIdentifier;
        public final Collection<? extends ResourcePath> includeDirs;

        public Normal(ModuleIdentifier mainModuleIdentifier, ModuleIdentifier moduleIdentifier,
            Collection<STask<?>> strFileGeneratingTasks,
            Collection<? extends ResourcePath> includeDirs) {
            super(moduleIdentifier, strFileGeneratingTasks);
            this.mainModuleIdentifier = mainModuleIdentifier;
            this.includeDirs = includeDirs;
        }

        @Override public ResolveInput resolveInput() {
            return new ResolveInput(mainModuleIdentifier, strFileGeneratingTasks, includeDirs);
        }

        @Override public ModuleIdentifier moduleIdentifier() {
            return moduleIdentifier;
        }

        @Override public Collection<STask<?>> strFileGeneratingTasks() {
            return strFileGeneratingTasks;
        }

        @Override public Collection<? extends ResourcePath> includeDirs() {
            return includeDirs;
        }

        @Override public FrontInput frontInput() {
            return this;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            CheckModuleInput.Normal that = (CheckModuleInput.Normal) o;

            if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
                return false;
            return includeDirs.equals(that.includeDirs);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + mainModuleIdentifier.hashCode();
            result = 31 * result + includeDirs.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CheckModuleInput.Normal(" + moduleIdentifier + ")";
        }
    }

    class FileOpenInEditor extends FrontInput.FileOpenInEditor implements CheckModuleInput {
        public final ModuleIdentifier mainModuleIdentifier;
        public final Collection<STask<?>> strFileGeneratingTasks;
        public final Collection<? extends ResourcePath> includeDirs;

        public FileOpenInEditor(ModuleIdentifier moduleIdentifier, LastModified<IStrategoTerm> ast,
            ModuleIdentifier mainModuleIdentifier, Collection<STask<?>> strFileGeneratingTasks,
            Collection<? extends ResourcePath> includeDirs) {
            super(moduleIdentifier, ast);
            this.mainModuleIdentifier = mainModuleIdentifier;
            this.strFileGeneratingTasks = strFileGeneratingTasks;
            this.includeDirs = includeDirs;
        }

        @Override public ResolveInput resolveInput() {
            return new ResolveInput(mainModuleIdentifier, strFileGeneratingTasks, includeDirs);
        }

        @Override public ModuleIdentifier moduleIdentifier() {
            return moduleIdentifier;
        }

        @Override public Collection<STask<?>> strFileGeneratingTasks() {
            return strFileGeneratingTasks;
        }

        @Override public Collection<? extends ResourcePath> includeDirs() {
            return includeDirs;
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

            if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
                return false;
            if(!strFileGeneratingTasks.equals(that.strFileGeneratingTasks))
                return false;
            return includeDirs.equals(that.includeDirs);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + mainModuleIdentifier.hashCode();
            result = 31 * result + strFileGeneratingTasks.hashCode();
            result = 31 * result + includeDirs.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CheckModuleInput.FileOpenInEditor(" + moduleIdentifier + ")";
        }
    }
}
