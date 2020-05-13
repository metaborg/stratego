package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import io.usethesource.capsule.BinaryRelation;
import io.usethesource.capsule.Map;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.SplitResult.ConstructorSignature;
import mb.stratego.build.strincr.SplitResult.StrategySignature;
import mb.stratego.build.util.StrIncrContext;

public class InsertCasts implements TaskDef<InsertCasts.Input, InsertCasts.Output> {
    public static final String id = InsertCasts.class.getCanonicalName();

    public static final class Input implements Serializable {
        final String moduleName;
        final StrategoImmutableMap strategyEnvironment;
        final StrategoImmutableRelation constructors;
        final StrategoImmutableRelation injectionClosure;
        final StrategoImmutableMap lubMap;
        final IStrategoTerm ast;
        final StrategySignature sig;

        Input(String moduleName, StrategoImmutableMap strategyEnvironment, StrategoImmutableRelation constructors,
            StrategoImmutableRelation injectionClosure, StrategoImmutableMap lubMap, IStrategoTerm ast, StrategySignature sig) {
            this.moduleName = moduleName;
            this.strategyEnvironment = strategyEnvironment;
            this.constructors = constructors;
            this.injectionClosure = injectionClosure;
            this.lubMap = lubMap;
            this.ast = ast;
            this.sig = sig;
        }

        @Override
        public String toString() {
            return "InsertCasts$Input(moduleName="+moduleName+", cifiedName="+ sig +")";
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(!(o instanceof Input))
                return false;
            Input input = (Input) o;
            return moduleName.equals(input.moduleName) && strategyEnvironment.equals(input.strategyEnvironment)
                && constructors.equals(input.constructors) && injectionClosure.equals(input.injectionClosure) && lubMap
                .equals(input.lubMap) && ast.equals(input.ast) && sig.equals(input.sig);
        }

        @Override
        public int hashCode() {
            return Objects
                .hash(moduleName, strategyEnvironment, constructors, injectionClosure, lubMap, ast, sig);
        }

        public static class Builder {
            private final String moduleName;
            final StrategoImmutableMap strategyEnvironment;
            final StrategoImmutableRelation constructors;
            final StrategoImmutableRelation injectionClosure;
            final StrategoImmutableMap lubMap;

            public Builder(String moduleName, java.util.Map<StrategySignature, IStrategoTerm> strategyEnv,
                BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm> constrs,
                BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections, ITermFactory tf) {
                this.moduleName = moduleName;
                strategyEnvironment = StrategoImmutableMap.fromMap(strategyEnv);
                constructors = new StrategoImmutableRelation(constrs);
                injectionClosure =
                    StrategoImmutableRelation.transitiveClosure(new StrategoImmutableRelation(injections));
                this.lubMap = new StrategoImmutableMap(lubMapFromInjClosure(injectionClosure, tf));
            }

            private static Map.Immutable<? extends IStrategoTerm, ? extends IStrategoTerm> lubMapFromInjClosure(
                StrategoImmutableRelation injectionClosure, ITermFactory tf) {
                /* TODO: find cyclic injections through entries that have the same type on both sides (x,x)
                 *  Use an arbitrary but deterministic method to choose a representative type in a cycle
                 *  Map members x,y from the same cycle to the representative type
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

            public Input build(IStrategoTerm ast, StrategySignature sig) {
                return new Input(moduleName, strategyEnvironment, constructors, injectionClosure, lubMap,
                    ast, sig);
            }
        }
    }

    public static final class Output implements Serializable {
        public final IStrategoTerm astWithCasts;
        public final List<Message<?>> messages;

        public Output(IStrategoTerm astWithCasts, List<Message<?>> messages) {
            this.astWithCasts = astWithCasts;
            this.messages = messages;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(!(o instanceof Output))
                return false;
            Output output = (Output) o;
            return astWithCasts.equals(output.astWithCasts) && messages.equals(output.messages);
        }

        @Override
        public int hashCode() {
            return Objects.hash(astWithCasts, messages);
        }
    }

    private final SubFrontend strIncrSubFront;
    private final StrIncrContext strContext;

    @Inject
    public InsertCasts(SubFrontend strIncrSubFront, StrIncrContext strContext) {
        this.strIncrSubFront = strIncrSubFront;
        this.strContext = strContext;
    }


    @Override
    public Output exec(ExecContext execContext, Input input) throws ExecException, InterruptedException {
        final ITermFactory tf = strContext.getFactory();

        assert input.strategyEnvironment.backingMap.containsKey(input.sig) : "Cannot find strategy " + input.sig
            + " to type check in given environment.";

        final IStrategoTerm tuple =
            tf.makeTuple(input.strategyEnvironment.withWrapper(tf), input.constructors.withWrapper(tf),
                input.injectionClosure.withWrapper(tf), input.lubMap.withWrapper(tf), input.ast);
        final SubFrontend.Input frontInput = SubFrontend.Input.insertCasts(input.moduleName, input.sig.cifiedName(), tuple);
        final SubFrontend.Output output = execContext.require(strIncrSubFront.createTask(frontInput));
        final IStrategoTerm astWithCasts = output.result.getSubterm(0);
        final IStrategoList errors = TermUtils.toListAt(output.result, 1);
        final IStrategoList warnings = TermUtils.toListAt(output.result, 2);
        final IStrategoList notes = TermUtils.toListAt(output.result, 3);

        List<Message<?>> messages = new ArrayList<>(errors.size() + warnings.size() + notes.size());
        for(IStrategoTerm errorTerm : errors) {
            messages.add(Message.from(execContext.logger(), input.moduleName, errorTerm, MessageSeverity.ERROR));
        }
        for(IStrategoTerm warningTerm : warnings) {
            messages.add(Message.from(execContext.logger(), input.moduleName, warningTerm, MessageSeverity.WARNING));
        }
        for(IStrategoTerm noteTerm : notes) {
            messages.add(Message.from(execContext.logger(), input.moduleName, noteTerm, MessageSeverity.NOTE));
        }
        return new Output(astWithCasts, messages);
//        return new Output(input.ast, messages);
    }

    @Override
    public String getId() {
        return id;
    }

}
