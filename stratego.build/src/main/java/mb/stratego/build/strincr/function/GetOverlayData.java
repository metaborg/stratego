package mb.stratego.build.strincr.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.task.output.ModuleData;

public class GetOverlayData implements SerializableFunction<ModuleData, ArrayList<OverlayData>> {
    private final Set<ConstructorSignature> usedConstructors;

    public GetOverlayData(Set<ConstructorSignature> usedConstructors) {
        this.usedConstructors = usedConstructors;
    }

    @Override public ArrayList<OverlayData> apply(ModuleData moduleData) {
        final ArrayList<OverlayData> result = new ArrayList<>();
        for(ConstructorSignature usedConstructor : usedConstructors) {
            final @Nullable List<OverlayData> overlayData =
                moduleData.overlayData.get(usedConstructor);
            if(overlayData != null) {
                result.addAll(overlayData);
            }
        }
        return result;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GetOverlayData that = (GetOverlayData) o;

        return usedConstructors.equals(that.usedConstructors);
    }

    @Override public int hashCode() {
        return usedConstructors.hashCode();
    }
}
