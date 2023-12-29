package benchmark.generic;

import api.Compiler;

public abstract class Program<C extends Compiler<?>> {
    public C compiler;

    public final void cleanup() {
        compiler.cleanup();
    }
}