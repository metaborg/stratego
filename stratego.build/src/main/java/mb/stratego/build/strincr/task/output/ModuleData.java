package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.task.Front;
import mb.stratego.build.termvisitors.HasDynamicRuleDefinitions;
import mb.stratego.build.util.InvalidASTException;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.WithLastModified;

/**
 * The AST of a module and some of it's data pre-extracted.
 * The imports are pre-resolved and therefore are not purely derived from the ast, same with the
 * messages. Everything in between is not put into hash/equals because it's directly derived from
 * the ast.
 */
public class ModuleData implements Serializable, WithLastModified {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final ArrayList<String> str2LibPackageNames;
    public final IStrategoTerm ast;
    public final ArrayList<IModuleImportService.ModuleIdentifier> imports;
    public final LinkedHashSet<SortSignature> sortData;
    public final LinkedHashSet<SortSignature> externalSortData;
    public final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData;
    public final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections;
    public final LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>>
        normalStrategyData;
    public final LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>>
        internalStrategyData;
    public final LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>>
        externalStrategyData;
    public final LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> dynamicRuleData;
    public final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> overlayData;
    public final LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts;
    public final LinkedHashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsedConstrs;
    public final LinkedHashSet<ConstructorSignature> usedConstructors;
    public final LinkedHashSet<StrategySignature> usedStrategies;
    public final LinkedHashMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules;
    public final LinkedHashSet<String> usedAmbiguousStrategies;
    public final ArrayList<Message> messages;
    public final long lastModified;
    protected final int hashCode;

    private transient @Nullable LinkedHashMap<String, LinkedHashSet<StrategyFrontData>>
        ambStrategyIndex = null;
    private transient @Nullable ArrayList<IStrategoTerm> dynamicRuleDefinitions = null;

    public ModuleData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        ArrayList<String> str2LibPackageName, IStrategoTerm ast,
        ArrayList<IModuleImportService.ModuleIdentifier> imports,
        LinkedHashSet<SortSignature> sortData, LinkedHashSet<SortSignature> externalSortData,
        LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData,
        LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections,
        LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> normalStrategyData,
        LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> internalStrategyData,
        LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> externalStrategyData,
        LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> dynamicRuleData,
        LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> overlayData,
        LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts, LinkedHashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsedConstrs,
        LinkedHashSet<ConstructorSignature> usedConstructors,
        LinkedHashSet<StrategySignature> usedStrategies,
        LinkedHashMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules,
        LinkedHashSet<String> usedAmbiguousStrategies, ArrayList<Message> messages,
        long lastModified) {
        this.moduleIdentifier = moduleIdentifier;
        this.str2LibPackageNames = str2LibPackageName;
        this.ast = ast;
        this.imports = imports;
        this.sortData = sortData;
        this.externalSortData = externalSortData;
        this.constrData = constrData;
        this.externalConstrData = externalConstrData;
        this.injections = injections;
        this.externalInjections = externalInjections;
        this.normalStrategyData = normalStrategyData;
        this.internalStrategyData = internalStrategyData;
        this.externalStrategyData = externalStrategyData;
        this.dynamicRuleData = dynamicRuleData;
        this.overlayData = overlayData;
        this.overlayAsts = overlayAsts;
        this.overlayUsedConstrs = overlayUsedConstrs;
        this.usedConstructors = usedConstructors;
        this.usedStrategies = usedStrategies;
        this.dynamicRules = dynamicRules;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
        this.messages = messages;
        this.lastModified = lastModified;
        this.hashCode = hashFunction();
    }

    public LinkedHashMap<String, LinkedHashSet<StrategyFrontData>> ambStrategyIndex() {
        if(ambStrategyIndex == null) {
            ambStrategyIndex = new LinkedHashMap<>();
            for(Map.Entry<StrategySignature, ArrayList<StrategyFrontData>> e : normalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, LinkedHashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, ArrayList<StrategyFrontData>> e : internalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, LinkedHashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, ArrayList<StrategyFrontData>> e : externalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, LinkedHashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, ArrayList<StrategyFrontData>> e : dynamicRuleData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, LinkedHashSet::new)
                    .addAll(e.getValue());
            }
        }
        return ambStrategyIndex;
    }

    public ArrayList<IStrategoTerm> dynamicRuleDefinitions() {
        if(dynamicRuleDefinitions == null) {
            dynamicRuleDefinitions = new ArrayList<>();
            final IStrategoList defs = Front.getDefs(moduleIdentifier, ast);
            for(IStrategoTerm def : defs) {
                if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                    throw new InvalidASTException(moduleIdentifier, def);
                }
                switch(TermUtils.toAppl(def).getName()) {
                    case "Rules":
                        // fall-through
                    case "Strategies":
                        for(IStrategoTerm strategyDef : def.getSubterm(0)) {
                            if(HasDynamicRuleDefinitions.visit(strategyDef)) {
                                dynamicRuleDefinitions.add(strategyDef);
                            }
                        }
                        break;
                }
            }
        }
        return dynamicRuleDefinitions;
    }

    @Override public boolean equals(Object o) {
        // N.B. since the whole AST is used in equals/hashcode, we don't need to check any of the fields derived from the AST
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleData that = (ModuleData) o;

        if(hashCode != that.hashCode)
            return false;
        if(lastModified != that.lastModified)
            return false;
        if(!moduleIdentifier.equals(that.moduleIdentifier))
            return false;
        if(!ast.equals(that.ast))
            return false;
        if(!imports.equals(that.imports))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + ast.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + messages.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "ModuleData@" + System.identityHashCode(this) + '{'
            + "moduleIdentifier=" + moduleIdentifier
            + ", str2LibPackageName='" + str2LibPackageNames + '\''
            + ", ast=" + ast.toString(4)
            + ", imports=" + imports
            + ", sortData=" + sortData.size()
            + ", externalSortData=" + externalSortData.size()
            + ", constrData=" + constrData.size()
            + ", externalConstrData=" + externalConstrData.size()
            + ", injections=" + injections.size()
            + ", externalInjections=" + externalInjections.size()
            + ", normalStrategyData=" + normalStrategyData.size()
            + ", internalStrategyData=" + internalStrategyData.size()
            + ", externalStrategyData=" + externalStrategyData.size()
            + ", dynamicRuleData=" + dynamicRuleData.size()
            + ", overlayData=" + overlayData.size()
            + ", overlayUsedConstrs=" + overlayUsedConstrs.size()
            + ", usedConstructors=" + usedConstructors.size()
            + ", usedStrategies=" + usedStrategies.size()
            + ", dynamicRules=" + dynamicRules.size()
            + ", usedAmbiguousStrategies=" + usedAmbiguousStrategies.size()
            + ", messages=" + messages.size()
            + ", lastModified=" + lastModified
            + (ambStrategyIndex == null ? "" : ", ambStrategyIndex=" + ambStrategyIndex.size())
            + '}';
        //@formatter:on
    }

    @Override public long lastModified() {
        return lastModified;
    }
}
