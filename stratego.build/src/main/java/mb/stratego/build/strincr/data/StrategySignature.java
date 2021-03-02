package mb.stratego.build.strincr.data;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.spoofax.interpreter.stratego.SDefT;
import org.spoofax.interpreter.terms.IStrategoInt;
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

    public StrategyType.Standard standardType(ITermFactory tf) {
        return standardType(tf, noStrategyArgs, noTermArgs);
    }

    public static StrategyType.Standard standardType(ITermFactory tf, int noStrategyArgs, int noTermArgs) {
        return StrategyType.Standard.fromArity(tf, noStrategyArgs, noTermArgs);
    }

    public HashMap<StrategySignature, StrategyType> dynamicRuleSignatures(ITermFactory tf) {
        final String n = cify(this.name);
        final int s = this.noStrategyArgs;
        final int t = this.noTermArgs;
        final HashMap<StrategySignature, StrategyType> result = new HashMap<>(40);
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

    public static @Nullable StrategySignature fromCall(IStrategoTerm term) {
        if(!TermUtils.isAppl(term)) {
            return null;
        }
        final int subtermCount = term.getSubtermCount();
        switch(TermUtils.toAppl(term).getName()) {
            case "CallT":
                if(subtermCount != 3) {
                    return null;
                }
                break;
            case "Call":
                if(subtermCount != 2) {
                    return null;
                }
                break;
            case "CallNoArgs":
                if(subtermCount != 1) {
                    return null;
                }
                break;
            default:
                return null;
        }
        final IStrategoString name = TermUtils.toStringAt(term.getSubterm(0), 0);
        final int sArity = subtermCount < 2 ? 0 : TermUtils.toListAt(term, 1).size();
        final int tArity = subtermCount < 3 ? 0 : TermUtils.toListAt(term, 2).size();
        return new StrategySignature(name, new StrategoInt(sArity), new StrategoInt(tArity));
    }

    public static @Nullable StrategySignature fromDefinition(IStrategoTerm term) {
        if(!TermUtils.isAppl(term)) {
            return null;
        }
        final IStrategoString name = TermUtils.toStringAt(term, 0);
        final int sArity;
        final int tArity;
        switch(TermUtils.toAppl(term).getName()) {
            case "SDef":
                // fall-through
            case "RDef": {
                final IStrategoTerm sargs = term.getSubterm(1);
                if(!TermUtils.isList(sargs)) {
                    return null;
                }
                sArity = sargs.getSubtermCount();
                tArity = 0;
                break;
            }
            case "SDefNoArgs":
                // fall-through
            case "RDefNoArgs":
                sArity = 0;
                tArity = 0;
                break;
            case "ExtSDef":
                // fall-through
            case "ExtSDefInl":
                // fall-through
            case "SDefT":
                // fall-through
            case "RDefT":
                // fall-through
            case "RDefP": {
                final IStrategoTerm sargs = term.getSubterm(1);
                if(!TermUtils.isList(sargs)) {
                    return null;
                }
                sArity = sargs.getSubtermCount();
                final IStrategoTerm targs = term.getSubterm(2);
                if(!TermUtils.isList(targs)) {
                    return null;
                }
                tArity = targs.getSubtermCount();
                break;
            }
            default:
                return null;
        }
        return new StrategySignature(name, new StrategoInt(sArity), new StrategoInt(tArity));
    }
}
