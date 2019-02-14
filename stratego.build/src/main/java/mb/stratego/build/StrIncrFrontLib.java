package mb.stratego.build;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;

import org.apache.commons.lang3.StringEscapeUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.compat.override.strc_compat.Main;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.spoofax.interpreter.core.Interpreter.cify;

public class StrIncrFrontLib implements TaskDef<StrIncrFrontLib.Input, StrIncrFront.Output> {
    public static final String id = StrIncrFrontLib.class.getCanonicalName();
    static final Set<String> builtinLibraries = new HashSet<>();

    static {
        builtinLibraries.add("libstratego-lib");
        builtinLibraries.add("libstratego-sglr");
        builtinLibraries.add("libstratego-gpp");
        builtinLibraries.add("libstratego-xtc");
        builtinLibraries.add("libstratego-aterm");
        builtinLibraries.add("libstratego-sdf");
        builtinLibraries.add("libstrc");
        builtinLibraries.add("libjava-front");
    }

    static final class Input implements Serializable {
        final String libraryName;

        Input(String libraryName) {
            this.libraryName = libraryName;
        }
    }

    @Override public StrIncrFront.Output exec(ExecContext execContext, Input input)
        throws ExecException, InterruptedException {
        IStrategoTerm ast = getBuiltinLibrary(input.libraryName);
        Set<String> constrs = new HashSet<>();
        Set<String> strategies = new HashSet<>();
        extractInformation(ast, constrs, strategies);
        return new StrIncrFront.Output(input.libraryName, Collections.emptyMap(), strategies, Collections.emptySet(),
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), constrs);
    }

    private void extractInformation(IStrategoTerm ast, Set<String> constrs, Set<String> strategyConstrs)
        throws ExecException {
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
        extractConstrs(Tools.listAt(constructorsTerm, 0), constrs);
        extractStrategies(Tools.listAt(strategiesTerm, 0), strategyConstrs);
    }

    private void extractConstrs(IStrategoList extConstrTerms, Set<String> constrs) throws ExecException {
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
    }

    private void extractStrategies(IStrategoList extSDefTerms, Set<String> strategyConstrs) throws ExecException {
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
    }

    private static IStrategoTerm getBuiltinLibrary(String libraryName) throws ExecException {
        switch(libraryName) {
            case "stratego-lib":
            case "libstrategolib":
            case "libstratego-lib": {
                return Main.getLibstrategolibRtree();
            }
            case "stratego-sglr":
            case "libstratego-sglr": {
                return Main.getLibstrategosglrRtree();
            }
            case "stratego-gpp":
            case "libstratego-gpp": {
                return Main.getLibstrategogppRtree();
            }
            case "stratego-xtc":
            case "libstratego-xtc": {
                return Main.getLibstrategoxtcRtree();
            }
            case "stratego-aterm":
            case "libstratego-aterm": {
                return Main.getLibstrategoatermRtree();
            }
            case "stratego-sdf":
            case "libstratego-sdf": {
                return Main.getLibstrategosdfRtree();
            }
            case "strc":
            case "libstrc": {
                return Main.getLibstrcRtree();
            }
            case "java-front":
            case "libjava-front":
                return Main.getLibjavafrontRtree();
        }
        throw new ExecException("Library was not one of the 8 built-in libraries: " + libraryName);
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.libraryName;
    }

    @Override public String desc(Input input) {
        return TaskDef.DefaultImpls.desc(this, input);
    }

    @Override public String desc(Input input, int maxLength) {
        return TaskDef.DefaultImpls.desc(this, input, maxLength);
    }

    @Override public Task<Input, StrIncrFront.Output> createTask(Input input) {
        return TaskDef.DefaultImpls.createTask(this, input);
    }

    @Override public STask<Input> createSerializableTask(Input input) {
        return TaskDef.DefaultImpls.createSerializableTask(this, input);
    }
}
