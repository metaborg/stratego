package mb.stratego.build.util;

import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoPlaceholder;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.AbstractWrappedTermFactory;

/**
 * This TermFactory is similar to {@link org.spoofax.terms.TermFactory}, but has a local usedStrings set. Therefore the
 * usedStrings will not be shared by different instances of the factory, which is used by the separate compiler for
 * Stratego to make newname only locally unique to each file that is processed. The base factory is still called to
 * build a string so it can record its usage. But the locally built IStrategoString is returned.
 */
public class LocallyUniqueStringTermFactory extends AbstractWrappedTermFactory {
    private static final int MAX_POOLED_STRING_LENGTH = 200;
    private final Set<String> usedStrings = new HashSet<>();
    private final ITermFactory baseFactory;

    public LocallyUniqueStringTermFactory(ITermFactory baseFactory) {
        super(baseFactory);
        this.baseFactory = baseFactory;
    }

    public LocallyUniqueStringTermFactory() {
        this(new TermFactory());
    }

    @Override public IStrategoString makeString(String s) {
        final IStrategoString string = new StrategoString(s, null);
        if(s.length() <= MAX_POOLED_STRING_LENGTH && TermFactory.endsWithDigit(s)) {
            synchronized(usedStrings) {
                usedStrings.add(s);
            }
        }
        // Run the base factory too in case it wants to register strings
        baseFactory.makeString(s);
        return string;
    }

    @Override public @Nullable IStrategoString tryMakeUniqueString(String name) {
        synchronized(usedStrings) {
            if(usedStrings.contains(name)) {
                return null;
            } else if(name.length() > MAX_POOLED_STRING_LENGTH) {
                throw new UnsupportedOperationException(
                    "String too long to be pooled (newname not allowed): " + name);
            } else {
                return makeString(name);
            }
        }
    }

    // AbstractWrappedTermFactory doesn't override the replace* methods, just the make* ones :(
    @Override public IStrategoAppl replaceAppl(IStrategoConstructor constructor,
        IStrategoTerm[] kids, IStrategoAppl old) {
        return baseFactory.replaceAppl(constructor, kids, old);
    }

    @Override public IStrategoList replaceList(IStrategoTerm[] kids, IStrategoList old) {
        return baseFactory.replaceList(kids, old);
    }

    @Override public IStrategoList replaceListCons(IStrategoTerm head, IStrategoList tail,
        IStrategoTerm oldHead, IStrategoList oldTail) {
        return baseFactory.replaceListCons(head, tail, oldHead, oldTail);
    }

    @Override public IStrategoTerm replaceTerm(IStrategoTerm term, IStrategoTerm old) {
        return baseFactory.replaceTerm(term, old);
    }

    @Override public IStrategoTuple replaceTuple(IStrategoTerm[] kids, IStrategoTuple old) {
        return baseFactory.replaceTuple(kids, old);
    }

    @Override public IStrategoPlaceholder replacePlaceholder(IStrategoTerm template,
        IStrategoPlaceholder old) {
        return baseFactory.replacePlaceholder(template, old);
    }

    public void clearUsedStrings() {
        usedStrings.clear();
    }
}
