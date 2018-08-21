package headfront.jetfuelview.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 20/08/2018.
 */
public class ProcessLauncher {

    private static List<Process> activeProcess = new ArrayList();

    public static void killAllChildrenProcess(){
        activeProcess.forEach(process -> process.destroy());
        activeProcess.forEach(process -> process.destroyForcibly());
    }

    public static void exec(Class klass){
        new Thread(() -> {
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
                int exitCode = process.waitFor();
                activeProcess.add(process);
                System.out.println("process stopped with exitCode " + exitCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
}
