package mb.stratego.build.strincr;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ImportResolution;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.CyclicOverlay2;
import mb.stratego.build.strincr.message.Message2;
import mb.stratego.build.strincr.message.UnresolvedImport2;
import mb.stratego.build.util.Algorithms;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;

public class Resolve implements TaskDef<Check.Input, GlobalData> {
    public static final String id = Resolve.class.getCanonicalName();

    public final Front front;

    @Inject public Resolve(Front front) {
        this.front = front;
    }

    @Override public GlobalData exec(ExecContext context, Check.Input input) throws IOException {
        final List<Message2<?>> messages = new ArrayList<>();

        final java.util.Set<ModuleIdentifier> seen = new HashSet<>();
        final Deque<ModuleIdentifier> workList = new ArrayDeque<>();
        workList.add(input.mainModuleIdentifier);
        seen.add(input.mainModuleIdentifier);

        final Set<ModuleIdentifier> allModuleIdentifiers = new HashSet<>();
        final Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex = new HashMap<>();
        final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex = new HashMap<>();
        final Map<String, Set<ModuleIdentifier>> ambStrategyIndex = new HashMap<>();
        final Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex = new HashMap<>();

        final Map<ConstructorSignature, Set<ConstructorSignature>> overlayUsesConstructors =
            new HashMap<>();

        do {
            final ModuleIdentifier moduleIdentifier = workList.remove();

            if(moduleIdentifier.isLibrary()) {
                allModuleIdentifiers.add(moduleIdentifier);
                // TODO: add dep on lib task again, add external strategy and constructor to indices?
            } else {
                final STask<ModuleData> sTask = front
                    .createSupplier(new Front.Input(moduleIdentifier, input.moduleImportService));
                allModuleIdentifiers.add(moduleIdentifier);

                final ModuleIndex index =
                    PieUtils.requirePartial(context, sTask, ModuleData.ToModuleIndex.INSTANCE);

                for(ConstructorSignature signature : index.constructors) {
                    Relation.getOrInitialize(constructorIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                }
                for(StrategySignature signature : index.strategies) {
                    Relation.getOrInitialize(strategyIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                    Relation.getOrInitialize(ambStrategyIndex, signature.name, HashSet::new)
                        .add(moduleIdentifier);
                }
                for(Map.Entry<ConstructorSignature, List<OverlayData>> e : index.overlayData
                    .entrySet()) {
                    Relation.getOrInitialize(overlayIndex, e.getKey(), HashSet::new)
                        .add(moduleIdentifier);
                    final Set<ConstructorSignature> overlayUsesCons =
                        Relation.getOrInitialize(overlayUsesConstructors, e.getKey(), HashSet::new);
                    for(OverlayData overlayData : e.getValue()) {
                        overlayUsesCons.addAll(overlayData.usedConstructors);
                    }
                }

                final Set<ModuleIdentifier> expandedImports =
                    expandImports(context, input.moduleImportService, index.imports,
                        index.lastModified, messages);
                expandedImports.removeAll(seen);
                workList.addAll(expandedImports);
                seen.addAll(expandedImports);
            }
        } while(!workList.isEmpty());

        checkCyclicOverlays(overlayUsesConstructors, messages);

        return new GlobalData(allModuleIdentifiers, constructorIndex, strategyIndex,
            ambStrategyIndex, overlayIndex, messages);
    }

    public static Set<ModuleIdentifier> expandImports(ExecContext context,
        IModuleImportService moduleImportService, List<IStrategoTerm> imports, long lastModified,
        @Nullable List<Message2<?>> messages) throws IOException {
        final Set<ModuleIdentifier> expandedImports = new HashSet<>();
        for(IStrategoTerm anImport : imports) {
            final ImportResolution importResolution =
                moduleImportService.resolveImport(context, anImport);
            if(importResolution instanceof IModuleImportService.UnresolvedImport) {
                if(messages != null) {
                    messages.add(new UnresolvedImport2(anImport, lastModified));
                }
            } else if(importResolution instanceof IModuleImportService.ResolvedImport) {
                expandedImports
                    .addAll(((IModuleImportService.ResolvedImport) importResolution).modules);
            }
        }
        return expandedImports;
    }

    private void checkCyclicOverlays(
        Map<ConstructorSignature, Set<ConstructorSignature>> overlayUsesConstructors,
        List<Message2<?>> messages) {
        final Deque<Set<ConstructorSignature>> topoSCCs = Algorithms
            .topoSCCs(overlayUsesConstructors.keySet(),
                sig -> overlayUsesConstructors.getOrDefault(sig, Collections.emptySet()));
        for(Set<ConstructorSignature> topoSCC : topoSCCs) {
            final ConstructorSignature signature = topoSCC.iterator().next();
            if(topoSCC.size() > 1 || overlayUsesConstructors
                .getOrDefault(signature, Collections.emptySet()).contains(signature)) {
                long lastModified = 0;
                for(ConstructorSignature sig : topoSCC) {
                    lastModified = Long.max(lastModified, sig.lastModified);
                }
                for(ConstructorSignature sig : topoSCC) {
                    messages.add(new CyclicOverlay2(sig, topoSCC, lastModified));
                }
            }
        }
    }

    @Override public String getId() {
        return id;
    }
}
