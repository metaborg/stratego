package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.nio.file.Path;

import jakarta.annotation.Nullable;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;

public class CheckModuleInput implements Serializable {
    public final FrontInput frontInput;
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final ResourcePath projectPath;
    protected final int hashCode;

    public CheckModuleInput(FrontInput frontInput,
        IModuleImportService.ModuleIdentifier mainModuleIdentifier, ResourcePath projectPath) {
        this.frontInput = frontInput;
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.projectPath = projectPath;
        this.hashCode = hashFunction();
    }

    public ResolveInput resolveInput() {
        final @Nullable FrontInput.FileOpenInEditor fileOpenInEditor;
        if(frontInput instanceof FrontInput.FileOpenInEditor) {
            fileOpenInEditor = (FrontInput.FileOpenInEditor) frontInput;
        } else {
            fileOpenInEditor = null;
        }
        return new ResolveInput(mainModuleIdentifier, frontInput.importResolutionInfo,
            fileOpenInEditor, frontInput.autoImportStd);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckModuleInput that = (CheckModuleInput) o;

        if(hashCode != that.hashCode)
            return false;
        if(!frontInput.equals(that.frontInput))
            return false;
        if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
            return false;
        return projectPath.equals(that.projectPath);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = frontInput.hashCode();
        result = 31 * result + mainModuleIdentifier.hashCode();
        result = 31 * result + projectPath.hashCode();
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "CheckModuleInput@" + System.identityHashCode(this) + '{'
            + "frontInput=" + frontInput
            + ", mainModuleIdentifier=" + mainModuleIdentifier
            + ", projectPath=" + projectPath
            + '}';
        //@formatter:on
    }
}
