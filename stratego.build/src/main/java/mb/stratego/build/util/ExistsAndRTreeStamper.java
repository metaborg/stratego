package mb.stratego.build.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import jakarta.annotation.Nullable;

import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.resource.ValueResourceStamp;
import mb.resource.ReadableResource;

public class ExistsAndRTreeStamper<R extends ReadableResource> implements ResourceStamper<R> {
    @Override public ValueResourceStamp<R> stamp(R resource) throws IOException {
        return new ValueResourceStamp<>(resource.exists() && isLibraryRTree(resource), this);
    }

    @Override public boolean equals(@Nullable Object o) {
        return this == o || o != null && this.getClass() == o.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "ExistsAndRTreeStamper()";
    }

    /**
     * Check if file starts with Specification/1 instead of Module/2
     *
     * @param rtreeFile Path to the file
     * @return if file starts with Specification/1
     * @throws IOException on file system trouble
     */
    public static boolean isLibraryRTree(ReadableResource rtreeFile) throws IOException {
        char[] chars = new char[4];
        try(BufferedReader r = new BufferedReader(new InputStreamReader(rtreeFile.openRead()))) {
            return r.read(chars) != -1 && Arrays.equals(chars, "Spec".toCharArray());
        }
    }
}
