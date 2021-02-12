package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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

        @Override public String toString() {
            return "Check.Input(" + mainModuleIdentifier + ", " + moduleImportService + ")";
        }
    }

    public static class Output implements Serializable {
        public final Map<ModuleIdentifier, STask<CheckModule.Output>> moduleCheckTasks;
        public final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex;
        public final List<Message2<?>> messages;
        public final boolean containsErrors;

        public Output(Map<ModuleIdentifier, STask<CheckModule.Output>> moduleCheckTasks,
            Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex, List<Message2<?>> messages,
            boolean containsErrors) {
            this.moduleCheckTasks = moduleCheckTasks;
            this.strategyIndex = strategyIndex;
            this.messages = messages;
            this.containsErrors = containsErrors;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Output output = (Output) o;

            if(!moduleCheckTasks.equals(output.moduleCheckTasks))
                return false;
            if(!strategyIndex.equals(output.strategyIndex))
                return false;
            return messages.equals(output.messages);
        }

        @Override public int hashCode() {
            int result = moduleCheckTasks.hashCode();
            result = 31 * result + strategyIndex.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Check.Output(" + messages.size() + ", " + containsErrors + ")";
        }

        public static class GetMessages implements Function<Check.Output, Messages>, Serializable {
            public static final GetMessages INSTANCE = new GetMessages();

            private GetMessages() {
            }

            @Override public Messages apply(Check.Output output) {
                return new Messages(output.messages, output.containsErrors);
            }
        }

        public static class Messages implements Serializable {
            public final List<Message2<?>> messages;
            public final boolean containsErrors;

            public Messages(List<Message2<?>> messages, boolean containsErrors) {
                this.messages = messages;
                this.containsErrors = containsErrors;
            }
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
        boolean containsErrors = false;
        final Set<ModuleIdentifier> modulesIdentifiers = PieUtils
            .requirePartial(context, resolve, input, GlobalData.AllModulesIdentifiers.Instance);

        for(ModuleIdentifier moduleIdentifier : modulesIdentifiers) {
            if(moduleIdentifier.isLibrary()) {
                continue;
            }
            final STask<CheckModule.Output> sTask = checkModule.createSupplier(
                new CheckModule.Input(input.mainModuleIdentifier, moduleIdentifier,
                    input.moduleImportService));
            moduleCheckTasks.put(moduleIdentifier, sTask);
            final CheckModule.Output output = context.require(sTask);
            for(StrategySignature strategySignature : output.strategyDataWithCasts.keySet()) {
                Relation.getOrInitialize(strategyIndex, strategySignature, HashSet::new)
                    .add(moduleIdentifier);
            }
            for(Message2<?> message : output.messages) {
                messages.add(message);
                containsErrors |= message.severity == MessageSeverity.ERROR;
            }
        }
        return new Output(moduleCheckTasks, strategyIndex, messages, containsErrors);
    }

    @Override public String getId() {
        return id;
    }
}
