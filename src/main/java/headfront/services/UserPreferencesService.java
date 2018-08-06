package headfront.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 02/04/2016.
 */
@Service
public class UserPreferencesService {

    public static final String WEB_SERVICE_POLLER = "webServicePoller.json";
    public static final String SYSTEM_VIEW_POLLER = "systemViewPoller.json";
    public static final String SYSTEM_VIEW_MODEL = "systemViewModel.json";
    private final String parentJetfuelFolder = "JetFuelView";
    private static final Logger LOG = LoggerFactory.getLogger(UserPreferencesService.class);
    private Map<String, Path> registeredPath = new HashMap<>();

    @PostConstruct
    public void init() {
        String path = System.getProperty("user.home") + File.separator + parentJetfuelFolder;
        File customDir = new File(path);
        if (!customDir.exists()) {
            boolean mkdirs = customDir.mkdirs();
            if (mkdirs) {
                LOG.info("UserPreference Folder is created at " + customDir.getAbsolutePath());
            } else {
                LOG.error("UserPreference Folder could not be created at " + customDir.getAbsolutePath());
            }
        }
        registeredPath.put(WEB_SERVICE_POLLER, Paths.get(path, WEB_SERVICE_POLLER));
        registeredPath.put(SYSTEM_VIEW_POLLER, Paths.get(path, SYSTEM_VIEW_POLLER));
        registeredPath.put(SYSTEM_VIEW_MODEL, Paths.get(path, SYSTEM_VIEW_MODEL));
    }

    public String getConfig(String preferenceName) {
        Path path = registeredPath.get(preferenceName);
        StringBuilder builder = new StringBuilder();
        if (path != null) {
            if (path.toFile().exists()) {
                List<String> lines = null;
                try {
                    lines = Files.readAllLines(path, Charset.defaultCharset());
                    lines.forEach(builder::append);
                } catch (IOException e) {
                    LOG.error("Unable to get " + preferenceName + " from " + path, e);
                }
            }
        }
        return builder.toString();
    }

    public void saveConfig(String preferenceName, String json) {
        Path path = registeredPath.get(preferenceName);
        try {
            if (path != null) {
                File file = path.toFile();
                if (!file.exists()) {
                    boolean newFile = file.createNewFile();
                    if (newFile) {
                        LOG.info("UserPreference file created at " + file.getAbsolutePath());
                    } else {
                        LOG.error("UserPreference file could not be created at " + file.getAbsolutePath());
                    }
                }
                Path write = Files.write(path, json.getBytes());
                LOG.info("Saved " + preferenceName + " at " + write.toString());
            }
        } catch (IOException e) {
            LOG.error("Unable to save " + preferenceName + " at " + path, e);
        }

    }
}
