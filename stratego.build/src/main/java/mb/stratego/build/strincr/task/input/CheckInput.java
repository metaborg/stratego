package mb.stratego.build.strincr.task.input;

import java.io.Serializable;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportService.ImportResolutionInfo;

public class CheckInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final ResourcePath projectPath;
    public final ImportResolutionInfo importResolutionInfo;
    public final boolean autoImportStd;
    protected final int hashCode;

    public CheckInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ResourcePath projectPath, ImportResolutionInfo importResolutionInfo, boolean autoImportStd) {
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.projectPath = projectPath;
        this.importResolutionInfo = importResolutionInfo;
        this.autoImportStd = autoImportStd;
        this.hashCode = hashFunction();
    }

    public ResolveInput resolveInput() {
        return new ResolveInput(mainModuleIdentifier, importResolutionInfo, autoImportStd);
    }

    public CheckModuleInput checkModuleInput(
        IModuleImportService.ModuleIdentifier moduleIdentifier) {
        return new CheckModuleInput(
            new FrontInput.Normal(moduleIdentifier, importResolutionInfo, autoImportStd),
            mainModuleIdentifier, projectPath);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckInput that = (CheckInput) o;

        if(hashCode != that.hashCode)
            return false;
        if(autoImportStd != that.autoImportStd)
            return false;
        if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
            return false;
        if(!projectPath.equals(that.projectPath))
            return false;
        return importResolutionInfo.equals(that.importResolutionInfo);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = mainModuleIdentifier.hashCode();
        result = 31 * result + projectPath.hashCode();
        result = 31 * result + importResolutionInfo.hashCode();
        result = 31 * result + (autoImportStd ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "CheckInput@" + System.identityHashCode(this) + '{'
            + "mainModuleIdentifier=" + mainModuleIdentifier
            + ", projectPath=" + projectPath
            + ", importResolutionInfo=" + importResolutionInfo
            + ", autoImportStd=" + autoImportStd
            + '}';
        //@formatter:on
    }
}
