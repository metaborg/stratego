package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.SplitResult.ConstructorSignature;
import mb.stratego.build.strincr.SplitResult.StrategySignature;

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
        final Set<StrategySignature> strategies;
        final Set<ConstructorSignature> constrs;

        Output(Set<StrategySignature> strategies, Set<ConstructorSignature> constrs) {
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

    @Inject public LibFrontend(ITermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override public Output exec(ExecContext execContext, Input input) throws Exception {
        BuildStats.executedFrontLibTasks++;
        final long startTime = System.nanoTime();
        final @Nullable File fileToRead = input.library.fileToRead();
        if(fileToRead != null) {
            execContext.require(fileToRead);
        }
        final IStrategoTerm ast = input.library.readLibraryFile(termFactory);
        // Expected: Specification([Signature([Constructors([...])]), Strategies([...])])
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl) ast).getName().equals("Specification"))) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification(...), but got: " + ast.toString(0));
        }
        final IStrategoTerm specList = ast.getSubterm(0);
        if(!(TermUtils.isList(specList) && specList.getSubtermCount() == 2)) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification([..., ...]), but got: " + ast.toString(2));
        }
        final IStrategoTerm signaturesTerm = specList.getSubterm(0);
        final IStrategoTerm strategiesTerm = specList.getSubterm(1);
        if(!(TermUtils.isAppl(signaturesTerm) && TermUtils.tryGetName(signaturesTerm).orElse("").equals("Signature")
            && signaturesTerm.getSubtermCount() == 1 && TermUtils.isList(signaturesTerm.getSubterm(0))
            && TermUtils.toListAt(signaturesTerm, 0).size() == 1)) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification([Signature([...]), ...]), but got: " + ast
                    .toString(3));
        }
        final IStrategoTerm constructorsTerm = TermUtils.toListAt(signaturesTerm, 0).getSubterm(0);
        if(!(TermUtils.isAppl(constructorsTerm) && TermUtils.tryGetName(constructorsTerm).orElse("").equals("Constructors")
            && constructorsTerm.getSubtermCount() == 1 && TermUtils.isList(signaturesTerm.getSubterm(0)))) {
            throw new ExecException("Malformed built-in library AST. "
                + "Expected Specification([Signature([Constructors([...])]), ...]), but got: " + ast.toString(3));
        }
        if(!(TermUtils.isAppl(strategiesTerm) && TermUtils.tryGetName(strategiesTerm).orElse("").equals("Strategies")
            && strategiesTerm.getSubtermCount() == 1 && TermUtils.isList(strategiesTerm.getSubterm(0)))) {
            throw new ExecException("Malformed built-in library AST. "
                + "Expected Specification([Signature([Constructors([...])]), Strategies([...])]), but got: " + ast
                .toString(3));
        }
        final Set<ConstructorSignature> constrs = extractConstrs(TermUtils.toListAt(constructorsTerm, 0));
        // TODO: Support type definitions for strategies
        final Set<StrategySignature> strategies = extractStrategies(TermUtils.toListAt(strategiesTerm, 0));

        BuildStats.frontLibTaskTime += System.nanoTime() - startTime;
        return new Output(strategies, constrs);
    }

    private Set<ConstructorSignature> extractConstrs(IStrategoList extConstrTerms) throws ExecException {
        // TODO: Support types of constructors
        final Set<ConstructorSignature> constrs = new HashSet<>();
        for(IStrategoTerm extConstrTerm : extConstrTerms) {
            if(TermUtils.isAppl(extConstrTerm)) {
                IStrategoAppl extConstrAppl = (IStrategoAppl) extConstrTerm;
                if(TermUtils.isAppl(extConstrAppl, "ExtOpDecl", 2)) {
                    if(!TermUtils.isString(extConstrAppl.getSubterm(0))) {
                        throw new ExecException(
                            "Malformed built-in library AST. " + "Expected ExtOpDecl(\"...\", ...) but got: "
                                + extConstrTerm.toString(2));
                    }
                    if(TermUtils.isAppl(extConstrAppl.getSubterm(1))
                        && TermUtils.isAppl(TermUtils.toApplAt(extConstrAppl, 1), "FunType", 2)
                        && TermUtils.isList(extConstrAppl.getSubterm(1).getSubterm(0))) {
                        final IStrategoList paramList = TermUtils.toListAt(extConstrAppl.getSubterm(1), 0);
                        constrs.add(new ConstructorSignature(TermUtils.toJavaStringAt(extConstrAppl, 0), paramList.size()));
                    } else if(TermUtils.isAppl(extConstrAppl.getSubterm(1))
                        && TermUtils.isAppl(TermUtils.toApplAt(extConstrAppl, 1), "ConstType", 1)) {
                        constrs.add(new ConstructorSignature(TermUtils.toJavaStringAt(extConstrAppl, 0), 0));
                    } else {
                        throw new ExecException("Malformed built-in library AST. "
                            + "Expected ExtOpDecl(\"...\", FunType([...], ...)) or ExtOpDecl(\"...\", ConstType(...)) but got: "
                            + extConstrTerm.toString(2));
                    }
                    continue;
                } else if(TermUtils.isAppl(extConstrAppl, "ExtOpDeclQ", 2)) {
                    if(!(TermUtils.isString(extConstrAppl.getSubterm(0))
                        && TermUtils.isAppl(extConstrAppl.getSubterm(1))
                        && TermUtils.isAppl(TermUtils.toApplAt(extConstrAppl, 1), "FunType", 2)
                        && TermUtils.isList(extConstrAppl.getSubterm(1).getSubterm(0)))) {
                        throw new ExecException("Malformed built-in library AST. "
                            + "Expected ExtOpDeclQ(\"...\", FunType([...], ...)) but got: " + extConstrTerm
                            .toString(2));
                    }
                    final IStrategoList paramList = TermUtils.toListAt(extConstrAppl.getSubterm(1), 0);
                    constrs.add(new ConstructorSignature(StringEscapeUtils.escapeJava(TermUtils.toJavaStringAt(extConstrAppl, 0)), paramList.size()));
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

    private Set<StrategySignature> extractStrategies(IStrategoList extSDefTerms) throws ExecException {
        final Set<StrategySignature> strategyConstrs = new HashSet<>();
        for(IStrategoTerm extSDefTerm : extSDefTerms) {
            if(!TermUtils.isAppl(extSDefTerm, "ExtSDef", 3)) {
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
                .add(new StrategySignature(TermUtils.toJavaString(name),  sargs.getSubtermCount(), targs.getSubtermCount()));
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
