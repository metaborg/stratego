package mb.stratego.build.util;

import mb.resource.hierarchical.ResourcePath;

public class CommonPaths {
    public static ResourcePath strSepCompSrcGenDir(ResourcePath root) {
        return resolve(srcGenDir(root), "stratego_sugar");
    }

    public static ResourcePath strSepCompStrategyDir(ResourcePath root, String strategy) {
        return resolve(strSepCompSrcGenDir(root), capitalsForDollars(strategy));
    }

    public static ResourcePath strSepCompStrategyFile(ResourcePath root, String projectName, String moduleName,
        String strategy) {
        return resolve(strSepCompStrategyDir(root, strategy), prepareModuleName(projectName, moduleName) + ".aterm");
    }

    public static ResourcePath strSepCompConstrListFile(ResourcePath root, String projectName, String moduleName,
        String strategy) {
        return resolve(strSepCompStrategyDir(root, strategy),
            prepareConstrListName(projectName, moduleName) + ".aterm");
    }

    public static ResourcePath strSepCompOverlayDir(ResourcePath root, String overlayName) {
        return resolve(strSepCompSrcGenDir(root), "overlays", capitalsForDollars(overlayName));
    }

    public static ResourcePath strSepCompOverlayFile(ResourcePath root, String projectName, String moduleName,
        String overlayName) {
        return resolve(strSepCompOverlayDir(root, overlayName), prepareModuleName(projectName, moduleName) + ".aterm");
    }

    public static ResourcePath strSepCompBoilerplateFile(ResourcePath root, String projectName, String moduleName) {
        return resolve(strSepCompSrcGenDir(root), prepareModuleName(projectName, moduleName) + ".aterm");
    }

    public static ResourcePath strSepCompPackedStrategyFile(ResourcePath root, String strategy) {
        return resolve(strSepCompStrategyDir(root, strategy), "packed$" + ".aterm");
    }

    public static ResourcePath strSepCompPackedBoilerplateFile(ResourcePath root) {
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

    public static ResourcePath srcGenDir(ResourcePath root) {
        return resolve(root, "src-gen");
    }

    public static ResourcePath resolve(ResourcePath dir, String path) {
        return dir.appendString(path);
    }

    public static ResourcePath resolve(ResourcePath dir, String... paths) {
        ResourcePath file = dir;
        for(String path : paths) {
            file = resolve(file, path);
        }
        return file;
    }
}
