package headfront.dataexplorer;

import headfront.utils.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Deepak on 08/07/2016.
 *
 * @todo I dont have spring in Data explorer yet hence a static class but convert this later
 */
public class JetFuelDataExplorerProperties {
    private static final Logger LOG = LoggerFactory.getLogger(JetFuelDataExplorerProperties.class);

    public static final JetFuelDataExplorerProperties instance = new JetFuelDataExplorerProperties();
    private PropertiesLoader properties = new PropertiesLoader("jetfueldataexplorer.properties");

    public static JetFuelDataExplorerProperties getInstance() {
        return instance;
    }

    public Object getProperty(String name) {
        return properties.getProperty(name);
    }

    public List<String> getPropertyList(String name) {
        return properties.getPropertyList(name);
    }

}
