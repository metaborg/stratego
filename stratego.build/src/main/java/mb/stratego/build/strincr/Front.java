package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.termvisitors.AllTdDesugarType;
import mb.stratego.build.termvisitors.CollectDynRuleSigs;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.TermWithLastModified;

import static mb.stratego.build.termvisitors.AllTdDesugarType.tryDesugarType;

/* TODO: Check the following aren't used directly in Java code
overlays

  FunTType(from, to) = FunTType([], [], from, to)
  FunTType(ft)       = FunTType([], [], ft, ft)
  FunTType()         = FunTType([], [], DynT(), DynT())
  ListT(elem)        = Sort("List", [elem])
  ListT()            = Sort("List", [_DynT()])
  TupleT(elems)      = Sort("Tuple", elems)
  DynT()             = DynT(_Dyn())

  FunTType(sargs, targs, from, to) = FunTType(sargs, targs, FunNoArgsType(from, to))
 */


/**
 * Task that takes a {@link ModuleIdentifier} and processes the corresponding AST. The AST is split
 * into {@link ModuleData}, which contains the original AST along with several lists of
 * information required in other tasks.
 */
public class Front implements TaskDef<Front.Input, ModuleData> {
    public static final String id = Front.class.getCanonicalName();

    public static class Input implements Serializable {
        public final ModuleIdentifier moduleIdentifier;
        public final IModuleImportService moduleImportService;

        public Input(ModuleIdentifier moduleIdentifier, IModuleImportService moduleImportService) {
            this.moduleIdentifier = moduleIdentifier;
            this.moduleImportService = moduleImportService;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!moduleIdentifier.equals(input.moduleIdentifier))
                return false;
            return moduleImportService.equals(input.moduleImportService);
        }

        @Override public int hashCode() {
            int result = moduleIdentifier.hashCode();
            result = 31 * result + moduleImportService.hashCode();
            return result;
        }
    }

    private final StrIncrContext strContext;
    private final ITermFactory tf;
    private final B b;

    @Inject public Front(StrIncrContext strContext) {
        this.strContext = strContext;
        this.tf = strContext.getFactory();
        this.b = new B(this.tf);
    }

    @Override public ModuleData exec(ExecContext context, Input input) throws Exception {
        final TermWithLastModified ast =
            input.moduleImportService.getModuleAst(input.moduleIdentifier);
        final List<IStrategoTerm> imports = new ArrayList<>();
        final Map<ConstructorSignature, List<ConstructorData>> constrData = new HashMap<>();
        final Map<ConstructorSignature, List<ConstructorData>> overlayData = new HashMap<>();
        final Map<StrategySignature, List<StrategyData>> strategyData = new HashMap<>();
        final Map<IStrategoTerm, List<IStrategoTerm>> injections = new HashMap<>();

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new WrongASTException(input.moduleIdentifier, def);
            }
            switch(TermUtils.toAppl(def).getName()) {
                case "Imports":
                    imports.addAll(def.getSubterm(0).getSubterms());
                    break;
                case "Signature":
                    addSigData(input.moduleIdentifier, constrData, injections, def);
                    break;
                case "Overlays":
                    addOverlayData(input.moduleIdentifier, overlayData, constrData, def);
                    break;
                case "Rules":
                    // fall-through
                case "Strategies":
                    addStrategyData(input.moduleIdentifier, strategyData, def);
                    break;
            }
        }

        return new ModuleData(input.moduleIdentifier, ast, imports, constrData, strategyData,
            overlayData);
    }

    private void addStrategyData(ModuleIdentifier moduleIdentifier,
        Map<StrategySignature, List<StrategyData>> strategyData, Iterable<IStrategoTerm> strategyDefs) {
        /*
        def-type-pair: DefHasType(name, t@FunNoArgsType(_, _)) -> ((name, 0, 0), <try(desugar-SType)> t)
        def-type-pair: DefHasType(name, t@FunType(sarg*, _)) -> ((name, <length> sarg*, 0), <try(desugar-SType)> t)
        def-type-pair: DefHasType(name, t@FunTType(sarg*, targ*, _)) ->
          ((name, <length> sarg*, <length> targ*), <try(desugar-SType)> t)
         */
        /*
        m-def-signature =
          try(Desugar)
          ; (  \ SDefT(f, xs, ys, s)      -> (f, <length>xs, <length>ys) \
            <+ \ ExtSDef(f, xs, ys)       -> (f, <length>xs, <length>ys) \
            <+ \ ExtSDefInl(f, xs, ys, s) -> (f, <length>xs, <length>ys) \
            <+ \ RDefT(f, xs, ys, s)      -> (f, <length>xs, <length>ys) \
            <+ \ DefHasType(f, FunTType(xs, ys, _, _)) -> (f, <length>xs, <length>ys) \
            <+ \ AnnoDef(a*, def)         -> <m-def-signature> def\)
         */
        for(IStrategoTerm strategyDef : strategyDefs) {
            // TODO: def-type-pair, m-def-signature

            // collect-om(dyn-rule-sig)
            for(StrategySignature dynRuleSig : CollectDynRuleSigs.collect(strategyDef)) {
                for(Map.Entry<StrategySignature, IStrategoTerm> e : dynRuleSig
                    .dynamicRuleSignatures(tf).entrySet()) {
                    final StrategyData data =
                        new StrategyData(e.getKey(), null, e.getValue(), true);
                    Relation.getOrInitialize(strategyData, e.getKey(), ArrayList::new).add(data);
                }
            }
        }
    }

    private void addOverlayData(ModuleIdentifier moduleIdentifier,
        Map<ConstructorSignature, List<ConstructorData>> overlayData,
        Map<ConstructorSignature, List<ConstructorData>> constrData,
        Iterable<IStrategoTerm> overlays) throws WrongASTException {
        /*
        extract-constr:
          OverlayNoArgs(c, _) -> ((c,0), ConstrType([], DynT()))

        extract-constr:
          Overlay(c, t*, _) -> ((c, <length> t*), ConstrType(<map(!DynT())> t*, DynT()))
         */
        final IStrategoTerm dynT = b.applShared("DynT", b.applShared("Dyn"));
        for(IStrategoTerm overlay : overlays) {
            final int arity;
            final IStrategoTerm type;
            final String name;
            if(TermUtils.isStringAt(overlay, 0)) {
                name = TermUtils.toJavaStringAt(overlay, 0);
                if(TermUtils.isAppl(overlay, "OverlayNoArgs", 2)) {
                    arity = 0;
                    type = b.applShared("ConstrType", B.list(), dynT);
                } else if(TermUtils.isAppl(overlay, "Overlay", 3) && TermUtils
                    .isListAt(overlay, 1)) {
                    arity = TermUtils.toListAt(overlay, 1).size();
                    type =
                        b.applShared("ConstrType", B.list(Collections.nCopies(arity, dynT)), dynT);
                } else {
                    throw new WrongASTException(moduleIdentifier, overlay);
                }
            } else {
                throw new WrongASTException(moduleIdentifier, overlay);
            }
            final ConstructorSignature signature = new ConstructorSignature(name, arity);
            final ConstructorData data = new ConstructorData(signature, overlay, type, true);
            Relation.getOrInitialize(constrData, signature, ArrayList::new).add(data);
            Relation.getOrInitialize(overlayData, signature, ArrayList::new).add(data);
        }
    }

    private void addSigData(ModuleIdentifier moduleIdentifier,
        Map<ConstructorSignature, List<ConstructorData>> constrData,
        Map<IStrategoTerm, List<IStrategoTerm>> injections, Iterable<IStrategoTerm> sigs)
        throws WrongASTException {
        for(IStrategoTerm sig : sigs) {
            if(TermUtils.isAppl(sig, "Constructors", 1)) {
                final IStrategoTerm constrs = sig.getSubterm(1);
                if(TermUtils.isList(constrs)) {
                    for(IStrategoTerm constrDef : constrs) {
                        final ConstructorSignature constrSig =
                            ConstructorSignature.fromTerm(constrDef);
                        if(constrSig == null) {
                            addInjectionData(moduleIdentifier, constrDef, injections, constrData);
                            continue;
                        }
                        final IStrategoTerm constrTerm =
                            AllTdDesugarType.visit(strContext, constrDef);
                        final IStrategoTerm constrType = constrType(moduleIdentifier, constrDef);
                        Relation.getOrInitialize(constrData, constrSig, ArrayList::new)
                            .add(new ConstructorData(constrSig, constrTerm, constrType));
                    }
                }
            }
        }
    }

    private IStrategoTerm constrType(ModuleIdentifier moduleIdentifier, IStrategoTerm constrDef)
        throws WrongASTException {
        /*
        extract-constr =
          (  ?OpDecl(c, ConstType(t1))
          <+ OpDeclQ(c, ConstType(t1))
          <+ ExtOpDecl(c, ConstType(t1))
          <+ ExtOpDeclQ(c, ConstType(t1)) )
        ; with(t2 := <desugar-Type> t1)
        ; !((c,0), ConstrType([], t2))

        extract-constr:
          (  ?OpDecl(c, FunType(t1*, ConstType(t1)))
          <+ OpDeclQ(c, FunType(t1*, ConstType(t1)))
          <+ ExtOpDecl(c, FunType(t1*, ConstType(t1)))
          <+ ExtOpDeclQ(c, FunType(t1*, ConstType(t1))) )
        ; with(
            t2 := <desugar-Type> t1
          ; t2* := <map(?ConstType(<desugar-Type>) <+ ?DynT())> t1*)
        ; !((c, <length> t1*), ConstrType(t2*, t2))
         */
        if(!TermUtils.isAppl(constrDef)) {
            throw new WrongASTException(moduleIdentifier, constrDef);
        }
        switch(TermUtils.toAppl(constrDef).getName()) {
            case "OpDecl":
                // fall-through
            case "OpDeclQ":
                // fall-through
            case "ExtOpDecl":
                // fall-through
            case "ExtOpDeclQ":
                break;
            default:
                throw new WrongASTException(moduleIdentifier, constrDef);
        }

        final IStrategoTerm opType = constrDef.getSubterm(1);
        if(!TermUtils.isAppl(opType)) {
            throw new WrongASTException(moduleIdentifier, opType);
        }

        final IStrategoTerm type;
        switch(TermUtils.toAppl(opType).getName()) {
            case "ConstType":
                if(opType.getSubtermCount() != 1) {
                    throw new WrongASTException(moduleIdentifier, opType);
                }
                type = b.applShared("ConstrType", B.list(),
                    AllTdDesugarType.tryDesugarType(tf, opType.getSubterm(0)));
                break;
            case "FunType":
                if(opType.getSubtermCount() != 2 || !TermUtils.isListAt(opType, 0) || !TermUtils
                    .isApplAt(opType, 1, "ConstType", 1)) {
                    throw new WrongASTException(moduleIdentifier, opType);
                }
                final IStrategoList froms = TermUtils.toListAt(opType, 0);
                final IStrategoTerm dynT = b.applShared("DynT", b.applShared("Dyn"));
                final IStrategoList.Builder fromTypesB = B.listBuilder(froms.size());
                for(IStrategoTerm tupleType : froms) {
                    if(!TermUtils.isAppl(tupleType, "ConstType", 1)) {
                        fromTypesB.add(dynT);
                    } else {
                        fromTypesB.add(tryDesugarType(tf, tupleType.getSubterm(0)));
                    }
                }
                type = b.applShared("ConstrType", fromTypesB.build(),
                    AllTdDesugarType.tryDesugarType(tf, opType.getSubterm(1).getSubterm(0)));
                break;
            default:
                throw new WrongASTException(moduleIdentifier, constrDef);
        }

        return type;
    }

    private void addInjectionData(ModuleIdentifier moduleIdentifier, IStrategoTerm constrDef,
        Map<IStrategoTerm, List<IStrategoTerm>> injections,
        Map<ConstructorSignature, List<ConstructorData>> constrData) throws WrongASTException {
        /*
        extract-inj:
          OpDeclInj(FunType([ConstType(from)], ConstType(to))) ->
            (<desugar-Type> from, <desugar-Type> to)

        extract-inj:
          ExtOpDeclInj(FunType([ConstType(from)], ConstType(to))) ->
            (<desugar-Type> from, <desugar-Type> to)

        extract-inj:
          OpDeclInj(FunType(t1*@[_, _ | _], ConstType(t1))) ->
            (TupleT(t2*), t2)
          with
            t2 := <desugar-Type> t1
          ; t2* := <map(?ConstType(<desugar-Type>) <+ ?DynT())> t1*

        extract-constr:
          ExtOpDeclInj(FunType(t1*@[_, _ | _], ConstType(t1))) ->
            (("", <length> t1*), ConstrType(t2*, t2))
          with
            t2 := <desugar-Type> t1
          ; t2* := <map(?ConstType(<desugar-Type>) <+ ?DynT())> t1*
         */
        if(!TermUtils.isAppl(constrDef)) {
            throw new WrongASTException(moduleIdentifier, constrDef);
        }
        switch(TermUtils.toAppl(constrDef).getName()) {
            case "OpDeclInj":
                // fall-through
            case "ExtOpDeclInj":
                if(constrDef.getSubtermCount() != 1) {
                    throw new WrongASTException(moduleIdentifier, constrDef);
                }
                final IStrategoTerm funType = constrDef.getSubterm(0);
                if(!TermUtils.isAppl(funType, "FunType", 2) || !TermUtils.isListAt(funType, 0)
                    || !TermUtils.isApplAt(funType, 1, "ConstType", 1)) {
                    throw new WrongASTException(moduleIdentifier, funType);
                }
                final IStrategoTerm to = tryDesugarType(tf, funType.getSubterm(1).getSubterm(0));

                final IStrategoList froms = TermUtils.toListAt(funType, 0);
                final IStrategoTerm from;
                switch(froms.size()) {
                    case 0:
                        throw new WrongASTException(moduleIdentifier, froms);
                    case 1:
                        final IStrategoTerm constType = froms.getSubterm(0);
                        if(!TermUtils.isAppl(constType, "ConstType", 1)) {
                            throw new WrongASTException(moduleIdentifier, constType);
                        }
                        from = tryDesugarType(tf, constType.getSubterm(0));
                        break;
                    default:
                        final IStrategoTerm dynT = b.applShared("DynT", b.applShared("Dyn"));
                        final IStrategoList.Builder tupleTypesB = B.listBuilder(froms.size());
                        for(IStrategoTerm tupleType : froms) {
                            if(!TermUtils.isAppl(tupleType, "ConstType", 1)) {
                                tupleTypesB.add(dynT);
                            } else {
                                tupleTypesB.add(tryDesugarType(tf, tupleType.getSubterm(0)));
                            }
                        }
                        final IStrategoList tupleTypes = tupleTypesB.build();
                        from = b.applShared("Sort", b.stringShared("Tuple"), tupleTypes);

                        final ConstructorSignature constrSig =
                            new ConstructorSignature("", froms.size());
                        final IStrategoTerm constrTerm =
                            AllTdDesugarType.visit(strContext, constrDef);
                        final IStrategoAppl constrType = b.applShared("ConstrType", tupleTypes, to);
                        Relation.getOrInitialize(constrData, constrSig, ArrayList::new)
                            .add(new ConstructorData(constrSig, constrTerm, constrType));
                        break;
                }
                Relation.getOrInitialize(injections, from, ArrayList::new).add(to);
        }
    }

    private static IStrategoList getDefs(ModuleIdentifier moduleIdentifier, IStrategoTerm ast)
        throws WrongASTException {
        if(TermUtils.isAppl(ast, "Module", 2)) {
            final IStrategoTerm defs = ast.getSubterm(1);
            if(TermUtils.isList(defs)) {
                return TermUtils.toList(defs);
            }
        }
        throw new WrongASTException(moduleIdentifier, ast);
    }

    @Override public String getId() {
        return id;
    }
}
