import org.strategoxt.stratego_lib.*;
import org.strategoxt.lang.*;
import org.strategoxt.lang.gradual.*;
import org.spoofax.interpreter.terms.*;
import static org.strategoxt.lang.Term.*;
import org.spoofax.interpreter.library.AbstractPrimitive;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.ref.WeakReference;

@SuppressWarnings("all") public class test_mixed
{
    protected static final boolean TRACES_ENABLED = true;

    protected static ITermFactory constantFactory;

    private static WeakReference<Context> initedContext;

    private static boolean isIniting;

    protected static IStrategoTerm const2396;

    protected static IStrategoTerm const2395;

    protected static IStrategoTerm const2394;

    protected static IStrategoTerm const2393;

    protected static IStrategoTerm const2392;

    public static IStrategoConstructor _consConc_2;

    public static IStrategoConstructor _consNone_0;

    public static IStrategoConstructor _consSome_1;

    public static IStrategoConstructor _consCar2_1;

    public static IStrategoConstructor _consCar1_1;

    public static Context init(Context context)
    {
        synchronized(test_mixed.class)
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
                    context.registerComponent("test_mixed");
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
        _consCar2_1 = termFactory.makeConstructor("Car2", 1);
        _consCar1_1 = termFactory.makeConstructor("Car1", 1);
    }

    public static void initConstants(ITermFactory termFactory)
    {
        const2392 = termFactory.makeInt(1);
        const2393 = termFactory.makeInt(2);
        const2394 = termFactory.makeString("cba");
        const2395 = termFactory.makeInt(54);
        const2396 = termFactory.makeInt(8);
    }

    @SuppressWarnings("all") public static class uncar2_0_0 extends Strategy
    {
        public static uncar2_0_0 instance = new uncar2_0_0();

        @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
        {
            context.push("uncar2_0_0");
            Fail22670:
            {
                switch(term.getType())
                {
                    case APPL:
                    {
                        switch(((IStrategoConstructor)((IStrategoAppl)((IStrategoAppl)term).getConstructor()).getConstructor()).getArity())
                        {
                            case 1:
                            {
                                switch(((IStrategoConstructor)((IStrategoAppl)((IStrategoAppl)term).getConstructor()).getConstructor()).getName())
                                {
                                    case "Car1":
                                    {
                                        term = $Cons_2_0.instance.invoke(context, term, uncar2_0_0_lifted0.instance, $Nil_0_0.instance);
                                        if(term == null)
                                            break Fail22670;
                                        break;
                                    }

                                    case "Car2":
                                    {
                                        term = $Cons_2_0.instance.invoke(context, term, uncar2_0_0_lifted2.instance, $Nil_0_0.instance);
                                        if(term == null)
                                            break Fail22670;
                                        break;
                                    }

                                    default:
                                    {
                                        if(true)
                                            break Fail22670;
                                        break;
                                    }
                                }
                                break;
                            }

                            default:
                            {
                                if(true)
                                    break Fail22670;
                                break;
                            }
                        }
                        break;
                    }

                    case STRING:
                    {
                        if(((IStrategoString)term).stringValue().equals("abc"))
                        {
                            term = test_mixed.const2394;
                        }
                        else
                        {
                            if(true)
                                break Fail22670;
                        }
                        break;
                    }

                    case INT:
                    {
                        if(((IStrategoInt)term).intValue() == 8)
                        {
                            term = test_mixed.const2395;
                        }
                        else
                        {
                            if(true)
                                break Fail22670;
                        }
                        break;
                    }

                    case REAL:
                    {
                        if(((IStrategoReal)term).realValue() == 5.4)
                        {
                            term = test_mixed.const2396;
                        }
                        else
                        {
                            if(true)
                                break Fail22670;
                        }
                        break;
                    }

                    default:
                    {
                        if(true)
                            break Fail22670;
                        break;
                    }
                }
                context.popOnSuccess();
                if(true)
                    return term;
            }
            context.popOnFailure();
            return null;
        }
    }

    @SuppressWarnings("all") private static final class uncar2_0_0_lifted2 extends Strategy
    {
        public static final uncar2_0_0_lifted2 instance = new uncar2_0_0_lifted2();

        @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
        {
            Fail22671:
            {
                term = test_mixed.const2393;
                if(true)
                    return term;
            }
            return null;
        }
    }

    @SuppressWarnings("all") private static final class uncar2_0_0_lifted0 extends Strategy
    {
        public static final uncar2_0_0_lifted0 instance = new uncar2_0_0_lifted0();

        @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
        {
            Fail22672:
            {
                term = test_mixed.const2392;
                if(true)
                    return term;
            }
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
            compiledContext.registerComponent("test_mixed");
            test_mixed.init(compiledContext);
            varScope.addSVar("uncar2_0_0", new InteropSDefT(uncar2_0_0.instance, context));
            compiledContext.typeInfo.registerConstructor(new Sort("List", Arrays.asList(new SortVar("a"))), "Car1", Arrays.asList(new SortVar("a")));
            compiledContext.typeInfo.registerConstructor(new Sort("List", Arrays.asList(new SortVar("a"))), "Car2", Arrays.asList(new SortVar("a")));
            compiledContext.typeInfo.finishRegistration();
        }

        private void registerLazy(org.spoofax.interpreter.core.IContext context, Context compiledContext, ClassLoader classLoader, org.spoofax.interpreter.core.VarScope varScope)
        {
            compiledContext.registerComponent("test_mixed");
            test_mixed.init(compiledContext);
            varScope.addSVar("uncar2_0_0", new InteropSDefT(classLoader, "test_mixed$uncar2_0_0", context));
            compiledContext.typeInfo.registerConstructor(new Sort("List", Arrays.asList(new SortVar("a"))), "Car1", Arrays.asList(new SortVar("a")));
            compiledContext.typeInfo.registerConstructor(new Sort("List", Arrays.asList(new SortVar("a"))), "Car2", Arrays.asList(new SortVar("a")));
            compiledContext.typeInfo.finishRegistration();
        }
    }
}