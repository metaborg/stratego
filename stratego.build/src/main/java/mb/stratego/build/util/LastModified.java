package mb.stratego.build.util;

import java.io.Serializable;
import java.util.function.Function;

public class LastModified<T> implements Serializable, WithLastModified {
    public final T wrapped;
    public final long lastModified;

    public LastModified(T wrapped, long lastModified) {
        this.wrapped = wrapped;
        this.lastModified = lastModified;
    }

    public static <T> LastModified<T> fromParent(T wrapped, LastModified<?> parent) {
        return new LastModified<>(wrapped, parent.lastModified);
    }

    @Override public long lastModified() {
        return lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        LastModified<?> that = (LastModified<?>) o;

        if(lastModified != that.lastModified)
            return false;
        return wrapped.equals(that.wrapped);
    }

    @Override public int hashCode() {
        int result = wrapped.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "LastModified{"
            + "wrapped=" + wrapped
            + ", lastModified=" + lastModified
            + '}';
        //@formatter:on
    }

    public String toString(Function<T, String> toString) {
        //@formatter:off
        return "LastModified{"
            + "wrapped=" + toString.apply(wrapped)
            + ", lastModified=" + lastModified
            + '}';
        //@formatter:on
    }
}
