package mb.stratego.build.strincr;

import java.util.HashMap;
import java.util.Map;

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
import org.spoofax.terms.util.TermUtils;

import static org.spoofax.interpreter.core.Interpreter.cify;

public class StrategySignature extends StrategoTuple {
    public final String name;
    public final int noStrategyArgs;
    public final int noTermArgs;

    public StrategySignature(String name, int noStrategyArgs, int noTermArgs) {
        super(new IStrategoTerm[] { new StrategoString(name, AbstractTermFactory.EMPTY_LIST),
                new StrategoInt(noStrategyArgs), new StrategoInt(noTermArgs) },
            AbstractTermFactory.EMPTY_LIST);
        this.name = name;
        this.noStrategyArgs = noStrategyArgs;
        this.noTermArgs = noTermArgs;
    }

    public StrategySignature(IStrategoString name, IStrategoInt noStrategyArgs,
        IStrategoInt noTermArgs) {
        super(new IStrategoTerm[] { name, noStrategyArgs, noTermArgs },
            AbstractTermFactory.EMPTY_LIST);
        this.name = name.stringValue();
        this.noStrategyArgs = noStrategyArgs.intValue();
        this.noTermArgs = noTermArgs.intValue();
    }

    public String cifiedName() {
        return cify(name) + "_" + noStrategyArgs + "_" + noTermArgs;
    }

    public IStrategoTerm standardType(ITermFactory tf) {
        return standardType(tf, noStrategyArgs, noTermArgs);
    }

    public static IStrategoTerm standardType(ITermFactory tf, int noStrategyArgs, int noTermArgs) {
        final IStrategoAppl sdyn = tf.makeAppl("SDyn");
        final IStrategoList.Builder sargTypes = tf.arrayListBuilder(noStrategyArgs);
        for(int i = 0; i < noStrategyArgs; i++) {
            sargTypes.add(sdyn);
        }
        final IStrategoAppl dyn = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
        final IStrategoList.Builder targTypes = tf.arrayListBuilder(noTermArgs);
        for(int i = 0; i < noTermArgs; i++) {
            targTypes.add(dyn);
        }
        return tf.makeAppl("FunTType", tf.makeList(sargTypes), tf.makeList(targTypes),
            tf.makeAppl("FunNoArgsType", dyn, dyn));
    }

    public Map<StrategySignature, IStrategoTerm> dynamicRuleSignatures(ITermFactory tf) {
        final String n = cify(this.name);
        final int s = this.noStrategyArgs;
        final int t = this.noTermArgs;
        final Map<StrategySignature, IStrategoTerm> result = new HashMap<>(40);
        result.put(this, standardType(tf));
        result.put(new StrategySignature("new-" + n, 0, 2), standardType(tf, 0, 2));
        result.put(new StrategySignature("undefine-" + n, 0, 1), standardType(tf, 0, 1));
        result.put(new StrategySignature("aux-" + n, s, t + 1), standardType(tf, s, t + 1));
        result.put(new StrategySignature("once-" + n, s, t), standardType(tf, s, t));
        result.put(new StrategySignature("bagof-" + n, s, t), standardType(tf, s, t));
        result
            .put(new StrategySignature("reverse-bagof-" + n, s + 1, t), standardType(tf, s + 1, t));
        result.put(new StrategySignature("bigbagof-" + n, s, t), standardType(tf, s, t));
        result.put(new StrategySignature("all-keys-" + n, s, t), standardType(tf, s, t));
        result.put(new StrategySignature("innermost-scope-" + n, s, t), standardType(tf, s, t));
        result.put(new StrategySignature("break-" + n, s, t), standardType(tf, s, t));
        result.put(new StrategySignature("break-to-label-" + n, s, t + 1),
            standardType(tf, s, t + 1));
        result.put(new StrategySignature("break-bp-" + n, s, t), standardType(tf, s, t));
        result.put(new StrategySignature("continue-" + n, s, t), standardType(tf, s, t));
        result.put(new StrategySignature("continue-to-label-" + n, s, t + 1),
            standardType(tf, s, t + 1));
        result
            .put(new StrategySignature("throw-" + n, s + 1, t + 1), standardType(tf, s + 1, t + 1));
        result.put(new StrategySignature("fold-" + n, s + 1, t), standardType(tf, s + 1, t));
        result.put(new StrategySignature("bigfold-" + n, s + 1, t), standardType(tf, s + 1, t));
        result.put(new StrategySignature("chain-" + n, s, t), standardType(tf, s, t));
        result.put(new StrategySignature("bigchain-" + n, s, t), standardType(tf, s, t));
        return result;
    }

    public static boolean isCified(String name) {
        try {
            int lastUnderlineOffset = name.lastIndexOf('_');
            if(lastUnderlineOffset == -1) {
                return false;
            }
            Integer.parseInt(name.substring(lastUnderlineOffset + 1));
            int penultimateUnderlineOffset = name.lastIndexOf('_', lastUnderlineOffset - 1);
            if(penultimateUnderlineOffset == -1) {
                return false;
            }
            Integer.parseInt(name.substring(penultimateUnderlineOffset + 1, lastUnderlineOffset));
        } catch(RuntimeException e) {
            return false;
        }
        return true;
    }

    public static @Nullable StrategySignature fromCified(String cifiedName) {
        try {
            int lastUnderlineOffset = cifiedName.lastIndexOf('_');
            if(lastUnderlineOffset == -1) {
                return null;
            }
            int termArity = Integer.parseInt(cifiedName.substring(lastUnderlineOffset + 1));
            int penultimateUnderlineOffset = cifiedName.lastIndexOf('_', lastUnderlineOffset - 1);
            if(penultimateUnderlineOffset == -1) {
                return null;
            }
            int strategyArity = Integer.parseInt(
                cifiedName.substring(penultimateUnderlineOffset + 1, lastUnderlineOffset));
            return new StrategySignature(
                SDefT.unescape(cifiedName.substring(0, penultimateUnderlineOffset)), strategyArity,
                termArity);
        } catch(NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable StrategySignature fromTuple(IStrategoTerm tuple) {
        if(!TermUtils.isTuple(tuple) && tuple.getSubtermCount() == 3) {
            return null;
        }
        if(!TermUtils.isStringAt(tuple, 0) || !TermUtils.isIntAt(tuple, 1) || !TermUtils
            .isIntAt(tuple, 2)) {
            return null;
        }
        return new StrategySignature(TermUtils.toStringAt(tuple, 0), TermUtils.toIntAt(tuple, 1),
            TermUtils.toIntAt(tuple, 2));
    }
}
