package strategolib.terms;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;

public class CompiledStrategyEqualityComparator implements EqualityComparator<Object> {
    private final Context context;
    private final Strategy compare;
    private final IStrategoTermBuilder factory;

    public CompiledStrategyEqualityComparator(Context env, Strategy comp) {
        this.context = env;
        this.compare = comp;
        this.factory = env.getFactory();
    }

    @Override public boolean equals(Object o1, Object o2) {
        if(o1 == null || o2 == null || !(o1 instanceof IStrategoTerm) || !(o2 instanceof IStrategoTerm)) {
            return false;
        }
        IStrategoTerm left = (IStrategoTerm) o1;
        IStrategoTerm right = (IStrategoTerm) o2;

        return compare.invoke(context, factory.makeTuple(left, right)) != null;
    }
}
