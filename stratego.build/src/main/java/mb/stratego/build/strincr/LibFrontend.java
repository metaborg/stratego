package mb.stratego.build.strincr;

import static org.spoofax.interpreter.core.Interpreter.cify;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.StringSetWithPositions;
import org.spoofax.terms.util.TermUtils;

public class LibFrontend implements TaskDef<LibFrontend.Input, LibFrontend.Output> {
    public static final String id = LibFrontend.class.getCanonicalName();

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
        final StringSetWithPositions strategies;
        final StringSetWithPositions constrs;

        Output(StringSetWithPositions strategies, StringSetWithPositions constrs) {
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

    private final ITermFactory termFactory;
    static ArrayList<Long> timestamps = new ArrayList<>();

    @Inject public LibFrontend(ITermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override public Output exec(ExecContext execContext, Input input) throws Exception {
        BuildStats.executedFrontLibTasks++;
        timestamps.add(System.nanoTime());
        final long startTime = System.nanoTime();
        final @Nullable File fileToRead = input.library.fileToRead();
        if(fileToRead != null) {
            timestamps.add(System.nanoTime());
            execContext.require(fileToRead);
            timestamps.add(System.nanoTime());
        }
        final IStrategoTerm ast = input.library.readLibraryFile(termFactory);
        // Expected: Specification([Signature([Constructors([...])]), Strategies([...])])
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl) ast).getName().equals("Specification"))) {
            timestamps.add(System.nanoTime());
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification(...), but got: " + ast.toString(0));
        }
        final IStrategoTerm specList = ast.getSubterm(0);
        if(!(TermUtils.isList(specList) && specList.getSubtermCount() == 2)) {
            timestamps.add(System.nanoTime());
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification([..., ...]), but got: " + ast.toString(2));
        }
        final IStrategoTerm signaturesTerm = specList.getSubterm(0);
        final IStrategoTerm strategiesTerm = specList.getSubterm(1);
        if(!(TermUtils.isAppl(signaturesTerm) && TermUtils.tryGetName(signaturesTerm).orElse("").equals("Signature")
            && signaturesTerm.getSubtermCount() == 1 && TermUtils.isList(signaturesTerm.getSubterm(0))
            && TermUtils.toListAt(signaturesTerm, 0).size() == 1)) {
            timestamps.add(System.nanoTime());
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification([Signature([...]), ...]), but got: " + ast
                    .toString(3));
        }
        final IStrategoTerm constructorsTerm = TermUtils.toListAt(signaturesTerm, 0).getSubterm(0);
        if(!(TermUtils.isAppl(constructorsTerm) && TermUtils.tryGetName(constructorsTerm).orElse("").equals("Constructors")
            && constructorsTerm.getSubtermCount() == 1 && TermUtils.isList(signaturesTerm.getSubterm(0)))) {
            timestamps.add(System.nanoTime());
            throw new ExecException("Malformed built-in library AST. "
                + "Expected Specification([Signature([Constructors([...])]), ...]), but got: " + ast.toString(3));
        }
        if(!(TermUtils.isAppl(strategiesTerm) && TermUtils.tryGetName(strategiesTerm).orElse("").equals("Strategies")
            && strategiesTerm.getSubtermCount() == 1 && TermUtils.isList(strategiesTerm.getSubterm(0)))) {
            timestamps.add(System.nanoTime());
            throw new ExecException("Malformed built-in library AST. "
                + "Expected Specification([Signature([Constructors([...])]), Strategies([...])]), but got: " + ast
                .toString(3));
        }
        final StringSetWithPositions constrs = extractConstrs(TermUtils.toListAt(constructorsTerm, 0));
        final StringSetWithPositions strategies = extractStrategies(TermUtils.toListAt(strategiesTerm, 0));

        BuildStats.frontLibTaskTime += System.nanoTime() - startTime;
        timestamps.add(System.nanoTime());
        return new Output(strategies, constrs);
    }

    private StringSetWithPositions extractConstrs(IStrategoList extConstrTerms) throws ExecException {
        final StringSetWithPositions constrs = new StringSetWithPositions();
        for(IStrategoTerm extConstrTerm : extConstrTerms) {
            if(TermUtils.isAppl(extConstrTerm)) {
                IStrategoAppl extConstrAppl = (IStrategoAppl) extConstrTerm;
                if(TermUtils.isAppl(extConstrAppl, "ExtOpDecl", 2)) {
                    if(!(TermUtils.isString(extConstrAppl.getSubterm(0)))) {
                        throw new ExecException(
                            "Malformed built-in library AST. " + "Expected ExtOpDecl(\"...\", ...) but got: "
                                + extConstrTerm.toString(2));
                    }
                    if(TermUtils.isAppl(extConstrAppl.getSubterm(1)) && TermUtils
                        .isAppl(TermUtils.toApplAt(extConstrAppl, 1), "FunType", 2) && TermUtils
                        .isList(extConstrAppl.getSubterm(1).getSubterm(0))) {
                        constrs.add(B.string(
                            TermUtils.toJavaStringAt(extConstrAppl, 0) + "_" + TermUtils.toListAt(extConstrAppl.getSubterm(1), 0)
                                .size()));
                    } else if(TermUtils.isAppl(extConstrAppl.getSubterm(1)) && TermUtils
                        .isAppl(TermUtils.toApplAt(extConstrAppl, 1), "ConstType", 1)) {
                        constrs.add(B.string(TermUtils.toJavaStringAt(extConstrAppl, 0) + "_0"));
                    } else {
                        throw new ExecException("Malformed built-in library AST. "
                            + "Expected ExtOpDecl(\"...\", FunType([...], ...)) or ExtOpDecl(\"...\", ConstType(...)) but got: "
                            + extConstrTerm.toString(2));
                    }
                    continue;
                } else if(TermUtils.isAppl(extConstrAppl, "ExtOpDeclQ", 2)) {
                    if(!(TermUtils.isString(extConstrAppl.getSubterm(0)) && TermUtils
                        .isAppl(extConstrAppl.getSubterm(1)) && TermUtils
                        .isAppl(TermUtils.toApplAt(extConstrAppl, 1), "FunType", 2) && TermUtils
                        .isList(extConstrAppl.getSubterm(1).getSubterm(0)))) {
                        throw new ExecException("Malformed built-in library AST. "
                            + "Expected ExtOpDeclQ(\"...\", FunType([...], ...)) but got: " + extConstrTerm
                            .toString(2));
                    }
                    constrs.add(B.string(StringEscapeUtils.escapeJava(TermUtils.toJavaStringAt(extConstrAppl, 0)) + "_" + TermUtils.toListAt(extConstrAppl.getSubterm(1), 0).size()));
                    continue;
                } else if(TermUtils.isAppl(extConstrAppl, "ExtOpDeclInj", 1)) {
                    continue;
                }
            }
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected constructor declaration but got: " + extConstrTerm
                    .toString(0));
        }
        return constrs;
    }

    private StringSetWithPositions extractStrategies(IStrategoList extSDefTerms) throws ExecException {
        final StringSetWithPositions strategyConstrs = new StringSetWithPositions();
        for(IStrategoTerm extSDefTerm : extSDefTerms) {
            if(!(TermUtils.isAppl(extSDefTerm, "ExtSDef", 3))) {
                throw new ExecException(
                    "Malformed built-in library AST. " + "Expected ExtSDef(..., ..., ...) but got: " + extSDefTerm
                        .toString(0));
            }
            IStrategoTerm name = extSDefTerm.getSubterm(0);
            IStrategoTerm sargs = extSDefTerm.getSubterm(1);
            IStrategoTerm targs = extSDefTerm.getSubterm(2);
            if(!(TermUtils.isString(name) && TermUtils.isList(sargs) && TermUtils.isList(targs))) {
                throw new ExecException(
                    "Malformed built-in library AST. " + "Expected ExtSDef(\"...\", ..., ...) but got: " + extSDefTerm
                        .toString(1));
            }
            strategyConstrs
                .add(B.string(cify(TermUtils.toJavaString(name)) + "_" + sargs.getSubtermCount() + "_" + targs.getSubtermCount()));
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
