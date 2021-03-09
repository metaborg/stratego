package mb.stratego.build.strincr.data;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;

public class ConstructorSignatureMatcher extends ConstructorSignature {
    public final @Nullable ConstructorSignature wrapped;

    public ConstructorSignatureMatcher(IStrategoString name, IStrategoInt noArgs) {
        super(name, noArgs, 0L);
        wrapped = null;
    }

    public ConstructorSignatureMatcher(ConstructorSignature sig) {
        super(sig.name, sig.noArgs, 0L);
        this.wrapped = sig;
    }
}
