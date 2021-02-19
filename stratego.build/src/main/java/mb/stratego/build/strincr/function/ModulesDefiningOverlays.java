package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorSignatureMatcher;
import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.strincr.IModuleImportService;

public class ModulesDefiningOverlays<T extends Set<IModuleImportService.ModuleIdentifier> & Serializable>
    implements Function<GlobalData, T>, Serializable {
    public final Set<ConstructorSignature> usedConstructors;

    public ModulesDefiningOverlays(Set<ConstructorSignature> usedConstructors) {
        this.usedConstructors = usedConstructors;
    }

    @SuppressWarnings("unchecked") @Override public T apply(GlobalData globalData) {
        final HashSet<IModuleImportService.ModuleIdentifier> result = new HashSet<>();
        for(ConstructorSignature usedConstructor : usedConstructors) {
            final @Nullable Set<IModuleImportService.ModuleIdentifier> moduleIdentifiers =
                globalData.overlayIndex.get(new ConstructorSignatureMatcher(usedConstructor));
            if(moduleIdentifiers != null) {
                result.addAll(moduleIdentifiers);
            }
        }
        return (T) result;
    }
}
