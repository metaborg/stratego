package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.WithLastModified;

/**
 * The information in the module data of a module as needed by the Resolve task for indexing.
 */
public class ModuleIndex implements Serializable, WithLastModified {
    public final ArrayList<String> str2LibPackageNames;
    public final ArrayList<IModuleImportService.ModuleIdentifier> imports;
    public final LinkedHashSet<SortSignature> sorts;
    public final LinkedHashSet<SortSignature> externalSorts;
    public final LinkedHashSet<ConstructorData> nonOverlayConstructors;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections;
    public final LinkedHashSet<ConstructorSignature> externalConstructors;
    public final LinkedHashSet<StrategyFrontData> strategies;
    public final LinkedHashSet<StrategySignature> internalStrategies;
    public final LinkedHashSet<StrategyFrontData> externalStrategies;
    public final LinkedHashMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules;
    public final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> overlayData;
    public final LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts;
    public final LinkedHashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsedConstrs;
    public final ArrayList<Message> messages;
    public final long lastModified;

    public ModuleIndex(ArrayList<String> str2LibPackageNames, ArrayList<IModuleImportService.ModuleIdentifier> imports,
        LinkedHashSet<SortSignature> sorts, LinkedHashSet<SortSignature> externalSorts,
        LinkedHashSet<ConstructorData> nonOverlayConstructors,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections, LinkedHashSet<ConstructorSignature> externalConstructors,
        LinkedHashSet<StrategyFrontData> strategies,
        LinkedHashSet<StrategySignature> internalStrategies,
        LinkedHashSet<StrategyFrontData> externalStrategies,
        LinkedHashMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules,
        LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> overlayData,
        LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts, LinkedHashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsedConstrs,
        ArrayList<Message> messages, long lastModified) {
        this.str2LibPackageNames = str2LibPackageNames;
        this.imports = imports;
        this.sorts = sorts;
        this.externalSorts = externalSorts;
        this.nonOverlayConstructors = nonOverlayConstructors;
        this.injections = injections;
        this.externalConstructors = externalConstructors;
        this.strategies = strategies;
        this.internalStrategies = internalStrategies;
        this.externalStrategies = externalStrategies;
        this.dynamicRules = dynamicRules;
        this.overlayData = overlayData;
        this.overlayAsts = overlayAsts;
        this.overlayUsedConstrs = overlayUsedConstrs;
        this.messages = messages;
        this.lastModified = lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleIndex that = (ModuleIndex) o;

        if(lastModified != that.lastModified)
            return false;
        if(!str2LibPackageNames.equals(that.str2LibPackageNames))
            return false;
        if(!imports.equals(that.imports))
            return false;
        if(!sorts.equals(that.sorts))
            return false;
        if(!externalSorts.equals(that.externalSorts))
            return false;
        if(!nonOverlayConstructors.equals(that.nonOverlayConstructors))
            return false;
        if(!injections.equals(that.injections))
            return false;
        if(!externalConstructors.equals(that.externalConstructors))
            return false;
        if(!strategies.equals(that.strategies))
            return false;
        if(!internalStrategies.equals(that.internalStrategies))
            return false;
        if(!externalStrategies.equals(that.externalStrategies))
            return false;
        if(!dynamicRules.equals(that.dynamicRules))
            return false;
        if(!overlayData.equals(that.overlayData))
            return false;
        if(!overlayAsts.equals(that.overlayAsts))
            return false;
        if(!overlayUsedConstrs.equals(that.overlayUsedConstrs))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = str2LibPackageNames.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + sorts.hashCode();
        result = 31 * result + externalSorts.hashCode();
        result = 31 * result + nonOverlayConstructors.hashCode();
        result = 31 * result + injections.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + strategies.hashCode();
        result = 31 * result + internalStrategies.hashCode();
        result = 31 * result + externalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + overlayData.hashCode();
        result = 31 * result + overlayAsts.hashCode();
        result = 31 * result + overlayUsedConstrs.hashCode();
        result = 31 * result + messages.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "ModuleIndex(" + imports + ", " + sorts + ", " + externalSorts + ", "
            + nonOverlayConstructors + ", " + injections + ", " + externalConstructors + ", "
            + strategies + ", " + internalStrategies + ", " + externalStrategies + ", "
            + dynamicRules + ", " + overlayData + ", " + messages + ", " + lastModified + ')';
    }

    @Override public long lastModified() {
        return lastModified;
    }

    /**
     * @param before the situation of the module before the change
     * @param after the situation of the module after the change
     * @return pair of additions and removals, in that order, of the change
     */
    public static Diff diff(ModuleIndex before, ModuleIndex after) {
        if(before.equals(after)) {
            return Diff.equal();
        }

        final ModuleIndex additions = additions(before, after);
        final ModuleIndex subtractions = additions(after, before);
        return Diff.from(additions, subtractions);
    }

    /**
     * @param before
     * @param after
     * @return roughly after - before
     */
    private static ModuleIndex additions(ModuleIndex before, ModuleIndex after) {
        final ArrayList<String> str2LibPackageNames = new ArrayList<>(after.str2LibPackageNames);
        final ArrayList<IModuleImportService.ModuleIdentifier> imports = new ArrayList<>(after.imports);
        final LinkedHashSet<SortSignature> sorts = new LinkedHashSet<>(after.sorts);
        final LinkedHashSet<SortSignature> externalSorts = new LinkedHashSet<>(after.externalSorts);
        final LinkedHashSet<ConstructorData> nonOverlayConstructors = new LinkedHashSet<>(after.nonOverlayConstructors);
        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections =
            Relation.copy(after.injections, LinkedHashMap::new, ArrayList::new);
        final LinkedHashSet<ConstructorSignature> externalConstructors = new LinkedHashSet<>(after.externalConstructors);
        final LinkedHashSet<StrategyFrontData> strategies = new LinkedHashSet<>(after.strategies);
        final LinkedHashSet<StrategySignature> internalStrategies = new LinkedHashSet<>(after.internalStrategies);
        final LinkedHashSet<StrategyFrontData> externalStrategies = new LinkedHashSet<>(after.externalStrategies);
        final LinkedHashMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules =
            Relation.copy(after.dynamicRules, LinkedHashMap::new, TreeSet::new);
        final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> overlayData =
            Relation.copy(after.overlayData, LinkedHashMap::new, ArrayList::new);
        final LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts =
            Relation.copy(after.overlayAsts, LinkedHashMap::new, ArrayList::new);
        final LinkedHashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsedConstrs =
            Relation.copy(after.overlayUsedConstrs, LinkedHashMap::new, LinkedHashSet::new);
        final ArrayList<Message> messages = new ArrayList<>(after.messages);
        final long lastModified = after.lastModified - before.lastModified;

        str2LibPackageNames.removeAll(before.str2LibPackageNames);
        imports.removeAll(before.imports);
        sorts.removeAll(before.sorts);
        externalSorts.removeAll(before.externalSorts);
        nonOverlayConstructors.removeAll(before.nonOverlayConstructors);
        Relation.removeAll(injections, before.injections);
        externalConstructors.removeAll(before.externalConstructors);
        strategies.removeAll(before.strategies);
        internalStrategies.removeAll(before.internalStrategies);
        externalStrategies.removeAll(before.externalStrategies);
        Relation.removeAll(dynamicRules, before.dynamicRules);
        Relation.removeAll(overlayData, before.overlayData);
        Relation.removeAll(overlayAsts, before.overlayAsts);
        Relation.removeAll(overlayUsedConstrs, before.overlayUsedConstrs);
        messages.removeAll(before.messages);

        return new ModuleIndex(str2LibPackageNames, imports, sorts, externalSorts,
            nonOverlayConstructors, injections, externalConstructors, strategies, internalStrategies,
            externalStrategies, dynamicRules, overlayData, overlayAsts, overlayUsedConstrs, messages, lastModified);
    }

    public static final class Diff {
        public final @Nullable ModuleIndex additions;
        public final @Nullable ModuleIndex subtractions;
        public final boolean isEqual;

        private Diff(ModuleIndex additions, ModuleIndex subtractions) {
            this.additions = additions;
            this.subtractions = subtractions;
            isEqual = false;
        }

        private Diff() {
            this.additions = null;
            this.subtractions = null;
            isEqual = true;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((additions == null) ? 0 : additions.hashCode());
            result = prime * result + ((subtractions == null) ? 0 : subtractions.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Diff other = (Diff) obj;
            if(additions == null) {
                if(other.additions != null)
                    return false;
            } else if(!additions.equals(other.additions))
                return false;
            if(subtractions == null) {
                if(other.subtractions != null)
                    return false;
            } else if(!subtractions.equals(other.subtractions))
                return false;
            return true;
        }

        @Override public String toString() {
            if(isEqual) {
                return "Diff.equal";
            }
            return "Diff(" + additions + ", " + subtractions + ")";
        }

        public static Diff equal() {
            return new Diff();
        }

        public static Diff from(ModuleIndex additions, ModuleIndex subtractions) {
            if(isEmpty(additions) && isEmpty(subtractions)) {
                return new Diff();
            }
            return new Diff(additions, subtractions);
        }

        private static boolean isEmpty(ModuleIndex diffMIndex) {
            return diffMIndex.str2LibPackageNames.isEmpty() && diffMIndex.imports.isEmpty()
                && diffMIndex.sorts.isEmpty() && diffMIndex.externalSorts.isEmpty()
                && diffMIndex.nonOverlayConstructors.isEmpty() && diffMIndex.injections.isEmpty()
                && diffMIndex.externalConstructors.isEmpty() && diffMIndex.strategies.isEmpty()
                && diffMIndex.internalStrategies.isEmpty() && diffMIndex.externalStrategies.isEmpty()
                && diffMIndex.dynamicRules.isEmpty() && diffMIndex.overlayData.isEmpty()
                && diffMIndex.overlayAsts.isEmpty() && diffMIndex.overlayUsedConstrs.isEmpty()
                && diffMIndex.messages.isEmpty();
        }
    }
}
