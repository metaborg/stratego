package mb.stratego.build;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.spoofax.interpreter.core.Interpreter.cify;

public class StrIncrFrontLib implements TaskDef<StrIncrFrontLib.Input, StrIncrFrontLib.Output> {
    public static final String id = StrIncrFrontLib.class.getCanonicalName();

    static final class Input implements Serializable {
        final Library library;

        Input(Library library) {
            this.library = library;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            return library.equals(input.library);
        }

        @Override public int hashCode() {
            return library.hashCode();
        }

        @Override public String toString() {
            return "StrIncrFrontLib$Input(" + library + ')';
        }
    }

    static final class Output implements Serializable {
        final Set<String> strategies;
        final Set<String> constrs;

        Output(Set<String> strategies, Set<String> constrs) {
            this.strategies = strategies;
            this.constrs = constrs;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Output output = (Output) o;

            //noinspection SimplifiableIfStatement
            if(!strategies.equals(output.strategies))
                return false;
            return constrs.equals(output.constrs);
        }

        @Override public int hashCode() {
            int result = strategies.hashCode();
            result = 31 * result + constrs.hashCode();
            return result;
        }

        @Override public String toString() {
            return "StrIncrFrontLib$Output(" + strategies + ", " + constrs + ')';
        }
    }

    private final ITermFactoryService termFactoryService;

    @Inject public StrIncrFrontLib(ITermFactoryService termFactoryService) {
        this.termFactoryService = termFactoryService;
    }

    @Override public Output exec(ExecContext execContext, Input input) throws Exception {
        final @Nullable File fileToRead = input.library.fileToRead();
        if(fileToRead != null) {
            execContext.require(fileToRead);
        }
        final IStrategoTerm ast = input.library.readLibraryFile(termFactoryService.getGeneric());
        // Expected: Specification([Signature([Constructors([...])]), Strategies([...])])
        if(!(Tools.isTermAppl(ast) && ((IStrategoAppl) ast).getName().equals("Specification"))) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification(...), but got: " + ast.toString(0));
        }
        final IStrategoTerm specList = ast.getSubterm(0);
        if(!(Tools.isTermList(specList) && specList.getSubtermCount() == 2)) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification([..., ...]), but got: " + ast.toString(2));
        }
        final IStrategoTerm signaturesTerm = specList.getSubterm(0);
        final IStrategoTerm strategiesTerm = specList.getSubterm(1);
        if(!(Tools.isTermAppl(signaturesTerm) && Tools.constructorName(signaturesTerm).equals("Signature")
            && signaturesTerm.getSubtermCount() == 1 && Tools.isTermList(signaturesTerm.getSubterm(0))
            && Tools.listAt(signaturesTerm, 0).size() == 1)) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification([Signature([...]), ...]), but got: " + ast
                    .toString(3));
        }
        final IStrategoTerm constructorsTerm = Tools.listAt(signaturesTerm, 0).getSubterm(0);
        if(!(Tools.isTermAppl(constructorsTerm) && Tools.constructorName(constructorsTerm).equals("Constructors")
            && constructorsTerm.getSubtermCount() == 1 && Tools.isTermList(signaturesTerm.getSubterm(0)))) {
            throw new ExecException("Malformed built-in library AST. "
                + "Expected Specification([Signature([Constructors([...])]), ...]), but got: " + ast.toString(3));
        }
        if(!(Tools.isTermAppl(strategiesTerm) && Tools.constructorName(strategiesTerm).equals("Strategies")
            && strategiesTerm.getSubtermCount() == 1 && Tools.isTermList(strategiesTerm.getSubterm(0)))) {
            throw new ExecException("Malformed built-in library AST. "
                + "Expected Specification([Signature([Constructors([...])]), Strategies([...])]), but got: " + ast
                .toString(3));
        }
        final Set<String> constrs = extractConstrs(Tools.listAt(constructorsTerm, 0));
        final Set<String> strategies = extractStrategies(Tools.listAt(strategiesTerm, 0));
        return new Output(strategies, constrs);
    }

    private Set<String> extractConstrs(IStrategoList extConstrTerms) throws ExecException {
        final Set<String> constrs = new HashSet<>();
        for(IStrategoTerm extConstrTerm : extConstrTerms) {
            if(Tools.isTermAppl(extConstrTerm)) {
                IStrategoAppl extConstrAppl = (IStrategoAppl) extConstrTerm;
                if(Tools.hasConstructor(extConstrAppl, "ExtOpDecl", 2)) {
                    if(!(Tools.isTermString(extConstrAppl.getSubterm(0)))) {
                        throw new ExecException(
                            "Malformed built-in library AST. " + "Expected ExtOpDecl(\"...\", ...) but got: "
                                + extConstrTerm.toString(2));
                    }
                    if(Tools.isTermAppl(extConstrAppl.getSubterm(1)) && Tools
                        .hasConstructor(Tools.applAt(extConstrAppl, 1), "FunType", 2) && Tools
                        .isTermList(extConstrAppl.getSubterm(1).getSubterm(0))) {
                        constrs.add(
                            Tools.javaStringAt(extConstrAppl, 0) + "_" + Tools.listAt(extConstrAppl.getSubterm(1), 0)
                                .size());
                    } else if(Tools.isTermAppl(extConstrAppl.getSubterm(1)) && Tools
                        .hasConstructor(Tools.applAt(extConstrAppl, 1), "ConstType", 1)) {
                        constrs.add(Tools.javaStringAt(extConstrAppl, 0) + "_0");
                    } else {
                        throw new ExecException("Malformed built-in library AST. "
                            + "Expected ExtOpDecl(\"...\", FunType([...], ...)) or ExtOpDecl(\"...\", ConstType(...)) but got: "
                            + extConstrTerm.toString(2));
                    }
                    continue;
                } else if(Tools.hasConstructor(extConstrAppl, "ExtOpDeclQ", 2)) {
                    if(!(Tools.isTermString(extConstrAppl.getSubterm(0)) && Tools
                        .isTermAppl(extConstrAppl.getSubterm(1)) && Tools
                        .hasConstructor(Tools.applAt(extConstrAppl, 1), "FunType", 2) && Tools
                        .isTermList(extConstrAppl.getSubterm(1).getSubterm(0)))) {
                        throw new ExecException("Malformed built-in library AST. "
                            + "Expected ExtOpDeclQ(\"...\", FunType([...], ...)) but got: " + extConstrTerm
                            .toString(2));
                    }
                    constrs.add(StringEscapeUtils.escapeJava(Tools.javaStringAt(extConstrAppl, 0)) + "_" + Tools
                        .listAt(extConstrAppl.getSubterm(1), 0).size());
                    continue;
                } else if(Tools.hasConstructor(extConstrAppl, "ExtOpDeclInj", 1)) {
                    continue;
                }
            }
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected constructor declaration but got: " + extConstrTerm
                    .toString(0));
        }
        return constrs;
    }

    private Set<String> extractStrategies(IStrategoList extSDefTerms) throws ExecException {
        final Set<String> strategyConstrs = new HashSet<>();
        for(IStrategoTerm extSDefTerm : extSDefTerms) {
            if(!(Tools.isTermAppl(extSDefTerm) && Tools.hasConstructor((IStrategoAppl) extSDefTerm, "ExtSDef", 3))) {
                throw new ExecException(
                    "Malformed built-in library AST. " + "Expected ExtSDef(..., ..., ...) but got: " + extSDefTerm
                        .toString(0));
            }
            IStrategoTerm name = extSDefTerm.getSubterm(0);
            IStrategoTerm sargs = extSDefTerm.getSubterm(1);
            IStrategoTerm targs = extSDefTerm.getSubterm(2);
            if(!(Tools.isTermString(name) && Tools.isTermList(sargs) && Tools.isTermList(targs))) {
                throw new ExecException(
                    "Malformed built-in library AST. " + "Expected ExtSDef(\"...\", ..., ...) but got: " + extSDefTerm
                        .toString(1));
            }
            strategyConstrs
                .add(cify(Tools.javaString(name)) + "_" + sargs.getSubtermCount() + "_" + targs.getSubtermCount());
        }
        return strategyConstrs;
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.library;
    }
}
