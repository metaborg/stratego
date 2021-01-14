package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.PieUtils;

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
        public final Map<StrategySignature, StrategyAnalysisData> strategyDataWithCasts;
        public final List<Message<?>> messages;

        public Output(Map<StrategySignature, StrategyAnalysisData> strategyDataWithCasts,
            List<Message<?>> messages) {
            this.strategyDataWithCasts = strategyDataWithCasts;
            this.messages = messages;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Output output = (Output) o;

            if(!strategyDataWithCasts.equals(output.strategyDataWithCasts))
                return false;
            return messages.equals(output.messages);
        }

        @Override public int hashCode() {
            int result = strategyDataWithCasts.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }
    }

    public final Resolve resolve;
    public final Front front;
    public final Lib lib;

    @Inject public Check(Resolve resolve, Front front, Lib lib) {
        this.resolve = resolve;
        this.front = front;
        this.lib = lib;
    }

    @Override public Output exec(ExecContext context, Input input) throws ExecException {
        // TODO: depend on all Front tasks and the Resolve task for messages.
        // TODO: depend on the Resolve task for GlobalData for types and module asts to process
        // TODO: run actual type checking job in separate tasks per module
        final Set<ModuleIdentifier> moduleDataTasks =
            PieUtils.requirePartial(context, resolve, input, GlobalData.AllModulesIdentifiers.Instance);

        for(ModuleIdentifier moduleIdentifier : moduleDataTasks) {
        }
        // Checks:
        //     - Cyclic overlays.
        //     - Gradual type check.
        //         - Provide relevant externals (overlapping with definitions in module), for checks
        //             of overlap between normal and external, and override/extend and external.
        //         - Provide overlays for desugaring
        //         - In stratego: provide externals checks, continue with desugaring immediately
        //             after
        return new Output(Collections.emptyMap(), Collections.emptyList());
    }

    @Override public String getId() {
        return id;
    }
}
