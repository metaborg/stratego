package mb.stratego.build.spoofax2;

import mb.pie.api.ExecException;
import mb.stratego.build.strincr.ParseStratego;
import mb.stratego.build.termvisitors.DisambiguateAsAnno;
import mb.stratego.build.util.StrIncrContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Spoofax2ParseStratego implements ParseStratego {
    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguageService languageService;
    private final ITermFactory termFactory;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;
    private final StrIncrContext strContext;

    @Inject public Spoofax2ParseStratego(
        IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService,
        ILanguageService languageService,
        ITermFactory termFactory,
        ISpoofaxUnitService unitService,
        ISpoofaxSyntaxService syntaxService,
        StrIncrContext strContext
    ) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.languageService = languageService;
        this.termFactory = termFactory;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
        this.strContext = strContext;
    }

    @Override
    public IStrategoTerm parse(InputStream inputStream, Charset charset, @Nullable String path) throws Exception {
        final @Nullable FileObject inputFile;
        if(path != null) {
            inputFile = resourceService.resolve(path);
        } else {
            inputFile = null;
        }

        final LanguageIdentifier strategoLanguageId = LanguageIdentifier.parse(SpoofaxConstants.LANG_STRATEGO_ID);
        final @Nullable ILanguageImpl strategoLang;
        final @Nullable ILanguageImpl strategoDialect;
        if(inputFile != null) {
            final @Nullable IdentifiedResource identified = languageIdentifierService.identifyToResource(inputFile);
            if(identified != null) {
                strategoLang = identified.language;
                strategoDialect = identified.dialect;
            } else {
                strategoLang = languageService.getImpl(strategoLanguageId);
                strategoDialect = null;
            }
        } else {
            strategoLang = languageService.getImpl(strategoLanguageId);
            strategoDialect = null;
        }

        if(strategoLang == null) {
            throw new ExecException("Cannot find/load Stratego language. Please add a source dependency "
                + "'org.metaborg:org.metaborg.meta.lang.stratego:${metaborgVersion}' in your metaborg.yaml file. ");
        }

        @Nullable IStrategoTerm ast;
        final String text = IOUtils.toString(inputStream, charset);
        final ISpoofaxInputUnit inputUnit = unitService.inputUnit(inputFile, text, strategoLang, strategoDialect);
        final ISpoofaxParseUnit parseResult = syntaxService.parse(inputUnit, JSGLRVersion.v2);
        ast = parseResult.ast();
        if(!parseResult.success() || ast == null) {
            throw new ExecException("Cannot parse stratego file " + inputFile + ": " + parseResult.messages());
        }

        // Remove ambiguity that occurs in old table from sdf2table when using JSGLR2 parser
        ast = new DisambiguateAsAnno(strContext).visit(ast);

        return ast;
    }

    @Override
    public IStrategoTerm parseRtree(InputStream inputStream) throws Exception {
        final IStrategoTerm ast = new TermReader(termFactory).parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Module")
            && ast.getSubtermCount() == 2)) {
            if(!(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Specification")
                && ast.getSubtermCount() == 1)) {
                throw new ExecException("Did not find Module/2 in RTree file. Found: \n" + ast.toString(2));
            } else {
                throw new ExecException("Bug in custom library detection. Please file a bug report and "
                    + "turn off Stratego separate compilation for now as a work-around. ");
            }
        }
        return ast;
    }
}
