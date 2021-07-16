package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;

import mb.resource.hierarchical.ResourcePath;

public class Stratego2LibInfo implements Serializable {
    public final String packageName;
    public final String groupId;
    public final String id;
    public final String version;
    public final ArrayList<ResourcePath> jarFiles;

    public Stratego2LibInfo(String packageName, String groupId, String id, String version,
        ArrayList<ResourcePath> jarFiles) {
        this.packageName = packageName;
        this.groupId = groupId;
        this.id = id;
        this.version = version;
        this.jarFiles = jarFiles;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Stratego2LibInfo that = (Stratego2LibInfo) o;

        if(!packageName.equals(that.packageName))
            return false;
        if(!groupId.equals(that.groupId))
            return false;
        if(!id.equals(that.id))
            return false;
        if(!version.equals(that.version))
            return false;
        return jarFiles.equals(that.jarFiles);
    }

    @Override public int hashCode() {
        int result = packageName.hashCode();
        result = 31 * result + groupId.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + jarFiles.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Stratego2Lib(" + packageName + ", " + groupId + ':' + id + ':' + version + ", "
            + jarFiles + ')';
    }
}
