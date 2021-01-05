package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ImportResolution;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.java.UnresolvedImport;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;

public class Resolve implements TaskDef<Resolve.Input, Resolve.GlobalData> {
    public static final String id = Resolve.class.getCanonicalName();

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

    public static class GlobalData implements Serializable {
        final Map<ModuleIdentifier, STask<ModuleData>> moduleDataTasks;
        final Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex;
        final List<Message<?>> messages;

        public GlobalData(Map<ModuleIdentifier, STask<ModuleData>> moduleDataTasks,
            Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex,
            List<Message<?>> messages) {
            this.moduleDataTasks = moduleDataTasks;
            this.constructorIndex = constructorIndex;
            this.messages = messages;
        }
    }

    public final Front front;
    public final Lib lib;

    @Inject public Resolve(Front front, Lib lib) {
        this.front = front;
        this.lib = lib;
    }

    @Override public GlobalData exec(ExecContext context, Input input) throws IOException {
        final List<Message<?>> messages = new ArrayList<>();

        final java.util.Set<ModuleIdentifier> seen = new HashSet<>();
        final Deque<ModuleIdentifier> workList = new ArrayDeque<>();
        workList.add(input.mainModuleIdentifier);
        seen.add(input.mainModuleIdentifier);

        final Map<ModuleIdentifier, STask<ModuleData>> moduleDataTasks = new HashMap<>();
        final Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex = new HashMap<>();
        final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex = new HashMap<>();
        final Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex = new HashMap<>();

        do {
            final ModuleIdentifier moduleIdentifier = workList.remove();

            if(moduleIdentifier.isLibrary()) {
                final STask<ModuleData> sTask = lib.createSupplier(
                    new Front.Input(moduleIdentifier, input.moduleImportService));
                moduleDataTasks.put(moduleIdentifier, sTask);
            } else {
                final STask<ModuleData> sTask = front
                    .createSupplier(new Front.Input(moduleIdentifier, input.moduleImportService));
                final ModuleIndex index =
                    PieUtils.requirePartial(context, sTask, ModuleData::toModuleIndex);
                moduleDataTasks.put(moduleIdentifier, sTask);
                for(ConstructorSignature signature : index.constructors) {
                    Relation.getOrInitialize(constructorIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                }
                for(StrategySignature signature : index.strategies) {
                    Relation.getOrInitialize(strategyIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                }
                for(ConstructorSignature signature : index.overlays) {
                    Relation.getOrInitialize(overlayIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                }

                final Set<ModuleIdentifier> expandedImports = new HashSet<>();
                for(IStrategoTerm anImport : index.imports) {
                    final ImportResolution importResolution =
                        input.moduleImportService.resolveImport(anImport);
                    if(importResolution instanceof IModuleImportService.UnresolvedImport) {
                        messages
                            .add(new UnresolvedImport(moduleIdentifier.moduleString(), anImport));
                    } else if(importResolution instanceof IModuleImportService.ResolvedImport) {
                        expandedImports.addAll(
                            ((IModuleImportService.ResolvedImport) importResolution).modules);
                    }
                }
                expandedImports.removeAll(seen);
                workList.addAll(expandedImports);
                seen.addAll(expandedImports);
            }
        } while(!workList.isEmpty());

        return new GlobalData(moduleDataTasks, constructorIndex, messages);
    }

    @Override public String getId() {
        return id;
    }
}
