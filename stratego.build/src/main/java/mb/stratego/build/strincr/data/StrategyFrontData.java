package mb.stratego.build.strincr.data;

import java.io.Serializable;

public class StrategyFrontData implements Serializable {
    public final StrategySignature signature;
    public final StrategyType type;
    public final Kind kind;

    public StrategyFrontData(StrategySignature signature, StrategyType type,
        Kind kind) {
        this.signature = signature;
        this.type = type;
        this.kind = kind;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        StrategyFrontData that = (StrategyFrontData) o;

        if(!signature.equals(that.signature))
            return false;
        if(!type.equals(that.type))
            return false;
        return kind == that.kind;
    }

    @Override public int hashCode() {
        int result = signature.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + kind.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StrategyFrontData(" + signature + ", " + type + ", " + kind + ')';
    }

    public enum Kind {
        Normal,
        External,
        Internal,
        Extend,
        Override,
        DynRuleGenerated,
        TypeDefinition
    }
}
