package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import mb.stratego.build.termvisitors.DesugarType;

public class LibFrontend implements TaskDef<LibFrontend.Input, LibFrontend.Output> {
    public static final String id = LibFrontend.class.getCanonicalName();

    static final class Input implements Serializable {
        final Library library;

        Input(Library library) {
            this.library = library;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            return library.equals(input.library);
        }

        @Override
        public int hashCode() {
            return library.hashCode();
        }

        @Override
        public String toString() {
            return "StrIncrFrontLib$Input(" + library + ')';
        }
    }

    static final class Output implements Serializable {
        final Map<StrategySignature, IStrategoTerm> strategies;
        final Map<ConstructorSignature, IStrategoTerm> constrs;
        final Set<IStrategoTerm> injs;

        Output(Map<StrategySignature, IStrategoTerm> strategies, Map<ConstructorSignature, IStrategoTerm> constrs,
            Set<IStrategoTerm> injs) {
            this.strategies = strategies;
            this.constrs = constrs;
            this.injs = injs;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Output output = (Output) o;
            if(!strategies.equals(output.strategies)) return false;
            if(!constrs.equals(output.constrs)) return false;
            return injs.equals(output.injs);
        }

        @Override public int hashCode() {
            int result = strategies.hashCode();
            result = 31 * result + constrs.hashCode();
            result = 31 * result + injs.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "StrIncrFrontLib$Output(" + strategies + ", " + constrs + ')';
        }
    }

    private final ITermFactory termFactory;

    @Inject public LibFrontend(ITermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override
    public Output exec(ExecContext execContext, Input input) throws Exception {
        BuildStats.executedFrontLibTasks++;
        final long startTime = System.nanoTime();
        final @Nullable File fileToRead = input.library.fileToRead();
        if(fileToRead != null) {
            execContext.require(fileToRead);
        }
        final IStrategoTerm ast = input.library.readLibraryFile(termFactory);
        // Expected: Specification([Signature([Constructors([...])]), Strategies([...])])
        if(!TermUtils.isAppl(ast, "Specification", 1)) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification(...), but got: " + ast.toString(0));
        }
        final IStrategoTerm specList = ast.getSubterm(0);
        if(!TermUtils.isList(specList, 2)) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification([..., ...]), but got: " + ast.toString(2));
        }
        final IStrategoTerm signaturesTerm = specList.getSubterm(0);
        final IStrategoTerm strategiesTerm = specList.getSubterm(1);
        if(!(TermUtils.isAppl(signaturesTerm, "Signature", 1)
            && TermUtils.isList(signaturesTerm.getSubterm(0), 1))) {
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected Specification([Signature([...]), ...]), but got: " + ast
                    .toString(3));
        }
        final IStrategoTerm constructorsTerm = TermUtils.toListAt(signaturesTerm, 0).getSubterm(0);
        if(!(TermUtils.isAppl(constructorsTerm, "Constructors", 1) && TermUtils.isList(signaturesTerm.getSubterm(0)))) {
            throw new ExecException("Malformed built-in library AST. "
                + "Expected Specification([Signature([Constructors([...])]), ...]), but got: " + ast.toString(3));
        }
        if(!(TermUtils.isAppl(strategiesTerm, "Strategies", 1) && TermUtils.isList(strategiesTerm.getSubterm(0)))) {
            throw new ExecException("Malformed built-in library AST. "
                + "Expected Specification([Signature([Constructors([...])]), Strategies([...])]), but got: " + ast
                .toString(3));
        }
        final Set<IStrategoTerm> injs = extractInjections(termFactory, TermUtils.toListAt(constructorsTerm, 0));
        final Map<ConstructorSignature, IStrategoTerm> constrs =
            extractConstrs(termFactory, TermUtils.toListAt(constructorsTerm, 0));
        final Map<StrategySignature, IStrategoTerm> strats =
            extractStrategies(termFactory, TermUtils.toListAt(strategiesTerm, 0));

        BuildStats.frontLibTaskTime += System.nanoTime() - startTime;
        return new Output(strats, constrs, injs);
    }

    private Set<IStrategoTerm> extractInjections(ITermFactory tf, IStrategoList extConstrTerms) throws ExecException {
        final Set<IStrategoTerm> injs = new HashSet<>();
        for(IStrategoTerm extConstrTerm : extConstrTerms) {
            if(TermUtils.isAppl(extConstrTerm, "ExtOpDeclInj", 1)) {
                IStrategoTerm optype = extConstrTerm.getSubterm(0);
                if(TermUtils.isAppl(optype, "FunType", 2)
                    && TermUtils.isListAt(optype, 0, 1)) {
                    final IStrategoTerm from = optype.getSubterm(0).getSubterm(0);
                    final IStrategoTerm to = optype.getSubterm(1);
                    if(TermUtils.isAppl(from, "ConstType", 1)
                        && TermUtils.isAppl(to, "ConstType", 1)) {
                        injs.add(tf.makeTuple(DesugarType.tryDesugarType(tf, from.getSubterm(0)), DesugarType
                            .tryDesugarType(tf, to.getSubterm(0))));
                    }
                }
            }
        }
        return injs;
    }

    private Map<ConstructorSignature, IStrategoTerm> extractConstrs(ITermFactory tf, IStrategoList extConstrTerms)
        throws ExecException {
        final Map<ConstructorSignature, IStrategoTerm> constrs = new HashMap<>();
        for(IStrategoTerm extConstrTerm : extConstrTerms) {
            if(TermUtils.isAppl(extConstrTerm, "ExtOpDecl", 2)) {
                if(!TermUtils.isString(extConstrTerm.getSubterm(0))) {
                    throw new ExecException(
                        "Malformed built-in library AST. " + "Expected ExtOpDecl(\"...\", ...) but got: "
                            + extConstrTerm.toString(2));
                }
                if(TermUtils.isAppl(extConstrTerm.getSubterm(1), "FunType", 2)
                    && TermUtils.isList(extConstrTerm.getSubterm(1).getSubterm(0))) {
                    final IStrategoTerm funtype = extConstrTerm.getSubterm(1);
                    final IStrategoList paramList = TermUtils.toListAt(funtype, 0);
                    final String constrName = TermUtils.toJavaStringAt(extConstrTerm, 0);
                    final ConstructorSignature sig = new ConstructorSignature(constrName, paramList.size());
                    constrs.put(sig, constrTypeFromFunType(tf, funtype));
                } else if(TermUtils.isAppl(extConstrTerm.getSubterm(1), "ConstType", 1)) {
                    final IStrategoTerm consttype = extConstrTerm.getSubterm(1);
                    final String constrName = TermUtils.toJavaStringAt(extConstrTerm, 0);
                    final ConstructorSignature sig = new ConstructorSignature(constrName, 0);
                    constrs.put(sig, constrTypeFromConstType(tf, consttype));
                } else {
                    throw new ExecException("Malformed built-in library AST. "
                        + "Expected ExtOpDecl(\"...\", FunType([...], ...)) or ExtOpDecl(\"...\", ConstType(...))"
                        + " but got: " + extConstrTerm.toString(2));
                }
                continue;
            } else if(TermUtils.isAppl(extConstrTerm, "ExtOpDeclQ", 2)) {
                if(!(TermUtils.isString(extConstrTerm.getSubterm(0)) && TermUtils.isAppl(extConstrTerm.getSubterm(1))
                    && TermUtils.isAppl(TermUtils.toApplAt(extConstrTerm, 1), "FunType", 2)
                    && TermUtils.isList(extConstrTerm.getSubterm(1).getSubterm(0)))) {
                    throw new ExecException("Malformed built-in library AST. "
                        + "Expected ExtOpDeclQ(\"...\", FunType([...], ...)) but got: " + extConstrTerm.toString(2));
                }
                final IStrategoTerm funtype = extConstrTerm.getSubterm(1);
                final IStrategoList paramList = TermUtils.toListAt(funtype, 0);
                final String constrName = TermUtils.toJavaStringAt(extConstrTerm, 0);
                final ConstructorSignature sig =
                    new ConstructorSignature(StringEscapeUtils.escapeJava(constrName), paramList.size());
                constrs.put(sig, constrTypeFromFunType(tf, funtype));
                continue;
            } else if(TermUtils.isAppl(extConstrTerm, "ExtOpDeclInj", 1)) {
                IStrategoTerm optype = extConstrTerm.getSubterm(0);
                if(TermUtils.isAppl(optype, "FunType", 2)
                    && TermUtils.isListAt(optype, 0)
                    && TermUtils.toListAt(optype, 0).size() > 1) {
                    final ConstructorSignature sig = new ConstructorSignature("", TermUtils.toListAt(optype, 0).size());
                    constrs.put(sig, constrTypeFromFunType(tf, optype));
                }
                continue;
            }
            throw new ExecException(
                "Malformed built-in library AST. " + "Expected constructor declaration but got: " + extConstrTerm
                    .toString(0));
        }
        return constrs;
    }

    private IStrategoTerm constrTypeFromConstType(ITermFactory tf, IStrategoTerm constType) throws ExecException {
        // ConstType(...)
        return tf
            .replaceTerm(tf.makeAppl("ConstrType", tf.makeList(), DesugarType
                .tryDesugarType(tf, constType.getSubterm(0))), constType);
    }

    private IStrategoTerm constrTypeFromFunType(ITermFactory tf, IStrategoTerm funType) throws ExecException {
        // FunType([...], ...)
        final IStrategoAppl dynt = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
        final IStrategoList args = TermUtils.toListAt(funType, 0);
        final IStrategoList.Builder b = tf.arrayListBuilder(args.size());
        for(IStrategoTerm t : args) {
            if(TermUtils.isAppl(t, "ConstType", 1)) {
                b.add(DesugarType.tryDesugarType(tf, t.getSubterm(0)));
            } else {
                b.add(dynt);
            }
        }
        return tf
            .replaceTerm(tf.makeAppl("ConstrType", tf.makeList(b), DesugarType
                .tryDesugarType(tf, funType.getSubterm(1).getSubterm(0))), funType);
    }

    private Map<StrategySignature, IStrategoTerm> extractStrategies(ITermFactory tf, IStrategoList extSDefTerms)
        throws ExecException {
        final Map<StrategySignature, IStrategoTerm> strategyConstrs = new HashMap<>();
        for(IStrategoTerm extSDefTerm : extSDefTerms) {
            if(TermUtils.isAppl(extSDefTerm, "ExtSDef", 3)) {
                IStrategoTerm name = extSDefTerm.getSubterm(0);
                IStrategoTerm sargs = extSDefTerm.getSubterm(1);
                IStrategoTerm targs = extSDefTerm.getSubterm(2);
                if(!(TermUtils.isString(name) && TermUtils.isList(sargs) && TermUtils.isList(targs))) {
                    throw new ExecException(
                        "Malformed built-in library AST. "
                            + "Expected ExtSDef(\"...\", ..., ...) but got: "
                            + extSDefTerm.toString(1));
                }
                final StrategySignature sig =
                    new StrategySignature(TermUtils.toJavaString(name), sargs.getSubtermCount(),
                        targs.getSubtermCount());
                strategyConstrs.put(sig, sig.standardType(tf).toTerm(tf));
            } else if(TermUtils.isAppl(extSDefTerm, "DefHasType", 2)) {
                IStrategoTerm name = extSDefTerm.getSubterm(0);
                IStrategoTerm sfuntype = extSDefTerm.getSubterm(1);
                if(!(TermUtils.isString(name) && TermUtils.isAppl(sfuntype, "FunTType", 3)
                    && TermUtils.isListAt(sfuntype, 0)
                    && TermUtils.isListAt(sfuntype, 1))) {
                    throw new ExecException(
                        "Malformed built-in library AST. "
                            + "Expected DefHasType(\"...\", FunTType([...], [...], ...)) but got: "
                            + extSDefTerm.toString(1));
                }
                IStrategoTerm sargs = sfuntype.getSubterm(0);
                IStrategoTerm targs = sfuntype.getSubterm(1);
                final StrategySignature sig =
                    new StrategySignature(TermUtils.toJavaString(name), sargs.getSubtermCount(),
                        targs.getSubtermCount());
                strategyConstrs.put(sig, sfuntype);
            } else {
                throw new ExecException(
                    "Malformed built-in library AST. "
                        + "Expected ExtSDef(..., ..., ...) or DefHasType(..., ...) but got: "
                        + extSDefTerm.toString(0));
            }
        }
        return strategyConstrs;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Serializable key(Input input) {
        return input.library;
    }
}
