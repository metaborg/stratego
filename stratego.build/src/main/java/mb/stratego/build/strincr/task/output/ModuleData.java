package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.WithLastModified;

/**
 * The AST of a module and some of it's data pre-extracted.
 */
public class ModuleData implements Serializable, WithLastModified {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final IStrategoTerm ast;
    public final ArrayList<IModuleImportService.ModuleIdentifier> imports;
    public final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData;
    public final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections;
    public final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>>
        normalStrategyData;
    public final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>>
        internalStrategyData;
    public final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>>
        externalStrategyData;
    public final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> dynamicRuleData;
    public final LinkedHashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData;
    public final LinkedHashSet<ConstructorSignature> usedConstructors;
    public final LinkedHashSet<StrategySignature> usedStrategies;
    public final LinkedHashSet<StrategySignature> dynamicRules;
    public final LinkedHashSet<String> usedAmbiguousStrategies;
    public final ArrayList<Message> messages;
    public final long lastModified;
    private transient @Nullable LinkedHashMap<String, LinkedHashSet<StrategyFrontData>>
        ambStrategyIndex = null;

    public ModuleData(IModuleImportService.ModuleIdentifier moduleIdentifier, IStrategoTerm ast,
        ArrayList<IModuleImportService.ModuleIdentifier> imports,
        LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData,
        LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> normalStrategyData,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> internalStrategyData,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> externalStrategyData,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> dynamicRuleData,
        LinkedHashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData,
        LinkedHashSet<ConstructorSignature> usedConstructors,
        LinkedHashSet<StrategySignature> usedStrategies,
        LinkedHashSet<StrategySignature> dynamicRules,
        LinkedHashSet<String> usedAmbiguousStrategies, ArrayList<Message> messages,
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
        this.messages = messages;
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
        if(!usedAmbiguousStrategies.equals(that.usedAmbiguousStrategies))
            return false;
        return messages.equals(that.messages);
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
        result = 31 * result + messages.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "ModuleData(" + moduleIdentifier + ')';
    }

    public LinkedHashMap<String, LinkedHashSet<StrategyFrontData>> ambStrategyIndex() {
        if(ambStrategyIndex == null) {
            ambStrategyIndex = new LinkedHashMap<>();
            for(Map.Entry<StrategySignature, LinkedHashSet<StrategyFrontData>> e : normalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, LinkedHashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, LinkedHashSet<StrategyFrontData>> e : internalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, LinkedHashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, LinkedHashSet<StrategyFrontData>> e : externalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, LinkedHashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, LinkedHashSet<StrategyFrontData>> e : dynamicRuleData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, LinkedHashSet::new)
                    .addAll(e.getValue());
            }
        }
        return ambStrategyIndex;
    }

    @Override public long lastModified() {
        return lastModified;
    }

}
