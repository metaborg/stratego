package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorSignatureMatcher;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.task.output.ModuleData;

public class ToOverlays implements Function<ModuleData, ArrayList<OverlayData>>, Serializable {
    private final Set<ConstructorSignature> usedConstructors;

    public ToOverlays(Set<ConstructorSignature> usedConstructors) {
        this.usedConstructors = usedConstructors;
    }

    @Override public ArrayList<OverlayData> apply(ModuleData moduleData) {
        final ArrayList<OverlayData> result = new ArrayList<>();
        for(ConstructorSignature usedConstructor : usedConstructors) {
            final @Nullable List<OverlayData> overlayData =
                moduleData.overlayData.get(new ConstructorSignatureMatcher(usedConstructor));
            if(overlayData != null) {
                result.addAll(overlayData);
            }
        }
        return result;
    }
}
