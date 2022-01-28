package mb.stratego.build.strincr.function;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.task.Front;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.termvisitors.HasDynamicRuleDefinitions;
import mb.stratego.build.util.InvalidASTException;

public class GetDynamicRuleDefinitions
    implements SerializableFunction<ModuleData, ArrayList<IStrategoTerm>> {
    public static final GetDynamicRuleDefinitions INSTANCE = new GetDynamicRuleDefinitions();

    @Override public ArrayList<IStrategoTerm> apply(ModuleData moduleData) {
        return moduleData.dynamicRuleDefinitions();
    }

    @Override public boolean equals(Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
