package mb.stratego.build.spoofax2;

import javax.annotation.Nullable;

import mb.resource.hierarchical.HierarchicalResource;
import mb.stratego.build.strincr.IModuleImportService;

public class ModuleIdentifier implements IModuleImportService.ModuleIdentifier {
    public final boolean isLibrary;
    public final String moduleString;
    public final HierarchicalResource resource;

    public ModuleIdentifier(boolean isLibrary, String moduleString, HierarchicalResource resource) {
        this.isLibrary = isLibrary;
        this.moduleString = moduleString;
        assert !moduleString.contains(".") : "moduleStrings should be valid Stratego module names, and not contain file extensions";
        this.resource = resource;
    }

    @Override public boolean isLibrary() {
        return false;
    }

    @Override public String moduleString() {
        return moduleString;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleIdentifier that = (ModuleIdentifier) o;

        if(isLibrary != that.isLibrary)
            return false;
        if(!moduleString.equals(that.moduleString))
            return false;
        return resource.equals(that.resource);
    }

    @Override public int hashCode() {
        int result = isLibrary ? 1 : 0;
        result = 31 * result + moduleString.hashCode();
        result = 31 * result + resource.hashCode();
        return result;
    }

    @Override public String toString() {
        return moduleString();
    }
}
