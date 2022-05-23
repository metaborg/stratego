package mb.stratego.build.spoofax2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.AggregateMetaborgException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoCommon;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;

import mb.pie.api.ExecException;
import mb.stratego.build.strincr.StrategoLanguage;
import mb.stratego.build.strincr.data.GTEnvironment;
import mb.stratego.build.termvisitors.DisambiguateAsAnno;
import mb.stratego.build.util.StrIncrContext;

public class Spoofax2StrategoLanguage implements StrategoLanguage {
    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguageService languageService;
    private final ITermFactory termFactory;
    private final StrategoCommon strategoCommon;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;
    private final StrIncrContext strContext;

    @Inject public Spoofax2StrategoLanguage(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ILanguageService languageService,
        ITermFactory termFactory, StrategoCommon strategoCommon,
        IStrategoRuntimeService strategoRuntimeService, ISpoofaxUnitService unitService,
        ISpoofaxSyntaxService syntaxService, StrIncrContext strContext) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.languageService = languageService;
        this.termFactory = termFactory;
        this.strategoCommon = strategoCommon;
        this.strategoRuntimeService = strategoRuntimeService;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
        this.strContext = strContext;
    }

    @Override public IStrategoTerm parse(InputStream inputStream, Charset charset, @Nullable String path)
        throws Exception {
        final @Nullable FileObject inputFile;
        if(path != null) {
            inputFile = resourceService.resolve(path);
        } else {
            inputFile = null;
        }

        @Nullable ILanguageImpl strategoLangImpl = null;
        @Nullable ILanguageImpl strategoDialect = null;
        if(inputFile != null) {
            final @Nullable IdentifiedResource identified =
                languageIdentifierService.identifyToResource(inputFile);
            if(identified != null) {
                strategoLangImpl = identified.language;
                strategoDialect = identified.dialect;
            }
        }
        if(strategoLangImpl == null) {
            final String ext = inputFile.getName().getExtension();
            switch(ext) {
                case "str2":
                    throw new ExecException("Cannot find/load Stratego 2 language. Please add a compile dependency "
                        + "'org.metaborg:stratego.lang:${metaborgVersion}' in your metaborg.yaml file. ");
                case "str":
                    throw new ExecException("Cannot find/load Stratego language. Please add a compile dependency "
                        + "'org.metaborg:org.metaborg.meta.lang.stratego:${metaborgVersion}' in your metaborg.yaml file. ");
                default:
                    throw new ExecException("Cannot find/load language to parse file " + inputFile + ".");
            }
        }

        @Nullable IStrategoTerm ast;
        final String text;
        try {
            text = IOUtils.toString(inputStream, charset);
        } catch(ClosedByInterruptException e) {
            throw new IOException("Interrupted while reading file", e);
        }
        final ISpoofaxInputUnit inputUnit = unitService.inputUnit(inputFile, text, strategoLangImpl, strategoDialect);
        final ISpoofaxParseUnit parseResult = syntaxService.parse(inputUnit, JSGLRVersion.v2);
        ast = parseResult.ast();
        if(!parseResult.success() || ast == null) {
            throw new IOException("Cannot parse stratego file " + inputFile + ": " + parseResult.messages());
        }

        // Remove ambiguity that occurs in old table from sdf2table when using JSGLR2 parser
        ast = new DisambiguateAsAnno(strContext).visit(ast);
        if(strategoDialect != null) {
            ast = metaExplode(ast, null);
        }

        return ast;
    }

    @Override public IStrategoTerm parseRtree(InputStream inputStream) throws Exception {
        final IStrategoTerm ast = new TermReader(termFactory).parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl) ast).getName().equals("Module") && ast.getSubtermCount() == 2)) {
            if(TermUtils.isAppl(ast) && ((IStrategoAppl) ast).getName().equals("Specification")
                    && ast.getSubtermCount() == 1) {
                throw new IOException(
                    "Custom library detected with Specification/1 term in RTree file. This is "
                        + "currently not supported. ");
            }
            throw new ExecException(
                "Did not find Module/2 in RTree file. Found: \n" + ast.toString(2));
        }
        return ast;
    }

    @Override public IStrategoTerm parseStr2Lib(InputStream inputStream) throws Exception {
        final IStrategoTerm ast = new TermReader(termFactory).parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl) ast).getName().equals("Str2Lib")
            && ast.getSubtermCount() == 3)) {
            throw new ExecException(
                "Did not find Str2Lib/3 in Str2Lib file. Found: \n" + ast.toString(2));
        }
        return ast;
    }

    @Override public IStrategoTerm insertCasts(String moduleName, GTEnvironment environment,
        String projectPath) throws ExecException {
        return callStrategy(environment, projectPath, "stratego2-insert-casts",
            " in module " + moduleName);
    }

    @Override public IStrategoTerm desugar(IStrategoTerm ast, String projectPath)
        throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-compile-top-level-def");
    }

    @Override public IStrategoTerm toJava(IStrategoList buildInput, String projectPath)
        throws ExecException {
        return callStrategy(buildInput, projectPath, "stratego2-strj-sep-comp");
    }

    @Override public IStrategoAppl toCongruenceAst(IStrategoTerm ast, String projectPath)
        throws ExecException {
        return TermUtils.toAppl(callStrategy(ast, projectPath, "stratego2-mk-cong-def"));
    }

    @Override public Collection<? extends IStrategoAppl> toCongruenceAsts(
        Collection<IStrategoTerm> asts, String projectPath) throws ExecException {
        final IStrategoList result = TermUtils.toList(callStrategy(termFactory.makeList(asts), projectPath, "stratego2-mk-cong-defs"));
        final ArrayList<IStrategoAppl> congruences = new ArrayList<>(result.size());
        for(IStrategoTerm t : result) {
            congruences.add(TermUtils.toAppl(t));
        }
        return congruences;
    }

    @Override public IStrategoTerm auxSignatures(IStrategoTerm ast, String projectPath)
        throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-aux-signatures");
    }

    @Override public IStrategoTerm overlapCheck(IStrategoTerm ast, String projectPath)
        throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-dyn-rule-overlap-check");
    }

    @Override public IStrategoTerm metaExplode(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "MetaExplode");
    }

    private IStrategoTerm callStrategy(IStrategoTerm input, String projectPath, String strategyName)
        throws ExecException {
        return callStrategy(input, projectPath, strategyName, null);
    }

    private IStrategoTerm callStrategy(IStrategoTerm input, String projectPath, String strategyName,
        @Nullable String extra) throws ExecException {
        final @Nullable ILanguageImpl strategoLangImpl;
        final @Nullable ILanguage strategoLang = languageService.getLanguage("StrategoLang");
        if(strategoLang != null) {
            strategoLangImpl = strategoLang.activeImpl();
            if(strategoLangImpl == null) {
                throw new ExecException("Cannot load stratego language implementation. ");
            }
        } else {
            throw new ExecException("Cannot load stratego language. ");
        }

        final @Nullable FileObject fileObject = projectPath == null ? null : resourceService.resolve(projectPath);
        @Nullable IStrategoTerm result = null;
        boolean finished = false;
        List<MetaborgException> exceptions = Lists.newArrayList();
        for(ILanguageComponent component : strategoLangImpl.components()) {
            if(!IStrategoCommon.hasStrategoFacets(component)) {
                continue;
            }

            try {
                final HybridInterpreter runtime =
                    strategoRuntimeService.runtime(component, fileObject);
                //noinspection deprecation
                runtime.getContext().setFactory(strContext.getFactory());
                runtime.getCompiledContext().setFactory(strContext.getFactory());
                strContext.resetUsedStringsInFactory();
                result = strategoCommon.invoke(runtime, input, strategyName);
                finished = true;
                break;
            } catch(MetaborgException ex) {
                exceptions.add(ex);
            }

        }
        @Nullable Exception cause = null;
        if(!finished) {
            cause = new AggregateMetaborgException(exceptions);
        }
        if(!finished || result == null) {
            throw new ExecException(
                "Call to " + strategyName + " failed" + (extra != null ? extra : "") + ". ", cause);
        }
        return result;
    }
}
