package mb.stratego.build.strincr.data;

public class ConstructorSignatureMatcher extends ConstructorSignature {
    public final ConstructorSignature wrapped;
    
    public ConstructorSignatureMatcher(ConstructorSignature sig) {
        super(sig.name, sig.noArgs, 0L);
        this.wrapped = sig;
    }
}
