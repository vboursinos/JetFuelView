package headfront.jetfuelview.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 20/08/2018.
 */
public class ProcessLauncher {

    private static Map<String, Process> activeProcess = new ConcurrentHashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(ProcessLauncher.class);

    public static void killAllChildrenProcess() {
        LOG.info("Going to kill " + activeProcess.size() + " processes");
        activeProcess.values().forEach(Process::destroy);
        activeProcess.values().forEach(Process::destroyForcibly);
        LOG.info("Killed all processes");
    }

    public static void exec(Map<String, Object> server) {
        new Thread(() -> {
            final String name = (String) server.get("name");
            try {
                String classpath = Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
                        .map(URL::getFile)
                        .collect(Collectors.joining(File.pathSeparator));
                Process process = new ProcessBuilder(
                        System.getProperty("java.home") + "/bin/java",
                        "-Dlog4j.configurationFile=log4j2-JetFuel.xml",
                        "-XX:MaxGCPauseMillis=10",
                        "-XX:SurvivorRatio=4",
                        "-XX:+UseConcMarkSweepGC",
                        "-classpath",
                        classpath,
                        "headfront.dataexplorer.DataExplorer",
                        "tcp://192.168.56.101:8001/amps/json",
                        "8199",
                        "UAT"
                )
                        .inheritIO()
                        .start();
                activeProcess.put(name, process);
                LOG.info("JetFuelExplorer for " + name + " ended.");
            } catch (Exception e) {
                LOG.info("Unable to start JetFuelExplorer for " + name, e);
            }
        }).start();
    }
}
