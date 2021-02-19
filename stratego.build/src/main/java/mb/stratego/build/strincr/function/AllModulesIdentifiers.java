package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.strincr.IModuleImportService;

public class AllModulesIdentifiers<T extends Set<IModuleImportService.ModuleIdentifier> & Serializable>
    implements Function<GlobalData, T>, Serializable {
    public static final AllModulesIdentifiers<?> Instance = new AllModulesIdentifiers<>();

    private AllModulesIdentifiers() {
    }

    @SuppressWarnings("unchecked") @Override public T apply(GlobalData globalData) {
        return (T) globalData.allModuleIdentifiers;
    }
}
