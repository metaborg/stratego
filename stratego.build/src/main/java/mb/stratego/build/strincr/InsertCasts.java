package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import io.usethesource.capsule.BinaryRelation;
import io.usethesource.capsule.Map;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.StrIncrContext;

public class InsertCasts implements TaskDef<InsertCasts.Input, InsertCasts.Output> {
    public static final String id = InsertCasts.class.getCanonicalName();

    public static final class Input implements Serializable {
        final StrategoImmutableMap strategyEnvironment;
        final StrategoImmutableRelation constructors;
        final StrategoImmutableRelation injectionClosure;
        final StrategoImmutableMap lubMap;
        final IStrategoTerm ast;
        final String cifiedName;

        Input(StrategoImmutableMap strategyEnvironment, StrategoImmutableRelation constructors,
            StrategoImmutableRelation injectionClosure, StrategoImmutableMap lubMap, IStrategoTerm ast, String cifiedName) {
            this.strategyEnvironment = strategyEnvironment;
            this.constructors = constructors;
            this.injectionClosure = injectionClosure;
            this.lubMap = lubMap;
            this.ast = ast;
            this.cifiedName = cifiedName;
        }

        @Override
        public String toString() {
            return "InsertCasts$Input";
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            Input input = (Input) o;
            return Objects.equals(strategyEnvironment, input.strategyEnvironment) && Objects
                .equals(constructors, input.constructors) && Objects.equals(injectionClosure, input.injectionClosure)
                && Objects.equals(lubMap, input.lubMap) && Objects.equals(ast, input.ast);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strategyEnvironment, constructors, injectionClosure, lubMap, ast);
        }

        public static class Builder {
            final StrategoImmutableMap strategyEnvironment;
            final StrategoImmutableRelation constructors;
            final StrategoImmutableRelation injectionClosure;
            final StrategoImmutableMap lubMap;

            public Builder(java.util.Map<IStrategoString, IStrategoTerm> strategyEnv,
                BinaryRelation.Immutable<IStrategoString, IStrategoTerm> constrs,
                BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections, ITermFactory tf) {
                strategyEnvironment = StrategoImmutableMap.fromMap(strategyEnv);
                constructors = new StrategoImmutableRelation(constrs);
                injectionClosure =
                    StrategoImmutableRelation.transitiveClosure(new StrategoImmutableRelation(injections));
                this.lubMap = new StrategoImmutableMap(lubMapFromInjClosure(injectionClosure, tf));
            }

            private static Map.Immutable<? extends IStrategoTerm, ? extends IStrategoTerm> lubMapFromInjClosure(
                StrategoImmutableRelation injectionClosure, ITermFactory tf) {
                /* TODO: find cyclic injections through entries that have the same type on both sides (x,x)
                 * Use an arbitrary but deterministic method to choose a representative type in a cycle
                 * Map members x,y from the same cycle to the representative type
                 */
                final Map.Transient<IStrategoTerm, IStrategoTerm> lubMap = Map.Transient.of();
                for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> entry : injectionClosure.backingRelation
                    .entrySet()) {
                    final IStrategoTerm from = entry.getKey();
                    final IStrategoTerm to = entry.getValue();
                    lubMap.__put(tf.makeTuple(from, to), to);
                }
                return lubMap.freeze();
            }

            public Input build(IStrategoTerm ast, String cifiedName) {
                // ast is of form `Strategies([def])`, we only need def
                return new Input(strategyEnvironment, constructors, injectionClosure, lubMap,
                    ast.getSubterm(0).getSubterm(0), cifiedName);
            }
        }
    }

    public static final class Output implements Serializable {
        public final IStrategoAppl astWithCasts;
        public final List<Message<?>> messages;

        public Output(IStrategoAppl astWithCasts, List<Message<?>> messages) {
            this.astWithCasts = astWithCasts;
            this.messages = messages;
        }
    }

    private final SubFrontend strIncrSubFront;
    private final StrIncrContext strContext;
    static ArrayList<Long> timestamps = new ArrayList<>();

    @Inject
    public InsertCasts(SubFrontend strIncrSubFront, StrIncrContext strContext) {
        this.strIncrSubFront = strIncrSubFront;
        this.strContext = strContext;
    }


    @Override
    public Output exec(ExecContext execContext, Input input) throws ExecException, InterruptedException {
        timestamps.add(System.nanoTime());
        final ITermFactory tf = strContext.getFactory();

        final IStrategoTerm tuple = tf.makeTuple(input.strategyEnvironment, input.constructors, input.injectionClosure, input.lubMap, input.ast);
        final SubFrontend.Input frontInput = SubFrontend.Input.insertCasts(input.cifiedName, tuple);
        final SubFrontend.Output output = execContext.require(strIncrSubFront.createTask(frontInput));
        final IStrategoAppl astWithCasts = TermUtils.toApplAt(output.result, 0);
        final IStrategoList errors = TermUtils.toListAt(output.result, 1);
        final IStrategoList warnings = TermUtils.toListAt(output.result, 2);
        final IStrategoList notes = TermUtils.toListAt(output.result, 3);
        timestamps.add(System.nanoTime());
        List<Message<?>> messages = new ArrayList<>(errors.size() + warnings.size() + notes.size());
        for(IStrategoTerm errorTerm : errors) {
            messages.add(Message.from(errorTerm, MessageSeverity.ERROR));
        }
        for(IStrategoTerm warningTerm : warnings) {
            messages.add(Message.from(warningTerm, MessageSeverity.WARNING));
        }
        for(IStrategoTerm noteTerm : notes) {
            messages.add(Message.from(noteTerm, MessageSeverity.NOTE));
        }
        return new Output(tf.makeAppl("Strategies", tf.makeList(astWithCasts)), messages);
    }

    @Override
    public String getId() {
        return id;
    }

}
