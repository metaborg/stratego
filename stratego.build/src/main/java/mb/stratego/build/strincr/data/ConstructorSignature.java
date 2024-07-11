package mb.stratego.build.strincr.data;

import java.util.Collections;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.StringUtils;
import org.spoofax.terms.util.TermUtils;

public class ConstructorSignature extends StrategoTuple {
    public final String name;
    public final int noArgs;

    public ConstructorSignature(String name, int noArgs) {
        super(new IStrategoTerm[] { new StrategoString(name, AbstractTermFactory.EMPTY_LIST),
            new StrategoInt(noArgs) }, AbstractTermFactory.EMPTY_LIST);
        this.name = name;
        this.noArgs = noArgs;
    }

    public ConstructorSignature(IStrategoString name, IStrategoInt noArgs) {
        super(new IStrategoTerm[] { name, noArgs }, AbstractTermFactory.EMPTY_LIST);
        this.name = name.stringValue();
        this.noArgs = noArgs.intValue();
    }

    public static @Nullable ConstructorSignature fromTerm(IStrategoTerm consDef) {
        if(!TermUtils.isAppl(consDef)) {
            return null;
        }
        final IStrategoString name;
        switch(TermUtils.toAppl(consDef).getName()) {
            case "OpDeclQ":
                // fall-through
            case "ExtOpDeclQ":
                // fall-through
                final String escapedNameString = StringUtils
                    .escape(TermUtils.toStringAt(consDef, 0).stringValue());
                name = new StrategoString(escapedNameString, AbstractTermFactory.EMPTY_LIST);
                AbstractTermFactory.staticCopyAttachments(consDef.getSubterm(0), name);
                break;
            case "OpDecl":
                // fall-through
            case "ExtOpDecl":
                name = TermUtils.toStringAt(consDef, 0);
                break;
            case "OpDeclInj":
                // fall-through
            case "ExtOpDeclInj":
                // fall-through
            default:
                return null;
        }

        final IStrategoAppl type = TermUtils.toApplAt(consDef, 1);
        final IStrategoInt arity;

        switch(type.getName()) {
            case "ConstType":
                arity = B.integer(0);
                break;
            case "FunType":
                arity = B.integer(TermUtils.toListAt(type, 0).size());
                break;
            default:
                return null;
        }

        return new ConstructorSignature(name, arity);
    }

    public IStrategoTerm toTerm(ITermFactory tf) {
        final IStrategoTerm dynT = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
        final IStrategoTerm opType;
        if(noArgs == 0) {
            opType = dynT;
        } else {
            opType = tf.makeAppl("FunType", tf.makeList(Collections.nCopies(noArgs, dynT)), dynT);
        }
        return tf.makeAppl("OpDecl", opType);
    }

    public @Nullable static Boolean isExternal(IStrategoTerm consDef) {
        if(!TermUtils.isAppl(consDef)) {
            return null;
        }
        boolean isExternal = true;
        switch(TermUtils.toAppl(consDef).getName()) {
            case "OpDecl":
            case "OpDeclQ":
            case "OpDeclInj":
                isExternal = false;
                // fall-through
            case "ExtOpDecl":
            case "ExtOpDeclQ":
            case "ExtOpDeclInj":
                // fall-through
                break;
            default:
                return null;
        }

        return isExternal;
    }

    public StrategySignature toCongruenceSig() {
        return new StrategySignature(TermUtils.toStringAt(this, 0), noArgs, 0);
    }

    // equals/hashcode/toString inherited from StrategoTuple
}
