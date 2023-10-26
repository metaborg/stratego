package mb.stratego.build.strincr.data;

import java.util.HashMap;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.stratego.SDefT;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.util.TermUtils;

import static org.spoofax.interpreter.core.Interpreter.cify;

public class StrategySignature extends StrategoTuple implements Comparable<StrategySignature> {
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

    public StrategySignature(IStrategoString name, int noStrategyArgs,
        int noTermArgs) {
        super(new IStrategoTerm[] { name, new StrategoInt(noStrategyArgs), new StrategoInt(noTermArgs) },
            AbstractTermFactory.EMPTY_LIST);
        this.name = name.stringValue();
        this.noStrategyArgs = noStrategyArgs;
        this.noTermArgs = noTermArgs;
    }

    public String cifiedName() {
        return cify(name) + "_" + noStrategyArgs + "_" + noTermArgs;
    }

    public StrategyType.Standard standardType(ITermFactory tf) {
        return StrategyType.Standard.fromArity(tf, noStrategyArgs, noTermArgs);
    }

    public HashMap<StrategySignature, StrategyType> dynamicRuleSignatures(ITermFactory tf) {
        final String n = this.name;
        final int s = this.noStrategyArgs;
        final int t = this.noTermArgs;
        final HashMap<StrategySignature, StrategyType> result = new HashMap<>(40);
        add(result, this, tf);
        add(result, new StrategySignature("new-" + n, 0, 2), tf);
        add(result, new StrategySignature("undefine-" + n, 0, 1), tf);
        // The aux- rules created depend on the free variables in the lhs of dynamic rules
        //     Therefore they are constructed in {@see CollectDynRuleSigs}
        add(result, new StrategySignature("once-" + n, s, t), tf);
        add(result, new StrategySignature("bagof-" + n, s, t), tf);
        add(result, new StrategySignature("reverse-bagof-" + n, s + 1, t), tf);
        add(result, new StrategySignature("bigbagof-" + n, s, t), tf);
        add(result, new StrategySignature("all-keys-" + n, s, t), tf);
        add(result, new StrategySignature("innermost-scope-" + n, 1, 0), tf);
        add(result, new StrategySignature("break-" + n, s, t), tf);
        add(result, new StrategySignature("break-to-label-" + n, s, t + 1), tf);
        add(result, new StrategySignature("break-bp-" + n, s, t), tf);
        add(result, new StrategySignature("continue-" + n, s, t), tf);
        add(result, new StrategySignature("continue-to-label-" + n, s, t + 1), tf);
        add(result, new StrategySignature("throw-" + n, s + 1, t + 1), tf);
        add(result, new StrategySignature("fold-" + n, s + 1, t), tf);
        add(result, new StrategySignature("bigfold-" + n, s + 1, t), tf);
        add(result, new StrategySignature("chain-" + n, s, t), tf);
        add(result, new StrategySignature("bigchain-" + n, s, t), tf);
        return result;
    }

    public static void add(HashMap<StrategySignature, StrategyType> map, StrategySignature sig,
        ITermFactory tf) {
        map.put(sig, sig.standardType(tf));
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
        return new StrategySignature(name, sArity, tArity);
    }

    public static @Nullable StrategySignature fromDefinition(IStrategoTerm term) {
        if(!TermUtils.isAppl(term)) {
            return null;
        }
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
            case "ExtTypedDef":{
                final IStrategoTerm sargs = term.getSubterm(1).getSubterm(0);
                if(!TermUtils.isList(sargs)) {
                    return null;
                }
                sArity = sargs.getSubtermCount();
                final IStrategoTerm targs = term.getSubterm(1).getSubterm(1);
                if(!TermUtils.isList(targs)) {
                    return null;
                }
                tArity = targs.getSubtermCount();
                break;
            }
            case "ExtTypedDefInl":
                // fall-through
            case "ExtSDef":
                // fall-through
            case "ExtSDefInl":
                // fall-through
            case "SDefT":
                // fall-through
            case "RDefT":
                // fall-through
            case "SDefP":
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
        final IStrategoString name = TermUtils.toStringAt(term, 0);
        return new StrategySignature(name, sArity, tArity);
    }

    public static @Nullable StrategySignature fromTuple(IStrategoTerm tuple) {
        if(!(TermUtils.isTuple(tuple, 3) && TermUtils.isStringAt(tuple, 0) && TermUtils.isIntAt(
            tuple, 1) && TermUtils.isIntAt(tuple, 2))) {
            return null;
        }
        return new StrategySignature(TermUtils.toStringAt(tuple, 0),
            TermUtils.toJavaIntAt(tuple, 1), TermUtils.toJavaIntAt(tuple, 2));
    }

    @Override public int compareTo(StrategySignature o) {
        final int nameComparison = this.name.compareTo(o.name);
        if(nameComparison != 0) {
            return nameComparison;
        }
        final int noStrategyArgsComparison = Integer.compare(this.noStrategyArgs, o.noStrategyArgs);
        if(noStrategyArgsComparison != 0) {
            return noStrategyArgsComparison;
        }
        return Integer.compare(this.noTermArgs, o.noTermArgs);
    }

    /**
     * Interpret strategy signature as congruence
     *
     * @return constructor signature corresponding with this strategy being it's congruence. Returns null if strategy has term arguments.
     */
    public @Nullable ConstructorSignature toConstructorSignature() {
        if(noTermArgs != 0) {
            return null;
        }
        return new ConstructorSignature(name, noStrategyArgs);
    }

    // equals/hashcode/toString inherited from StrategoTuple
}
