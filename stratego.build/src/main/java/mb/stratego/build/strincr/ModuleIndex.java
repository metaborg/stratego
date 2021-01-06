package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.Message;

/**
 * The information in the module data of a module as needed by the Resolve task for indexing.
 */
public class ModuleIndex implements Serializable {
    public final List<IStrategoTerm> imports;
    public final Set<ConstructorSignature> constructors;
    public final Set<StrategySignature> strategies;
    public final Set<ConstructorSignature> overlays;
    public final List<Message<?>> messages;

    public ModuleIndex(List<IStrategoTerm> imports, Set<ConstructorSignature> constructors,
        Set<StrategySignature> strategies, Set<ConstructorSignature> overlays,
        List<Message<?>> messages) {
        this.imports = imports;
        this.constructors = constructors;
        this.strategies = strategies;
        this.overlays = overlays;
        this.messages = messages;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleIndex that = (ModuleIndex) o;

        if(!imports.equals(that.imports))
            return false;
        if(!constructors.equals(that.constructors))
            return false;
        if(!strategies.equals(that.strategies))
            return false;
        return overlays.equals(that.overlays);
    }

    @Override public int hashCode() {
        int result = imports.hashCode();
        result = 31 * result + constructors.hashCode();
        result = 31 * result + strategies.hashCode();
        result = 31 * result + overlays.hashCode();
        return result;
    }
}
