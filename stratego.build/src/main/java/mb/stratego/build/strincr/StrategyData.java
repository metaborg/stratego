package mb.stratego.build.strincr;

import java.util.Objects;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class StrategyData {
    public final StrategySignature signature;
    public final @Nullable IStrategoTerm astTerm;
    public final IStrategoTerm type;
    public final boolean generatedFromDynamicRule;

    public StrategyData(StrategySignature signature, @Nullable IStrategoTerm astTerm, IStrategoTerm type,
        boolean generatedFromDynamicRule) {
        this.signature = signature;
        this.astTerm = astTerm;
        this.type = type;
        this.generatedFromDynamicRule = generatedFromDynamicRule;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        StrategyData that = (StrategyData) o;

        if(generatedFromDynamicRule != that.generatedFromDynamicRule)
            return false;
        if(!signature.equals(that.signature))
            return false;
        if(!Objects.equals(astTerm, that.astTerm))
            return false;
        return type.equals(that.type);
    }

    @Override public int hashCode() {
        int result = signature.hashCode();
        result = 31 * result + (astTerm != null ? astTerm.hashCode() : 0);
        result = 31 * result + type.hashCode();
        result = 31 * result + (generatedFromDynamicRule ? 1 : 0);
        return result;
    }
}
