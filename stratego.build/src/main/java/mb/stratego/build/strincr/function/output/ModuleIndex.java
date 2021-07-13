package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.WithLastModified;

/**
 * The information in the module data of a module as needed by the Resolve task for indexing.
 */
public class ModuleIndex implements Serializable, WithLastModified {
    public final ArrayList<IModuleImportService.ModuleIdentifier> imports;
    public final LinkedHashSet<SortSignature> sorts;
    public final LinkedHashSet<ConstructorData> constructors;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections;
    public final LinkedHashSet<SortSignature> externalSorts;
    public final LinkedHashSet<ConstructorSignature> externalConstructors;
    public final LinkedHashSet<StrategyFrontData> strategies;
    public final LinkedHashSet<StrategySignature> internalStrategies;
    public final LinkedHashSet<StrategySignature> externalStrategies;
    public final LinkedHashSet<StrategySignature> dynamicRules;
    public final LinkedHashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData;
    public final ArrayList<Message> messages;
    public final long lastModified;

    public ModuleIndex(ArrayList<IModuleImportService.ModuleIdentifier> imports,
        LinkedHashSet<SortSignature> sorts, LinkedHashSet<ConstructorData> constructors,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        LinkedHashSet<SortSignature> externalSorts,
        LinkedHashSet<ConstructorSignature> externalConstructors,
        LinkedHashSet<StrategyFrontData> strategies,
        LinkedHashSet<StrategySignature> internalStrategies,
        LinkedHashSet<StrategySignature> externalStrategies,
        LinkedHashSet<StrategySignature> dynamicRules,
        LinkedHashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData,
        ArrayList<Message> messages, long lastModified) {
        this.imports = imports;
        this.sorts = sorts;
        this.constructors = constructors;
        this.injections = injections;
        this.externalSorts = externalSorts;
        this.externalConstructors = externalConstructors;
        this.strategies = strategies;
        this.internalStrategies = internalStrategies;
        this.externalStrategies = externalStrategies;
        this.dynamicRules = dynamicRules;
        this.overlayData = overlayData;
        this.messages = messages;
        this.lastModified = lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleIndex that = (ModuleIndex) o;

        if(lastModified != that.lastModified)
            return false;
        if(!imports.equals(that.imports))
            return false;
        if(!sorts.equals(that.sorts))
            return false;
        if(!constructors.equals(that.constructors))
            return false;
        if(!injections.equals(that.injections))
            return false;
        if(!externalSorts.equals(that.externalSorts))
            return false;
        if(!externalConstructors.equals(that.externalConstructors))
            return false;
        if(!strategies.equals(that.strategies))
            return false;
        if(!internalStrategies.equals(that.internalStrategies))
            return false;
        if(!externalStrategies.equals(that.externalStrategies))
            return false;
        if(!dynamicRules.equals(that.dynamicRules))
            return false;
        if(!overlayData.equals(that.overlayData))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = imports.hashCode();
        result = 31 * result + sorts.hashCode();
        result = 31 * result + constructors.hashCode();
        result = 31 * result + injections.hashCode();
        result = 31 * result + externalSorts.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + strategies.hashCode();
        result = 31 * result + internalStrategies.hashCode();
        result = 31 * result + externalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + overlayData.hashCode();
        result = 31 * result + messages.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "ModuleIndex(" + imports + ", " + sorts + ", " + constructors + ", " + injections
            + ", " + externalSorts + ", " + externalConstructors + ", " + strategies + ", "
            + internalStrategies + ", " + externalStrategies + ", " + dynamicRules + ", "
            + overlayData + ", " + messages + ", " + lastModified + ')';
    }

    @Override public long lastModified() {
        return lastModified;
    }
}
