package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.annotation.Nullable;

import mb.pie.api.ExecContext;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.stratego.build.strincr.data.GTEnvironment;

public interface StrategoLanguage {
    /**
     * Parses an inputstream of Stratego code in textual format to an AST, and calls metaExplode if a dialect
     *
     * @param inputStream the Stratego code
     * @param charset     the charset of the stream
     * @param path        the path of the file from which the stream originated
     * @return an ATerm representation of the AST of the program
     * @throws Exception On failing to load the Stratego language, getting interrupted, IO problems, parsing problems etc.
     */
    IStrategoTerm parse(ExecContext context, InputStream inputStream, Charset charset, @Nullable String path) throws Exception;

    /**
     * Parses an inputstream of Stratego code in textual ATerm format or BAF representing an RTree AST
     *
     * @param inputStream the Stratego code
     * @return an ATerm representation of the AST of the program
     * @throws Exception On IO problems, parsing problems, or an AST with unexpected top-level constructor
     */
    default IStrategoTerm parseRtree(InputStream inputStream) throws Exception {
        final IStrategoTerm ast = newTermReader().parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Module") && ast.getSubtermCount() == 2)) {
            if(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Specification")
                && ast.getSubtermCount() == 1) {
                throw new IOException("Custom library detected with Specification/1 term in RTree file. This is "
                    + "currently not supported. ");
            }
            throw new ExecException("Did not find Module/2 in RTree file. Found: \n" + ast.toString(2));
        }
        return ast;
    }

    /**
     * Parses an inputstream of Stratego code in textual ATerm format or BAF representing an Str2Lib AST
     *
     * @param inputStream the Stratego code
     * @return an ATerm representation of the AST of the program
     * @throws Exception On IO problems, parsing problems, or an AST with unexpected top-level constructor
     */
    default IStrategoTerm parseStr2Lib(InputStream inputStream) throws Exception {
        final IStrategoTerm ast = newTermReader().parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Str2Lib") && ast.getSubtermCount() == 3)) {
            throw new ExecException("Did not find Str2Lib/3 in Str2Lib file. Found: \n" + ast.toString(2));
        }
        return ast;
    }

    TermReader newTermReader();

    /**
     * Extract the package names from the Str2Lib ast
     *
     * @param ast the ast to extract from
     * @return The package name in the str2lib or null is unavailable
     */
    default ArrayList<String> extractPackageNames(IStrategoTerm ast) {
        final ArrayList<String> packageNames = new ArrayList<>();
        if(TermUtils.isAppl(ast, "Str2Lib", 3)) {
            final IStrategoList components = TermUtils.toListAt(ast, 1);
            for(IStrategoTerm component : components) {
                if(TermUtils.isAppl(component, "Package", 1)) {
                    packageNames.add(TermUtils.toJavaStringAt(component, 0));
                }
            }
        }
        return packageNames;
    }

    /**
     * Call to the gradual type system for Stratego, to type-check an AST and insert casts where necessary
     *
     * @param moduleName The module name of the module to type-check
     * @param environment The "environment" object containing both the AST to type-check and global type information necessary to type-check the module
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return A tuple with the transformed AST and lists of messages (ast, error*, warning*, note*)
     * @throws ExecException On failing to load the Stratego language, internal error inside the type-checker
     */
    default IStrategoTerm insertCasts(String moduleName, GTEnvironment environment, String projectPath) throws
        ExecException {
        return callStrategy(environment, projectPath, "stratego2-insert-casts");
    }

    /**
     * Call to the desugaring code for Stratego, to transform an AST into a desugared AST
     *
     * @param ast the ast to desugar
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return A 2-tuple of the desugared AST and a list of strategy signatures (as 3-tuples) of uncified names (desugaredAst, strategySig*)
     * @throws ExecException On failing to load the Stratego language, internal error inside the desugarer
     */
    default IStrategoTerm desugar(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-compile-top-level-def");
    }


    default IStrategoTerm postparseDesugar(IStrategoTerm ast) throws ExecException {
        return callStrategy(ast, null, "stratego2-postparse-desugar");
    }

    /**
     * Call to the code generation code for Stratego, to transform an AST into Java and write it to
     * file.
     *
     * @param buildInput  the command line options and AST to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The list of files that were written
     * @throws ExecException On failing to load the Stratego language, internal error inside the codegen
     */
    default IStrategoTerm toJava(IStrategoList buildInput, String projectPath) throws ExecException {
        return callStrategy(buildInput, projectPath, "stratego2-strj-sep-comp");
    }

    /**
     * Call to the congruence construction code for Stratego, to transform an AST of an overlay or
     * constructor signature to a strategy definition
     *
     * @param ast         the AST to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The AST of the strategy definition that is the congruence
     * @throws ExecException On failing to load the Stratego language, internal error inside the congruence construction code
     */
    default IStrategoAppl toCongruenceAst(IStrategoTerm ast, String projectPath) throws ExecException {
        return TermUtils.toAppl(callStrategy(ast, projectPath, "stratego2-mk-cong-def"));
    }

    /**
     * Call to the congruence construction code for Stratego, to transform the ASTs of all overlays
     * to a strategy definitions (using all at once to apply in each others bodies)
     *
     * @param asts        the ASTs of the overlays to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The list of ASTs of the strategy definitions for the congruences
     * @throws ExecException On failing to load the Stratego language, internal error inside the congruence construction code
     */
    default Collection<? extends IStrategoAppl> toCongruenceAsts(Collection<IStrategoTerm> asts, String projectPath) throws ExecException {
        final IStrategoList result = TermUtils.toList(callStrategy(makeList(asts), projectPath, "stratego2-mk-cong-defs"));
        final ArrayList<IStrategoAppl> congruences = new ArrayList<>(result.size());
        for(IStrategoTerm t : result) {
            congruences.add(TermUtils.toAppl(t));
        }
        return congruences;
    }

    IStrategoTerm makeList(Collection<IStrategoTerm> terms);

    /**
     * Call to the aux rule signature construction code for Stratego, to transform an AST with
     * dynamic rule definitions into a list of strategy signatures of the aux rules generated from
     * the dynamic rules.
     *
     * @param ast         the AST to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The list of strategy signatures where name starts with aux-
     * @throws ExecException On failing to load the Stratego language, internal error inside the code
     */
    default IStrategoTerm auxSignatures(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-aux-signatures");
    }
    /**
     * Call to the dynamic rule left-hand side overlap check of Stratego, transforming an AST with
     * _all_ dynamic rule definitions into a list of overlap error messages.
     *
     * @param ast         the AST to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The list of error messages from overlapping left-hand sides
     * @throws ExecException On failing to load the Stratego language, internal error inside the code
     */
    default IStrategoTerm overlapCheck(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-dyn-rule-overlap-check");
    }

    /**
     * Call to the concrete- to abstract-syntax translation of Stratego, transforming an AST with
     * concrete syntax AST nodes into normal Stratego abstract syntax.
     *
     * @param ast         the AST with concrete syntax
     * @return The AST with abstract syntax
     * @throws ExecException On failing to load the Stratego language, internal error inside the code
     */
    default IStrategoTerm metaExplode(IStrategoTerm ast) throws ExecException {
        return callStrategy(ast, null, "stratego2-meta-explode");
    }

    IStrategoTerm callStrategy(IStrategoTerm input, @Nullable String projectPath, String strategyName)
        throws ExecException;
}
