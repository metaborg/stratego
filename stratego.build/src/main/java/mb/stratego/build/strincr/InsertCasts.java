package mb.stratego.build.strincr;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.StrIncrContext;

import com.google.inject.Inject;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.AbstractTermFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InsertCasts implements TaskDef<InsertCasts.Input, InsertCasts.Output> {
    public static final String id = InsertCasts.class.getCanonicalName();

    public static final class Input implements Serializable {
        final StrategoImmutableMap strategyEnvironment;
        final StrategoImmutableMap constructors;
        final StrategoImmutableSet injectionClosure;
        final StrategoImmutableMap lubMap;
        final IStrategoTerm strictnessLevel;
        final Map<String, List<IStrategoAppl>> asts;

        Input(StrategoImmutableMap strategyEnvironment, StrategoImmutableMap constructors,
            StrategoImmutableSet injectionClosure, StrategoImmutableMap lubMap, IStrategoTerm strictnessLevel,
            Map<String, List<IStrategoAppl>> asts) {
            this.strategyEnvironment = strategyEnvironment;
            this.constructors = constructors;
            this.injectionClosure = injectionClosure;
            this.lubMap = lubMap;
            this.strictnessLevel = strictnessLevel;
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
                && Objects.equals(lubMap, input.lubMap) && Objects.equals(strictnessLevel, input.strictnessLevel)
                && Objects.equals(asts, input.asts);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strategyEnvironment, constructors, injectionClosure, lubMap, strictnessLevel, asts);
        }
    }

    public static final class Output implements Serializable {
        final Map<String, List<IStrategoAppl>> astsWithCasts;

        public Output(Map<String, List<IStrategoAppl>> astsWithCasts) {
            this.astsWithCasts = astsWithCasts;
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
        Map<String, List<IStrategoAppl>> result = new HashMap<>(2 * input.asts.size());

        final ITermFactory factory = strContext.getFactory();
        final IStrategoTerm sEnv = input.strategyEnvironment.withWrapper(factory);
        final IStrategoTerm constrs = input.constructors.withWrapper(factory);
        final IStrategoTerm injClos = input.injectionClosure.withWrapper(factory);
        final IStrategoTerm lubMap = input.lubMap.withWrapper(factory);

        for(Map.Entry<String, List<IStrategoAppl>> e : input.asts.entrySet()) {
            final String cifiedName = e.getKey();
            final List<IStrategoAppl> ast = e.getValue();
            final IStrategoTerm tuple = factory.makeTuple(sEnv, constrs, injClos, lubMap, input.strictnessLevel,
                factory.makeList(ast));
//            SubFrontend.Output output = execContext.require(strIncrSubFront
//                .createTask(new SubFrontend.Input(null, cifiedName, SubFrontend.InputType.InsertCasts, tuple)));
//            final IStrategoList outputList = (IStrategoList) output.result;
//            final List<IStrategoAppl> list = new ArrayList<>(outputList.size());
//            for(IStrategoTerm term : outputList) {
//                list.add((IStrategoAppl) term);
//            }
//            result.put(cifiedName, list);

        }

        return new Output(result);
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
