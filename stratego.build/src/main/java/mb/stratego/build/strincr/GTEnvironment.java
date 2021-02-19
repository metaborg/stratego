package mb.stratego.build.strincr;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoTuple;

import io.usethesource.capsule.BinaryRelation;
import io.usethesource.capsule.Map;

public class GTEnvironment extends StrategoTuple {
    public final StrategoImmutableMap strategyEnvironment;
    public final StrategoImmutableRelation constructors;
    public final StrategoImmutableRelation injectionClosure;
    public final StrategoImmutableMap lubMap;
    public final StrategoImmutableRelation aliasMap;
    public final IStrategoTerm ast;
    public final long lastModified;

    private GTEnvironment(StrategoImmutableMap strategyEnvironment,
        StrategoImmutableRelation constructors, StrategoImmutableRelation injectionClosure,
        StrategoImmutableMap lubMap, StrategoImmutableRelation aliasMap, IStrategoTerm ast,
        ITermFactory tf, long lastModified) {
        super(
            new IStrategoTerm[] { strategyEnvironment.withWrapper(tf), constructors.withWrapper(tf),
                injectionClosure.withWrapper(tf), lubMap.withWrapper(tf), aliasMap.withWrapper(tf),
                ast }, null);
        this.strategyEnvironment = strategyEnvironment;
        this.constructors = constructors;
        this.injectionClosure = injectionClosure;
        this.lubMap = lubMap;
        this.aliasMap = aliasMap;
        this.ast = ast;
        this.lastModified = lastModified;
    }

    public static GTEnvironment from(StrategoImmutableMap strategyEnvironment,
        BinaryRelation.Immutable<ConstructorSignature, ConstructorType> constructors,
        BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections, IStrategoTerm ast,
        ITermFactory tf, long lastModified) {
        final StrategoImmutableRelation injectionClosure =
            StrategoImmutableRelation.transitiveClosure(new StrategoImmutableRelation(injections));
        return new GTEnvironment(strategyEnvironment, new StrategoImmutableRelation(constructors),
            injectionClosure, lubMapFromInjClosure(injectionClosure, tf), StrategoImmutableRelation
            .transitiveClosure(
                new StrategoImmutableRelation(extractAliases(constructors, injections))), ast, tf, lastModified);
    }

    private static BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> extractAliases(
        BinaryRelation.Immutable<ConstructorSignature, ConstructorType> constrs,
        BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections) {
        BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> invInjections = injections.inverse();
        BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> aliases =
            BinaryRelation.Transient.of();
        outer:
        for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> e : injections.entrySet()) {
            final IStrategoTerm key = e.getKey();
            final IStrategoTerm value = e.getValue();
            // we select injections where no other injections go into the target type
            if(invInjections.get(value).size() == 1) {
                // and there are no constructors, for the target type
                for(ConstructorType t : constrs.values()) {
                    if(t.to.equals(value)) {
                        continue outer;
                    }
                }
                // then save that as the inverse of the original injection
                aliases.__insert(value, key);
            }
        }
        return aliases.freeze();
    }

    private static StrategoImmutableMap lubMapFromInjClosure(
        StrategoImmutableRelation injectionClosure, ITermFactory tf) {
        /* TODO: find cyclic injections through entries that have the same type on both sides (x,x)
         *      Use an arbitrary but deterministic method to choose a representative type in a cycle
         *      Map members x,y from the same cycle to the representative type
         *      Arbitrary method: order by name of sort, pick first. But then we need to extract
         *      the name from the term or have a nicer representation of types.
         */
        final Map.Transient<IStrategoTerm, IStrategoTerm> lubMap =
            Map.Transient.of();
        for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> entry : injectionClosure.backingRelation
            .entrySet()) {
            final IStrategoTerm from = entry.getKey();
            final IStrategoTerm to = entry.getValue();
            lubMap.__put(tf.makeTuple(from, to), to);
            lubMap.__put(tf.makeTuple(to, from), to);
        }
        return new StrategoImmutableMap(lubMap.freeze());
    }
}
