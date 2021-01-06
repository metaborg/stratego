package mb.stratego.build.strincr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import static mb.stratego.build.termvisitors.DesugarType.tryDesugarType;

public class ConstructorType {
    private final List<IStrategoTerm> from;
    public final IStrategoTerm to;

    public ConstructorType(List<IStrategoTerm> from, IStrategoTerm to) {
        this.from = from;
        this.to = to;
    }

    public List<IStrategoTerm> getFrom() {
        return Collections.unmodifiableList(from);
    }

    public IStrategoTerm toTerm(IStrategoTermBuilder tf) {
        return tf.makeAppl("ConstrType", tf.makeList(from), to);
    }

    public IStrategoTerm toOpType(ITermFactory tf) {
        IStrategoList.Builder froms = tf.arrayListBuilder(from.size());
        for(IStrategoTerm t : from) {
            froms.add(tf.makeAppl("ConstType", t));
        }
        return tf.makeAppl("FunType", tf.makeList(froms), tf.makeAppl("ConstType", to));
    }

    public static @Nullable ConstructorType fromOpType(ITermFactory tf, IStrategoTerm opType) {
        final ConstructorType type;
        switch(TermUtils.toAppl(opType).getName()) {
            case "ConstType":
                if(opType.getSubtermCount() != 1) {
                    return null;
                }
                type = new ConstructorType(Collections.emptyList(),
                    tryDesugarType(tf, opType.getSubterm(0)));
                break;
            case "FunType":
                if(opType.getSubtermCount() != 2 || !TermUtils.isListAt(opType, 0) || !TermUtils
                    .isApplAt(opType, 1, "ConstType", 1)) {
                    return null;
                }
                final IStrategoList froms = TermUtils.toListAt(opType, 0);
                final IStrategoTerm dynT = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
                final List<IStrategoTerm> fromTypes = new ArrayList<>(froms.size());
                for(IStrategoTerm tupleType : froms) {
                    if(!TermUtils.isAppl(tupleType, "ConstType", 1)) {
                        fromTypes.add(dynT);
                    } else {
                        fromTypes.add(tryDesugarType(tf, tupleType.getSubterm(0)));
                    }
                }
                type = new ConstructorType(fromTypes,
                    tryDesugarType(tf, opType.getSubterm(1).getSubterm(0)));
                break;
            default:
                return null;
        }
        return type;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ConstructorType that = (ConstructorType) o;

        if(!from.equals(that.from))
            return false;
        return to.equals(that.to);
    }

    @Override public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }
}
