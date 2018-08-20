package headfront.jetfuelview.util;

import headfront.dataexplorer.DataExplorer;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 20/08/2018.
 */
public class ProcessLauncher {

    public static int exec(Class klass) throws IOException,
            InterruptedException {
//        String javaHome = System.getProperty("java.home");
//        String javaBin = javaHome +
//                File.separator + "bin" +
//                File.separator + "java";
//        String classpath = System.getProperty("java.class.path");
//        String className = klass.getCanonicalName();
//
//        ProcessBuilder builder = new ProcessBuilder(
//                javaBin, "-cp", classpath, className);
//
//        Process process = builder.start();
//        process.waitFor();
//        return process.exitValue
// ();

        Platform.runLater(() -> {
            try {
                String classpath = Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
                        .map(URL::getFile)
                        .collect(Collectors.joining(File.pathSeparator));
                
                Process process = new ProcessBuilder(
                        System.getProperty("java.home") + "/bin/java",
                        "-Dlog4j.configurationFile=log4j2-JetFuel.xml -XX:MaxGCPauseMillis=10 -XX:SurvivorRatio=4 -XX:+UseConcMarkSweepGC",
                        "-classpath",
                        classpath,
                        "headfront.dataexplorer.DataExplorer",
                        "tcp://192.168.56.101:8001/amps/json 8199 UAT"
                )
                        .inheritIO()
                        .start();
                int exitCode = process.waitFor();
                System.out.println("process stopped with exitCode " + exitCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return 1;
    }
}
