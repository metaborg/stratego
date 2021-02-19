package mb.stratego.build.strincr.data;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;

public class ConstructorData {
    public final ConstructorSignature signature;
    public final IStrategoAppl astTerm;
    public final ConstructorType type;
//    public final boolean isExternal; // is this useful?

    public ConstructorData(ConstructorSignature signature, IStrategoAppl astTerm,
        ConstructorType type) {
        this.signature = signature;
        this.astTerm = astTerm;
        this.type = type;
    }

    public boolean isOverlay() {
        return false;
    }

    @Override public boolean equals(@Nullable Object o) {
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
