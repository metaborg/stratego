package mb.stratego.build.strincr.data;

import java.util.Collections;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

public class SortSignature extends StrategoTuple {
    public final String name;
    public final int noArgs;

    public SortSignature(String name, int noArgs) {
        super(new IStrategoTerm[] { new StrategoString(name, AbstractTermFactory.EMPTY_LIST),
            new StrategoInt(noArgs) }, AbstractTermFactory.EMPTY_LIST);
        this.name = name;
        this.noArgs = noArgs;
    }

    public SortSignature(IStrategoString name, IStrategoInt noArgs) {
        super(new IStrategoTerm[] { name, noArgs }, AbstractTermFactory.EMPTY_LIST);
        this.name = name.stringValue();
        this.noArgs = noArgs.intValue();
    }

    public static @Nullable SortSignature fromTerm(IStrategoTerm sortDef) {
        if(!TermUtils.isAppl(sortDef)) {
            return null;
        }
        final IStrategoString name;
        final IStrategoInt arity;
        switch(TermUtils.toAppl(sortDef).getName()) {
            case "SortNoArgs":
                name = TermUtils.toStringAt(sortDef, 0);
                arity = B.integer(0);
                break;
            case "Sort":
            case "ExtSort":
                name = TermUtils.toStringAt(sortDef, 0);

                if(TermUtils.isListAt(sortDef, 1)) {
                    arity = B.integer(TermUtils.toListAt(sortDef, 1).size());
                } else  {
                    return null;
                }
                break;
            default:
                return null;
        }

        return new SortSignature(name, arity);
    }

    public IStrategoTerm toTerm(IStrategoTermBuilder tf) {
        final IStrategoTerm star = tf.makeAppl("Star");
        final IStrategoTerm sortDef;
        if(noArgs == 0) {
            sortDef = tf.makeAppl("Sort", tf.makeString(name));
        } else {
            sortDef = tf.makeAppl("Sort", tf.makeString(name),
                tf.makeList(Collections.nCopies(noArgs, star)));
        }
        return sortDef;
    }

    public IStrategoTerm toExtDefTerm(IStrategoTermBuilder tf) {
        final IStrategoTerm star = tf.makeAppl("Star");
        return tf.makeAppl("ExtSort", tf.makeString(name),
            tf.makeList(Collections.nCopies(noArgs, star)));
    }

    public @Nullable static Boolean isExternal(IStrategoTerm sortDef) {
        if(!TermUtils.isAppl(sortDef)) {
            return null;
        }
        boolean isExternal = true;
        switch(TermUtils.toAppl(sortDef).getName()) {
            case "SortNoArgs":
            case "Sort":
                isExternal = false;
                // fall-through
            case "ExtSort":
                break;
            default:
                return null;
        }

        return isExternal;
    }

    // equals/hashcode/toString inherited from StrategoTuple
}
