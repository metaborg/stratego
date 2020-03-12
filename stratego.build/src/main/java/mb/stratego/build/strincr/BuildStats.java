package mb.stratego.build.strincr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    public static Map<String, Long> tldSubFrontendCTreeSize = new HashMap<>();
    public static Map<String, Long> strategyBackendCTreeSize = new HashMap<>();

    // @formatter:off
    public static final String CSV_HEADER = "\"Frontend time\","
        + "\"Frontend tasks\","
        + "\"Frontend size\","
        + "\"Backend time\","
        + "\"Backend tasks\","
        + "\"Backend size\","
        + "\"Lib time\","
        + "\"Lib tasks\","
        + "\"Shuffle time\","
        + "\"Shuffle lib time\","
        + "\"Static check time\","
        + "\"Shuffle backend time\"";
    // @formatter:on

    // @formatter:off
    public static final String CSV_HEADER2 = "\"Total time\","
        + "\"PIE overhead\","
        + "\"Frontend time\","
        + "\"SubFrontend time\","
        + "\"Backend time\","
        + "\"Lib time\","
        + "\"Analysis time\","
        + "\"InsertCasts time\","
        + "\"StrIncr time\"";
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
        tldSubFrontendCTreeSize.clear();
        strategyBackendCTreeSize.clear();

        Frontend.timestamps.clear();
        SubFrontend.timestamps.clear();
        Backend.timestamps.clear();
        LibFrontend.timestamps.clear();
        Analysis.timestamps.clear();
        InsertCasts.timestamps.clear();
        StrIncr.timestamps.clear();
    }

    public static String csv() {
        long frontSize = 0L;
        for(Long l : tldSubFrontendCTreeSize.values()) {
            frontSize += l;
        }
        long backSize = 0L;
        for(Long l : strategyBackendCTreeSize.values()) {
            backSize += l;
        }
        // @formatter:off
        return frontTaskTime + "," + executedFrontTasks + "," + frontSize
            + "," + backTaskTime + "," + executedBackTasks + "," + backSize
            + "," + frontLibTaskTime + "," + executedFrontLibTasks
            + "," + shuffleTime
            + "," + shuffleLibTime
            + "," + checkTime
            + "," + shuffleBackendTime;
        // @formatter:on
    }

    public static String csv2(long totalTime) {
        final long frontTime = computeTime(Frontend.timestamps);
        final long subFrontTime = computeTime(SubFrontend.timestamps);
        final long backTime = computeTime(Backend.timestamps);
        final long libTime = computeTime(LibFrontend.timestamps);
        final long analysisTime = computeTime(Analysis.timestamps);
        final long castTime = computeTime(InsertCasts.timestamps);;
        final long strIncrTime = computeTime(StrIncr.timestamps);
        long totalRecordedTime = frontTime + subFrontTime + backTime + libTime + analysisTime + castTime + strIncrTime;
        final long pieTime = totalTime - totalRecordedTime;
        // @formatter:off
        return totalTime +
            "," + pieTime +
            "," + frontTime +
            "," + subFrontTime +
            "," +  backTime +
            "," +  libTime +
            "," +  analysisTime +
            "," +  castTime +
            "," +  strIncrTime;
        // @formatter:on
    }

    public static long computeTime(ArrayList<Long> timestamps) {
        long frontTime = 0L;
        for(Iterator<Long> iterator = timestamps.iterator(); iterator.hasNext(); ) {
            Long l1 = iterator.next();
            Long l2 = iterator.next();
            frontTime += l2 - l1;
        }
        return frontTime;
    }
}
