package mb.stratego.build.strincr.data;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;

public class ConstructorData implements Serializable {
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

    public IStrategoTerm toTerm(IStrategoTermBuilder tf) {
        return tf.makeAppl("OpDecl", tf.makeString(signature.name), type.toOpType(tf));
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o)
            return true;
        // N.B. using instanceof check to allow for BoilerplateConstructorData subclass
        if(!(o instanceof ConstructorData))
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

    @Override public String toString() {
        return "ConstructorData(" + signature + ", " + astTerm + ", " + type + ')';
    }
}
