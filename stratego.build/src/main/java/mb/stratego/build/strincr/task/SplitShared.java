package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategyFrontData.Kind;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.termvisitors.CollectDynRuleSigs;
import mb.stratego.build.termvisitors.DesugarType;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.util.InvalidASTException;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;

import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.DynRuleGenerated;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.Extend;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.External;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.Internal;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.Normal;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.TypeDefinition;

public abstract class SplitShared {
    public final IModuleImportService moduleImportService;
    protected final StrIncrContext strContext;
    protected final ITermFactory tf;
    protected final B b;

    public SplitShared(StrIncrContext strContext, IModuleImportService moduleImportService) {
        this.strContext = strContext;
        this.tf = strContext.getFactory();
        this.b = new B(this.tf);
        this.moduleImportService = moduleImportService;
    }

    protected void addStrategyData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> strategyData,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> internalStrategyData,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> externalStrategyData,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> dynamicRuleData,
        LinkedHashSet<StrategySignature> dynamicRules, IStrategoTerm strategyDefs)
         {
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
            if(TermUtils.isAppl(strategyDef, "DefHasType", 3)) {
                final IStrategoTerm funTType = strategyDef.getSubterm(1);
                final @Nullable StrategyType strategyType = StrategyType.fromTerm(tf, funTType);
                if(strategyType == null) {
                    throw new InvalidASTException(moduleIdentifier, funTType);
                }
                final StrategySignature strategySignature =
                    new StrategySignature(TermUtils.toJavaStringAt(strategyDef, 0),
                        strategyType.getStrategyArguments().size(),
                        strategyType.getTermArguments().size());
                Relation.getOrInitialize(strategyData, strategySignature, LinkedHashSet::new)
                    .add(new StrategyFrontData(strategySignature, strategyType, TypeDefinition));
            } else {
                Kind kind = Normal;
                HashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> dataMap = strategyData;
                if(TermUtils.isAppl(strategyDef, "AnnoDef", 2)) {
                    for(IStrategoTerm anno : strategyDef.getSubterm(0)) {
                        if(TermUtils.isAppl(anno, "Internal", 0)) {
                            kind = Internal;
                            dataMap = internalStrategyData;
                        } else if(TermUtils.isAppl(anno, "Extend", 0)) {
                            kind = Extend;
                        } else if(TermUtils.isAppl(anno, "Override", 0)) {
                            kind = Kind.Override;
                        }
                    }
                    strategyDef = strategyDef.getSubterm(1);
                }
                if(!TermUtils.isAppl(strategyDef)) {
                    throw new InvalidASTException(moduleIdentifier, strategyDef);
                }
                if(!TermUtils.isStringAt(strategyDef, 0)) {
                    throw new InvalidASTException(moduleIdentifier, strategyDef);
                }
                switch(TermUtils.toAppl(strategyDef).getName()) {
                    case "ExtSDef":
                    case "ExtSDefInl":
                        kind = External;
                        dataMap = externalStrategyData;
                        break;
                }
                final @Nullable StrategySignature strategySignature =
                    StrategySignature.fromDefinition(strategyDef);
                if(strategySignature == null) {
                    throw new InvalidASTException(moduleIdentifier, strategyDef);
                }
                Relation.getOrInitialize(dataMap, strategySignature, LinkedHashSet::new)
                    .add(new StrategyFrontData(strategySignature, null, kind));
            }

            // collect-om(dyn-rule-sig)
            for(StrategySignature dynRuleSig : CollectDynRuleSigs.collect(strategyDef)) {
                dynamicRules.add(dynRuleSig);
                for(StrategySignature signature : dynRuleSig.dynamicRuleSignatures(tf).keySet()) {
                    Relation.getOrInitialize(dynamicRuleData, signature, LinkedHashSet::new)
                        .add(new StrategyFrontData(signature, null, DynRuleGenerated));
                }
            }
        }
    }

    protected void addOverlayData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        HashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData,
        IStrategoTerm overlays, long lastModified) {
        /*
        extract-constr:
          OverlayNoArgs(c, _) -> ((c,0), ConstrType([], DynT()))

        extract-constr:
          Overlay(c, t*, _) -> ((c, <length> t*), ConstrType(<map(!DynT())> t*, DynT()))
         */
        final IStrategoTerm dynT = b.applShared("DynT", b.applShared("Dyn"));
        for(IStrategoTerm overlay : overlays) {
            final int arity;
            final ConstructorType type;
            final String name;
            if(TermUtils.isStringAt(overlay, 0)) {
                name = TermUtils.toJavaStringAt(overlay, 0);
                if(TermUtils.isAppl(overlay, "OverlayNoArgs", 2)) {
                    arity = 0;
                    type = new ConstructorType(tf, new ArrayList<>(0), dynT);
                } else if(TermUtils.isAppl(overlay, "Overlay", 3) && TermUtils
                    .isListAt(overlay, 1)) {
                    arity = TermUtils.toListAt(overlay, 1).size();
                    type =
                        new ConstructorType(tf, new ArrayList<>(Collections.nCopies(arity, dynT)),
                            dynT);
                } else {
                    throw new InvalidASTException(moduleIdentifier, overlay);
                }
            } else {
                throw new InvalidASTException(moduleIdentifier, overlay);
            }
            final LinkedHashSet<ConstructorSignature> usedConstructors = new LinkedHashSet<>();
            new UsedConstrs(usedConstructors, lastModified).visit(overlay);
            final ConstructorSignature signature =
                new ConstructorSignature(name, arity, lastModified);
            final OverlayData data =
                new OverlayData(signature, (IStrategoAppl) overlay, type, usedConstructors);
            Relation.getOrInitialize(constrData, signature, ArrayList::new).add(data);
            Relation.getOrInitialize(overlayData, signature, ArrayList::new).add(data);
        }
    }

    protected void addSigData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections, IStrategoTerm sigs,
        long lastModified) {
        for(IStrategoTerm sig : sigs) {
            if(TermUtils.isAppl(sig, "Constructors", 1)) {
                final IStrategoTerm constrs = sig.getSubterm(0);
                if(!TermUtils.isList(constrs)) {
                    throw new InvalidASTException(moduleIdentifier, constrs);
                }
                for(IStrategoTerm constrDef : constrs) {
                    final @Nullable ConstructorSignature constrSig =
                        ConstructorSignature.fromTerm(constrDef, lastModified);
                    if(constrSig == null) {
                        addInjectionData(moduleIdentifier, constrDef, injections,
                            externalInjections, constrData, lastModified);
                        continue;
                    }
                    final IStrategoTerm constrTerm = DesugarType.alltd(strContext, constrDef);
                    final ConstructorType constrType = constrType(moduleIdentifier, constrDef);
                    final HashMap<ConstructorSignature, ArrayList<ConstructorData>> dataMap;
                    if(ConstructorSignature.isExternal(constrDef)) {
                        dataMap = externalConstrData;
                    } else {
                        dataMap = constrData;
                    }
                    Relation.getOrInitialize(dataMap, constrSig, ArrayList::new).add(
                        new ConstructorData(constrSig, (IStrategoAppl) constrTerm, constrType));
                }
            }
        }
    }

    private ConstructorType constrType(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IStrategoTerm constrDef) {
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
            throw new InvalidASTException(moduleIdentifier, constrDef);
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
                throw new InvalidASTException(moduleIdentifier, constrDef);
        }

        final IStrategoTerm opType = constrDef.getSubterm(1);
        if(!TermUtils.isAppl(opType)) {
            throw new InvalidASTException(moduleIdentifier, opType);
        }

        final @Nullable ConstructorType type = ConstructorType.fromOpType(tf, opType);

        if(type == null) {
            throw new InvalidASTException(moduleIdentifier, opType);
        }

        return type;
    }

    private void addInjectionData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IStrategoTerm constrDef, HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData, long lastModified) {
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
            throw new InvalidASTException(moduleIdentifier, constrDef);
        }
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> dataMap = externalInjections;
        switch(TermUtils.toAppl(constrDef).getName()) {
            case "OpDeclInj":
                dataMap = injections;
                // fall-through
            case "ExtOpDeclInj":
                if(constrDef.getSubtermCount() != 1) {
                    throw new InvalidASTException(moduleIdentifier, constrDef);
                }
                final IStrategoTerm opType = constrDef.getSubterm(0);
                final @Nullable ConstructorType constrType = ConstructorType.fromOpType(tf, opType);
                if(constrType == null) {
                    throw new InvalidASTException(moduleIdentifier, opType);
                }

                final IStrategoTerm from;
                final ArrayList<IStrategoTerm> froms = constrType.getFrom();
                switch(froms.size()) {
                    case 0:
                        // ignore this weird edge-case generated from strategoGT/syntax/sugar/string-quotations.sdf3
                        return;
                    case 1:
                        from = froms.get(0);
                        break;
                    default:
                        final IStrategoList tupleTypes = tf.makeList(froms);
                        from = b.applShared("Sort", b.stringShared("Tuple"), tupleTypes);

                        final ConstructorSignature constrSig =
                            new ConstructorSignature("", froms.size(), lastModified);
                        final IStrategoTerm constrTerm =
                            tf.replaceTerm(constrType.toOpType(tf), constrDef);
                        Relation.getOrInitialize(constrData, constrSig, ArrayList::new).add(
                            new ConstructorData(constrSig, (IStrategoAppl) constrTerm, constrType));
                        break;
                }
                Relation.getOrInitialize(dataMap, from, ArrayList::new).add(constrType.to);
        }
    }

    protected LastModified<IStrategoTerm> getModuleAst(ExecContext context, FrontInput input)
        throws Exception {
        if(input instanceof FrontInput.Normal) {
            return getModuleAst(context, (FrontInput.Normal) input);
        } else if(input instanceof FrontInput.FileOpenInEditor) {
            return ((FrontInput.FileOpenInEditor) input).ast;
        } else {
            throw new RuntimeException("Unknown subclass of FrontInput: " + input.getClass());
        }
    }

    private LastModified<IStrategoTerm> getModuleAst(ExecContext context, FrontInput.Normal input)
        throws Exception {
        return moduleImportService
            .getModuleAst(context, input.moduleIdentifier, input.strFileGeneratingTasks);
    }
}
