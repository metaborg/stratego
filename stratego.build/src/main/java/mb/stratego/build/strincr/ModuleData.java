package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.WithLastModified;

/**
 * The AST of a module and some of it's data pre-extracted.
 */
public class ModuleData implements Serializable, WithLastModified {
    public final ModuleIdentifier moduleIdentifier;
    public final IStrategoTerm ast;
    public final List<IStrategoTerm> imports;
    public final Map<ConstructorSignature, List<ConstructorData>> constrData;
    public final Map<IStrategoTerm, List<IStrategoTerm>> injections;
    public final Map<StrategySignature, Set<StrategyFrontData>> strategyData;
    public final Map<ConstructorSignature, List<ConstructorData>> overlayData;
    public final Set<ConstructorSignature> usedConstructors;
    public final Set<StrategySignature> usedStrategies;
    public final Set<String> usedAmbiguousStrategies;
    public final List<Message<?>> messages;
    public final long lastModified;

    public ModuleData(ModuleIdentifier moduleIdentifier, IStrategoTerm ast,
        List<IStrategoTerm> imports, Map<ConstructorSignature, List<ConstructorData>> constrData,
        Map<IStrategoTerm, List<IStrategoTerm>> injections,
        Map<StrategySignature, Set<StrategyFrontData>> strategyData,
        Map<ConstructorSignature, List<ConstructorData>> overlayData,
        Set<ConstructorSignature> usedConstructors, Set<StrategySignature> usedStrategies,
        Set<String> usedAmbiguousStrategies, List<Message<?>> messages, long lastModified) {
        this.moduleIdentifier = moduleIdentifier;
        this.ast = ast;
        this.imports = imports;
        this.constrData = constrData;
        this.injections = injections;
        this.strategyData = strategyData;
        this.overlayData = overlayData;
        this.usedConstructors = usedConstructors;
        this.usedStrategies = usedStrategies;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
        this.messages = messages;
        this.lastModified = lastModified;
    }

    public static class ToModuleIndex implements Function<ModuleData, ModuleIndex>, Serializable {
        public static final ModuleData.ToModuleIndex INSTANCE = new ModuleData.ToModuleIndex();

        private ToModuleIndex() {
        }

        @Override public ModuleIndex apply(ModuleData moduleData) {
            return new ModuleIndex(moduleData.imports,
                new HashSet<>(moduleData.constrData.keySet()),
                new HashSet<>(moduleData.strategyData.keySet()),
                new HashSet<>(moduleData.overlayData.keySet()), moduleData.messages,
                moduleData.lastModified);
        }
    }

    public static class ToModuleUsageData
        implements Function<ModuleData, ModuleUsageData>, Serializable {
        public static final ModuleData.ToModuleUsageData INSTANCE =
            new ModuleData.ToModuleUsageData();

        private ToModuleUsageData() {
        }

        @Override public ModuleUsageData apply(ModuleData moduleData) {
            return new ModuleUsageData(moduleData.ast, moduleData.usedConstructors,
                moduleData.usedStrategies, moduleData.usedAmbiguousStrategies,
                moduleData.lastModified);
        }
    }

    @Override public long lastModified() {
        return lastModified;
    }
}
