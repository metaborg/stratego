package mb.stratego.build.strincr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.flowspec.terms.StrategoArrayList;
import mb.stratego.build.util.Relation;

public class SplitResult {
    public final String moduleName;
    public final List<IStrategoTerm> imports;
    public final Map<String, IStrategoTerm> strategyDefs;
    public final Map<String, IStrategoTerm> consDefs;
    public final Map<String, IStrategoTerm> olayDefs;

    public SplitResult(String moduleName, List<IStrategoTerm> imports, Map<String, IStrategoTerm> strategyDefs,
        Map<String, IStrategoTerm> consDefs, Map<String, IStrategoTerm> olayDefs) {
        this.moduleName = moduleName;
        this.imports = imports;
        this.strategyDefs = strategyDefs;
        this.consDefs = consDefs;
        this.olayDefs = olayDefs;
    }

    public static SplitResult fromTerm(IStrategoTerm splitTerm) {
        final String moduleName = Tools.javaStringAt(splitTerm, 0);
        final IStrategoList imps = Tools.listAt(splitTerm, 1);
        final IStrategoList strats = Tools.listAt(splitTerm, 2);
        final IStrategoList cons = Tools.listAt(splitTerm, 3);
        final IStrategoList olays = Tools.listAt(splitTerm, 4);

        final List<IStrategoTerm> imports = new ArrayList<>(imps.size());
        for(IStrategoTerm imp : imps) {
            imports.add(imp);
        }

        final Map<String, IStrategoTerm> strategyDefs = assocListToMap(strats);
        final Map<String, IStrategoTerm> consDefs = assocListToMap(cons);
        final Map<String, IStrategoTerm> olayDefs = assocListToMap(olays);

        return new SplitResult(moduleName, imports, strategyDefs, consDefs, olayDefs);
    }

    private static Map<String, IStrategoTerm> assocListToMap(final IStrategoList assocList) {
        final Map<String, List<IStrategoTerm>> resultMap = new HashMap<>(assocList.size() * 2);
        for(IStrategoTerm pair : assocList) {
            final String name = Tools.javaStringAt(pair, 0);
            final IStrategoTerm def = Tools.termAt(pair, 1);
            Relation.getOrInitialize(resultMap, name, ArrayList::new).add(def);
        }
        return packMapValues(resultMap);
    }

    private static Map<String, IStrategoTerm> packMapValues(final Map<String, List<IStrategoTerm>> listOfValuesMap) {
        final Map<String, IStrategoTerm> packedValuesMap = new HashMap<>(listOfValuesMap.size() * 2);
        for(Map.Entry<String, List<IStrategoTerm>> e : listOfValuesMap.entrySet()) {
            packedValuesMap.put(e.getKey(), StrategoArrayList.fromList(e.getValue()));
        }
        return packedValuesMap;
    }
}
