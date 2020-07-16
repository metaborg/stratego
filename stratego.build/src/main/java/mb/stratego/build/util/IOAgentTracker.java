package mb.stratego.build.util;

import org.spoofax.interpreter.library.IOAgent;

public interface IOAgentTracker {
    IOAgent agent();

    String stdout();

    String stderr();
}
