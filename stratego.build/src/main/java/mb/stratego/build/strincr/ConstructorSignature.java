package mb.stratego.build.strincr;

import static org.spoofax.interpreter.core.Interpreter.cify;

import javax.annotation.Nullable;

import org.spoofax.interpreter.stratego.SDefT;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTermBuilder;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.StringUtils;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.util.WithLastModified;

public class ConstructorSignature extends StrategoTuple implements WithLastModified {
    public final String name;
    public final int noArgs;
    public final long lastModified;

    public ConstructorSignature(String name, int noArgs, long lastModified) {
        super(new IStrategoTerm[] { new StrategoString(name, AbstractTermFactory.EMPTY_LIST),
            new StrategoInt(noArgs) }, AbstractTermFactory.EMPTY_LIST);
        this.name = name;
        this.noArgs = noArgs;
        this.lastModified = lastModified;
    }

    public ConstructorSignature(IStrategoString name, IStrategoInt noArgs, long lastModified) {
        super(new IStrategoTerm[] { name, noArgs }, AbstractTermFactory.EMPTY_LIST);
        this.name = name.stringValue();
        this.noArgs = noArgs.intValue();
        this.lastModified = lastModified;
    }

    @Override protected boolean doSlowMatch(IStrategoTerm second) {
        if(this.getClass() == ConstructorSignature.class && second.getClass() == ConstructorSignature.class) {
            if(((ConstructorSignature) second).lastModified != lastModified) {
                return false;
            }
        }
        return super.doSlowMatch(second);
    }

    public String cifiedName() {
        return cify(name) + "_" + noArgs;
    }

    public IStrategoTerm standardType(ITermFactory tf) {
        final IStrategoAppl dyn = tf.makeAppl("DynT", tf.makeAppl("Dyn"));
        final IStrategoList.Builder sargTypes = tf.arrayListBuilder(noArgs);
        for(int i = 0; i < noArgs; i++) {
            sargTypes.add(dyn);
        }
        return tf.makeAppl("ConstrType", tf.makeList(sargTypes), dyn);
    }

    public static boolean isCified(String name) {
        try {
            int lastUnderlineOffset = name.lastIndexOf('_');
            if(lastUnderlineOffset == -1) {
                return false;
            }
            Integer.parseInt(name.substring(lastUnderlineOffset + 1));
        } catch(RuntimeException e) {
            return false;
        }
        return true;
    }

    public static @Nullable ConstructorSignature fromCified(String cifiedName, long lastModified) {
        try {
            int lastUnderlineOffset = cifiedName.lastIndexOf('_');
            if(lastUnderlineOffset == -1) {
                return null;
            }
            int arity = Integer.parseInt(cifiedName.substring(lastUnderlineOffset + 1));
            return new ConstructorSignature(
                SDefT.unescape(cifiedName.substring(0, lastUnderlineOffset)), arity, lastModified);
        } catch(NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable ConstructorSignature fromTuple(IStrategoTerm tuple, long lastModified) {
        if(!TermUtils.isTuple(tuple) || tuple.getSubtermCount() != 2 || !TermUtils
            .isIntAt(tuple, 1)) {
            return null;
        }
        if(TermUtils.isStringAt(tuple, 0)) {
            return new ConstructorSignature(TermUtils.toStringAt(tuple, 0),
                TermUtils.toIntAt(tuple, 1), lastModified);
        }
        if(TermUtils.isApplAt(tuple, 0) && TermUtils.tryGetName(tuple.getSubterm(0))
            .map(n -> n.equals("Q")).orElse(false)) {
            final String escapedNameString =
                StringUtils.escape(TermUtils.toStringAt(tuple.getSubterm(0), 0).stringValue());
            final StrategoString escapedName =
                new StrategoString(escapedNameString, AbstractTermFactory.EMPTY_LIST);
            AbstractTermFactory.staticCopyAttachments(tuple.getSubterm(0), escapedName);
            return new ConstructorSignature(escapedName, TermUtils.toIntAt(tuple, 1), lastModified);
        }
        return null;
    }

    public static @Nullable ConstructorSignature fromTerm(IStrategoTerm consDef,
        long lastModified) {
        if(!TermUtils.isAppl(consDef)) {
            return null;
        }
        final IStrategoString name;
        switch(TermUtils.toAppl(consDef).getName()) {
            case "OpDeclQ":
                // fall-through
            case "ExtOpDeclQ":
                final String escapedNameString = StringUtils
                    .escape(TermUtils.toStringAt(consDef.getSubterm(0), 0).stringValue());
                name = new StrategoString(escapedNameString, AbstractTermFactory.EMPTY_LIST);
                AbstractTermFactory.staticCopyAttachments(consDef.getSubterm(0), name);
                break;
            case "OpDecl":
                // fall-through
            case "ExtOpDecl":
                name = TermUtils.toStringAt(consDef, 0);
                break;
            case "OpDeclInj":
                // fall-through
            case "ExtOpDeclInj":
                // fall-through
            default:
                return null;
        }

        final IStrategoAppl type = TermUtils.toApplAt(consDef, 1);
        final IStrategoInt arity;

        switch(type.getName()) {
            case "ConstType":
                arity = B.integer(0);
                break;
            case "FunType":
                arity = B.integer(TermUtils.toListAt(type, 0).size());
                break;
            default:
                return null;
        }

        return new ConstructorSignature(name, arity, lastModified);
    }

    public StrategySignature toCongruenceSig() {
        return new StrategySignature(name, noArgs, 0);
    }

    @Override public long lastModified() {
        return lastModified;
    }

    public IStrategoAppl congruenceAst(IStrategoTermBuilder tf) {
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
        final IStrategoAppl aTermConstType =
            tf.makeAppl("ConstType", tf.makeAppl("SortNoArgs", tf.makeString("ATerm")));
        final IStrategoAppl defaultStrat =
            tf.makeAppl("FunType", tf.makeList(aTermConstType), aTermConstType);
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
            varDecs[i] = tf.makeAppl("VarDec", sVar, defaultStrat);
        } locals[2 * noArgs] = tf.makeString("w");
        locals[2 * noArgs + 1] = tf.makeString("a");
        // @formatter:off
        IStrategoAppl tail = tf.makeAppl("Seq",
            tf.makeAppl("Build", tf.makeAppl("Var", tf.makeString("w"))),
            tf.makeAppl("Build",
                tf.makeAppl("Anno",
                    tf.makeAppl("Op",
                        tf.makeString(name),
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
            tf.makeString(toCongruenceSig().cifiedName()),
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
                                    tf.makeString(name),
                                    tf.makeList(matchVars)),
                                tf.makeAppl("Var", tf.makeString("a")))),
                        tail))));
        // @formatter:on
    }

    public static IStrategoAppl annoCongAst(IStrategoTermBuilder tf) {
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
        final IStrategoAppl aTermConstType =
            tf.makeAppl("ConstType", tf.makeAppl("SortNoArgs", tf.makeString("ATerm")));
        final IStrategoAppl defaultStrat =
            tf.makeAppl("FunType", tf.makeList(aTermConstType), aTermConstType);
        // @formatter:off
        return tf.makeAppl("SDefT",
            tf.makeString("Anno__Cong_____2_0"),
            tf.makeList(
                tf.makeAppl("VarDec", tf.makeString("f1"), defaultStrat),
                tf.makeAppl("VarDec", tf.makeString("f2"), defaultStrat)),
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
}
