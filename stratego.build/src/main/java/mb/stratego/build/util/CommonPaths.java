package mb.stratego.build.util;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;

public class CommonPaths {
    public static FileObject strSepCompSrcGenDir(FileObject root) {
        return resolve(srcGenDir(root), "stratego_sugar");
    }

    public static FileObject strSepCompStrategyDir(FileObject root, String strategy) {
        return resolve(strSepCompSrcGenDir(root), capitalsForDollars(strategy));
    }

    public static FileObject strSepCompStrategyFile(FileObject root, String projectName, String moduleName,
        String strategy) {
        return resolve(strSepCompStrategyDir(root, strategy), prepareModuleName(projectName, moduleName) + ".aterm");
    }

    public static FileObject strSepCompConstrListFile(FileObject root, String projectName, String moduleName,
        String strategy) {
        return resolve(strSepCompStrategyDir(root, strategy),
            prepareConstrListName(projectName, moduleName) + ".aterm");
    }

    public static FileObject strSepCompOverlayDir(FileObject root, String overlayName) {
        return resolve(strSepCompSrcGenDir(root), "overlays", capitalsForDollars(overlayName));
    }

    public static FileObject strSepCompOverlayFile(FileObject root, String projectName, String moduleName,
        String overlayName) {
        return resolve(strSepCompOverlayDir(root, overlayName), prepareModuleName(projectName, moduleName) + ".aterm");
    }

    public static FileObject strSepCompBoilerplateFile(FileObject root, String projectName, String moduleName) {
        return resolve(strSepCompSrcGenDir(root), prepareModuleName(projectName, moduleName) + ".aterm");
    }

    public static FileObject strSepCompPackedStrategyFile(FileObject root, String strategy) {
        return resolve(strSepCompStrategyDir(root, strategy), "packed$" + ".aterm");
    }

    public static FileObject strSepCompPackedBoilerplateFile(FileObject root) {
        return resolve(strSepCompSrcGenDir(root), "packed$" + ".aterm");
    }

    public static String capitalsForDollars(String strategy) {
        return strategy.replaceAll("[A-Z]", "\\$$0");
    }

    public static String prepareModuleName(String projectName, String moduleName) {
        return projectName + "&" + capitalsForDollars(moduleName).replace('/', '+');
    }

    public static String prepareConstrListName(String projectName, String moduleName) {
        return projectName + "&" + capitalsForDollars(moduleName).replace('/', '+') + "&constrs";
    }

    public static FileObject srcGenDir(FileObject root) {
        return resolve(root, "src-gen");
    }

    public static FileObject resolve(FileObject dir, String path) {
        try {
            return dir.resolveFile(path);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    public static FileObject resolve(FileObject dir, String... paths) {
        FileObject file = dir;
        for(String path : paths) {
            file = resolve(file, path);
        }
        return file;
    }
}
