package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;

public class Str2LibInfo implements Serializable {
    public final LinkedHashSet<SortSignature> sorts;
    public final LinkedHashSet<ConstructorData> constructors;
    public final LinkedHashMap<StrategySignature, StrategyType> strategyFrontData;

    public Str2LibInfo(LinkedHashSet<SortSignature> sorts,
        LinkedHashSet<ConstructorData> constructors,
        LinkedHashMap<StrategySignature, StrategyType> strategyFrontData) {
        this.sorts = sorts;
        this.constructors = constructors;
        this.strategyFrontData = strategyFrontData;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Str2LibInfo that = (Str2LibInfo) o;

        if(!sorts.equals(that.sorts))
            return false;
        if(!constructors.equals(that.constructors))
            return false;
        return strategyFrontData.equals(that.strategyFrontData);
    }

    @Override public int hashCode() {
        int result = sorts.hashCode();
        result = 31 * result + constructors.hashCode();
        result = 31 * result + strategyFrontData.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Str2LibInfo(" + sorts + ", " + constructors + ", " + strategyFrontData + ')';
    }
}
