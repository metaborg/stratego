package mb.stratego.build.strincr.function;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.function.output.OverlayData;
import mb.stratego.build.strincr.task.output.ModuleData;

public class GetOverlayData implements SerializableFunction<ModuleData, OverlayData> {
    private final LinkedHashSet<ConstructorSignature> usedConstructors;

    public GetOverlayData(LinkedHashSet<ConstructorSignature> usedConstructors) {
        this.usedConstructors = usedConstructors;
    }

    @Override public OverlayData apply(ModuleData moduleData) {
        final ArrayList<IStrategoTerm> overlayAsts = new ArrayList<>();
        final LinkedHashSet<ConstructorSignature> usedConstrs = new LinkedHashSet<>();
        for(ConstructorSignature usedConstructor : usedConstructors) {
            final @Nullable List<IStrategoTerm> asts =
                moduleData.overlayAsts.get(usedConstructor);
            if(asts != null) {
                overlayAsts.addAll(asts);
            }
            final @Nullable LinkedHashSet<ConstructorSignature> constrs =
                moduleData.overlayUsedConstrs.get(usedConstructor);
            if(constrs != null) {
                usedConstrs.addAll(constrs);
            }
        }
        return new OverlayData(overlayAsts, usedConstrs);
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
