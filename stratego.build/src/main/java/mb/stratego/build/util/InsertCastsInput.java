package mb.stratego.build.util;

import java.io.Serializable;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.GTEnvironment;
import mb.stratego.build.strincr.IModuleImportService;

public final class InsertCastsInput implements Serializable {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final ResourcePath projectPath;
    public final GTEnvironment environment;

    public InsertCastsInput(IModuleImportService.ModuleIdentifier moduleIdentifier,
        ResourcePath projectPath, GTEnvironment environment) {
        this.moduleIdentifier = moduleIdentifier;
        this.projectPath = projectPath;
        this.environment = environment;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        InsertCastsInput input = (InsertCastsInput) o;

        if(!moduleIdentifier.equals(input.moduleIdentifier))
            return false;
        if(!projectPath.equals(input.projectPath))
            return false;
        return environment.equals(input.environment);
    }

    @Override public int hashCode() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + projectPath.hashCode();
        result = 31 * result + environment.hashCode();
        return result;
    }

    @Override public String toString() {
        return "InsertCasts.Input2(" + moduleIdentifier + ")";
    }
}
