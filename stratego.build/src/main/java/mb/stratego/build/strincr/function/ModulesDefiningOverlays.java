package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorSignatureMatcher;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ModulesDefiningOverlays
    implements Function<GlobalData, HashSet<ModuleIdentifier>>, Serializable {
    public final Set<ConstructorSignature> usedConstructors;

    public ModulesDefiningOverlays(Set<ConstructorSignature> usedConstructors) {
        this.usedConstructors = usedConstructors;
    }

    @Override public HashSet<ModuleIdentifier> apply(GlobalData globalData) {
        final HashSet<ModuleIdentifier> result = new HashSet<>();
        for(ConstructorSignature usedConstructor : usedConstructors) {
            final @Nullable Set<ModuleIdentifier> moduleIdentifiers =
                globalData.overlayIndex.get(new ConstructorSignatureMatcher(usedConstructor));
            if(moduleIdentifiers != null) {
                result.addAll(moduleIdentifiers);
            }
        }
        return result;
    }
}
