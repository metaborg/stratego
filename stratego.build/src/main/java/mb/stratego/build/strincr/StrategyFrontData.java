package mb.stratego.build.strincr;

import java.util.Objects;

import javax.annotation.Nullable;

// TODO: create class hierarchy of Normal, External, Internal, DynRuleGenerated, TypeDefinition(StrategyType).
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

    public @Nullable StrategyType type() {
        return type;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        StrategyFrontData that = (StrategyFrontData) o;

        if(!signature.equals(that.signature))
            return false;
        if(!Objects.equals(type, that.type))
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
