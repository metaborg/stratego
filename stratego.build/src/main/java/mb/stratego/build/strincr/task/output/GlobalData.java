package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorSignatureMatcher;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.output.CompileGlobalIndex;
import mb.stratego.build.strincr.function.output.CongruenceGlobalIndex;
import mb.stratego.build.strincr.function.output.GlobalConsInj;
import mb.stratego.build.strincr.message.Message;

public class GlobalData implements Serializable {
    public final LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections;
    public final LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
        strategyIndex;
    public final LinkedHashMap<ConstructorSignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
        overlayIndex;
    public final LinkedHashSet<ConstructorSignatureMatcher> nonExternalConstructors;
    public final LinkedHashSet<ConstructorSignatureMatcher> externalConstructors;
    public final LinkedHashSet<StrategySignature> internalStrategies;
    public final LinkedHashSet<StrategySignature> externalStrategies;
    public final LinkedHashSet<StrategySignature> dynamicRules;
    public final ArrayList<Message> messages;
    private transient @Nullable CompileGlobalIndex compileGlobalIndex = null;
    private transient @Nullable CongruenceGlobalIndex congruenceGlobalIndex = null;
    private transient @Nullable GlobalConsInj globalConsInj = null;

    public GlobalData(LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers,
        LinkedHashMap<ConstructorSignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>> overlayIndex,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections,
        LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>> strategyIndex,
        LinkedHashSet<ConstructorSignatureMatcher> nonExternalConstructors,
        LinkedHashSet<ConstructorSignatureMatcher> externalConstructors,
        LinkedHashSet<StrategySignature> internalStrategies,
        LinkedHashSet<StrategySignature> externalStrategies,
        LinkedHashSet<StrategySignature> dynamicRules, ArrayList<Message> messages) {
        this.allModuleIdentifiers = allModuleIdentifiers;
        this.nonExternalInjections = nonExternalInjections;
        this.strategyIndex = strategyIndex;
        this.overlayIndex = overlayIndex;
        this.nonExternalConstructors = nonExternalConstructors;
        this.externalConstructors = externalConstructors;
        this.internalStrategies = internalStrategies;
        this.externalStrategies = externalStrategies;
        this.dynamicRules = dynamicRules;
        this.messages = messages;
    }

    public CompileGlobalIndex getCompileGlobalIndex() {
        if(compileGlobalIndex == null) {
            final LinkedHashSet<StrategySignature> nonExternalStrategies =
                new LinkedHashSet<>(strategyIndex.keySet());
            nonExternalStrategies.removeAll(externalStrategies);
            nonExternalStrategies.addAll(internalStrategies);
            compileGlobalIndex = new CompileGlobalIndex(nonExternalStrategies, dynamicRules);
        }
        return compileGlobalIndex;
    }

    public CongruenceGlobalIndex getCongruenceGlobalIndex() {
        if(congruenceGlobalIndex == null) {
            final LinkedHashSet<StrategySignature> nonExternalStrategies =
                getCompileGlobalIndex().nonExternalStrategies;
            congruenceGlobalIndex =
                new CongruenceGlobalIndex(nonExternalConstructors, externalConstructors,
                    nonExternalStrategies);
        }
        return congruenceGlobalIndex;
    }

    public GlobalConsInj getGlobalConsInj() {
        if(globalConsInj == null) {
            globalConsInj = new GlobalConsInj(allModuleIdentifiers, nonExternalInjections,
                getCompileGlobalIndex().nonExternalStrategies);
        }
        return globalConsInj;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GlobalData that = (GlobalData) o;

        if(!allModuleIdentifiers.equals(that.allModuleIdentifiers))
            return false;
        if(!nonExternalInjections.equals(that.nonExternalInjections))
            return false;
        if(!strategyIndex.equals(that.strategyIndex))
            return false;
        if(!overlayIndex.equals(that.overlayIndex))
            return false;
        if(!nonExternalConstructors.equals(that.nonExternalConstructors))
            return false;
        if(!externalConstructors.equals(that.externalConstructors))
            return false;
        if(!internalStrategies.equals(that.internalStrategies))
            return false;
        if(!externalStrategies.equals(that.externalStrategies))
            return false;
        if(!dynamicRules.equals(that.dynamicRules))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = allModuleIdentifiers.hashCode();
        result = 31 * result + nonExternalInjections.hashCode();
        result = 31 * result + strategyIndex.hashCode();
        result = 31 * result + overlayIndex.hashCode();
        result = 31 * result + nonExternalConstructors.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + internalStrategies.hashCode();
        result = 31 * result + externalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        return "GlobalData(" + allModuleIdentifiers + ")";
    }
}
