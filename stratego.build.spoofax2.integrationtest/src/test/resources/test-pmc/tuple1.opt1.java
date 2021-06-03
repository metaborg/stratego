import org.strategoxt.stratego_lib.*;
import org.strategoxt.lang.*;
import org.strategoxt.lang.gradual.*;
import org.spoofax.interpreter.terms.*;
import static org.strategoxt.lang.Term.*;
import org.spoofax.interpreter.library.AbstractPrimitive;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.ref.WeakReference;

@SuppressWarnings("all") public class tuple1.opt1  
{ 
  protected static final boolean TRACES_ENABLED = true;

  protected static ITermFactory constantFactory;

  private static WeakReference<Context> initedContext;

  private static boolean isIniting;

  public static IStrategoConstructor _consConc_2;

  public static IStrategoConstructor _consNone_0;

  public static IStrategoConstructor _consSome_1;

  public static IStrategoConstructor _consDR_DUMMY_0;

  public static IStrategoConstructor _consDR_UNDEFINE_1;

  public static Context init(Context context)
  { 
    synchronized(tuple1.opt1.class)
    { 
      if(isIniting)
        return null;
      try
      { 
        isIniting = true;
        ITermFactory termFactory = context.getFactory();
        if(constantFactory == null)
        { 
          initConstructors(termFactory);
          initConstants(termFactory);
        }
        if(initedContext == null || initedContext.get() != context)
        { 
          org.strategoxt.stratego_lib.Main.init(context);
          context.registerComponent("tuple1.opt1");
        }
        initedContext = new WeakReference<Context>(context);
        constantFactory = termFactory;
      }
      finally
      { 
        isIniting = false;
      }
      return context;
    }
  }

  public static Context init()
  { 
    return init(new Context());
  }

  public static Strategy getMainStrategy()
  { 
    return null;
  }

  public static void initConstructors(ITermFactory termFactory)
  { 
    _consConc_2 = termFactory.makeConstructor("Conc", 2);
    _consNone_0 = termFactory.makeConstructor("None", 0);
    _consSome_1 = termFactory.makeConstructor("Some", 1);
    _consDR_DUMMY_0 = termFactory.makeConstructor("DR_DUMMY", 0);
    _consDR_UNDEFINE_1 = termFactory.makeConstructor("DR_UNDEFINE", 1);
  }

  public static void initConstants(ITermFactory termFactory)
  { }

  @SuppressWarnings("all") public static class s2_0_0 extends Strategy 
  { 
    public static s2_0_0 instance = new s2_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
    { 
      Fail4:
      { 
        if(term.getTermType() != IStrategoTerm.TUPLE || term.getSubtermCount() != 2)
          break Fail4;
        IStrategoTerm arg0 = term.getSubterm(0);
        if(arg0.getTermType() != IStrategoTerm.INT || 1 != ((IStrategoInt)arg0).intValue())
          break Fail4;
        IStrategoTerm arg1 = term.getSubterm(1);
        if(arg1.getTermType() != IStrategoTerm.INT || 2 != ((IStrategoInt)arg1).intValue())
          break Fail4;
        if(true)
          return term;
      }
      context.push("s2_0_0");
      context.popOnFailure();
      return null;
    }
  }

  @SuppressWarnings("all") public static class $Anno__$Cong_____2_0 extends Strategy 
  { 
    public static $Anno__$Cong_____2_0 instance = new $Anno__$Cong_____2_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy anno_cong_str_arg11, Strategy anno_cong_str_arg21)
    { 
      ITermFactory termFactory = context.getFactory();
      context.push("Anno__Cong_____2_0");
      Fail5:
      { 
        IStrategoTerm anno_cong_term_m1 = null;
        IStrategoTerm anno_cong_anno_m1 = null;
        IStrategoTerm anno_cong_term_b1 = null;
        anno_cong_term_m1 = term;
        IStrategoList annos2 = term.getAnnotations();
        anno_cong_anno_m1 = annos2;
        term = anno_cong_str_arg11.invoke(context, anno_cong_term_m1);
        if(term == null)
          break Fail5;
        anno_cong_term_b1 = term;
        term = anno_cong_str_arg21.invoke(context, anno_cong_anno_m1);
        if(term == null)
          break Fail5;
        term = termFactory.annotateTerm(anno_cong_term_b1, checkListAnnos(termFactory, term));
        context.popOnSuccess();
        if(true)
          return term;
      }
      context.popOnFailure();
      return null;
    }
  }

  @SuppressWarnings("all") public static class $D$R__$U$N$D$E$F$I$N$E_1_0 extends Strategy 
  { 
    public static $D$R__$U$N$D$E$F$I$N$E_1_0 instance = new $D$R__$U$N$D$E$F$I$N$E_1_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy cong_arg_11)
    { 
      ITermFactory termFactory = context.getFactory();
      context.push("DR__UNDEFINE_1_0");
      Fail6:
      { 
        IStrategoTerm trans_cong4 = null;
        IStrategoTerm trans_cong3 = null;
        if(term.getTermType() != IStrategoTerm.APPL || tuple1.opt1._consDR_UNDEFINE_1 != ((IStrategoAppl)term).getConstructor())
          break Fail6;
        trans_cong3 = term.getSubterm(0);
        IStrategoList annos3 = term.getAnnotations();
        trans_cong4 = annos3;
        term = cong_arg_11.invoke(context, trans_cong3);
        if(term == null)
          break Fail6;
        term = termFactory.annotateTerm(termFactory.makeAppl(tuple1.opt1._consDR_UNDEFINE_1, new IStrategoTerm[]{term}), checkListAnnos(termFactory, trans_cong4));
        context.popOnSuccess();
        if(true)
          return term;
      }
      context.popOnFailure();
      return null;
    }
  }

  @SuppressWarnings("all") public static class $D$R__$D$U$M$M$Y_0_0 extends Strategy 
  { 
    public static $D$R__$D$U$M$M$Y_0_0 instance = new $D$R__$D$U$M$M$Y_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
    { 
      Fail7:
      { 
        if(term.getTermType() != IStrategoTerm.APPL || tuple1.opt1._consDR_DUMMY_0 != ((IStrategoAppl)term).getConstructor())
          break Fail7;
        if(true)
          return term;
      }
      context.push("DR__DUMMY_0_0");
      context.popOnFailure();
      return null;
    }
  }

  public static void registerInterop(org.spoofax.interpreter.core.IContext context, Context compiledContext)
  { 
    new InteropRegisterer().registerLazy(context, compiledContext, InteropRegisterer.class.getClassLoader());
  }

  @SuppressWarnings("unused") public static class InteropRegisterer extends org.strategoxt.lang.InteropRegisterer 
  { 
    @Override public void register(org.spoofax.interpreter.core.IContext context, Context compiledContext)
    { 
      register(context, compiledContext, context.getVarScope());
    }

    @Override public void registerLazy(org.spoofax.interpreter.core.IContext context, Context compiledContext, ClassLoader classLoader)
    { 
      registerLazy(context, compiledContext, classLoader, context.getVarScope());
    }

    private void register(org.spoofax.interpreter.core.IContext context, Context compiledContext, org.spoofax.interpreter.core.VarScope varScope)
    { 
      compiledContext.registerComponent("tuple1.opt1");
      tuple1.opt1.init(compiledContext);
      varScope.addSVar("s2_0_0", new InteropSDefT(s2_0_0.instance, context));
      varScope.addSVar("Anno__Cong_____2_0", new InteropSDefT($Anno__$Cong_____2_0.instance, context));
      varScope.addSVar("DR__UNDEFINE_1_0", new InteropSDefT($D$R__$U$N$D$E$F$I$N$E_1_0.instance, context));
      varScope.addSVar("DR__DUMMY_0_0", new InteropSDefT($D$R__$D$U$M$M$Y_0_0.instance, context));
      compiledContext.typeInfo.registerConstructor(new Sort("ATerm", Arrays.asList()), "DR_UNDEFINE", Arrays.asList(new Sort("ATerm", Arrays.asList())));
      compiledContext.typeInfo.registerConstructor(new Sort("ATerm", Arrays.asList()), "DR_DUMMY", Arrays.asList());
      compiledContext.typeInfo.finishRegistration();
    }

    private void registerLazy(org.spoofax.interpreter.core.IContext context, Context compiledContext, ClassLoader classLoader, org.spoofax.interpreter.core.VarScope varScope)
    { 
      compiledContext.registerComponent("tuple1.opt1");
      tuple1.opt1.init(compiledContext);
      varScope.addSVar("s2_0_0", new InteropSDefT(classLoader, "tuple1.opt1$s2_0_0", context));
      varScope.addSVar("Anno__Cong_____2_0", new InteropSDefT(classLoader, "tuple1.opt1$$Anno__$Cong_____2_0", context));
      varScope.addSVar("DR__UNDEFINE_1_0", new InteropSDefT(classLoader, "tuple1.opt1$$D$R__$U$N$D$E$F$I$N$E_1_0", context));
      varScope.addSVar("DR__DUMMY_0_0", new InteropSDefT(classLoader, "tuple1.opt1$$D$R__$D$U$M$M$Y_0_0", context));
      compiledContext.typeInfo.registerConstructor(new Sort("ATerm", Arrays.asList()), "DR_UNDEFINE", Arrays.asList(new Sort("ATerm", Arrays.asList())));
      compiledContext.typeInfo.registerConstructor(new Sort("ATerm", Arrays.asList()), "DR_DUMMY", Arrays.asList());
    }
  }
}