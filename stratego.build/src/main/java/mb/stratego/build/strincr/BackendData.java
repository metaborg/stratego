package mb.stratego.build.strincr;

import org.spoofax.interpreter.terms.IStrategoAppl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackendData {
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
}
