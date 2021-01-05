package mb.stratego.build.strincr;

import javax.annotation.Nullable;

import org.spoofax.interpreter.stratego.SDefT;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
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

import static org.spoofax.interpreter.core.Interpreter.cify;

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

    public String cifiedName() {
        return cify(name) + "_" + noArgs;
    }

    public IStrategoTerm standardType(ITermFactory tf) {
        final IStrategoAppl dyn = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
        final IStrategoList.Builder sargTypes = tf.arrayListBuilder(noArgs);
        for(int i = 0; i < noArgs; i++) {
            sargTypes.add(dyn);
        }
        return tf.makeAppl("ConstrType", tf.makeList(sargTypes), dyn);
    }

    public static boolean isCified(String name) {
        try {
            int lastUnderlineOffset = name.lastIndexOf('_');
            if(lastUnderlineOffset == -1) {
                return false;
            }
            Integer.parseInt(name.substring(lastUnderlineOffset + 1));
        } catch(RuntimeException e) {
            return false;
        }
        return true;
    }

    public static @Nullable ConstructorSignature fromCified(String cifiedName) {
        try {
            int lastUnderlineOffset = cifiedName.lastIndexOf('_');
            if(lastUnderlineOffset == -1) {
                return null;
            }
            int arity = Integer.parseInt(cifiedName.substring(lastUnderlineOffset + 1));
            return new ConstructorSignature(
                SDefT.unescape(cifiedName.substring(0, lastUnderlineOffset)), arity);
        } catch(NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable ConstructorSignature fromTuple(IStrategoTerm tuple) {
        if(!TermUtils.isTuple(tuple) || tuple.getSubtermCount() != 2 || !TermUtils
            .isIntAt(tuple, 1)) {
            return null;
        }
        if(TermUtils.isStringAt(tuple, 0)) {
            return new ConstructorSignature(TermUtils.toStringAt(tuple, 0),
                TermUtils.toIntAt(tuple, 1));
        }
        if(TermUtils.isApplAt(tuple, 0) && TermUtils.tryGetName(tuple.getSubterm(0))
            .map(n -> n.equals("Q")).orElse(false)) {
            final String escapedNameString =
                StringUtils.escape(TermUtils.toStringAt(tuple.getSubterm(0), 0).stringValue());
            final StrategoString escapedName =
                new StrategoString(escapedNameString, AbstractTermFactory.EMPTY_LIST);
            AbstractTermFactory.staticCopyAttachments(tuple.getSubterm(0), escapedName);
            return new ConstructorSignature(escapedName, TermUtils.toIntAt(tuple, 1));
        }
        return null;
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
                final String escapedNameString = StringUtils
                    .escape(TermUtils.toStringAt(consDef.getSubterm(0), 0).stringValue());
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

    public StrategySignature toCongruenceSig() {
        return new StrategySignature(name, noArgs, 0);
    }
}
