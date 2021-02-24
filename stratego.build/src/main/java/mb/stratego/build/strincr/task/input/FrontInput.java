package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.STask;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.util.LastModified;

public abstract class FrontInput implements Serializable {
    public final ModuleIdentifier moduleIdentifier;

    public FrontInput(ModuleIdentifier moduleIdentifier) {
        this.moduleIdentifier = moduleIdentifier;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        FrontInput that = (FrontInput) o;

        return moduleIdentifier.equals(that.moduleIdentifier);
    }

    @Override public int hashCode() {
        return moduleIdentifier.hashCode();
    }

    @Override public String toString() {
        return "FrontInput." + this.getClass().getSimpleName() + "(" + moduleIdentifier + ")";
    }

    public static class Normal extends FrontInput {
        public final ArrayList<STask<?>> strFileGeneratingTasks;

        public Normal(ModuleIdentifier moduleIdentifier,
            ArrayList<STask<?>> strFileGeneratingTasks) {
            super(moduleIdentifier);
            this.strFileGeneratingTasks = strFileGeneratingTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            Normal normal = (Normal) o;

            return strFileGeneratingTasks.equals(normal.strFileGeneratingTasks);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + strFileGeneratingTasks.hashCode();
            return result;
        }
    }

    public static class FileOpenInEditor extends FrontInput {
        public final LastModified<IStrategoTerm> ast;

        public FileOpenInEditor(ModuleIdentifier moduleIdentifier,
            LastModified<IStrategoTerm> ast) {
            super(moduleIdentifier);
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
