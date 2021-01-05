package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import io.usethesource.capsule.BinaryRelation;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.TermEqWithAttachments;

public class SplitResult implements Serializable {
    public final String moduleName;
    public final String inputFileString;
    public final List<IStrategoTerm> imports;
    public final Map<StrategySignature, IStrategoTerm> strategyDefs;
    public final Map<ConstructorSignature, List<IStrategoTerm>> consDefs;
    public final Map<ConstructorSignature, IStrategoTerm> olayDefs;
    public final Map<StrategySignature, IStrategoTerm> defTypes;
    public final Set<StrategySignature> dynRuleSigs;
    public final BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm> consTypes;
    public final BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections;

    public SplitResult(String moduleName, String inputFileString, List<IStrategoTerm> imports,
        Map<StrategySignature, IStrategoTerm> strategyDefs, Map<ConstructorSignature, List<IStrategoTerm>> consDefs,
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

        final List<IStrategoTerm> imports = imps.getSubterms().stream().map(TermEqWithAttachments::new).collect(
            Collectors.toList());

        final Map<StrategySignature, IStrategoTerm> strategyDefs = stratAssocListToMapOfLists(strats);
        final Map<ConstructorSignature, List<IStrategoTerm>> consDefs = consAssocListToRel(cons);
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
            IStrategoTerm consType = new TermEqWithAttachments(consTypePair.getSubterm(1));
            consTypes.__insert(consSig, consType);
        }
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> injections = BinaryRelation.Transient.of();
        for(IStrategoTerm injPair : injPairs) {
            IStrategoTerm fromType = new TermEqWithAttachments(injPair.getSubterm(0));
            IStrategoTerm toType = new TermEqWithAttachments(injPair.getSubterm(1));
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
            final IStrategoTerm def = new TermEqWithAttachments(pair.getSubterm(1));
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
            final IStrategoTerm value = new TermEqWithAttachments(pair.getSubterm(1));
            if(!resultMap.containsKey(sig)) {
                resultMap.put(sig, value);
            }
        }
        return resultMap;
    }

    private static Map<ConstructorSignature, List<IStrategoTerm>> consAssocListToRel(final IStrategoList assocList) {
        final Map<ConstructorSignature, List<IStrategoTerm>> resultMap = new HashMap<>(assocList.size() * 2);
        for(IStrategoTerm pair : assocList) {
            final ConstructorSignature sig = ConstructorSignature.fromTuple(pair.getSubterm(0));
            if(sig == null) {
                // case where the signature name is Inj() for injections.
                continue;
            }
            final IStrategoTerm def = new TermEqWithAttachments(pair.getSubterm(1));
            Relation.getOrInitialize(resultMap, sig, ArrayList::new).add(def);
        }
        return resultMap;
    }

    private static Map<ConstructorSignature, IStrategoTerm> consAssocListToMap(final IStrategoList assocList) {
        return packMapValues(consAssocListToRel(assocList));
    }

    private static <K, V extends IStrategoTerm> Map<K, IStrategoTerm> packMapValues(
        final Map<K, List<V>> listOfValuesMap) {
        final Map<K, IStrategoTerm> packedValuesMap = new HashMap<>(listOfValuesMap.size() * 2);
        for(Map.Entry<K, List<V>> e : listOfValuesMap.entrySet()) {
            packedValuesMap.put(e.getKey(), B.list(e.getValue()));
        }
        return packedValuesMap;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SplitResult that = (SplitResult)o;
        return Objects.equals(moduleName, that.moduleName) &&
            Objects.equals(inputFileString, that.inputFileString) &&
            Objects.equals(imports, that.imports) &&
            Objects.equals(strategyDefs, that.strategyDefs) &&
            Objects.equals(consDefs, that.consDefs) &&
            Objects.equals(olayDefs, that.olayDefs) &&
            Objects.equals(defTypes, that.defTypes) &&
            Objects.equals(dynRuleSigs, that.dynRuleSigs) &&
            Objects.equals(consTypes, that.consTypes) &&
            Objects.equals(injections, that.injections);
    }

    @Override public int hashCode() {
        return Objects.hash(moduleName, inputFileString, imports, strategyDefs, consDefs, olayDefs, defTypes, dynRuleSigs, consTypes, injections);
    }
}
