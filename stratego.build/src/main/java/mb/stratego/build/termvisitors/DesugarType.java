package mb.stratego.build.termvisitors;

import javax.annotation.Nullable;

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

    public static @Nullable IStrategoTerm  desugarType(ITermFactory tf, IStrategoTerm term) {
        @Nullable IStrategoTerm result;
        // desugar-Type- = otf(\SortVar("str") -> StringT()\)
        // desugar-Type- = otf(\SortVar("int") -> IntT()\)
        // desugar-Type- = otf(\SortVar("real") -> RealT()\)
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
                default:
                    result = term;
                    break;
            }
        } else {
            if(TermUtils.isAppl(term, "SortNoArgs", 1)) {
                // desugar-Type- = otf(\SortNoArgs(x) -> Sort(x, [])\)
                result = tf.makeAppl("Sort", term.getSubterm(0), tf.makeList());
            } else if(TermUtils.isAppl(term, "Sort", 2)) {
                // desugar-Type- = otf(?a;Sort(id, map(desugar-Type-));not(?a))
                final IStrategoList params = TermUtils.toListAt(term, 1);
                final IStrategoList.Builder types = tf.arrayListBuilder(params.size());
                for(IStrategoTerm t : params) {
                    types.add(tryDesugarType(tf, t));
                }
                result = tf.makeAppl("Sort", term.getSubterm(0), tf.makeList(types));
            } else if(TermUtils.isAppl(term, "SortList", 1)) {
                // desugar-Type- = otf(\SortList(xs) -> <foldr(!Sort("Nil",[]), !Sort("Cons",[<Fst>,<Snd>]))> xs\)
                IStrategoTerm desugared = tf.makeAppl("Sort", tf.makeString("Nil"), tf.makeList());
                for(IStrategoTerm t : term.getSubterm(0)) {
                    desugared =
                        tf.makeAppl("Sort", tf.makeString("Cons"), tf.makeList(t, desugared));
                }
                result = desugared;
            } else if(TermUtils.isAppl(term, "SortListTl", 2)) {
                // desugar-Type- = otf(\SortListTl(xs, y) -> <foldr(!y, !Sort("Cons",[<Fst>,<Snd>]))> xs\)
                IStrategoTerm desugared = term.getSubterm(1);
                for(IStrategoTerm t : term.getSubterm(0)) {
                    desugared =
                        tf.makeAppl("Sort", tf.makeString("Cons"), tf.makeList(t, desugared));
                }
                result = desugared;
            } else if(TermUtils.isAppl(term, "SortTuple", 1)) {
                // desugar-Type- = otf(\SortTuple(xs) -> TupleT(<map(desugar-Type)> xs)\)
                IStrategoList.Builder types =
                    tf.arrayListBuilder(term.getSubterm(0).getSubtermCount());
                for(IStrategoTerm t : term.getSubterm(0)) {
                    types.add(tryDesugarType(tf, t));
                }
                result = tf.makeAppl("TupleT", tf.makeList(types));
            } else if(TermUtils.isAppl(term, "TupleT", 2)) {
                // desugar-Type- = otf(\TupleT(t1, t2) -> TupleT([<desugar-Type> t1 | <map(desugar-Type)> t2])\)
                IStrategoList.Builder types = tf.arrayListBuilder();
                types.add(tryDesugarType(tf, term.getSubterm(0)));
                for(IStrategoTerm t : term.getSubterm(1)) {
                    types.add(tryDesugarType(tf, t));
                }
                result = tf.makeAppl("TupleT", tf.makeList(types));
            } else {
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
        @Nullable IStrategoTerm result;
        if(TermUtils.isAppl(term, "TP", 0)) {
            // desugar-SType = otf(\TP() -> FunTType([], [], TP())\)
            result = tf.makeAppl("FunTType", tf.makeList(), tf.makeList(), term);
        } else if(TermUtils.isAppl(term, "FunNoArgsType", 2)) {
            // desugar-SType = otf(\FunNoArgsType(i, o) -> FunTType([], [], FunNoArgsType(<desugar-Type> i, <desugar-Type> o))\)
            final IStrategoTerm i = tryDesugarType(tf, term.getSubterm(0));
            final IStrategoTerm o = tryDesugarType(tf, term.getSubterm(1));
            result = tf.makeAppl("FunTType", tf.makeList(), tf.makeList(), tf.makeAppl("FunNoArgsType", i, o));
        } else if(TermUtils.isAppl(term, "FunType", 2)) {
//            desugar-SType = otf(\FunType(sargs, t) -> FunTType(<map(try(desugar-SType))> sargs, [], <desugar-Type> t)\)
            final IStrategoList sargs = TermUtils.toListAt(term, 0);
            final IStrategoList.Builder sargs2 = tf.arrayListBuilder(sargs.size());
            for(IStrategoTerm sarg : sargs) {
                sargs2.add(tryDesugarSType(tf, sarg));
            }
            final IStrategoTerm t = tryDesugarType(tf, term.getSubterm(1));
            result = tf.makeAppl("FunTType", sargs2.build(), tf.makeList(), t);
        } else if(TermUtils.isAppl(term, "FunTType", 4) && TermUtils.isApplAt(term, 2, "FunNoArgsType", 2)) {
//            desugar-SType = otf(?a;FunTType(map(try(desugar-SType)), map(desugar-Type), FunNoArgsType(desugar-Type, desugar-Type));not(?a))
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
            final IStrategoTerm i = tryDesugarType(tf, term.getSubterm(2).getSubterm(0));
            final IStrategoTerm o = tryDesugarType(tf, term.getSubterm(2).getSubterm(1));
            result = tf.makeAppl("FunTType", sargs2.build(), targs2.build(), tf.makeAppl("FunNoArgsType", i, o));
            if(result.equals(term)) {
                return null;
            }
        } else {
            return null;
        }
        return tf.replaceTerm(result, term);
    }
}
