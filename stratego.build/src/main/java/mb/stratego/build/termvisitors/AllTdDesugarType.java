package mb.stratego.build.termvisitors;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.SRTS_all;
import org.strategoxt.lang.Strategy;

public class AllTdDesugarType {
    public static IStrategoTerm visit(Context context, IStrategoTerm t) {
        IStrategoTerm result = desugarType(context.getFactory(), t);
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
        IStrategoTerm result = desugarType(tf, term);
        return result == null ? term : result;
    }

    public static @Nullable IStrategoTerm desugarType(ITermFactory tf, IStrategoTerm term) {
        @Nullable IStrategoTerm result = null;
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
}
