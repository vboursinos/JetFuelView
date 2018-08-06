package headfront.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 28/03/2016.
 */
@Service("processService")
public class ProcessService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessService.class);


    private List<Process> processes = new ArrayList();

    public void launchJavaProcess(String name) {
        String classpath = Arrays.stream(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs())
                .map(URL::getFile)
                .collect(Collectors.joining(File.pathSeparator));
        Process process = null;
        try {
            process = new ProcessBuilder(
                    System.getProperty("java.home") + "/bin/java",
                    "-classpath",
                    classpath,
                    "", //DataExplorerViewTest.class.getCanonicalName(),
                    name
            )
                    .inheritIO()
                    .start();
            processes.add(process);
            LOG.info("Launched a java process for " + name);
        } catch (IOException e) {
            LOG.error("Unable to launched a java process with name  " + name);
        }

    }

    public void shutdownAllProcess() {
        processes.forEach(Process::destroyForcibly);
    }
}
