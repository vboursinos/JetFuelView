package headfront.dataexplorer;

import headfront.utils.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Deepak on 08/07/2016.
 *
 * @todo I dont have spring in Data explorer yet hence a static class but convert this later
 */
public class StatsProperties {
    private static final Logger LOG = LoggerFactory.getLogger(StatsProperties.class);

    public static final StatsProperties instance = new StatsProperties();
    private PropertiesLoader properties = new PropertiesLoader("statFields.properties");

    public static StatsProperties getInstance() {
        return instance;
    }

    public Object getProperty(String name) {
        return properties.getProperty(name);
    }

    public List<String> getPropertyList(String name) {
        return properties.getPropertyList(name);
    }

    public List<String> getPropertyKeys() {
        Set<Object> propertyKeys = properties.getPropertyKeys();
        List<String> keys = new ArrayList<>();
        propertyKeys.forEach(key -> keys.add(key.toString()));
        Collections.sort(keys);
        return keys;
    }
}
