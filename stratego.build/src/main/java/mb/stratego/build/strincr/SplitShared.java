package mb.stratego.build.strincr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.StrategyFrontData.Kind;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.termvisitors.CollectDynRuleSigs;
import mb.stratego.build.termvisitors.DesugarType;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.TermWithLastModified;

import static mb.stratego.build.strincr.StrategyFrontData.Kind.DynRuleGenerated;
import static mb.stratego.build.strincr.StrategyFrontData.Kind.Extend;
import static mb.stratego.build.strincr.StrategyFrontData.Kind.External;
import static mb.stratego.build.strincr.StrategyFrontData.Kind.Internal;
import static mb.stratego.build.strincr.StrategyFrontData.Kind.Normal;
import static mb.stratego.build.strincr.StrategyFrontData.Kind.TypeDefinition;

public class SplitShared {
    protected final StrIncrContext strContext;
    protected final ITermFactory tf;
    protected final B b;

    @Inject public SplitShared(StrIncrContext strContext) {
        this.strContext = strContext;
        this.tf = strContext.getFactory();
        this.b = new B(this.tf);
    }

    protected void addStrategyData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        Map<StrategySignature, Set<StrategyFrontData>> strategyData,
        TermWithLastModified strategyDefs, List<Message<?>> messages) throws WrongASTException {
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
        for(IStrategoTerm strategyDef : strategyDefs.term) {
            if(TermUtils.isAppl(strategyDef, "DefHasType", 3)) {
                final IStrategoTerm funTType = strategyDef.getSubterm(1);
                final StrategyType strategyType = StrategyType.fromTerm(funTType);
                if(strategyType == null) {
                    throw new WrongASTException(moduleIdentifier, funTType);
                }
                final StrategySignature strategySignature =
                    new StrategySignature(TermUtils.toJavaStringAt(strategyDef, 0),
                        strategyType.getStrategyArguments().size(),
                        strategyType.getTermArguments().size());
                Relation.getOrInitialize(strategyData, strategySignature, HashSet::new)
                    .add(new StrategyFrontData(strategySignature, strategyType, TypeDefinition));
            } else {
                Kind kind = Normal;
                if(TermUtils.isAppl(strategyDef, "AnnoDef", 2)) {
                    for(IStrategoTerm anno : strategyDef.getSubterm(0)) {
                        if(TermUtils.isAppl(anno, "Internal", 0)) {
                            kind = Internal;
                        } else if(TermUtils.isAppl(anno, "Extend", 0)) {
                            kind = Extend;
                        } else if(TermUtils.isAppl(anno, "Override", 0)) {
                            kind = Kind.Override;
                        }
                    }
                    strategyDef = strategyDef.getSubterm(1);
                }
                if(!TermUtils.isAppl(strategyDef)) {
                    throw new WrongASTException(moduleIdentifier, strategyDef);
                }
                if(!TermUtils.isStringAt(strategyDef, 0)) {
                    throw new WrongASTException(moduleIdentifier, strategyDef);
                }
                final String name = TermUtils.toJavaStringAt(strategyDef, 0);
                final int sArity;
                final int tArity;
                switch(TermUtils.toAppl(strategyDef).getName()) {
                    case "ExtSDef":
                        kind = External;
                        // fall-through
                    case "SDef":
                        // fall-through
                    case "RDef": {
                        final IStrategoTerm sargs = strategyDef.getSubterm(1);
                        if(!TermUtils.isList(sargs)) {
                            throw new WrongASTException(moduleIdentifier, sargs);
                        }
                        sArity = sargs.getSubtermCount();
                        tArity = 0;
                        break;
                    }
                    case "SDefNoArgs":
                        // fall-through
                    case "RDefNoArgs":
                        sArity = 0;
                        tArity = 0;
                        break;
                    case "ExtSDefInl":
                        kind = External;
                        // fall-through
                    case "SDefT":
                        // fall-through
                    case "RDefT":
                        // fall-through
                    case "RDefP": {
                        final IStrategoTerm sargs = strategyDef.getSubterm(1);
                        if(!TermUtils.isList(sargs)) {
                            throw new WrongASTException(moduleIdentifier, sargs);
                        }
                        sArity = sargs.getSubtermCount();
                        final IStrategoTerm targs = strategyDef.getSubterm(1);
                        if(!TermUtils.isList(targs)) {
                            throw new WrongASTException(moduleIdentifier, targs);
                        }
                        tArity = targs.getSubtermCount();
                        break;
                    }
                    default:
                        throw new WrongASTException(moduleIdentifier, strategyDef);
                }
                final StrategySignature strategySignature =
                    new StrategySignature(name, sArity, tArity);
                Relation.getOrInitialize(strategyData, strategySignature, HashSet::new).add(new StrategyFrontData(strategySignature, null,
                    kind));
            }

            // collect-om(dyn-rule-sig)
            for(StrategySignature dynRuleSig : CollectDynRuleSigs.collect(strategyDef)) {
                for(Map.Entry<StrategySignature, StrategyType> e : dynRuleSig
                    .dynamicRuleSignatures(tf).entrySet()) {
                    final StrategySignature signature = e.getKey();
                    final StrategyType type = e.getValue();

                    Relation.getOrInitialize(strategyData, signature, HashSet::new).add(new StrategyFrontData(signature, null, DynRuleGenerated));
                }
            }
        }
    }

    protected void addOverlayData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        Map<ConstructorSignature, List<ConstructorData>> overlayData,
        Map<ConstructorSignature, List<ConstructorData>> constrData, TermWithLastModified overlays)
        throws WrongASTException {
        /*
        extract-constr:
          OverlayNoArgs(c, _) -> ((c,0), ConstrType([], DynT()))

        extract-constr:
          Overlay(c, t*, _) -> ((c, <length> t*), ConstrType(<map(!DynT())> t*, DynT()))
         */
        final IStrategoTerm dynT = b.applShared("DynT", b.applShared("Dyn"));
        for(IStrategoTerm overlay : overlays.term) {
            final int arity;
            final ConstructorType type;
            final String name;
            if(TermUtils.isStringAt(overlay, 0)) {
                name = TermUtils.toJavaStringAt(overlay, 0);
                if(TermUtils.isAppl(overlay, "OverlayNoArgs", 2)) {
                    arity = 0;
                    type = new ConstructorType(Collections.emptyList(), dynT);
                } else if(TermUtils.isAppl(overlay, "Overlay", 3) && TermUtils
                    .isListAt(overlay, 1)) {
                    arity = TermUtils.toListAt(overlay, 1).size();
                    type = new ConstructorType(Collections.nCopies(arity, dynT), dynT);
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

    protected void addSigData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        Map<ConstructorSignature, List<ConstructorData>> constrData,
        Map<TermWithLastModified, List<TermWithLastModified>> injections, TermWithLastModified sigs)
        throws WrongASTException {
        for(IStrategoTerm sig : sigs.term) {
            if(TermUtils.isAppl(sig, "Constructors", 1)) {
                final IStrategoTerm constrs = sig.getSubterm(1);
                if(TermUtils.isList(constrs)) {
                    for(IStrategoTerm constrDef : constrs) {
                        final ConstructorSignature constrSig =
                            ConstructorSignature.fromTerm(constrDef);
                        if(constrSig == null) {
                            addInjectionData(moduleIdentifier,
                                TermWithLastModified.fromParent(constrDef, sigs), injections,
                                constrData);
                            continue;
                        }
                        final IStrategoTerm constrTerm = DesugarType.alltd(strContext, constrDef);
                        final ConstructorType constrType = constrType(moduleIdentifier, constrDef);
                        Relation.getOrInitialize(constrData, constrSig, ArrayList::new)
                            .add(new ConstructorData(constrSig, constrTerm, constrType));
                    }
                }
            }
        }
    }

    private ConstructorType constrType(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IStrategoTerm constrDef) throws WrongASTException {
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

        final ConstructorType type = ConstructorType.fromOpType(tf, opType);

        if(type == null) {
            throw new WrongASTException(moduleIdentifier, opType);
        }

        return type;
    }

    private void addInjectionData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        TermWithLastModified constrDefWLM,
        Map<TermWithLastModified, List<TermWithLastModified>> injections,
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
        final IStrategoTerm constrDef = constrDefWLM.term;
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
                final IStrategoTerm opType = constrDef.getSubterm(0);
                final ConstructorType constrType = ConstructorType.fromOpType(tf, opType);
                if(constrType == null) {
                    throw new WrongASTException(moduleIdentifier, opType);
                }

                final TermWithLastModified to =
                    TermWithLastModified.fromParent(constrType.to, constrDefWLM);
                final TermWithLastModified from;
                final List<IStrategoTerm> froms = constrType.getFrom();
                switch(froms.size()) {
                    case 0:
                        throw new WrongASTException(moduleIdentifier, opType);
                    case 1:
                        from = TermWithLastModified.fromParent(froms.get(0), constrDefWLM);
                        break;
                    default:
                        final IStrategoList tupleTypes = tf.makeList(froms);
                        from = TermWithLastModified
                            .fromParent(b.applShared("Sort", b.stringShared("Tuple"), tupleTypes),
                                constrDefWLM);

                        final ConstructorSignature constrSig =
                            new ConstructorSignature("", froms.size());
                        final IStrategoTerm constrTerm =
                            tf.replaceTerm(constrType.toOpType(tf), constrDef);
                        Relation.getOrInitialize(constrData, constrSig, ArrayList::new)
                            .add(new ConstructorData(constrSig, constrTerm, constrType));
                        break;
                }
                Relation.getOrInitialize(injections, from, ArrayList::new).add(to);
        }
    }
}
