package mb.stratego.build.strincr.task;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.inject.Inject;

import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.strj.strj_sep_comp_0_0;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKeyString;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.StrategoLanguage;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.ContainsErrors;
import mb.stratego.build.strincr.function.GetStr2LibInfo;
import mb.stratego.build.strincr.function.output.Str2LibInfo;
import mb.stratego.build.strincr.task.input.BackInput;
import mb.stratego.build.strincr.task.output.BackOutput;
import mb.stratego.build.util.GenerateStratego;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.StrIncrContext;

/**
 * Runs per strategy definition. This task desugars the strategy, and then generates Java code for
 * it. Desugaring here does means that dynamic rules are only lifted out at this point, which
 * complicates things slightly. So strategies that contribute to the same dynamic rule are bundled
 * into one larger Back task instead of being compiled separately. Desugaring output of these
 * dynamic rule Back tasks are analysed as well, to be able to generate the DYNAMIC_CALLS strategy
 * correctly.
 * Another artefact of desugaring late is that we don't care to pass constructors to the desugaring.
 * Therefore no congruence strategies are generated. Instead we do this in a separate Back task for
 * all constructors at once. If this turns out to be costly we can split it up to generate per
 * module or even per constructor.
 */
public class Back implements TaskDef<BackInput, BackOutput> {
    public static final String id = "stratego." + Back.class.getSimpleName();

    public final StrategoLanguage strategoLanguage;
    public final GenerateStratego generateStratego;
    public final ITermFactory tf;
    public final ResourcePathConverter resourcePathConverter;
    public final Resolve resolve;
    public final Check check;
    public final Front front;

    @Inject public Back(StrategoLanguage strategoLanguage, StrIncrContext strContext,
        GenerateStratego generateStratego, ResourcePathConverter resourcePathConverter,
        Resolve resolve, Check check, Front front) {
        this.strategoLanguage = strategoLanguage;
        this.tf = strContext.getFactory();
        this.generateStratego = generateStratego;
        this.resourcePathConverter = resourcePathConverter;
        this.resolve = resolve;
        this.check = check;
        this.front = front;
    }

    @Override public BackOutput exec(ExecContext context, BackInput input) throws Exception {
        if(PieUtils.requirePartial(context, check, input.checkInput, ContainsErrors.INSTANCE)) {
            return BackOutput.dependentTasksHaveErrorMessages;
        }

        final LinkedHashSet<StrategySignature> compiledStrategies = new LinkedHashSet<>();

        // N.B. this call is potentially a lot of work:
        final IStrategoTerm ctree = input.buildCTree(context, this, compiledStrategies);

        // Call Stratego compiler
        // Note that we need --library and turn off fusion with --fusion for separate compilation
        // @formatter:off
        final Arguments arguments = new Arguments();
        arguments.add("-i", "passedExplicitly.ctree");
        arguments.add("-o", resourcePathConverter.toString(input.outputDir));
//        arguments.add("--verbose", 3);
        arguments.addLine(input.packageName != null ? "-p " + input.packageName : "");
        arguments.add("--fusion");
        // @formatter:on
        if(input instanceof BackInput.Boilerplate) {
            arguments.add("--boilerplate");
            final BackInput.Boilerplate boilerplateInput = (BackInput.Boilerplate) input;
            if(boilerplateInput.library) {
                arguments.add("--library");
            }
            final Str2LibInfo str2LibInfo = PieUtils
                .requirePartial(context, resolve, boilerplateInput.checkInput.resolveInput(),
                    GetStr2LibInfo.INSTANCE);
            final IStrategoTerm str2Lib = GenerateStratego
                .packStr2Library(tf, boilerplateInput.libraryName, str2LibInfo.sorts,
                    str2LibInfo.constructors, str2LibInfo.strategyFrontData, input.packageName);

            // Output str2lib file
            final HierarchicalResource str2LibResource = context.getResourceService()
                .getHierarchicalResource(boilerplateInput.str2LibFile());
            context.provide(str2LibResource);
            try(final OutputStream os = str2LibResource.openWrite()) {
                Writer out = new BufferedWriter(new OutputStreamWriter(os));
                str2Lib.writeAsString(out, Integer.MAX_VALUE);
                out.flush();
            }
        } else {
            arguments.add("--single-strategy");
            arguments.add("--library");
        }

        for(ResourcePath includeDir : input.checkInput.importResolutionInfo.includeDirs) {
            arguments.add("-I", resourcePathConverter.toString(includeDir));
        }

        if(input.cacheDir != null) {
            arguments.add("--cache-dir", resourcePathConverter.toString(input.cacheDir));
        }

        for(String constant : input.constants) {
            // Needed in boilerplate for generating a strategy (e.g. $C$O$N$S$T$A$N$T_0_0), needed
            //     in single-strategy to turn e.g. prim("CONSTANT") into Build(theconstantvalue),
            //     in the example where you give pass -DCONSTANT=theconstantvalue.
            arguments.add("-D", constant);
        }

        for(IModuleImportService.ModuleIdentifier linkedLibrary : input.checkInput.importResolutionInfo.linkedLibraries) {
            if(linkedLibrary instanceof BuiltinLibraryIdentifier) {
                arguments.add("-la", ((BuiltinLibraryIdentifier) linkedLibrary).cmdArgString);
            }
            // N.B. If non-built-in libraries are split off and modelled under linkedLibraries,
            //      we'll need to add `-la [package-name]` for those libraries here.
        }

        arguments.add("--silent");
        arguments.addAll(input.extraArgs);


        final IStrategoTerm result1 = strategoLanguage
            .toJava(buildInput(ctree, arguments, strj_sep_comp_0_0.instance.getName()),
                resourcePathConverter.toString(input.checkInput.projectPath));

        final LinkedHashSet<ResourcePath> resultFiles = new LinkedHashSet<>();
        assert TermUtils.isList(result1);
        for(IStrategoTerm fileNameTerm : result1) {
            if(TermUtils.isString(fileNameTerm)) {
                final HierarchicalResource file = context.getResourceService().getHierarchicalResource(ResourceKeyString.parse(TermUtils.toJavaString(fileNameTerm)));
                context.provide(file);
                resultFiles.add(file.getPath());
            }
        }

        return new BackOutput(resultFiles, compiledStrategies);
    }

    private static IStrategoList buildInput(IStrategoTerm ctree, Arguments arguments, String name) {
        Collection<String> strings = arguments.asStrings(null);
        final IStrategoTerm[] args = new IStrategoTerm[strings.size() + 2];
        args[0] = B.string(name);
        args[1] = ctree;
        int i = 2;
        for(String string : strings) {
            args[i] = B.string(string);
            i++;
        }

        return B.list(args);
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(BackInput input) {
        return input.key();
    }
}
