package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.task.output.ModuleData;

public class GetConstrData implements Function<ModuleData, ArrayList<ConstructorData>>, Serializable {
    public static final GetConstrData INSTANCE = new GetConstrData();

    @Override public ArrayList<ConstructorData> apply(ModuleData moduleData) {
        final ArrayList<ConstructorData> constructorData = new ArrayList<>();
        for(List<ConstructorData> value : moduleData.constrData.values()) {
            for(ConstructorData data : value) {
                if(!data.isOverlay()) {
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
