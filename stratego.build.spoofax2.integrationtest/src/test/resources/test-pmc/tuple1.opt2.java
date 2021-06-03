import org.strategoxt.stratego_lib.*;
import org.strategoxt.lang.*;
import org.strategoxt.lang.gradual.*;
import org.spoofax.interpreter.terms.*;
import static org.strategoxt.lang.Term.*;
import org.spoofax.interpreter.library.AbstractPrimitive;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.ref.WeakReference;

@SuppressWarnings("all") public class tuple1.opt2  
{ 
  protected static final boolean TRACES_ENABLED = true;

  protected static ITermFactory constantFactory;

  private static WeakReference<Context> initedContext;

  private static boolean isIniting;

  public static IStrategoConstructor _consConc_2;

  public static IStrategoConstructor _consNone_0;

  public static IStrategoConstructor _consSome_1;

  protected static IStrategoConstructor _consC2_0;

  protected static IStrategoConstructor _consC1_2;

  public static IStrategoConstructor _consDR_DUMMY_0;

  public static IStrategoConstructor _consDR_UNDEFINE_1;

  public static Context init(Context context)
  { 
    synchronized(tuple1.opt2.class)
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
          context.registerComponent("tuple1.opt2");
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
    _consC2_0 = termFactory.makeConstructor("C2", 0);
    _consC1_2 = termFactory.makeConstructor("C1", 2);
    _consDR_DUMMY_0 = termFactory.makeConstructor("DR_DUMMY", 0);
    _consDR_UNDEFINE_1 = termFactory.makeConstructor("DR_UNDEFINE", 1);
  }

  public static void initConstants(ITermFactory termFactory)
  { }

  @SuppressWarnings("all") public static class s3_0_0 extends Strategy 
  { 
    public static s3_0_0 instance = new s3_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
    { 
      Fail0:
      { 
        if(term.getTermType() != IStrategoTerm.APPL || tuple1.opt2._consC1_2 != ((IStrategoAppl)term).getConstructor())
          break Fail0;
        IStrategoTerm arg0 = term.getSubterm(0);
        if(arg0.getTermType() != IStrategoTerm.APPL || tuple1.opt2._consC2_0 != ((IStrategoAppl)arg0).getConstructor())
          break Fail0;
        if(true)
          return term;
      }
      context.push("s3_0_0");
      context.popOnFailure();
      return null;
    }
  }

  @SuppressWarnings("all") public static class $Anno__$Cong_____2_0 extends Strategy 
  { 
    public static $Anno__$Cong_____2_0 instance = new $Anno__$Cong_____2_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy anno_cong_str_arg10, Strategy anno_cong_str_arg20)
    { 
      ITermFactory termFactory = context.getFactory();
      context.push("Anno__Cong_____2_0");
      Fail1:
      { 
        IStrategoTerm anno_cong_term_m0 = null;
        IStrategoTerm anno_cong_anno_m0 = null;
        IStrategoTerm anno_cong_term_b0 = null;
        anno_cong_term_m0 = term;
        IStrategoList annos0 = term.getAnnotations();
        anno_cong_anno_m0 = annos0;
        term = anno_cong_str_arg10.invoke(context, anno_cong_term_m0);
        if(term == null)
          break Fail1;
        anno_cong_term_b0 = term;
        term = anno_cong_str_arg20.invoke(context, anno_cong_anno_m0);
        if(term == null)
          break Fail1;
        term = termFactory.annotateTerm(anno_cong_term_b0, checkListAnnos(termFactory, term));
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

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy cong_arg_10)
    { 
      ITermFactory termFactory = context.getFactory();
      context.push("DR__UNDEFINE_1_0");
      Fail2:
      { 
        IStrategoTerm trans_cong1 = null;
        IStrategoTerm trans_cong0 = null;
        if(term.getTermType() != IStrategoTerm.APPL || tuple1.opt2._consDR_UNDEFINE_1 != ((IStrategoAppl)term).getConstructor())
          break Fail2;
        trans_cong0 = term.getSubterm(0);
        IStrategoList annos1 = term.getAnnotations();
        trans_cong1 = annos1;
        term = cong_arg_10.invoke(context, trans_cong0);
        if(term == null)
          break Fail2;
        term = termFactory.annotateTerm(termFactory.makeAppl(tuple1.opt2._consDR_UNDEFINE_1, new IStrategoTerm[]{term}), checkListAnnos(termFactory, trans_cong1));
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
      Fail3:
      { 
        if(term.getTermType() != IStrategoTerm.APPL || tuple1.opt2._consDR_DUMMY_0 != ((IStrategoAppl)term).getConstructor())
          break Fail3;
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
      compiledContext.registerComponent("tuple1.opt2");
      tuple1.opt2.init(compiledContext);
      varScope.addSVar("s3_0_0", new InteropSDefT(s3_0_0.instance, context));
      varScope.addSVar("Anno__Cong_____2_0", new InteropSDefT($Anno__$Cong_____2_0.instance, context));
      varScope.addSVar("DR__UNDEFINE_1_0", new InteropSDefT($D$R__$U$N$D$E$F$I$N$E_1_0.instance, context));
      varScope.addSVar("DR__DUMMY_0_0", new InteropSDefT($D$R__$D$U$M$M$Y_0_0.instance, context));
      compiledContext.typeInfo.registerConstructor(new Sort("ATerm", Arrays.asList()), "DR_UNDEFINE", Arrays.asList(new Sort("ATerm", Arrays.asList())));
      compiledContext.typeInfo.registerConstructor(new Sort("ATerm", Arrays.asList()), "DR_DUMMY", Arrays.asList());
      compiledContext.typeInfo.finishRegistration();
    }

    private void registerLazy(org.spoofax.interpreter.core.IContext context, Context compiledContext, ClassLoader classLoader, org.spoofax.interpreter.core.VarScope varScope)
    { 
      compiledContext.registerComponent("tuple1.opt2");
      tuple1.opt2.init(compiledContext);
      varScope.addSVar("s3_0_0", new InteropSDefT(classLoader, "tuple1.opt2$s3_0_0", context));
      varScope.addSVar("Anno__Cong_____2_0", new InteropSDefT(classLoader, "tuple1.opt2$$Anno__$Cong_____2_0", context));
      varScope.addSVar("DR__UNDEFINE_1_0", new InteropSDefT(classLoader, "tuple1.opt2$$D$R__$U$N$D$E$F$I$N$E_1_0", context));
      varScope.addSVar("DR__DUMMY_0_0", new InteropSDefT(classLoader, "tuple1.opt2$$D$R__$D$U$M$M$Y_0_0", context));
      compiledContext.typeInfo.registerConstructor(new Sort("ATerm", Arrays.asList()), "DR_UNDEFINE", Arrays.asList(new Sort("ATerm", Arrays.asList())));
      compiledContext.typeInfo.registerConstructor(new Sort("ATerm", Arrays.asList()), "DR_DUMMY", Arrays.asList());
    }
  }
}