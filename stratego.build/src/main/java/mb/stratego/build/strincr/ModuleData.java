package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.TermWithLastModified;

/**
 * The AST of a module and some of it's data pre-extracted.
 */
public class ModuleData implements Serializable {
    public final ModuleIdentifier moduleIdentifier;
    public final TermWithLastModified ast;
    public final List<TermWithLastModified> imports;
    public final Map<ConstructorSignature, List<ConstructorData>> constrData;
    public final Map<TermWithLastModified, List<TermWithLastModified>> injections;
    public final Map<StrategySignature, Set<StrategyFrontData>> strategyData;
    public final Map<ConstructorSignature, List<ConstructorData>> overlayData;
    public final Set<ConstructorSignature> usedConstructors;
    public final Set<StrategySignature> usedStrategies;
    public final Set<String> usedAmbiguousStrategies;
    public final List<Message<?>> messages;

    public ModuleData(ModuleIdentifier moduleIdentifier, TermWithLastModified ast,
        List<TermWithLastModified> imports,
        Map<ConstructorSignature, List<ConstructorData>> constrData,
        Map<TermWithLastModified, List<TermWithLastModified>> injections,
        Map<StrategySignature, Set<StrategyFrontData>> strategyData,
        Map<ConstructorSignature, List<ConstructorData>> overlayData,
        Set<ConstructorSignature> usedConstructors, Set<StrategySignature> usedStrategies,
        Set<String> usedAmbiguousStrategies, List<Message<?>> messages) {
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
    }

    public ModuleIndex toModuleIndex() {
        return new ModuleIndex(imports, constrData.keySet(), strategyData.keySet(),
            overlayData.keySet(), messages);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleData that = (ModuleData) o;

        if(!moduleIdentifier.equals(that.moduleIdentifier))
            return false;
        if(!ast.equals(that.ast))
            return false;
        if(!imports.equals(that.imports))
            return false;
        if(!constrData.equals(that.constrData))
            return false;
        if(!injections.equals(that.injections))
            return false;
        if(!strategyData.equals(that.strategyData))
            return false;
        if(!overlayData.equals(that.overlayData))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + ast.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + constrData.hashCode();
        result = 31 * result + injections.hashCode();
        result = 31 * result + strategyData.hashCode();
        result = 31 * result + overlayData.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }
}
