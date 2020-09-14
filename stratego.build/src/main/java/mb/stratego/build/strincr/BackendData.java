package mb.stratego.build.strincr;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackendData {
    final List<IStrategoTerm> consDefs = new ArrayList<>();
    // Cified-strategy-name to definitions of that strategy
    final Map<String, List<IStrategoAppl>> strategyASTs = new HashMap<>();
    // Constructor_arity of congruence to definition of that strategy
    final Map<String, IStrategoAppl> congrASTs = new HashMap<>();
    // Cified-strategy-name to constructor_arity names that were used in the body
    final Map<String, Set<String>> strategyConstrs = new HashMap<>();
    // Overlay_arity names to constructor_arity names used
    final Map<String, Set<String>> overlayConstrs = new HashMap<>();
    // Overlay_arity name to definition of that overlay
    final Map<String, List<IStrategoAppl>> overlayASTs = new HashMap<>();

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((congrASTs == null) ? 0 : congrASTs.hashCode());
        result = prime * result + ((overlayASTs == null) ? 0 : overlayASTs.hashCode());
        result = prime * result + ((overlayConstrs == null) ? 0 : overlayConstrs.hashCode());
        result = prime * result + ((strategyASTs == null) ? 0 : strategyASTs.hashCode());
        result = prime * result + ((strategyConstrs == null) ? 0 : strategyConstrs.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        BackendData other = (BackendData) obj;
        if(congrASTs == null) {
            if(other.congrASTs != null)
                return false;
        } else if(!congrASTs.equals(other.congrASTs))
            return false;
        if(overlayASTs == null) {
            if(other.overlayASTs != null)
                return false;
        } else if(!overlayASTs.equals(other.overlayASTs))
            return false;
        if(overlayConstrs == null) {
            if(other.overlayConstrs != null)
                return false;
        } else if(!overlayConstrs.equals(other.overlayConstrs))
            return false;
        if(strategyASTs == null) {
            if(other.strategyASTs != null)
                return false;
        } else if(!strategyASTs.equals(other.strategyASTs))
            return false;
        if(strategyConstrs == null) {
            if(other.strategyConstrs != null)
                return false;
        } else if(!strategyConstrs.equals(other.strategyConstrs))
            return false;
        return true;
    }
}
