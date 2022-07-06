package mb.stratego.build.strincr.message;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignature;

public class CyclicOverlay extends Message {
    public final String cycle;

    public CyclicOverlay(IStrategoTerm name, Set<ConstructorSignature> cycle, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
        this.cycle = cycle.toString();
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        CyclicOverlay that = (CyclicOverlay) o;

        return cycle.equals(that.cycle);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + cycle.hashCode();
        return result;
    }
}
