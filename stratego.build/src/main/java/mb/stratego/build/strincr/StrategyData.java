package mb.stratego.build.strincr;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class StrategyData {
    public final StrategySignature signature;
    public final List<IStrategoTerm> astTerms;
    public @Nullable StrategyType type;
//    public final boolean isExternal; // is this useful?
    public boolean generatedFromDynamicRule;

    public StrategyData(StrategySignature signature, List<IStrategoTerm> astTerms, @Nullable StrategyType type,
        boolean generatedFromDynamicRule) {
        this.signature = signature;
        this.astTerms = astTerms;
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
        if(!astTerms.equals(that.astTerms))
            return false;
        return Objects.equals(type, that.type);
    }

    @Override public int hashCode() {
        int result = signature.hashCode();
        result = 31 * result + astTerms.hashCode();
        result = 31 * result + (type == null ? 0 : type.hashCode());
        result = 31 * result + (generatedFromDynamicRule ? 1 : 0);
        return result;
    }
}
