package mb.stratego.build.strincr.function;

import java.util.ArrayList;
import java.util.List;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.task.output.ModuleData;

public class GetConstrData
    implements SerializableFunction<ModuleData, ArrayList<ConstructorData>> {
    public static final GetConstrData INSTANCE = new GetConstrData();

    @Override public ArrayList<ConstructorData> apply(ModuleData moduleData) {
        final ArrayList<ConstructorData> constructorData = new ArrayList<>();
        for(List<ConstructorData> value : moduleData.constrData.values()) {
            for(ConstructorData data : value) {
                if(!data.isOverlay) {
                    constructorData.add(data);
                }
            }
        }
        return constructorData;
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
