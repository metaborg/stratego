package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;

/**
 * The AST of a module and some of it's data pre-extracted.
 */
public class ModuleData implements Serializable {
    public final ModuleIdentifier moduleIdentifier;
    public final IStrategoTerm ast;
    public final List<IStrategoTerm> imports;
    public final Map<ConstructorSignature, List<ConstructorData>> constrData;
    public final Map<StrategySignature, List<StrategyData>> strategyData;
    public final Map<ConstructorSignature, List<ConstructorData>> overlayData;

    public ModuleData(ModuleIdentifier moduleIdentifier, IStrategoTerm ast, List<IStrategoTerm> imports,
        Map<ConstructorSignature, List<ConstructorData>> constrData,
        Map<StrategySignature, List<StrategyData>> strategyData,
        Map<ConstructorSignature, List<ConstructorData>> overlayData) {
        this.moduleIdentifier = moduleIdentifier;
        this.ast = ast;
        this.imports = imports;
        this.constrData = constrData;
        this.strategyData = strategyData;
        this.overlayData = overlayData;
    }

    public ModuleIndex toModuleIndex() {
        return new ModuleIndex(imports, constrData.keySet(), strategyData.keySet(), overlayData.keySet());
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
        if(!strategyData.equals(that.strategyData))
            return false;
        return overlayData.equals(that.overlayData);
    }

    @Override public int hashCode() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + ast.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + constrData.hashCode();
        result = 31 * result + strategyData.hashCode();
        result = 31 * result + overlayData.hashCode();
        return result;
    }
}
