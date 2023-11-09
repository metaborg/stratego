package mb.stratego.build.strincr.task.input;

import java.io.Serializable;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportService.ImportResolutionInfo;
import mb.stratego.build.util.LastModified;

public abstract class FrontInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final ImportResolutionInfo importResolutionInfo;
    public final boolean autoImportStd;
    protected final int hashCode;

    public FrontInput(IModuleImportService.ModuleIdentifier moduleIdentifier,
        ImportResolutionInfo importResolutionInfo, boolean autoImportStd) {
        this.moduleIdentifier = moduleIdentifier;
        this.importResolutionInfo = importResolutionInfo;
        this.autoImportStd = autoImportStd;
        this.hashCode = hashFunction();
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        FrontInput that = (FrontInput) o;

        if(hashCode != that.hashCode)
            return false;
        if(autoImportStd != that.autoImportStd)
            return false;
        if(!moduleIdentifier.equals(that.moduleIdentifier))
            return false;
        return importResolutionInfo.equals(that.importResolutionInfo);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + importResolutionInfo.hashCode();
        result = 31 * result + (autoImportStd ? 1 : 0);
        return result;
    }

    @Override public abstract String toString();

    public static class Normal extends FrontInput {
        public Normal(IModuleImportService.ModuleIdentifier moduleIdentifier,
            ImportResolutionInfo importResolutionInfo, boolean autoImportStd) {
            super(moduleIdentifier, importResolutionInfo, autoImportStd);
        }

        @Override public String toString() {
            //@formatter:off
            return "FrontInput.Normal@" + System.identityHashCode(this) + '{'
                + "moduleIdentifier=" + moduleIdentifier
                + ", importResolutionInfo=" + importResolutionInfo
                + ", autoImportStd=" + autoImportStd
                + '}';
            //@formatter:on
        }
    }

    public static class FileOpenInEditor extends FrontInput {
        public final LastModified<IStrategoTerm> ast;

        public FileOpenInEditor(IModuleImportService.ModuleIdentifier moduleIdentifier,
            ImportResolutionInfo importResolutionInfo, LastModified<IStrategoTerm> ast,
            boolean autoImportStd) {
            super(moduleIdentifier, importResolutionInfo, autoImportStd);
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

        @Override public String toString() {
            //@formatter:off
            return "FrontInput.FileOpenInEditor@" + System.identityHashCode(this) + '{'
                + "moduleIdentifier=" + moduleIdentifier
                + ", importResolutionInfo=" + importResolutionInfo
                + ", autoImportStd=" + autoImportStd
                + ", ast=" + ast
                + '}';
            //@formatter:on
        }

        public FrontInput withoutOpenFile() {
            return new FrontInput.Normal(moduleIdentifier, importResolutionInfo, autoImportStd);
        }
    }
}
