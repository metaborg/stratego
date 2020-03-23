package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.StrIncrContext;

public class InsertCasts implements TaskDef<InsertCasts.Input, InsertCasts.Output> {
    public static final String id = InsertCasts.class.getCanonicalName();

    public static final class Input implements Serializable {
        final StrategoImmutableMap strategyEnvironment;
        final StrategoImmutableMap constructors;
        final StrategoImmutableSet injectionClosure;
        final StrategoImmutableMap lubMap;
        final Map<String, List<IStrategoAppl>> asts;

        Input(StrategoImmutableMap strategyEnvironment, StrategoImmutableMap constructors,
            StrategoImmutableSet injectionClosure, StrategoImmutableMap lubMap, Map<String, List<IStrategoAppl>> asts) {
            this.strategyEnvironment = strategyEnvironment;
            this.constructors = constructors;
            this.injectionClosure = injectionClosure;
            this.lubMap = lubMap;
            this.asts = asts;
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
                && Objects.equals(lubMap, input.lubMap) && Objects.equals(asts, input.asts);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strategyEnvironment, constructors, injectionClosure, lubMap, asts);
        }
    }

    public static final class Output implements Serializable {
        final Map<String, List<IStrategoAppl>> astsWithCasts;
        final List<Message<IStrategoString>> messages;

        public Output(Map<String, List<IStrategoAppl>> astsWithCasts, List<Message<IStrategoString>> messages) {
            this.astsWithCasts = astsWithCasts;
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
        Map<String, List<IStrategoAppl>> resultAsts = new HashMap<>(2 * input.asts.size());
        List<Message<IStrategoString>> messages = new ArrayList<>();

        final ITermFactory factory = strContext.getFactory();
        final IStrategoTerm sEnv = input.strategyEnvironment.withWrapper(factory);
        final IStrategoTerm constrs = input.constructors.withWrapper(factory);
        final IStrategoTerm injClos = input.injectionClosure.withWrapper(factory);
        final IStrategoTerm lubMap = input.lubMap.withWrapper(factory);

        for(Map.Entry<String, List<IStrategoAppl>> e : input.asts.entrySet()) {
            final String cifiedName = e.getKey();
            final List<IStrategoAppl> asts = e.getValue();
            final List<IStrategoAppl> astsWithCasts = new ArrayList<>(asts.size());
            for(IStrategoAppl ast : asts) {
                final IStrategoTerm tuple = factory.makeTuple(sEnv, constrs, injClos, lubMap, ast);
                final SubFrontend.Input frontInput =
                    new SubFrontend.Input(null, cifiedName, SubFrontend.InputType.InsertCasts, tuple);
                final SubFrontend.Output output = execContext.require(strIncrSubFront.createTask(frontInput));
                final IStrategoAppl astWithCasts = TermUtils.toApplAt(output.result, 0);
                final IStrategoList messageList = TermUtils.toListAt(output.result, 1);
                astsWithCasts.add(astWithCasts);
                for(IStrategoTerm message : messageList) {
                    messages.add(Message.from(message));
                }
                resultAsts.put(cifiedName, astsWithCasts);
            }
        }

        timestamps.add(System.nanoTime());
        return new Output(resultAsts, messages);
    }

    @Override
    public String getId() {
        return id;
    }

    //    @Override
    //    public Serializable key(Input input) {
    //        return input;
    //    }
}
