package mb.stratego.build.termvisitors;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.util.StringSetWithPositions;

public class UsedConstrs extends TermVisitor {
    private final StringSetWithPositions usedConstrs;
    private final ITermFactory tf = new TermFactory();

    public UsedConstrs(StringSetWithPositions usedConstrs) {
        this.usedConstrs = usedConstrs;
    }

    @Override public void preVisit(IStrategoTerm term) {
        registerConsUse(term);
    }

    void registerConsUse(IStrategoTerm term) {
        if(TermUtils.isAppl(term) && TermUtils.isAppl(term, "Op", 2)) {
            if(TermUtils.isString(term.getSubterm(0))) {
                final IStrategoString nameAST = TermUtils.toStringAt(term, 0);
                final String name = nameAST.stringValue();
                if(!name.equals("")) {
                    final IStrategoString cifiedName =
                        B.string(name + "_" + TermUtils.toListAt(term, 1).size());
                    tf.copyAttachments(nameAST, cifiedName);
                    usedConstrs.add(cifiedName);
                }
            } else {
                final IStrategoString nameAST = TermUtils.toStringAt(term.getSubterm(0), 0);
                final IStrategoString cifiedName = B.string(strategoEscape(nameAST.stringValue()) + TermUtils.toListAt(term, 1).size());
                tf.copyAttachments(nameAST, cifiedName);
                usedConstrs.add(cifiedName);
            }
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
