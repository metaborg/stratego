package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message2;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;

public class Check implements TaskDef<Check.Input, Check.Output> {
    public static final String id = Check.class.getCanonicalName();

    public static class Input implements Serializable {
        public final ModuleIdentifier mainModuleIdentifier;
        public final IModuleImportService moduleImportService;

        public Input(ModuleIdentifier mainModuleIdentifier,
            IModuleImportService moduleImportService) {
            this.mainModuleIdentifier = mainModuleIdentifier;
            this.moduleImportService = moduleImportService;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!mainModuleIdentifier.equals(input.mainModuleIdentifier))
                return false;
            return moduleImportService.equals(input.moduleImportService);
        }

        @Override public int hashCode() {
            int result = mainModuleIdentifier.hashCode();
            result = 31 * result + moduleImportService.hashCode();
            return result;
        }
    }

    public static class Output implements Serializable {
        public final Map<ModuleIdentifier, STask<CheckModule.Output>> moduleCheckTasks;
        public final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex;
        public final List<Message2<?>> messages;

        public Output(Map<ModuleIdentifier, STask<CheckModule.Output>> moduleCheckTasks,
            Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex,
            List<Message2<?>> messages) {
            this.moduleCheckTasks = moduleCheckTasks;
            this.strategyIndex = strategyIndex;
            this.messages = messages;
        }
    }

    public final Resolve resolve;
    public final CheckModule checkModule;

    @Inject public Check(Resolve resolve, CheckModule checkModule) {
        this.resolve = resolve;
        this.checkModule = checkModule;
    }

    @Override public Output exec(ExecContext context, Input input) throws ExecException {
        final Map<ModuleIdentifier, STask<CheckModule.Output>> moduleCheckTasks = new HashMap<>();
        final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex = new HashMap<>();
        final List<Message2<?>> messages = new ArrayList<>();
        final Set<ModuleIdentifier> moduleDataTasks = PieUtils
            .requirePartial(context, resolve, input, GlobalData.AllModulesIdentifiers.Instance);

        for(ModuleIdentifier moduleIdentifier : moduleDataTasks) {
            final STask<CheckModule.Output> sTask = checkModule
                .createSupplier(new CheckModule.Input(input.mainModuleIdentifier, moduleIdentifier, input.moduleImportService));
            moduleCheckTasks.put(moduleIdentifier, sTask);
            final CheckModule.Output output = context.require(sTask);
            for(StrategySignature strategySignature : output.strategyDataWithCasts.keySet()) {
                Relation.getOrInitialize(strategyIndex, strategySignature, HashSet::new)
                    .add(moduleIdentifier);
            }
        }
        return new Output(moduleCheckTasks, strategyIndex, messages);
    }

    @Override public String getId() {
        return id;
    }
}
