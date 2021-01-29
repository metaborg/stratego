package mb.stratego.build.strincr;

import javax.annotation.Nullable;

public class StrategyFrontData {
    public final StrategySignature signature;
    protected @Nullable StrategyType type;
    public final Kind kind;

    protected StrategyFrontData(StrategySignature signature, @Nullable StrategyType type,
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
        if(!(type != null && type.equals(that.type)))
            return false;
        return kind == that.kind;
    }

    @Override public int hashCode() {
        int result = signature.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + kind.hashCode();
        return result;
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
