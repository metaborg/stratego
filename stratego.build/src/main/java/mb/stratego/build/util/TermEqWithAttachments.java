package mb.stratego.build.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.ITermAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.attachments.TermAttachmentType;

public class TermEqWithAttachments implements IStrategoTerm {
    public final IStrategoTerm term;

    public TermEqWithAttachments(IStrategoTerm term) {
        if(term instanceof TermEqWithAttachments) {
            this.term = ((TermEqWithAttachments) term).term;
        } else {
            this.term = term;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        TermEqWithAttachments that = (TermEqWithAttachments) o;

        return equalsWithAttachments(term, that.term);
    }

    @Override
    public int hashCode() {
        final @Nullable OriginAttachment origin = term.getAttachment(OriginAttachment.TYPE);
        final @Nullable ImploderAttachment location = term.getAttachment(ImploderAttachment.TYPE);
        int result = term.hashCode();
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    public static boolean equalsWithAttachments(@Nullable IStrategoTerm t1, @Nullable IStrategoTerm t2) {
        if(t1 == t2) {
            return true;
        }
        if(t1 == null || t2 == null) {
            return false;
        }
        final TermType termType = t1.getType();
        final TermType type2 = t2.getType();
        if(termType != type2) {
            return false;
        }

        final @Nullable OriginAttachment oa1 = t1.getAttachment(OriginAttachment.TYPE);
        final @Nullable OriginAttachment oa2 = t2.getAttachment(OriginAttachment.TYPE);
        final @Nullable IStrategoTerm origin1 = oa1 == null ? null : oa1.getOrigin();
        final @Nullable IStrategoTerm origin2 = oa2 == null ? null : oa2.getOrigin();
        if(!equalsWithAttachments(origin1, origin2)) {
            return false;
        }

        final @Nullable ImploderAttachment ia1 = t1.getAttachment(ImploderAttachment.TYPE);
        final @Nullable ImploderAttachment ia2 = t2.getAttachment(ImploderAttachment.TYPE);
        if(!Objects.equals(ia1, ia2)) {
            return false;
        }

        if(!equalsWithAttachmentsList(t1.getAnnotations(), t2.getAnnotations())) {
            return false;
        }

        switch(termType) {
            case APPL:
                return equalsWithAttachmentsAppl((IStrategoAppl) t1, (IStrategoAppl) t2);
            case LIST:
            case TUPLE:
                return equalsWithAttachmentsList(t1, t2);
            case INT:
                return ((IStrategoInt) t1).intValue() == ((IStrategoInt) t2).intValue();
            case REAL:
                //noinspection FloatingPointEquality
                return ((IStrategoReal) t1).realValue() == ((IStrategoReal) t2).realValue();
            case STRING:
                return ((IStrategoString) t1).stringValue().equals(((IStrategoString) t2).stringValue());
            case BLOB:
                return t1.match(t2);
            default:
                throw new IllegalStateException("Unknown term type: " + termType);
        }
    }

    private static boolean equalsWithAttachmentsAppl(IStrategoAppl appl1, IStrategoAppl appl2) {
        IStrategoConstructor cons1 = appl1.getConstructor();
        IStrategoConstructor cons2 = appl2.getConstructor();
        if(cons1 != cons2 && !cons1.match(cons2))
            return false;
        return equalsWithAttachmentsList(appl1, appl2);
    }

    private static boolean equalsWithAttachmentsList(IStrategoTerm term1, IStrategoTerm term2) {
        IStrategoTerm[] children1 = term1.getAllSubterms();
        IStrategoTerm[] children2 = term2.getAllSubterms();
        if(children1.length != children2.length)
            return false;
        for(int i = 0; i < children1.length; i++) {
            if(!equalsWithAttachments(children1[i], children2[i]))
                return false;
        }
        return true;
    }

    @Override
    public int getSubtermCount() {
        return term.getSubtermCount();
    }

    @Override
    public IStrategoTerm getSubterm(int index) {
        return term.getSubterm(index);
    }

    @Override
    public IStrategoTerm[] getAllSubterms() {
        return term.getAllSubterms();
    }

    @Override
    public List<IStrategoTerm> getSubterms() {
        return term.getSubterms();
    }

    @Override
    public int getTermType() {
        return term.getTermType();
    }

    @Override
    public TermType getType() {
        return term.getType();
    }

    @Override
    public IStrategoList getAnnotations() {
        return term.getAnnotations();
    }

    @Override
    public boolean match(IStrategoTerm second) {
        return term.match(second);
    }

    @Override
    public void prettyPrint(ITermPrinter pp) {
        term.prettyPrint(pp);
    }

    @Override
    public String toString(int maxDepth) {
        return term.toString(maxDepth);
    }

    @Override
    public void writeAsString(Appendable output) throws IOException {
        term.writeAsString(output);
    }

    @Override
    public void writeAsString(Appendable output, int maxDepth) throws IOException {
        term.writeAsString(output, maxDepth);
    }

    @Override
    public Iterator<IStrategoTerm> iterator() {
        return term.iterator();
    }

    @Override
    @Nullable
    public <T extends ITermAttachment> T getAttachment(@Nullable TermAttachmentType<T> type) {
        return term.getAttachment(type);
    }

    @Override
    public void putAttachment(ITermAttachment resourceAttachment) {
        term.putAttachment(resourceAttachment);
    }

    @Override
    @Nullable
    public ITermAttachment removeAttachment(TermAttachmentType<?> attachmentType) {
        return term.removeAttachment(attachmentType);
    }

    @Override
    public boolean isList() {
        return term.isList();
    }

}
