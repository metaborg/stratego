package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;

import mb.resource.hierarchical.ResourcePath;

/**
 * Data class that contains a path to the .str2lib file that describes the Stratego 2 library, and
 * jar files and/or directories with class files that should be copied by the Stratego compiler so
 * they are included in the final result of compilation.
 * These jar files may include libraries that are not the product of the Stratego 2 compiler but are
 * used by the compiled Stratego code. At the same time, it is not required to provide the class
 * files or jar of the Stratego 2 library, but then they should be added to the classpath when
 * compiling the Java files that the Stratego 2 compiler outputs. 
 */
public class Stratego2LibInfo implements Serializable {
    public final ResourcePath str2libFile;
    public final ArrayList<ResourcePath> jarFilesOrDirectories;

    public Stratego2LibInfo(ResourcePath str2libFile, ArrayList<ResourcePath> jarFilesOrDirectories) {
        this.str2libFile = str2libFile;
        this.jarFilesOrDirectories = jarFilesOrDirectories;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Stratego2LibInfo that = (Stratego2LibInfo) o;

        if(!str2libFile.equals(that.str2libFile))
            return false;
        return jarFilesOrDirectories.equals(that.jarFilesOrDirectories);
    }

    @Override public int hashCode() {
        int result = str2libFile.hashCode();
        result = 31 * result + jarFilesOrDirectories.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Stratego2Lib(" + str2libFile + ", " + jarFilesOrDirectories + ')';
    }
}
