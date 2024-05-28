package mb.stratego.build.strincr;

import jakarta.annotation.Nullable;

import mb.resource.hierarchical.ResourcePath;

public class ModuleIdentifier implements IModuleImportService.ModuleIdentifier, Comparable<ModuleIdentifier> {
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
            ".") : "moduleString should be valid Stratego module name, and not contain a file extension";
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

        if(legacyStratego != that.legacyStratego)
            return false;
        if(isLibrary != that.isLibrary)
            return false;
        if(!moduleString.equals(that.moduleString))
            return false;
        return path.equals(that.path);
    }

    @Override public int hashCode() {
        int result = Boolean.hashCode(legacyStratego);
        result = 31 * result + Boolean.hashCode(isLibrary);
        result = 31 * result + moduleString.hashCode();
        result = 31 * result + path.hashCode();

        return result;
    }

    @Override public String toString() {
        return moduleString();
    }

    @Override public int compareTo(ModuleIdentifier o) {
        return path.asString().compareTo(o.path.asString());
    }
}
