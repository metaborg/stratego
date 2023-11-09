package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.Objects;

import jakarta.annotation.Nullable;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportService.ImportResolutionInfo;

public class ResolveInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final ImportResolutionInfo importResolutionInfo;
    public final @Nullable FrontInput.FileOpenInEditor fileOpenInEditor;
    public final boolean autoImportStd;
    protected final int hashCode;

    public ResolveInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ImportResolutionInfo importResolutionInfo,
        @Nullable FrontInput.FileOpenInEditor fileOpenInEditor, boolean autoImportStd) {
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.importResolutionInfo = importResolutionInfo;
        this.fileOpenInEditor = fileOpenInEditor;
        this.autoImportStd = autoImportStd;
        this.hashCode = hashFunction();
    }

    public ResolveInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ImportResolutionInfo importResolutionInfo, boolean autoImportStd) {
        this(mainModuleIdentifier, importResolutionInfo, null, autoImportStd);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ResolveInput that = (ResolveInput) o;

        if(hashCode != that.hashCode)
            return false;
        if(autoImportStd != that.autoImportStd)
            return false;
        if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
            return false;
        if(!importResolutionInfo.equals(that.importResolutionInfo))
            return false;
        return Objects.equals(fileOpenInEditor, that.fileOpenInEditor);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = mainModuleIdentifier.hashCode();
        result = 31 * result + importResolutionInfo.hashCode();
        result = 31 * result + (fileOpenInEditor != null ? fileOpenInEditor.hashCode() : 0);
        result = 31 * result + (autoImportStd ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "ResolveInput@" + System.identityHashCode(this) + '{'
            + "mainModuleIdentifier=" + mainModuleIdentifier
            + ", importResolutionInfo=" + importResolutionInfo
            + (fileOpenInEditor == null ? "" : ", fileOpenInEditor=" + fileOpenInEditor)
            + ", autoImportStd=" + autoImportStd
            + '}';
        //@formatter:on
    }

    public ResolveInput withoutOpenFile() {
        return new ResolveInput(mainModuleIdentifier, importResolutionInfo, autoImportStd);
    }
}
