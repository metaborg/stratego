package mb.stratego.build.strincr.task.input;

import java.io.Serializable;

import mb.stratego.build.strincr.IModuleImportService;

public class FrontInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final IModuleImportService moduleImportService;

    public FrontInput(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IModuleImportService moduleImportService) {
        this.moduleIdentifier = moduleIdentifier;
        this.moduleImportService = moduleImportService;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        FrontInput input = (FrontInput) o;

        if(!moduleIdentifier.equals(input.moduleIdentifier))
            return false;
        return moduleImportService.equals(input.moduleImportService);
    }

    @Override public int hashCode() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + moduleImportService.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Front.Input(" + moduleIdentifier + ")";
    }
}
