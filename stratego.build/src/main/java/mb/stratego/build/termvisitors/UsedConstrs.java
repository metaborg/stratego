package mb.stratego.build.termvisitors;

import java.util.HashSet;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.ConstructorSignature;
import mb.stratego.build.util.StringSetWithPositions;

public class UsedConstrs extends TermVisitor {
    private final StringSetWithPositions usedConstrs;
    private final ITermFactory tf = new TermFactory();

    private final Set<ConstructorSignature> usedConstructors;

    public UsedConstrs(StringSetWithPositions usedConstrs) {
        this.usedConstrs = usedConstrs;
        this.usedConstructors = new HashSet<>();
    }

    public UsedConstrs(Set<ConstructorSignature> usedConstructors) {
        this.usedConstrs = new StringSetWithPositions();
        this.usedConstructors = usedConstructors;
    }

    @Override public void preVisit(IStrategoTerm term) {
        registerConsUse(term);
    }

    void registerConsUse(IStrategoTerm term) {
        if(TermUtils.isAppl(term, "Op", 2)) {
            if(TermUtils.isString(term.getSubterm(0))) {
                final IStrategoString nameAST = TermUtils.toStringAt(term, 0);
                final String name = nameAST.stringValue();
                if(!name.isEmpty()) {
                    final int arity = TermUtils.toListAt(term, 1).size();
                    final IStrategoString cifiedName =
                        B.string(name + "_" + arity);
                    tf.copyAttachments(nameAST, cifiedName);
                    usedConstrs.add(cifiedName);
                    usedConstructors.add(new ConstructorSignature(name, arity));
                }
            } else {
                final IStrategoString nameAST = TermUtils.toStringAt(term.getSubterm(0), 0);
                final String name = strategoEscape(nameAST.stringValue());
                final int arity = TermUtils.toListAt(term, 1).size();
                final IStrategoString cifiedName = B.string(name + "_" + arity);
                tf.copyAttachments(nameAST, cifiedName);
                usedConstrs.add(cifiedName);
                usedConstructors.add(new ConstructorSignature(name, arity));
            }
        } else if(TermUtils.isAppl(term, "CongQ", 2)) {
            final String name = TermUtils.toJavaStringAt(term, 0);
            final int arity = TermUtils.toListAt(term, 1).size();
            usedConstructors.add(new ConstructorSignature(name, arity));
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
