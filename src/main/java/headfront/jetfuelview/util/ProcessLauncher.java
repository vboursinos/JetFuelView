package headfront.jetfuelview.util;

import headfront.guiwidgets.PopUpDialog;
import headfront.utils.StringUtils;
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

    public static void exec(String id, final Map<String, Object> server) {
        if (activeProcess.containsKey(id)) {
            PopUpDialog.showWarningPopup("JetFuelExplorer already running", "JetFuelExplorer already running for " + id);
        } else {
            new Thread(() -> {
                try {
                    String classpath = Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
                            .map(URL::getFile)
                            .collect(Collectors.joining(File.pathSeparator));
                    final String ampsJsonUrlWithCredential = StringUtils.getAmpsJsonConnectionStringWithCredentials(server.get("url").toString(),
                            server.get("username").toString(),
                            server.get("credentials").toString());
                    Process process = new ProcessBuilder(
                            System.getProperty("java.home") + "/bin/java",
                            "-Dlog4j.configurationFile=log4j2-JetFuel.xml",
                            "-XX:MaxGCPauseMillis=10",
                            "-XX:SurvivorRatio=4",
                            "-XX:+UseConcMarkSweepGC",
                            "-classpath",
                            classpath,
                            "headfront.dataexplorer.DataExplorer",
                            ampsJsonUrlWithCredential,
                            server.get("adminPort").toString(),
                            server.get("environment").toString()
                    )
                            .inheritIO()
                            .start();
                    LOG.info("Launched JetFuelView for " + id);
                    activeProcess.put(id, process);
                    process.waitFor();
                    activeProcess.remove(id);
                    LOG.info("JetFuelExplorer for " + id + " ended.");
                } catch (Exception e) {
                    LOG.info("Unable to start JetFuelExplorer for " + id, e);
                }
            }).start();
        }
    }
}
