package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.spoofax.interpreter.stratego.SDefT;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import io.usethesource.capsule.BinaryRelation;
import mb.stratego.build.util.Relation;

import static org.spoofax.interpreter.core.Interpreter.cify;

public class SplitResult {
    public final String moduleName;
    public final String inputFileString;
    public final List<IStrategoTerm> imports;
    public final Map<String, IStrategoTerm> strategyDefs;
    public final Map<String, IStrategoTerm> consDefs;
    public final Map<String, IStrategoTerm> olayDefs;
    public final Map<String, IStrategoTerm> defTypes;
    public final Set<StrategySignature> dynRuleSigs;
    public final BinaryRelation.Immutable<String, IStrategoTerm> consTypes;
    public final BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections;

    public SplitResult(String moduleName, String inputFileString, List<IStrategoTerm> imports, Map<String, IStrategoTerm> strategyDefs,
        Map<String, IStrategoTerm> consDefs, Map<String, IStrategoTerm> olayDefs, Map<String, IStrategoTerm> defTypes,
        Set<StrategySignature> dynRuleSigs, BinaryRelation.Immutable<String, IStrategoTerm> consTypes,
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

        final Map<String, List<IStrategoTerm>> resultMap = new HashMap<>(strats.size() * 2);
        for(IStrategoTerm pair : strats) {
            // pair == (name1, Strategies([def]))
            final String name1 = TermUtils.toJavaStringAt(pair, 0);
            final IStrategoTerm def = pair.getSubterm(1).getSubterm(0).getSubterm(0);
            Relation.getOrInitialize(resultMap, name1, ArrayList::new).add(def);
        }
        final Map<String, IStrategoTerm> strategyDefs = packMapValues(resultMap);
        final Map<String, IStrategoTerm> consDefs = assocListToMap(cons);
        final Map<String, IStrategoTerm> olayDefs = assocListToMap(olays);
        final Map<String, IStrategoTerm> defTypes = assocListToMap(deftys);

        final Set<StrategySignature> dynRuleSigs = new HashSet<>(dynRuleSignatures.size() * 2);
        for(IStrategoTerm dynRuleSignature : dynRuleSignatures) {
            final String name = TermUtils.toJavaStringAt(dynRuleSignature, 0);
            final int noStrategyArgs = TermUtils.toJavaIntAt(dynRuleSignature, 1);
            final int noTermArgs = TermUtils.toJavaIntAt(dynRuleSignature, 2);
            dynRuleSigs.add(new StrategySignature(name, noStrategyArgs, noTermArgs));
        }

        final BinaryRelation.Transient<String, IStrategoTerm> consTypes = BinaryRelation.Transient.of();
        for(IStrategoTerm consTypePair : consTypePairs) {
            IStrategoTuple consSig = TermUtils.toTupleAt(consTypePair, 0);
            IStrategoTerm consType = consTypePair.getSubterm(1);
            consTypes.__insert(consSigToString(consSig), consType);
        }
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> injections = BinaryRelation.Transient.of();
        for(IStrategoTerm injPair : injPairs) {
            IStrategoTerm fromType = injPair.getSubterm(0);
            IStrategoTerm toType = injPair.getSubterm(1);
            injections.__insert(fromType, toType);
        }

        return new SplitResult(moduleName, inputFileString, imports, strategyDefs, consDefs, olayDefs, defTypes, dynRuleSigs, consTypes.freeze(),
            injections.freeze());
    }

    public static String consSigToString(IStrategoTuple consSig) {
        return TermUtils.toJavaStringAt(consSig, 0) + "_" + TermUtils.toJavaIntAt(consSig, 1);
    }

    private static Map<String, IStrategoTerm> assocListToMap(final IStrategoList assocList) {
        final Map<String, List<IStrategoTerm>> resultMap = new HashMap<>(assocList.size() * 2);
        for(IStrategoTerm pair : assocList) {
            final String name = TermUtils.toJavaStringAt(pair, 0);
            final IStrategoTerm def = pair.getSubterm(1);
            Relation.getOrInitialize(resultMap, name, ArrayList::new).add(def);
        }
        return packMapValues(resultMap);
    }

    private static Map<String, IStrategoTerm> packMapValues(final Map<String, List<IStrategoTerm>> listOfValuesMap) {
        final Map<String, IStrategoTerm> packedValuesMap = new HashMap<>(listOfValuesMap.size() * 2);
        for(Map.Entry<String, List<IStrategoTerm>> e : listOfValuesMap.entrySet()) {
            packedValuesMap.put(e.getKey(), B.list(e.getValue()));
        }
        return packedValuesMap;
    }

    public static class StrategySignature implements Serializable {
        public final String name;
        public final int noStrategyArgs;
        public final int noTermArgs;

        public StrategySignature(String name, int noStrategyArgs, int noTermArgs) {
            this.name = name;
            this.noStrategyArgs = noStrategyArgs;
            this.noTermArgs = noTermArgs;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(!(o instanceof StrategySignature))
                return false;
            StrategySignature that = (StrategySignature) o;
            return noStrategyArgs == that.noStrategyArgs && noTermArgs == that.noTermArgs && Objects
                .equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, noStrategyArgs, noTermArgs);
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
            final IStrategoList.Builder targTypes = tf.arrayListBuilder(noStrategyArgs);
            for(int i = 0; i < noTermArgs; i++) {
                targTypes.add(dyn);
            }
            return tf.makeAppl("FunTType", tf.makeList(sargTypes), tf.makeList(targTypes), dyn, dyn);
        }

        public Map<IStrategoString, IStrategoTerm> dynamicRuleSignatures(ITermFactory tf) {
            final String n = cify(this.name);
            final int s = this.noStrategyArgs;
            final int t = this.noTermArgs;
            final Map<IStrategoString, IStrategoTerm> result = new HashMap<>(40);
            result.put(tf.makeString(cifiedName()), standardType(tf));
            result.put(tf.makeString("new_" + n + "_0_2"), standardType(tf, 0, 2));
            result.put(tf.makeString("undefine_" + n + "_0_1"), standardType(tf, 0, 1));
            result.put(tf.makeString("aux_" + n + "_" + s + "_" + (t + 1)), standardType(tf, s, t + 1));
            result.put(tf.makeString("once_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("bagof_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("reverse_bagof_" + n + "_" + (s + 1) + "_" + t), standardType(tf, s + 1, t));
            result.put(tf.makeString("bigbagof_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("all_keys_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("innermost_scope_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("break_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("break_to_label_" + n + "_" + s + "_" + (t + 1)), standardType(tf, s, t + 1));
            result.put(tf.makeString("break_bp_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("continue_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("continue_to_label_" + n + "_" + s + "_" + (t + 1)), standardType(tf, s, t + 1));
            result.put(tf.makeString("throw_" + n + "_" + (s + 1) + "_" + (t + 1)), standardType(tf, s + 1, t + 1));
            result.put(tf.makeString("fold_" + n + "_" + (s + 1) + "_" + t), standardType(tf, s + 1, t));
            result.put(tf.makeString("bigfold_" + n + "_" + (s + 1) + "_" + t), standardType(tf, s + 1, t));
            result.put(tf.makeString("chain_" + n + "_" + s + "_" + t), standardType(tf, s, t));
            result.put(tf.makeString("bigchain_" + n + "_" + s + "_" + t), standardType(tf, s, t));
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

        public static StrategySignature fromCified(String cifiedName) throws RuntimeException {
            try {
                int lastUnderlineOffset = cifiedName.lastIndexOf('_');
                if(lastUnderlineOffset == -1) {
                    throw new RuntimeException("Attempted to deconstruct a non-cified name: " + cifiedName);
                }
                int termArity = Integer.parseInt(cifiedName.substring(lastUnderlineOffset+1));
                int penultimateUnderlineOffset = cifiedName.lastIndexOf('_', lastUnderlineOffset-1);
                if(penultimateUnderlineOffset == -1) {
                    throw new RuntimeException("Attempted to deconstruct a non-cified name: " + cifiedName);
                }
                int strategyArity = Integer.parseInt(cifiedName.substring(penultimateUnderlineOffset+1, lastUnderlineOffset));
                return new StrategySignature(SDefT.unescape(cifiedName.substring(0, penultimateUnderlineOffset)), strategyArity, termArity);
            } catch(NumberFormatException e) {
                throw new RuntimeException("Attempted to deconstruct a non-cified name: " + cifiedName, e);
            }
        }
    }
}
