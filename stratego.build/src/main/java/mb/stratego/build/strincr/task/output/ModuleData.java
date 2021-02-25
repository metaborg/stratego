package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.WithLastModified;

/**
 * The AST of a module and some of it's data pre-extracted.
 */
public class ModuleData implements Serializable, WithLastModified {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final IStrategoTerm ast;
    public final ArrayList<IStrategoTerm> imports;
    public final HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData;
    public final HashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData;
    public final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections;
    public final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections;
    public final HashMap<StrategySignature, HashSet<StrategyFrontData>> normalStrategyData;
    public final HashMap<StrategySignature, HashSet<StrategyFrontData>> internalStrategyData;
    public final HashMap<StrategySignature, HashSet<StrategyFrontData>> externalStrategyData;
    public final HashMap<StrategySignature, HashSet<StrategyFrontData>> dynamicRuleData;
    public final HashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData;
    public final HashSet<ConstructorSignature> usedConstructors;
    public final HashSet<StrategySignature> usedStrategies;
    public final HashSet<StrategySignature> dynamicRules;
    public final HashSet<String> usedAmbiguousStrategies;
    public final long lastModified;
    private transient @Nullable HashMap<String, HashSet<StrategyFrontData>> ambStrategyIndex = null;

    public ModuleData(IModuleImportService.ModuleIdentifier moduleIdentifier, IStrategoTerm ast,
        ArrayList<IStrategoTerm> imports,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections,
        HashMap<StrategySignature, HashSet<StrategyFrontData>> normalStrategyData,
        HashMap<StrategySignature, HashSet<StrategyFrontData>> internalStrategyData,
        HashMap<StrategySignature, HashSet<StrategyFrontData>> externalStrategyData,
        HashMap<StrategySignature, HashSet<StrategyFrontData>> dynamicRuleData,
        HashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData,
        HashSet<ConstructorSignature> usedConstructors, HashSet<StrategySignature> usedStrategies,
        HashSet<StrategySignature> dynamicRules, HashSet<String> usedAmbiguousStrategies,
        long lastModified) {
        this.moduleIdentifier = moduleIdentifier;
        this.ast = ast;
        this.imports = imports;
        this.constrData = constrData;
        this.externalConstrData = externalConstrData;
        this.injections = injections;
        this.externalInjections = externalInjections;
        this.normalStrategyData = normalStrategyData;
        this.internalStrategyData = internalStrategyData;
        this.externalStrategyData = externalStrategyData;
        this.dynamicRuleData = dynamicRuleData;
        this.overlayData = overlayData;
        this.usedConstructors = usedConstructors;
        this.usedStrategies = usedStrategies;
        this.dynamicRules = dynamicRules;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
        this.lastModified = lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleData that = (ModuleData) o;

        if(lastModified != that.lastModified)
            return false;
        if(!moduleIdentifier.equals(that.moduleIdentifier))
            return false;
        if(!ast.equals(that.ast))
            return false;
        if(!imports.equals(that.imports))
            return false;
        if(!constrData.equals(that.constrData))
            return false;
        if(!externalConstrData.equals(that.externalConstrData))
            return false;
        if(!injections.equals(that.injections))
            return false;
        if(!externalInjections.equals(that.externalInjections))
            return false;
        if(!normalStrategyData.equals(that.normalStrategyData))
            return false;
        if(!internalStrategyData.equals(that.internalStrategyData))
            return false;
        if(!externalStrategyData.equals(that.externalStrategyData))
            return false;
        if(!dynamicRuleData.equals(that.dynamicRuleData))
            return false;
        if(!overlayData.equals(that.overlayData))
            return false;
        if(!usedConstructors.equals(that.usedConstructors))
            return false;
        if(!usedStrategies.equals(that.usedStrategies))
            return false;
        if(!dynamicRules.equals(that.dynamicRules))
            return false;
        return usedAmbiguousStrategies.equals(that.usedAmbiguousStrategies);
    }

    @Override public int hashCode() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + ast.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + constrData.hashCode();
        result = 31 * result + externalConstrData.hashCode();
        result = 31 * result + injections.hashCode();
        result = 31 * result + externalInjections.hashCode();
        result = 31 * result + normalStrategyData.hashCode();
        result = 31 * result + internalStrategyData.hashCode();
        result = 31 * result + externalStrategyData.hashCode();
        result = 31 * result + dynamicRuleData.hashCode();
        result = 31 * result + overlayData.hashCode();
        result = 31 * result + usedConstructors.hashCode();
        result = 31 * result + usedStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + usedAmbiguousStrategies.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "ModuleData(" + moduleIdentifier + ')';
    }

    public HashMap<String, HashSet<StrategyFrontData>> ambStrategyIndex() {
        if(ambStrategyIndex == null) {
            ambStrategyIndex = new HashMap<>();
            for(Map.Entry<StrategySignature, HashSet<StrategyFrontData>> e : normalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, HashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, HashSet<StrategyFrontData>> e : internalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, HashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, HashSet<StrategyFrontData>> e : externalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, HashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, HashSet<StrategyFrontData>> e : dynamicRuleData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, HashSet::new)
                    .addAll(e.getValue());
            }
        }
        return ambStrategyIndex;
    }

    @Override public long lastModified() {
        return lastModified;
    }

}
