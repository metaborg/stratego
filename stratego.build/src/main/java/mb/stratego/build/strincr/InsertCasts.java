package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.strc.insert_casts_0_0;

import io.usethesource.capsule.BinaryRelation;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.SplitResult.ConstructorSignature;
import mb.stratego.build.strincr.SplitResult.StrategySignature;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.build.util.TermEqWithAttachments;

public class InsertCasts implements TaskDef<InsertCasts.Input, InsertCasts.Output> {
    public static final String id = InsertCasts.class.getCanonicalName();

    public static final class Input implements Serializable {
        final String moduleName;
        final StrategoImmutableMap strategyEnvironment;
        final StrategoImmutableRelation constructors;
        final StrategoImmutableRelation injectionClosure;
        final StrategoImmutableRelation lubMap;
        final StrategoImmutableRelation aliasMap;
        final IStrategoTerm ast;
        final StrategySignature sig;

        Input(String moduleName, StrategoImmutableMap strategyEnvironment, StrategoImmutableRelation constructors,
            StrategoImmutableRelation injectionClosure, StrategoImmutableRelation lubMap,
            StrategoImmutableRelation aliasMap, IStrategoTerm ast, StrategySignature sig) {
            this.moduleName = moduleName;
            this.strategyEnvironment = strategyEnvironment;
            this.constructors = constructors;
            this.injectionClosure = injectionClosure;
            this.lubMap = lubMap;
            this.aliasMap = aliasMap;
            this.ast = ast;
            this.sig = sig;
        }

        @Override
        public String toString() {
            return "InsertCasts$Input(moduleName="+moduleName+", cifiedName="+ sig +")";
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input) o;
            if(!moduleName.equals(input.moduleName)) return false;
            if(!strategyEnvironment.equals(input.strategyEnvironment)) return false;
            if(!constructors.equals(input.constructors)) return false;
            if(!injectionClosure.equals(input.injectionClosure)) return false;
            if(!lubMap.equals(input.lubMap)) return false;
            if(!aliasMap.equals(input.aliasMap)) return false;
            if(!ast.equals(input.ast)) return false;
            return sig.equals(input.sig);
        }

        @Override public int hashCode() {
            int result = moduleName.hashCode();
            result = 31 * result + strategyEnvironment.hashCode();
            result = 31 * result + constructors.hashCode();
            result = 31 * result + injectionClosure.hashCode();
            result = 31 * result + lubMap.hashCode();
            result = 31 * result + aliasMap.hashCode();
            result = 31 * result + ast.hashCode();
            result = 31 * result + sig.hashCode();
            return result;
        }

        public static class Builder {
            private final String moduleName;
            final StrategoImmutableMap strategyEnvironment;
            final StrategoImmutableRelation constructors;
            final StrategoImmutableRelation injectionClosure;
            final StrategoImmutableRelation lubMap;
            final StrategoImmutableRelation aliasMap;

            public Builder(String moduleName, java.util.Map<StrategySignature, IStrategoTerm> strategyEnv,
                BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm> constrs,
                BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections,
                ITermFactory tf) {
                this.moduleName = moduleName;
                strategyEnvironment = StrategoImmutableMap.fromMap(strategyEnv);
                constructors = new StrategoImmutableRelation(constrs);
                injectionClosure =
                    StrategoImmutableRelation.transitiveClosure(new StrategoImmutableRelation(injections));
                this.lubMap = new StrategoImmutableRelation(lubMapFromInjClosure(injectionClosure, tf));
                this.aliasMap = StrategoImmutableRelation
                    .transitiveClosure(new StrategoImmutableRelation(extractAliases(constrs, injections)));
            }

            private static BinaryRelation.Immutable<? extends IStrategoTerm, ? extends IStrategoTerm> extractAliases(
                BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm> constrs,
                BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> injections) {
                BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> invInjections =
                    injections.inverse();
                BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> aliases = BinaryRelation.Transient.of();
                outer:
                for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> e : injections.entrySet()) {
                    final IStrategoTerm key = e.getKey();
                    final IStrategoTerm value = e.getValue();
                    // we select injections where no other injections go into the target type
                    if(invInjections.get(value).size() == 1) {
                        // and there are no constructors, for the target type
                        for(IStrategoTerm t : constrs.values()) {
                            if(TermUtils.isAppl(t, "ConstrType", 2) && t.getSubterm(1).equals(value)) {
                                continue outer;
                            }
                        }
                        // then save that as the inverse of the original injection
                        aliases.__insert(value, key);
                    }
                }
                return aliases.freeze();
            }

            private static BinaryRelation.Immutable<? extends IStrategoTerm, ? extends IStrategoTerm> lubMapFromInjClosure(
                StrategoImmutableRelation injectionClosure, ITermFactory tf) {
                /* TODO: find cyclic injections through entries that have the same type on both sides (x,x)
                 *  Use an arbitrary but deterministic method to choose a representative type in a cycle
                 *  Map members x,y from the same cycle to the representative type
                 */
                final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> lubMap = BinaryRelation.Transient.of();
                for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> entry : injectionClosure.backingRelation
                    .entrySet()) {
                    final IStrategoTerm from = entry.getKey();
                    final IStrategoTerm to = entry.getValue();
                    lubMap.__put(tf.makeTuple(from, to), to);
                    lubMap.__put(tf.makeTuple(to, from), to);
                }
                return lubMap.freeze();
            }

            public Input build(IStrategoTerm ast, StrategySignature sig) {
                return new Input(moduleName, strategyEnvironment, constructors, injectionClosure, lubMap, aliasMap,
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
            if(getClass() != o.getClass())
                return false;
            Output output = (Output) o;
            return astWithCasts.equals(output.astWithCasts) && messages.equals(output.messages);
        }

        @Override
        public int hashCode() {
            return Objects.hash(astWithCasts, messages);
        }
    }

    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;

    @Inject
    public InsertCasts(IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext) {
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
    }


    @Override
    public Output exec(ExecContext execContext, Input input) throws ExecException, InterruptedException {
        final ITermFactory tf = strContext.getFactory();

        assert input.strategyEnvironment.backingMap.containsKey(input.sig) : "Cannot find strategy " + input.sig
            + " to type check in given environment.";

        // (strats, constrs, injection-closure, lub-map, aliases, ast)
        final IStrategoTerm tuple =
            tf.makeTuple(input.strategyEnvironment.withWrapper(tf), input.constructors.withWrapper(tf),
                input.injectionClosure.withWrapper(tf), input.lubMap.withWrapper(tf), input.aliasMap.withWrapper(tf), input.ast);

        final StrategoExecutor.ExecutionResult output = StrategoExecutor.runLocallyUniqueStringStrategy(
            ioAgentTrackerFactory, execContext.logger(), true, insert_casts_0_0.instance, tuple, strContext);
        if(!output.success) {
            throw new ExecException("Call to insert_casts failed on " + input.moduleName + ":" + input.sig.cifiedName() + ": \n" + output.exception);
        }

        final IStrategoTerm astWithCasts = new TermEqWithAttachments(output.result.getSubterm(0));
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
    }

    @Override
    public String getId() {
        return id;
    }

}
