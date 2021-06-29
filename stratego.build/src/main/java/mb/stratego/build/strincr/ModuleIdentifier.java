package mb.stratego.build.strincr;

import javax.annotation.Nullable;

import mb.resource.hierarchical.ResourcePath;

public class ModuleIdentifier implements IModuleImportService.ModuleIdentifier {
    public final boolean legacyStratego;
    public final boolean isLibrary;
    public final String moduleString;
    public final ResourcePath path;

    public ModuleIdentifier(boolean legacyStratego, boolean isLibrary, String moduleString,
        ResourcePath path) {
        this.legacyStratego = legacyStratego;
        this.isLibrary = isLibrary;
        this.moduleString = moduleString;
        assert !moduleString.contains(
            ".") : "moduleStrings should be valid Stratego module names, and not contain file extensions";
        this.path = path;
    }

    @Override public boolean legacyStratego() {
        return legacyStratego;
    }

    @Override public boolean isLibrary() {
        return isLibrary;
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
        return path.equals(that.path);
    }

    @Override public int hashCode() {
        int result = isLibrary ? 1 : 0;
        result = 31 * result + moduleString.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override public String toString() {
        return moduleString();
    }
}
