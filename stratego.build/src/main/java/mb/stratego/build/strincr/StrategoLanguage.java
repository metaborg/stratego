package mb.stratego.build.strincr;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecException;
import mb.stratego.build.strincr.data.GTEnvironment;

public interface StrategoLanguage {
    /**
     * Parses an inputstream of Stratego code in textual format to an AST
     *
     * @param inputStream the Stratego code
     * @param charset     the charset of the stream
     * @param path        the path of the file from which the stream originated
     * @return an ATerm representation of the AST of the program
     * @throws Exception On failing to load the Stratego language, getting interrupted, IO problems, parsing problems etc.
     */
    IStrategoTerm parse(InputStream inputStream, Charset charset, @Nullable String path) throws Exception;

    /**
     * Parses an inputstream of Stratego code in textual ATerm format or BAF representing an RTree AST
     *
     * @param inputStream the Stratego code
     * @return an ATerm representation of the AST of the program
     * @throws Exception On IO problems, parsing problems, or an AST with unexpected top-level constructor
     */
    IStrategoTerm parseRtree(InputStream inputStream) throws Exception;

    /**
     * Parses an inputstream of Stratego code in textual ATerm format or BAF representing an Str2Lib AST
     *
     * @param inputStream the Stratego code
     * @return an ATerm representation of the AST of the program
     * @throws Exception On IO problems, parsing problems, or an AST with unexpected top-level constructor
     */
    IStrategoTerm parseStr2Lib(InputStream inputStream) throws Exception;

    /**
     * Extract the package name from the Str2Lib ast
     *
     * @param ast the ast to extract from
     * @return The package name in the str2lib or null is unavailable
     */
    default @Nullable String extractPackageName(IStrategoTerm ast) {
        @Nullable String packageName = null;
        if(TermUtils.isAppl(ast, "Str2Lib", 3)) {
            final IStrategoList components = TermUtils.toListAt(ast, 1);
            for(IStrategoTerm component : components) {
                if(TermUtils.isAppl(component, "Package", 1)) {
                    packageName = TermUtils.toJavaStringAt(component, 0);
                }
            }
        }
        return packageName;
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
    IStrategoTerm insertCasts(String moduleName, GTEnvironment environment, String projectPath) throws
        ExecException;

    /**
     * Call to the desugaring code for Stratego, to transform an AST into a desugared AST
     *
     * @param ast the ast to desugar
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return A 2-tuple of the desugared AST and a list of strategy signatures (as 3-tuples) of uncified names (desugaredAst, strategySig*)
     * @throws ExecException On failing to load the Stratego language, internal error inside the desugarer
     */
    IStrategoTerm desugar(IStrategoTerm ast, String projectPath) throws ExecException;

    /**
     * Call to the code generation code for Stratego, to transform an AST into Java and write it to
     * file.
     *
     * @param buildInput  the command line options and AST to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The list of files that were written
     * @throws ExecException On failing to load the Stratego language, internal error inside the codegen
     */
    IStrategoTerm toJava(IStrategoList buildInput, String projectPath) throws ExecException;

    /**
     * Call to the congruence construction code for Stratego, to transform an AST of an overlay or
     * constructor signature to a strategy definition
     *
     * @param ast         the AST to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The AST of the strategy definition that is the congruence
     * @throws ExecException On failing to load the Stratego language, internal error inside the congruence construction code
     */
    IStrategoAppl toCongruenceAst(IStrategoTerm ast, String projectPath) throws ExecException;

    /**
     * Call to the congruence construction code for Stratego, to transform the ASTs of all overlays
     * to a strategy definitions (using all at once to apply in each others bodies)
     *
     * @param asts        the ASTs of the overlays to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The list of ASTs of the strategy definitions for the congruences
     * @throws ExecException On failing to load the Stratego language, internal error inside the congruence construction code
     */
    Collection<? extends IStrategoAppl> toCongruenceAsts(Collection<? extends IStrategoAppl> asts, String projectPath) throws ExecException;

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
    IStrategoTerm auxSignatures(IStrategoTerm ast, String projectPath) throws ExecException;

    /**
     * Call to the dynamic rule left-hand side overlap check of Stratego, transforming an AST with
     * _all_ dynamic rule definitions into a list of overlap error messages.
     *
     * @param ast         the AST to transform
     * @param projectPath The path of the project the module resides in (to be removed at some point)
     * @return The list of error messages from overlapping left-hand sides
     * @throws ExecException On failing to load the Stratego language, internal error inside the code
     */
    IStrategoTerm overlapCheck(IStrategoTerm ast, String projectPath) throws ExecException;
}
