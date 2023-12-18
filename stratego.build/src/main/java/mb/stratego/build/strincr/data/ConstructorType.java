package mb.stratego.build.strincr.data;

import static mb.stratego.build.termvisitors.DesugarType.tryDesugarType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.util.TermUtils;

public class ConstructorType extends StrategoAppl {
    private final ArrayList<IStrategoTerm> from;
    public final IStrategoTerm to;
    public final boolean isOverlay;

    public ConstructorType(IStrategoTermBuilder tf, ArrayList<IStrategoTerm> from,
        IStrategoTerm to) {
        this(tf, from, to, false);
    }

    public ConstructorType(IStrategoTermBuilder tf, ArrayList<IStrategoTerm> from,
        IStrategoTerm to, boolean isOverlay) {
        super(tf.makeConstructor("ConstrType", 2), new IStrategoTerm[] { tf.makeList(from), to },
            isOverlay ? tf.makeList(tf.makeAppl("Overlay")) : null);
        this.from = from;
        this.to = to;
        this.isOverlay = isOverlay;
    }

    public List<IStrategoTerm> getFrom() {
        return Collections.unmodifiableList(from);
    }

    public IStrategoTerm toOpType(IStrategoTermBuilder tf) {
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

    public static IStrategoTerm typeToConstType(IStrategoTermBuilder tf, IStrategoTerm type) {
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
                if(opType.getSubtermCount() != 2 || !TermUtils.isListAt(opType, 0)) {
                    return null;
                }
                final IStrategoList froms = TermUtils.toListAt(opType, 0);
                final IStrategoTerm dynT = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
                final ArrayList<IStrategoTerm> fromTypes = new ArrayList<>(froms.size());
                for(IStrategoTerm from : froms) {
                    if(TermUtils.isAppl(from, "ConstType", 1)) {
                        fromTypes.add(tryDesugarType(tf, from.getSubterm(0)));
                    } else {
                        fromTypes.add(dynT);
                    }
                }
                if(TermUtils.isApplAt(opType, 1, "ConstType", 1)) {
                    type = new ConstructorType(tf, fromTypes,
                        tryDesugarType(tf, opType.getSubterm(1).getSubterm(0)));
                } else {
                    type = new ConstructorType(tf, fromTypes, dynT);
                }
                break;
            default:
                return null;
        }
        return type;
    }

    public static @Nullable ConstructorType fromOverlayDecl(ITermFactory tf, IStrategoTerm olayDecl) {
        final IStrategoAppl dynT = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
        final ConstructorType type;
        switch(TermUtils.toAppl(olayDecl).getName()) {
            case "OverlayDeclNoArgs": {
                if(olayDecl.getSubtermCount() != 2) {
                    return null;
                }
                final ArrayList<IStrategoTerm> fromTypes = new ArrayList<>(0);
                if(TermUtils.isApplAt(olayDecl, 1, "ConstType", 1)) {
                    type = new ConstructorType(tf, fromTypes,
                        tryDesugarType(tf, olayDecl.getSubterm(1).getSubterm(0)), true);
                } else {
                    type = new ConstructorType(tf, fromTypes, dynT, true);
                }
                break;
            }
            case "OverlayDecl": {
                if(olayDecl.getSubtermCount() != 3 || !TermUtils.isListAt(olayDecl, 1)) {
                    return null;
                }
                final IStrategoList froms = TermUtils.toListAt(olayDecl, 1);
                final ArrayList<IStrategoTerm> fromTypes = new ArrayList<>(froms.size());
                for(IStrategoTerm from : froms) {
                    fromTypes.add(tryDesugarType(tf, from));
                }
                if(TermUtils.isApplAt(olayDecl, 2, "ConstType", 1)) {
                    type = new ConstructorType(tf, fromTypes,
                        tryDesugarType(tf, olayDecl.getSubterm(2).getSubterm(0)), true);
                } else {
                    type = new ConstructorType(tf, fromTypes, dynT, true);
                }
                break;
            }
            default:
                return null;
        }
        return type;
    }

    // equals/hashcode/toString inherited from StrategoAppl
}
