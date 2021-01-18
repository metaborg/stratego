package mb.stratego.build.strincr;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class ConstructorData {
    public final ConstructorSignature signature;
    public final IStrategoTerm astTerm;
    public final ConstructorType type;
//    public final boolean isExternal; // is this useful?

    public ConstructorData(ConstructorSignature signature, IStrategoTerm astTerm,
        ConstructorType type) {
        this.signature = signature;
        this.astTerm = astTerm;
        this.type = type;
    }

    public boolean isOverlay() {
        return false;
    };

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ConstructorData that = (ConstructorData) o;

        if(!signature.equals(that.signature))
            return false;
        if(!astTerm.equals(that.astTerm))
            return false;
        return type.equals(that.type);
    }

    @Override public int hashCode() {
        int result = signature.hashCode();
        result = 31 * result + astTerm.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
