package mb.stratego.build.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.util.B;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;

public class GenerateStratego {
    private final ITermFactory tf;
    private final IStrategoAppl funTypeATerm;
    private final IStrategoTerm newSVar;
    private final IStrategoTerm newTVar;
    private final IStrategoAppl id;
    private final IStrategoAppl emptyStringLit;

    public final ConstructorSignature dr_dummy =
        new ConstructorSignature(new StrategoString("DR_DUMMY", null), new StrategoInt(0));
    public final ConstructorSignature dr_undefine =
        new ConstructorSignature(new StrategoString("DR_UNDEFINE", null), new StrategoInt(1));
    public final ConstructorSignature anno_cong__ =
        new ConstructorSignature(new StrategoString("Anno_Cong__", null), new StrategoInt(2));

    public final IStrategoTerm dr_dummyTerm;
    public final IStrategoTerm dr_undefineTerm;
    public final IStrategoTerm anno_cong__Term;

    public final IStrategoAppl anno_cong__ast;

    @jakarta.inject.Inject public GenerateStratego(StrIncrContext context) {
        this.tf = context.getFactory();
        final IStrategoAppl aTerm = tf.makeAppl("Sort", B.string("ATerm"), B.list());
        IStrategoAppl constTypeATerm = tf.makeAppl("ConstType", aTerm);
        funTypeATerm = tf.makeAppl("FunType", constTypeATerm, constTypeATerm);
        newSVar = tf.makeAppl("VarDec", B.string("a"), funTypeATerm);
        newTVar = tf.makeAppl("VarDec", B.string("a"), constTypeATerm);
        id = tf.makeAppl("Id");
        emptyStringLit = tf.makeAppl("Anno", tf.makeAppl("Str", tf.makeString("")),
            tf.makeAppl("Op", tf.makeString("Nil"), tf.makeList()));

        dr_dummyTerm = dr_dummy.toTerm(tf);
        dr_undefineTerm = dr_undefine.toTerm(tf);
        anno_cong__Term = anno_cong__.toTerm(tf);
        anno_cong__ast = annoCongAst();
    }

    public static IStrategoTerm packStr2Library(IStrategoTermBuilder tf, String libraryName,
        Collection<SortSignature> sorts, Collection<ConstructorData> constructors,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        Map<StrategySignature, StrategyType> strategyFrontData, ArrayList<String> packageNames) {
        final IStrategoList.Builder packages = tf.arrayListBuilder(packageNames.size());
        for(String packageName : packageNames) {
            packages.add(tf.makeAppl("Package", tf.makeString(packageName)));
        }
        return tf.makeAppl("Str2Lib", tf.makeString(libraryName),
            tf.makeList(packages),
            tf.makeList(packStr2Spec(tf, sorts, constructors, injections, strategyFrontData)));
    }

    public static IStrategoTerm packStr2Spec(IStrategoTermBuilder tf, Collection<SortSignature> sorts,
        Collection<ConstructorData> constructors, LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        Map<StrategySignature, StrategyType> strategyFrontData) {
        return tf.makeAppl("Specification", tf.makeList(tf.makeAppl("Signature",
            tf.makeList(tf.makeAppl("Sorts", packStr2Sorts(tf, sorts)),
                tf.makeAppl("Constructors", packStr2ConstructorsInjections(tf, constructors, injections)))),
            tf.makeAppl("Strategies", packStr2Strategies(tf, strategyFrontData))));
    }

    private static IStrategoList packStr2ConstructorsInjections(IStrategoTermBuilder tf,
        Collection<ConstructorData> constructors, LinkedHashMap<IStrategoTerm,ArrayList<IStrategoTerm>> injections) {
        final IStrategoList.Builder cons = tf.arrayListBuilder(constructors.size());
        for(ConstructorData d : constructors) {
            cons.add(d.toExtTerm(tf));
        }
        for(Map.Entry<IStrategoTerm,ArrayList<IStrategoTerm>> injection : injections.entrySet()) {
            final IStrategoTerm from = ConstructorType.typeToConstType(tf, injection.getKey());
            for(IStrategoTerm to : injection.getValue()) {
                cons.add(tf.makeAppl("ExtOpDeclInj",
                    tf.makeAppl("FunType", tf.makeList(from), ConstructorType.typeToConstType(tf, to))));
            }
        }
        return tf.makeList(cons);
    }

    private static IStrategoTerm packStr2Sorts(IStrategoTermBuilder tf,
        Collection<SortSignature> sorts) {
        final IStrategoList.Builder builder = tf.arrayListBuilder(sorts.size());
        for(SortSignature sort : sorts) {
            builder.add(sort.toExtDefTerm(tf));
        }
        return tf.makeList(builder);
    }

    public static IStrategoTerm packStr2Strategies(IStrategoTermBuilder tf,
        Map<StrategySignature, StrategyType> strategyFrontData) {
        final IStrategoList.Builder builder = tf.arrayListBuilder(strategyFrontData.size());
        for(Map.Entry<StrategySignature, StrategyType> e : strategyFrontData.entrySet()) {
            builder.add(tf.makeAppl("ExtTypedDef", tf.makeString(e.getKey().name), e.getValue()));
        }
        return tf.makeList(builder);
    }

    public IStrategoTerm packBoilerplate(Collection<? extends IStrategoTerm> constructors,
        Collection<? extends IStrategoAppl> strategyContributions) {
        return tf.makeAppl("Specification", tf.makeList(tf.makeAppl("Signature",
            tf.makeList(tf.makeAppl("Constructors", tf.makeList(constructors)))),
            tf.makeAppl("Strategies", tf.makeList(strategyContributions))));
    }

    public IStrategoTerm packStrategy(Collection<IStrategoTerm> overlayContributions,
        Collection<? extends IStrategoAppl> strategyContributions) {
        final IStrategoAppl term;
        if(overlayContributions.isEmpty()) {
            term = tf.makeAppl("Specification", tf.makeList(
                tf.makeAppl("Signature", tf.makeList(tf.makeAppl("Constructors", tf.makeList()))),
                tf.makeAppl("Strategies", tf.makeList(strategyContributions))));
        } else {
            term = tf.makeAppl("Specification", tf.makeList(
                tf.makeAppl("Signature", tf.makeList(tf.makeAppl("Constructors", tf.makeList()))),
                tf.makeAppl("Overlays", tf.makeList(overlayContributions)),
                tf.makeAppl("Strategies", tf.makeList(strategyContributions))));
        }
        return term;
    }

    public IStrategoTerm packStrategies(Collection<? extends IStrategoAppl> strategies) {
        return tf.makeAppl("Specification", tf.makeList(
            tf.makeAppl("Signature", tf.makeList(tf.makeAppl("Constructors", tf.makeList()))),
            tf.makeAppl("Strategies", tf.makeList(strategies))));
    }

    public List<IStrategoAppl> declStubs(Collection<StrategySignature> strategySignatures) {
        final List<IStrategoAppl> decls = new ArrayList<>(strategySignatures.size());
        for(StrategySignature sig : strategySignatures) {
            decls.add(sdefStub(sig.cifiedName(), sig.noStrategyArgs, sig.noTermArgs));
        }
        return decls;
    }

    private IStrategoAppl sdefStub(String strategyName, int svars, int tvars) {
        final IStrategoTerm name = tf.makeString(strategyName);

        final IStrategoTerm[] newSVarArray = new IStrategoTerm[svars];
        Arrays.fill(newSVarArray, newSVar);
        final IStrategoTerm newSVars = B.list(newSVarArray);

        final IStrategoTerm[] newTVarArray = new IStrategoTerm[tvars];
        Arrays.fill(newTVarArray, newTVar);
        final IStrategoTerm newTVars = B.list(newTVarArray);

        return tf.makeAppl("SDefT", name, newSVars, newTVars, id);
    }

    public @Nullable IStrategoAppl dynamicCallsDefinition(
        Collection<StrategySignature> dynamicRulesNewGenerated,
        Collection<StrategySignature> dynamicRulesUndefineGenerated) {
        @Nullable IStrategoAppl body = null;

        /* concrete syntax:
         *   new-[dr-rule-name](|"", "")
         * abstract syntax, desugared and name mangled:
         *   CallT(
         *     "new_[dr-rule-name]_0_2",
         *     [],
         *     [Anno(Str("\"\""), Op("Nil", [])), Anno(Str("\"\""), Op("Nil", []))])
         * strung together with `[call] <+ [other-calls]` or `GuardedLChoice([call], Id(), [other-calls])`
         */
        for(StrategySignature dynamicRule : dynamicRulesNewGenerated) {
            final String drRuleNameNew = Interpreter.cify("new-" + dynamicRule.name) + "_0_2";
            final IStrategoAppl call =
                tf.makeAppl("CallT", tf.makeAppl("SVar", tf.makeString(drRuleNameNew)),
                    tf.makeList(), tf.makeList(emptyStringLit, emptyStringLit));
            if(body == null) {
                body = call;
            } else {
                body = tf.makeAppl("GuardedLChoice", call, id, body);
            }
        }

        /* concrete syntax:
         *   undefine-[dr-rule-name](|"")
         * abstract syntax, desugared and name mangled:
         *   CallT("undefine_[dr-rule-name]_0_1", [], [Anno(Str("\"\""), Op("Nil", []))])
         * strung together with `[call] <+ [other-calls]` or `GuardedLChoice([call], Id(), [other-calls])`
         */
        for(StrategySignature dynamicRule : dynamicRulesUndefineGenerated) {
            final String drRuleNameNew = Interpreter.cify("undefine-" + dynamicRule.name) + "_0_1";
            final IStrategoAppl call =
                tf.makeAppl("CallT", tf.makeAppl("SVar", tf.makeString(drRuleNameNew)),
                    tf.makeList(), tf.makeList(emptyStringLit));
            if(body == null) {
                body = call;
            } else {
                body = tf.makeAppl("GuardedLChoice", call, id, body);
            }
        }
        if(body == null) {
            return null;
        }

        final String dynamicCalls = Interpreter.cify("DYNAMIC_CALLS") + "_0_0";
        return tf
            .makeAppl("SDefT", tf.makeString(dynamicCalls), tf.makeList(), tf.makeList(), body);
    }

    public IStrategoAppl congruenceAst(ConstructorSignature constructorSignature) {
        int noArgs = constructorSignature.noArgs;
        /*
        // congruence for constructor Foo : Bar -> Baz
        Foo_1_0(cong_arg_1 : ATerm -> ATerm|) =
        { cong_arg_1x, cong_arg_1y, w, a:
          ?w
        ; ?Foo(cong_arg_1x){^a}
        ; !cong_arg_1x; cong_arg_1(|); ?cong__arg_1y
        ; !w
        ; !Foo(cong__arg_1y){^a}
        }
         */
        final IStrategoString[] locals = new IStrategoString[2 * noArgs + 2];
        final IStrategoAppl[] varDecs = new IStrategoAppl[noArgs];
        final IStrategoAppl[] sVars = new IStrategoAppl[noArgs];
        final IStrategoAppl[] matchVars = new IStrategoAppl[noArgs];
        final IStrategoAppl[] buildVars = new IStrategoAppl[noArgs];
        for(int i = 0; i < noArgs; i++) {
            final IStrategoString matchVar = tf.makeString("cong_arg_" + i + "x");
            locals[2 * i] = matchVar;
            final IStrategoString buildVar = tf.makeString("cong_arg_" + i + "y");
            locals[2 * i + 1] = buildVar;
            matchVars[i] = tf.makeAppl("Var", matchVar);
            buildVars[i] = tf.makeAppl("Var", buildVar);
            final IStrategoString sVar = tf.makeString("con_arg_" + i);
            sVars[i] = tf.makeAppl("SVar", sVar);
            varDecs[i] = tf.makeAppl("VarDec", sVar, funTypeATerm);
        }
        locals[2 * noArgs] = tf.makeString("w");
        locals[2 * noArgs + 1] = tf.makeString("a");
        // @formatter:off
        IStrategoAppl tail = tf.makeAppl("Seq",
            tf.makeAppl("Build", tf.makeAppl("Var", tf.makeString("w"))),
            tf.makeAppl("Build",
                tf.makeAppl("Anno",
                    tf.makeAppl("Op",
                        tf.makeString(constructorSignature.name),
                        tf.makeList(buildVars)),
                    tf.makeAppl("Var", tf.makeString("a")))));
        // @formatter:on
        for(int i = noArgs - 1; i >= 0; i--) {
            // @formatter:off
            tail = tf.makeAppl("Seq",
                tf.makeAppl("Build", matchVars[i]),
                tf.makeAppl("Seq",
                    tf.makeAppl("CallT", sVars[i], tf.makeList(), tf.makeList()),
                    tf.makeAppl("Seq",
                        tf.makeAppl("Match", buildVars[i]),
                        tail)));
            // @formatter:on
        }
        // @formatter:off
        return tf.makeAppl("SDefT",
            tf.makeString(constructorSignature.toCongruenceSig().cifiedName()),
            tf.makeList(varDecs),
            tf.makeList(),
            tf.makeAppl("Scope",
                tf.makeList(locals),
                tf.makeAppl("Seq",
                    tf.makeAppl("Match",
                        tf.makeAppl("Var", tf.makeString("w"))),
                    tf.makeAppl("Seq",
                        tf.makeAppl("Match",
                            tf.makeAppl("Anno",
                                tf.makeAppl("Op",
                                    tf.makeString(constructorSignature.name),
                                    tf.makeList(matchVars)),
                                tf.makeAppl("Var", tf.makeString("a")))),
                        tail))));
        // @formatter:on
    }

    private IStrategoAppl annoCongAst() {
        /*
         // Added by default. Although now also in the standard library, so doesn't need to be added once Stratego is bootstrapped.
         Anno__Cong_____2_0(f1 : ATerm -> ATerm, f2 : ATerm -> ATerm|) =
         { x1, x2, y1, y2:
           ?x1{^x2}
         ; !x1; f1(|); ?y1
         ; !x2; f2(|); ?y2
         ; !x1
         ; !y1{^y2}
         }
         */
        // @formatter:off
        return tf.makeAppl("SDefT",
            tf.makeString("Anno__Cong_____2_0"),
            tf.makeList(
                tf.makeAppl("VarDec", tf.makeString("f1"), funTypeATerm),
                tf.makeAppl("VarDec", tf.makeString("f2"), funTypeATerm)),
            tf.makeList(),
            tf.makeAppl("Scope",
                tf.makeList(
                    tf.makeString("x1"),
                    tf.makeString("x2"),
                    tf.makeString("y1"),
                    tf.makeString("y2")
                ),
                tf.makeAppl("Seq",
                    tf.makeAppl("Match",
                        tf.makeAppl("Anno",
                            tf.makeAppl("Var", tf.makeString("x1")),
                            tf.makeAppl("Var", tf.makeString("x2")))),
                    tf.makeAppl("Seq",
                        tf.makeAppl("Build",
                            tf.makeAppl("Var", tf.makeString("x1"))),
                        tf.makeAppl("Seq",
                            tf.makeAppl("CallT",
                                tf.makeAppl("SVar", tf.makeString("f1")),
                                tf.makeList(),
                                tf.makeList()),
                            tf.makeAppl("Seq",
                                tf.makeAppl("Match",
                                    tf.makeAppl("Var", tf.makeString("y1"))),
                                tf.makeAppl("Seq",
                                    tf.makeAppl("Build",
                                        tf.makeAppl("Var", tf.makeString("x2"))),
                                    tf.makeAppl("Seq",
                                        tf.makeAppl("CallT",
                                            tf.makeAppl("SVar", tf.makeString("f2")),
                                            tf.makeList(),
                                            tf.makeList()),
                                        tf.makeAppl("Seq",
                                            tf.makeAppl("Match",
                                                tf.makeAppl("Var", tf.makeString("y2"))),
                                            tf.makeAppl("Seq",
                                                tf.makeAppl("Build",
                                                    tf.makeAppl("Var", tf.makeString("x1"))),
                                                tf.makeAppl("Build",
                                                    tf.makeAppl("Anno",
                                                        tf.makeAppl("Var", tf.makeString("y1")),
                                                        tf.makeAppl("Var", tf.makeString("y2")))) ))))))))));
        // @formatter:on
    }

    public IStrategoAppl emptyModuleAst(IModuleImportService.ModuleIdentifier moduleIdentifier) {
        return tf.makeAppl("Module", tf.makeString(moduleIdentifier.moduleString()),
            tf.makeList(
                tf.makeAppl("Signature", tf.makeList(tf.makeAppl("Constructors", tf.makeList()))),
                tf.makeAppl("Strategies", tf.makeList())));
    }
}
