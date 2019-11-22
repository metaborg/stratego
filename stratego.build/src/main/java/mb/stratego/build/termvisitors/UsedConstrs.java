package mb.stratego.build.termvisitors;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.attachments.OriginAttachment;

import mb.flowspec.terms.B;
import mb.stratego.build.util.StringSetWithPositions;

public class UsedConstrs extends TermVisitor {
    private final StringSetWithPositions usedConstrs;

    public UsedConstrs(StringSetWithPositions usedConstrs) {
        this.usedConstrs = usedConstrs;
    }

    @Override public void preVisit(IStrategoTerm term) {
        registerConsUse(term);
    }

    void registerConsUse(IStrategoTerm term) {
        if(Tools.isTermAppl(term) && Tools.hasConstructor((IStrategoAppl) term, "Op", 2)) {
            if(Tools.isTermString(term.getSubterm(0))) {
                final IStrategoString nameAST = Tools.stringAt(term, 0);
                final String name = nameAST.stringValue();
                if(!name.equals("")) {
                    final IStrategoString cifiedName =
                        B.string(name + "_" + Tools.listAt(term, 1).size());
                    cifiedName.putAttachment(nameAST.getAttachment(OriginAttachment.TYPE));
                    usedConstrs.add(cifiedName);
                }
            } else {
                final IStrategoString nameAST = Tools.stringAt(Tools.<IStrategoTerm>termAt(term, 0), 0);
                final IStrategoString cifiedName = B.string(strategoEscape(nameAST.stringValue()) + Tools.listAt(term, 1).size());
                cifiedName.putAttachment(nameAST.getAttachment(OriginAttachment.TYPE));
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
