package mb.stratego.build.strincr.data;

import java.io.Serializable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;

public class ConstructorData implements Serializable {
    public final ConstructorSignature signature;
    public final ConstructorType type;
    public final boolean isOverlay;
//    public final boolean isExternal; // is this useful?

    public ConstructorData(ConstructorSignature signature, ConstructorType type) {
        this(signature, type, false);
    }

    public ConstructorData(ConstructorSignature signature, ConstructorType type, boolean isOverlay) {
        this.signature = signature;
        this.type = type;
        this.isOverlay = isOverlay;
    }

    public IStrategoTerm toTerm(IStrategoTermBuilder tf) {
        return tf.makeAppl("OpDecl", tf.makeString(signature.name), type.toOpType(tf));
    }

    public IStrategoTerm toExtTerm(IStrategoTermBuilder tf) {
        return tf.makeAppl("ExtOpDecl", tf.makeString(signature.name), type.toOpType(tf));
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ConstructorData that = (ConstructorData) o;

        if(isOverlay != that.isOverlay)
            return false;
        if(!signature.equals(that.signature))
            return false;
        return type.equals(that.type);
    }

    @Override public int hashCode() {
        int result = signature.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (isOverlay ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "ConstructorData(" + signature + ", " + type + ')' + (isOverlay ?
            "{Overlay()}" : "");
    }
}
