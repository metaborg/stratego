package mb.stratego.build.strincr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BuildStats {
    public static long executedFrontTasks = 0;
    public static long executedFrontLibTasks = 0;
    public static long executedBackTasks = 0;
    public static long frontTaskTime = 0;
    public static long frontLibTaskTime = 0;
    public static long backTaskTime = 0;
    public static long shuffleTime = 0;
    public static long shuffleLibTime = 0;
    public static long checkTime = 0;
    public static long shuffleBackendTime = 0;
    public static Set<String> generatedJavaFiles = new HashSet<>();
    // strategy name -> no. for each module defining the strategy, which is how many definitions there were in that module
    public static Map<String, List<Integer>> modulesDefiningStrategy = new TreeMap<>();
    public static Map<String, Long> moduleFrontendCTreeSize = new HashMap<>();
    public static Map<String, Long> strategyBackendCTreeSize = new HashMap<>();

    // @formatter:off
    public static final String CSV_HEADER = "\"Frontend time\","
        + "\"Frontend tasks\","
        + "\"Backend time\","
        + "\"Backend tasks\","
        + "\"Lib time\","
        + "\"Lib tasks\","
        + "\"Shuffle time\","
        + "\"Shuffle lib time\","
        + "\"Static check time\","
        + "\"Shuffle backend time\"";
    // @formatter:on

    public static void reset() {
        executedFrontTasks = 0;
        executedFrontLibTasks = 0;
        executedBackTasks = 0;
        frontTaskTime = 0;
        frontLibTaskTime = 0;
        backTaskTime = 0;
        shuffleTime = 0;
        shuffleLibTime = 0;
        checkTime = 0;
        shuffleBackendTime = 0;
        generatedJavaFiles.clear();
        modulesDefiningStrategy.clear();
        strategyBackendCTreeSize.clear();
    }

    public static String csv() {
        // @formatter:off
        return frontTaskTime + "," + executedFrontTasks
            + "," + backTaskTime + "," + executedBackTasks
            + "," + frontLibTaskTime + "," + executedFrontLibTasks
            + "," + shuffleTime
            + "," + shuffleLibTime
            + "," + checkTime
            + "," + shuffleBackendTime;
        // @formatter:on
    }
}
