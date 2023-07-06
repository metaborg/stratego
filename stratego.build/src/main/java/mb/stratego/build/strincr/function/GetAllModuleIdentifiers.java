package mb.stratego.build.strincr.function;

import java.util.LinkedHashSet;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.task.output.GlobalData;

public class GetAllModuleIdentifiers
    implements SerializableFunction<GlobalData, LinkedHashSet<IModuleImportService.ModuleIdentifier>> {
    public static final GetAllModuleIdentifiers INSTANCE = new GetAllModuleIdentifiers();

    @Override public LinkedHashSet<IModuleImportService.ModuleIdentifier> apply(GlobalData globalData) {
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