package mb.stratego.build.strincr.function;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.annotation.Nullable;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ModulesDefiningOverlays implements
    SerializableFunction<GlobalData, LinkedHashSet<IModuleImportService.ModuleIdentifier>> {
    public final Set<ConstructorSignature> usedConstructors;

    public ModulesDefiningOverlays(Set<ConstructorSignature> usedConstructors) {
        this.usedConstructors = usedConstructors;
    }

    @Override
    public LinkedHashSet<IModuleImportService.ModuleIdentifier> apply(GlobalData globalData) {
        final LinkedHashSet<IModuleImportService.ModuleIdentifier> result = new LinkedHashSet<>();
        for(ConstructorSignature usedConstructor : usedConstructors) {
            final @Nullable Set<IModuleImportService.ModuleIdentifier> moduleIdentifiers =
                globalData.overlayIndex.get(usedConstructor);
            if(moduleIdentifiers != null) {
                result.addAll(moduleIdentifiers);
            }
        }
        return result;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModulesDefiningOverlays that = (ModulesDefiningOverlays) o;

        return usedConstructors.equals(that.usedConstructors);
    }

    @Override public int hashCode() {
        return usedConstructors.hashCode();
    }
}
