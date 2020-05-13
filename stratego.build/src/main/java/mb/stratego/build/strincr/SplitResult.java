package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

import io.usethesource.capsule.BinaryRelation;
import mb.stratego.build.util.Relation;

import static org.spoofax.interpreter.core.Interpreter.cify;

public class SplitResult {
    public final String moduleName;
    public final String inputFileString;
    public final List<IStrategoTerm> imports;
    public final Map<StrategySignature, IStrategoTerm> strategyDefs;
    public final Map<ConstructorSignature, IStrategoTerm> consDefs;
    public final Map<ConstructorSignature, IStrategoTerm> olayDefs;
    public final Map<StrategySignature, IStrategoTerm> defTypes;
    public final Set<StrategySignature> dynRuleSigs;
    public final BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm> consTypes;
    public final BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections;

    public SplitResult(String moduleName, String inputFileString, List<IStrategoTerm> imports,
        Map<StrategySignature, IStrategoTerm> strategyDefs, Map<ConstructorSignature, IStrategoTerm> consDefs,
        Map<ConstructorSignature, IStrategoTerm> olayDefs, Map<StrategySignature, IStrategoTerm> defTypes,
        Set<StrategySignature> dynRuleSigs,
        BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm> consTypes,
        BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections) {
        this.moduleName = moduleName;
        this.inputFileString = inputFileString;
        this.imports = imports;
        this.strategyDefs = strategyDefs;
        this.consDefs = consDefs;
        this.olayDefs = olayDefs;
        this.defTypes = defTypes;
        this.dynRuleSigs = dynRuleSigs;
        this.consTypes = consTypes;
        this.injections = injections;
    }

    public static SplitResult fromTerm(IStrategoTerm splitTerm, String inputFileString) {
        final String moduleName = TermUtils.toJavaStringAt(splitTerm, 0);
        final IStrategoList imps = TermUtils.toListAt(splitTerm, 1);
        final IStrategoList strats = TermUtils.toListAt(splitTerm, 2);
        final IStrategoList cons = TermUtils.toListAt(splitTerm, 3);
        final IStrategoList olays = TermUtils.toListAt(splitTerm, 4);
        final IStrategoList deftys = TermUtils.toListAt(splitTerm, 5);
        final IStrategoList dynRuleSignatures = TermUtils.toListAt(splitTerm, 6);
        final IStrategoList consTypePairs = TermUtils.toListAt(splitTerm, 7);
        final IStrategoList injPairs = TermUtils.toListAt(splitTerm, 8);

        final List<IStrategoTerm> imports = imps.getSubterms();

        final Map<StrategySignature, IStrategoTerm> strategyDefs = stratAssocListToMapOfLists(strats);
        final Map<ConstructorSignature, IStrategoTerm> consDefs = consAssocListToMap(cons);
        final Map<ConstructorSignature, IStrategoTerm> olayDefs = consAssocListToMap(olays);
        final Map<StrategySignature, IStrategoTerm> defTypes = stratAssocListToMap(deftys);

        final Set<StrategySignature> dynRuleSigs = new HashSet<>(dynRuleSignatures.size() * 2);
        for(IStrategoTerm dynRuleSignature : dynRuleSignatures) {
            dynRuleSigs.add(StrategySignature.fromTuple(dynRuleSignature));
        }

        final BinaryRelation.Transient<ConstructorSignature, IStrategoTerm> consTypes = BinaryRelation.Transient.of();
        for(IStrategoTerm consTypePair : consTypePairs) {
            ConstructorSignature consSig = ConstructorSignature.fromTuple(consTypePair.getSubterm(0));
            Objects.requireNonNull(consSig,
                () -> "Cannot turn term " + consTypePair.getSubterm(0) + " into a constructor signature. Not a pair of a string and an int?");
            IStrategoTerm consType = consTypePair.getSubterm(1);
            consTypes.__insert(consSig, consType);
        }
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> injections = BinaryRelation.Transient.of();
        for(IStrategoTerm injPair : injPairs) {
            IStrategoTerm fromType = injPair.getSubterm(0);
            IStrategoTerm toType = injPair.getSubterm(1);
            injections.__insert(fromType, toType);
        }

        return new SplitResult(moduleName, inputFileString, imports, strategyDefs, consDefs, olayDefs, defTypes,
            dynRuleSigs, consTypes.freeze(), injections.freeze());
    }

    private static Map<StrategySignature, IStrategoTerm> stratAssocListToMapOfLists(final IStrategoList assocList) {
        final Map<StrategySignature, List<IStrategoTerm>> resultMap = new HashMap<>(assocList.size() * 2);
        for(IStrategoTerm pair : assocList) {
            final StrategySignature sig = StrategySignature.fromTuple(pair.getSubterm(0));
            Objects.requireNonNull(sig,
                () -> "Cannot turn term " + pair.getSubterm(0) + " into a strategy signature. Not a pair of a string and two ints?");
            final IStrategoTerm def = pair.getSubterm(1);
            Relation.getOrInitialize(resultMap, sig, ArrayList::new).add(def);
        }
        return packMapValues(resultMap);
    }

    private static Map<StrategySignature, IStrategoTerm> stratAssocListToMap(final IStrategoList assocList) {
        final Map<StrategySignature, IStrategoTerm> resultMap = new HashMap<>(assocList.size() * 2);
        for(IStrategoTerm pair : assocList) {
            final StrategySignature sig = StrategySignature.fromTuple(pair.getSubterm(0));
            Objects.requireNonNull(sig,
                () -> "Cannot turn term " + pair.getSubterm(0) + " into a strategy signature. Not a pair of a string and two ints?");
            final IStrategoTerm value = pair.getSubterm(1);
            if(!resultMap.containsKey(sig)) {
                resultMap.put(sig, value);
            }
        }
        return resultMap;
    }

    private static Map<ConstructorSignature, IStrategoTerm> consAssocListToMap(final IStrategoList assocList) {
        final Map<ConstructorSignature, List<IStrategoTerm>> resultMap = new HashMap<>(assocList.size() * 2);
        for(IStrategoTerm pair : assocList) {
            final ConstructorSignature sig = ConstructorSignature.fromTuple(pair.getSubterm(0));
            if(sig == null) {
                // case where the signature name is Inj() for injections.
                continue;
            }
            final IStrategoTerm def = pair.getSubterm(1);
            Relation.getOrInitialize(resultMap, sig, ArrayList::new).add(def);
        }
        return packMapValues(resultMap);
    }

    private static <K, V extends IStrategoTerm> Map<K, IStrategoTerm> packMapValues(
        final Map<K, List<V>> listOfValuesMap) {
        final Map<K, IStrategoTerm> packedValuesMap = new HashMap<>(listOfValuesMap.size() * 2);
        for(Map.Entry<K, List<V>> e : listOfValuesMap.entrySet()) {
            packedValuesMap.put(e.getKey(), B.list(e.getValue()));
        }
        return packedValuesMap;
    }

    public static class StrategySignature extends StrategoTuple implements Serializable {
        public final String name;
        public final int noStrategyArgs;
        public final int noTermArgs;

        public StrategySignature(String name, int noStrategyArgs, int noTermArgs) {
            super(new IStrategoTerm[] { new StrategoString(name, AbstractTermFactory.EMPTY_LIST),
                new StrategoInt(noStrategyArgs), new StrategoInt(noTermArgs) }, AbstractTermFactory.EMPTY_LIST);
            this.name = name;
            this.noStrategyArgs = noStrategyArgs;
            this.noTermArgs = noTermArgs;
        }

        public StrategySignature(IStrategoString name, IStrategoInt noStrategyArgs, IStrategoInt noTermArgs) {
            super(new IStrategoTerm[] { name, noStrategyArgs, noTermArgs }, AbstractTermFactory.EMPTY_LIST);
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
            return tf.makeAppl("FunTType", tf.makeList(sargTypes), tf.makeList(targTypes), dyn, dyn);
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
            result.put(new StrategySignature("reverse-bagof-" + n, s + 1, t), standardType(tf, s + 1, t));
            result.put(new StrategySignature("bigbagof-" + n, s, t), standardType(tf, s, t));
            result.put(new StrategySignature("all-keys-" + n, s, t), standardType(tf, s, t));
            result.put(new StrategySignature("innermost-scope-" + n, s, t), standardType(tf, s, t));
            result.put(new StrategySignature("break-" + n, s, t), standardType(tf, s, t));
            result.put(new StrategySignature("break-to-label-" + n, s, t + 1), standardType(tf, s, t + 1));
            result.put(new StrategySignature("break-bp-" + n, s, t), standardType(tf, s, t));
            result.put(new StrategySignature("continue-" + n, s, t), standardType(tf, s, t));
            result.put(new StrategySignature("continue-to-label-" + n, s, t + 1), standardType(tf, s, t + 1));
            result.put(new StrategySignature("throw-" + n, s + 1, t + 1), standardType(tf, s + 1, t + 1));
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
                Integer.parseInt(name.substring(lastUnderlineOffset+1));
                int penultimateUnderlineOffset = name.lastIndexOf('_', lastUnderlineOffset-1);
                if(penultimateUnderlineOffset == -1) {
                    return false;
                }
                Integer.parseInt(name.substring(penultimateUnderlineOffset+1, lastUnderlineOffset));
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
                int termArity = Integer.parseInt(cifiedName.substring(lastUnderlineOffset+1));
                int penultimateUnderlineOffset = cifiedName.lastIndexOf('_', lastUnderlineOffset-1);
                if(penultimateUnderlineOffset == -1) {
                    return null;
                }
                int strategyArity = Integer.parseInt(cifiedName.substring(penultimateUnderlineOffset+1, lastUnderlineOffset));
                return new StrategySignature(SDefT.unescape(cifiedName.substring(0, penultimateUnderlineOffset)), strategyArity, termArity);
            } catch(NumberFormatException e) {
                return null;
            }
        }

        public static @Nullable StrategySignature fromTuple(IStrategoTerm tuple) {
            if(!TermUtils.isTuple(tuple) && tuple.getSubtermCount() == 3) {
                return null;
            }
            if(!TermUtils.isStringAt(tuple, 0) || !TermUtils.isIntAt(tuple, 1) || !TermUtils.isIntAt(tuple, 2)) {
                return null;
            }
            return new StrategySignature(TermUtils.toStringAt(tuple, 0), TermUtils.toIntAt(tuple, 1),
                TermUtils.toIntAt(tuple, 2));
        }
    }

    public static class ConstructorSignature extends StrategoTuple implements Serializable {
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
                Integer.parseInt(name.substring(lastUnderlineOffset+1));
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
                int arity = Integer.parseInt(cifiedName.substring(lastUnderlineOffset+1));
                return new ConstructorSignature(SDefT.unescape(cifiedName.substring(0, lastUnderlineOffset)), arity);
            } catch(NumberFormatException e) {
                return null;
            }
        }

        public static @Nullable ConstructorSignature fromTuple(IStrategoTerm tuple) {
            if(!TermUtils.isTuple(tuple) || tuple.getSubtermCount() != 2 || !TermUtils.isIntAt(tuple, 1)) {
                return null;
            }
            if(TermUtils.isStringAt(tuple, 0)) {
                return new ConstructorSignature(TermUtils.toStringAt(tuple, 0), TermUtils.toIntAt(tuple, 1));
            }
            if(TermUtils.isApplAt(tuple, 0) && TermUtils.tryGetName(tuple.getSubterm(0)).map(n -> n.equals("Q"))
                .orElse(false)) {
                final String escapedNameString =
                    StringUtils.escape(TermUtils.toStringAt(tuple.getSubterm(0), 0).stringValue());
                final StrategoString escapedName =
                    new StrategoString(escapedNameString, AbstractTermFactory.EMPTY_LIST);
                AbstractTermFactory.staticCopyAttachments(tuple.getSubterm(0), escapedName);
                return new ConstructorSignature(escapedName, TermUtils.toIntAt(tuple, 1));
            }
            return null;
        }

        public StrategySignature toCongruenceSig() {
            return new StrategySignature(name, noArgs, 0);
        }
    }
}
