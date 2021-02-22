package mb.stratego.build.strincr.task.input;

import java.io.Serializable;

import mb.stratego.build.strincr.IModuleImportService;

public class ResolveInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;
    public final IModuleImportService moduleImportService;

    public ResolveInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        IModuleImportService moduleImportService) {
        this.mainModuleIdentifier = mainModuleIdentifier;
        this.moduleImportService = moduleImportService;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ResolveInput input = (ResolveInput) o;

        if(!mainModuleIdentifier.equals(input.mainModuleIdentifier))
            return false;
        return moduleImportService.equals(input.moduleImportService);
    }

    @Override public int hashCode() {
        int result = mainModuleIdentifier.hashCode();
        result = 31 * result + moduleImportService.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Resolve.Input(" + mainModuleIdentifier + ", " + moduleImportService + ")";
    }
}
