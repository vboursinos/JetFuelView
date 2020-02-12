package headfront.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by Deepak on 08/07/2016.
 *
 * @todo I dont have spring in Data explorer yet hence a static class but convert this later
 */
public class PropertiesLoader {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);

    private Properties properties = new Properties();

    public PropertiesLoader(String propertyFile) {
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(propertyFile));
        } catch (IOException e) {
            LOG.error("Unable to load properties file  " + propertyFile, e);
        }
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Set<Object> getPropertyKeys() {
        return properties.keySet();
    }

    public List<String> getPropertyList(String name) {
        String data = (String) properties.get(name);
        if (data != null) {
            String[] parts = data.split(";");
            List<String> listToReturn = new ArrayList<>();
            for (String part : parts) {
                listToReturn.add(part.trim());
            }
            return listToReturn;
        }
        return Collections.emptyList();
    }

}
