package mb.stratego.build.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.util.B;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.StrategySignature;

public class GenerateStratego {
    private final ITermFactory tf;
    private final IStrategoTerm newSVar;
    private final IStrategoTerm newTVar;

    public final ConstructorSignature dr_dummy =
        new ConstructorSignature(new StrategoString("DR_DUMMY", null), new StrategoInt(0));
    public final ConstructorSignature dr_undefine =
        new ConstructorSignature(new StrategoString("DR_UNDEFINE", null), new StrategoInt(1));
    public final ConstructorSignature anno_cong__ =
        new ConstructorSignature(new StrategoString("Anno_Cong__", null), new StrategoInt(2));

    public final IStrategoTerm dr_dummyTerm;
    public final IStrategoTerm dr_undefineTerm;
    public final IStrategoTerm anno_cong__Term;

    @Inject public GenerateStratego(StrIncrContext context) {
        this.tf = context.getFactory();
        final IStrategoAppl aTerm = tf.makeAppl("Sort", B.string("ATerm"), B.list());
        newSVar = tf.makeAppl("VarDec", B.string("a"), tf.makeAppl("FunType", aTerm, aTerm));
        newTVar = tf.makeAppl("VarDec", B.string("a"), tf.makeAppl("ConstType", aTerm));
        dr_dummyTerm = dr_dummy.toTerm(tf);
        dr_undefineTerm = dr_undefine.toTerm(tf);
        anno_cong__Term = anno_cong__.toTerm(tf);
    }

    public List<IStrategoAppl> declStubs(Collection<StrategySignature> strategySignatures) {
        final List<IStrategoAppl> decls = new ArrayList<>(strategySignatures.size());
        for(StrategySignature sig : strategySignatures) {
            decls.add(sdefStub(tf, sig.cifiedName(), sig.noStrategyArgs, sig.noTermArgs));
        }
        return decls;
    }

    private IStrategoAppl sdefStub(ITermFactory tf, String strategyName, int svars, int tvars) {
        final IStrategoAppl newBody = tf.makeAppl("Id");
        final IStrategoTerm name = tf.makeString(strategyName);

        final IStrategoTerm[] newSVarArray = new IStrategoTerm[svars];
        Arrays.fill(newSVarArray, newSVar);
        final IStrategoTerm newSVars = B.list(newSVarArray);

        final IStrategoTerm[] newTVarArray = new IStrategoTerm[tvars];
        Arrays.fill(newTVarArray, newTVar);
        final IStrategoTerm newTVars = B.list(newTVarArray);

        return tf.makeAppl("SDefT", name, newSVars, newTVars, newBody);
    }

    public @Nullable IStrategoAppl dynamicCallsDefinition(
        Collection<String> dynamicRulesNewGenerated,
        Collection<String> dynamicRulesUndefineGenerated) {
        @Nullable IStrategoAppl body = null;
        final IStrategoAppl id = tf.makeAppl("Id");
        final IStrategoAppl emptyStringLit =
            tf.makeAppl("Anno", tf.makeAppl("Str", tf.makeString("")),
                tf.makeAppl("Op", tf.makeString("Nil"), tf.makeList()));

        /* concrete syntax:
         *   new-[dr-rule-name](|"", "")
         * abstract syntax, desugared and name mangled:
         *   CallT(
         *     "new_[dr-rule-name]_0_2",
         *     [],
         *     [Anno(Str("\"\""), Op("Nil", [])), Anno(Str("\"\""), Op("Nil", []))])
         * strung together with `[call] <+ [other-calls]` or `GuardedLChoice([call], Id(), [other-calls])`
         */
        for(String dynamicRuleName : dynamicRulesNewGenerated) {
            final String drRuleNameNew = Interpreter.cify("new-" + dynamicRuleName) + "_0_2";
            final IStrategoAppl call =
                tf.makeAppl("CallT", tf.makeAppl("SVar", tf.makeString(drRuleNameNew)),
                    tf.makeList(), tf.makeList(emptyStringLit, emptyStringLit));
            if(body == null) {
                body = call;
            } else {
                body = tf.makeAppl("GuardedLChoice", call, id, body);
            }
        }

        /* concrete syntax:
         *   undefine-[dr-rule-name](|"")
         * abstract syntax, desugared and name mangled:
         *   CallT("undefine_[dr-rule-name]_0_1", [], [Anno(Str("\"\""), Op("Nil", []))])
         * strung together with `[call] <+ [other-calls]` or `GuardedLChoice([call], Id(), [other-calls])`
         */
        for(String dynamicRuleName : dynamicRulesUndefineGenerated) {
            final String drRuleNameNew = Interpreter.cify("undefine-" + dynamicRuleName) + "_0_1";
            final IStrategoAppl call =
                tf.makeAppl("CallT", tf.makeAppl("SVar", tf.makeString(drRuleNameNew)),
                    tf.makeList(), tf.makeList(emptyStringLit));
            if(body == null) {
                body = call;
            } else {
                body = tf.makeAppl("GuardedLChoice", call, id, body);
            }
        }
        if(body == null) {
            return null;
        }

        final String dynamicCalls = Interpreter.cify("DYNAMIC_CALLS") + "_0_0";
        return tf
            .makeAppl("SDefT", tf.makeString(dynamicCalls), tf.makeList(), tf.makeList(), body);
    }
}
