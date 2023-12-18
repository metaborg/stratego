package mb.stratego.build.termvisitors;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.SRTS_all;
import org.strategoxt.lang.Strategy;

public class DesugarType {
    public static IStrategoTerm alltd(Context context, IStrategoTerm t) {
        return visit(context, t);
    }

    private static IStrategoTerm visit(Context context, IStrategoTerm t) {
        final @Nullable IStrategoTerm result = desugarType(context.getFactory(), t);
        if(result == null) {
            return SRTS_all.instance.invoke(context, t, new Strategy() {
                @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
                    return visit(context, current);
                }
            });
        }
        return result;
    }

    public static IStrategoTerm tryDesugarType(ITermFactory tf, IStrategoTerm term) {
        final @Nullable IStrategoTerm result = desugarType(tf, term);
        return result == null ? term : result;
    }

    public static @Nullable IStrategoTerm desugarType(ITermFactory tf, IStrategoTerm term) {
        if(!TermUtils.isAppl(term)) {
            return null;
        }
        final IStrategoTerm result;
        // desugar-Type- = otf(\SortVar("string") -> StringT()\)
        // desugar-Type- = otf(\SortVar("int") -> IntT()\)
        // desugar-Type- = otf(\SortVar("real") -> RealT()\)
        // desugar-Type- = otf(\SortVar("blob") -> BlobT()\)
        if(TermUtils.isAppl(term, "SortVar", 1)) {
            switch(TermUtils.toJavaStringAt(term, 0)) {
                case "string":
                    result = tf.makeAppl("StringT");
                    break;
                case "int":
                    result = tf.makeAppl("IntT");
                    break;
                case "real":
                    result = tf.makeAppl("RealT");
                    break;
                case "blob":
                    result = tf.makeAppl("BlobT");
                    break;
                default:
                    result = term;
                    break;
            }
        } else {
            switch(TermUtils.toAppl(term).getName()) {
                // desugar-Type- = otf(\SortNoArgs(x) -> Sort(x, [])\)
                case "SortNoArgs":
                    if(term.getSubtermCount() == 1) {
                        result = tf.makeAppl("Sort", term.getSubterm(0), tf.makeList());
                        break;
                    }
                    return null;
                // desugar-Type- = otf(?a;Sort(id, map(desugar-Type-));not(?a))
                case "Sort":
                    if(term.getSubtermCount() == 2) {
                        final IStrategoList params = TermUtils.toListAt(term, 1);
                        final IStrategoList.Builder types = tf.arrayListBuilder(params.size());
                        for(IStrategoTerm t : params) {
                            types.add(tryDesugarType(tf, t));
                        }
                        result = tf.makeAppl("Sort", term.getSubterm(0), tf.makeList(types));
                        break;
                    }
                    return null;
                // desugar-Type- = otf(\SortList(xs) -> <foldr(!Sort("Nil",[]), !Sort("Cons",[<Fst;desugar-Type>,<Snd;desugar-Type>]))> xs\)
                case "SortList":
                    if(term.getSubtermCount() == 1) {
                        IStrategoTerm desugared =
                            tf.makeAppl("Sort", tf.makeString("Nil"), tf.makeList());
                        for(IStrategoTerm t : term.getSubterm(0)) {
                            desugared = tf.makeAppl("Sort", tf.makeString("Cons"),
                                tf.makeList(tryDesugarType(tf, t), desugared));
                        }
                        result = desugared;
                        break;
                    }
                    return null;
                // desugar-Type- = otf(\SortListTl(xs, y) -> <foldr(<desugar-Type> y, !Sort("Cons",[<Fst;desugar-Type>,<Snd;desugar-Type>]))> xs\)
                case "SortListTl":
                    if(term.getSubtermCount() == 2) {
                        IStrategoTerm desugared = tryDesugarType(tf, term.getSubterm(1));
                        for(IStrategoTerm t : term.getSubterm(0)) {
                            desugared = tf.makeAppl("Sort", tf.makeString("Cons"),
                                tf.makeList(tryDesugarType(tf, t), desugared));
                        }
                        result = desugared;
                        break;
                    }
                    return null;
                // desugar-Type- = otf(\SortTuple(xs) -> Sort("Tuple", <map(desugar-Type)> xs)\)
                case "SortTuple":
                    if(term.getSubtermCount() == 1) {
                        IStrategoList.Builder types =
                            tf.arrayListBuilder(term.getSubterm(0).getSubtermCount());
                        for(IStrategoTerm t : term.getSubterm(0)) {
                            types.add(tryDesugarType(tf, t));
                        }
                        result = tf.makeAppl("Sort", tf.makeString("Tuple"), tf.makeList(types));
                        break;
                    }
                    return null;
                // desugar-Type- = otf(\TupleT(t1, t2) -> Sort("Tuple", [<desugar-Type> t1 | <map(desugar-Type)> t2])\)
                case "TupleT":
                    if(term.getSubtermCount() == 2) {
                        IStrategoList.Builder types = tf.arrayListBuilder();
                        types.add(tryDesugarType(tf, term.getSubterm(0)));
                        for(IStrategoTerm t : term.getSubterm(1)) {
                            types.add(tryDesugarType(tf, t));
                        }
                        result = tf.makeAppl("Sort", tf.makeString("Tuple"), tf.makeList(types));
                        break;
                    }
                    return null;
                default:
                    return null;
            }
        }
        return tf.replaceTerm(result, term);
    }

    public static IStrategoTerm tryDesugarSType(ITermFactory tf, IStrategoTerm term) {
        final @Nullable IStrategoTerm result = desugarSType(tf, term);
        return result == null ? term : result;
    }

    private static @Nullable IStrategoTerm desugarSType(ITermFactory tf, IStrategoTerm term) {
        if(!TermUtils.isAppl(term)) {
            return null;
        }
        final IStrategoTerm result;
        switch(TermUtils.toAppl(term).getName()) {
            // desugar-SType = otf(\TP() -> FunTType([], [], TP())\)
            case "TP":
                if(term.getSubtermCount() == 0) {
                    result = tf.makeAppl("FunTType", tf.makeList(), tf.makeList(), term);
                    break;
                }
                return null;
            // desugar-SType = otf(\st@FunNoArgsType(i, o) -> FunTType([], [], <desugar-SSimpleFunType> st)\)
            case "FunNoArgsType":
                if(term.getSubtermCount() == 2) {
                    result = tf.makeAppl("FunTType", tf.makeList(), tf.makeList(),
                        desugarSSimpleFunType(tf, term));
                    break;
                }
                return null;
            // desugar-SType = otf(\FunType(sargs, t) -> FunTType(<map(try(desugar-SType))> sargs, [], <desugar-Type> t)\)
            case "FunType":
                if(term.getSubtermCount() == 2) {
                    final IStrategoList sargs = TermUtils.toListAt(term, 0);
                    final IStrategoList.Builder sargs2 = tf.arrayListBuilder(sargs.size());
                    for(IStrategoTerm sarg : sargs) {
                        sargs2.add(tryDesugarSType(tf, sarg));
                    }
                    final IStrategoTerm t = desugarSSimpleFunType(tf, term.getSubterm(1));
                    result = tf.makeAppl("FunTType", sargs2.build(), tf.makeList(), t);
                    break;
                }
                return null;
            // desugar-SType = otf(?a;FunTType(map(try(desugar-SType)), map(desugar-Type), desugar-SSimpleFunType);not(?a))
            case "FunTType":
                if(term.getSubtermCount() == 3) {
                    final IStrategoList sargs = TermUtils.toListAt(term, 0);
                    final IStrategoList.Builder sargs2 = tf.arrayListBuilder(sargs.size());
                    for(IStrategoTerm sarg : sargs) {
                        sargs2.add(tryDesugarSType(tf, sarg));
                    }
                    final IStrategoList targs = TermUtils.toListAt(term, 1);
                    final IStrategoList.Builder targs2 = tf.arrayListBuilder(targs.size());
                    for(IStrategoTerm targ : targs) {
                        targs2.add(tryDesugarType(tf, targ));
                    }
                    result = tf.makeAppl("FunTType", sargs2.build(), targs2.build(),
                        desugarSSimpleFunType(tf, term.getSubterm(2)));
                    if(result.equals(term)) {
                        return null;
                    }
                    break;
                }
                return null;
            default:
                return null;
        }
        return tf.replaceTerm(result, term);
    }

    private static @Nullable IStrategoTerm desugarSSimpleFunType(ITermFactory tf, IStrategoTerm term) {
        if(!TermUtils.isAppl(term)) {
            return null;
        }
        final IStrategoTerm result;
        switch(TermUtils.toAppl(term).getName()) {
            // desugar-SSimpleFunType = ?TP()
            case "TP":
                if(term.getSubtermCount() == 0) {
                    result = term;
                    break;
                }
                return null;
            // desugar-SSimpleFunType = otf(FunNoArgsType(desugar-Type, desugar-Type))
            case "FunNoArgsType":
                if(term.getSubtermCount() == 2) {
                    final IStrategoTerm i = tryDesugarType(tf, term.getSubterm(0));
                    final IStrategoTerm o = tryDesugarType(tf, term.getSubterm(1));
                    result = tf.makeAppl("FunNoArgsType", i, o);
                    break;
                }
                return null;
            default:
                return null;
        }
        return tf.replaceTerm(result, term);
    }
}
