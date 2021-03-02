package mb.stratego.build.strincr.data;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.util.TermUtils;

import static mb.stratego.build.termvisitors.DesugarType.tryDesugarType;

public class ConstructorType extends StrategoAppl {
    private final ArrayList<IStrategoTerm> from;
    public final IStrategoTerm to;

    public ConstructorType(IStrategoTermBuilder tf, ArrayList<IStrategoTerm> from,
        IStrategoTerm to) {
        super(tf.makeConstructor("ConstrType", 2), new IStrategoTerm[] { tf.makeList(from), to },
            null);
        this.from = from;
        this.to = to;
    }

    public ArrayList<IStrategoTerm> getFrom() {
        return from;
    }

    public IStrategoTerm toOpType(ITermFactory tf) {
        final IStrategoTerm to2;
        to2 = typeToConstType(tf, to);
        if(from.size() == 0) {
            return to2;
        }
        IStrategoList.Builder froms = tf.arrayListBuilder(from.size());
        for(IStrategoTerm t : from) {
            froms.add(typeToConstType(tf, t));
        }
        return tf.makeAppl("FunType", tf.makeList(froms), to2);
    }

    public static IStrategoTerm typeToConstType(ITermFactory tf, IStrategoTerm type) {
        final IStrategoTerm to2;
        if(TermUtils.isAppl(type, "DynT", 1)) {
            to2 = type;
        } else {
            to2 = tf.makeAppl("ConstType", type);
        }
        return to2;
    }

    public static @Nullable ConstructorType fromOpType(ITermFactory tf, IStrategoTerm opType) {
        final ConstructorType type;
        switch(TermUtils.toAppl(opType).getName()) {
            case "ConstType":
                if(opType.getSubtermCount() != 1) {
                    return null;
                }
                type = new ConstructorType(tf, new ArrayList<>(0),
                    tryDesugarType(tf, opType.getSubterm(0)));
                break;
            case "FunType":
                if(opType.getSubtermCount() != 2 || !TermUtils.isListAt(opType, 0) || !TermUtils
                    .isApplAt(opType, 1, "ConstType", 1)) {
                    return null;
                }
                final IStrategoList froms = TermUtils.toListAt(opType, 0);
                final IStrategoTerm dynT = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
                final ArrayList<IStrategoTerm> fromTypes = new ArrayList<>(froms.size());
                for(IStrategoTerm from : froms) {
                    if(!TermUtils.isAppl(from, "ConstType", 1)) {
                        fromTypes.add(dynT);
                    } else {
                        fromTypes.add(tryDesugarType(tf, from.getSubterm(0)));
                    }
                }
                type = new ConstructorType(tf, fromTypes,
                    tryDesugarType(tf, opType.getSubterm(1).getSubterm(0)));
                break;
            default:
                return null;
        }
        return type;
    }
}
