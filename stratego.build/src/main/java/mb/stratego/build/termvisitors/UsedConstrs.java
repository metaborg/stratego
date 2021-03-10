package mb.stratego.build.termvisitors;

import java.util.Set;
import java.util.function.BiFunction;

import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.data.ConstructorSignature;

public class UsedConstrs extends TermVisitor {
    private final ITermFactory tf = new TermFactory();

    protected final Set<ConstructorSignature> usedConstructors;

    public UsedConstrs(Set<ConstructorSignature> usedConstructors) {
        this.usedConstructors = usedConstructors;
    }

    @Override public void preVisit(IStrategoTerm term) {
        registerConsUse(term);
    }

    void registerConsUse(IStrategoTerm term) {
        if(TermUtils.isAppl(term, "Op", 2)) {
            if(TermUtils.isString(term.getSubterm(0))) {
                final IStrategoString nameAST = TermUtils.toStringAt(term, 0);
                if(!nameAST.stringValue().isEmpty()) {
                    final int arity = TermUtils.toListAt(term, 1).size();
                    IStrategoInt noArgs = new StrategoInt(arity);
                    usedConstructors.add(new ConstructorSignature(nameAST, noArgs));
                }
            } else {
                final IStrategoString nameAST = TermUtils.toStringAt(term.getSubterm(0), 0);
                final IStrategoString escapedNameAST = (IStrategoString) tf
                    .replaceTerm(tf.makeString(strategoEscape(nameAST.stringValue())), nameAST);
                final int arity = TermUtils.toListAt(term, 1).size();
                IStrategoInt noArgs = new StrategoInt(arity);
                usedConstructors.add(new ConstructorSignature(escapedNameAST, noArgs));
            }
        } else if(TermUtils.isAppl(term, "CongQ", 2)) {
            final IStrategoString nameAST = TermUtils.toStringAt(term, 0);
            final int arity = TermUtils.toListAt(term, 1).size();
            IStrategoInt noArgs = new StrategoInt(arity);
            usedConstructors.add(new ConstructorSignature(nameAST, noArgs));
        }
    }

    private String strategoEscape(String s) {
        //@formatter:off
        return s
            .replace("\"", "\\\"")
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
        //@formatter:on
    }
}
