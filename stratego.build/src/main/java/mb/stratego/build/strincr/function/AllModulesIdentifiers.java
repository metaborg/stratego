package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.function.Function;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.task.output.GlobalData;

public class AllModulesIdentifiers
    implements Function<GlobalData, LinkedHashSet<IModuleImportService.ModuleIdentifier>>,
    Serializable {
    public static final AllModulesIdentifiers INSTANCE = new AllModulesIdentifiers();

    private AllModulesIdentifiers() {
    }

    @Override
    public LinkedHashSet<IModuleImportService.ModuleIdentifier> apply(GlobalData globalData) {
        return globalData.allModuleIdentifiers;
    }

    @Override public boolean equals(Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
