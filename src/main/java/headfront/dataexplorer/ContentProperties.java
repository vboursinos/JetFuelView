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
public class ContentProperties {
    private static final Logger LOG = LoggerFactory.getLogger(ContentProperties.class);

    public static final ContentProperties instance = new ContentProperties();
    private PropertiesLoader properties = new PropertiesLoader("contentText.properties");

    public static ContentProperties getInstance() {
        return instance;
    }

    public Object getProperty(String name) {
        return properties.getProperty(name);
    }

    public List<String> getPropertyList(String name) {
        return properties.getPropertyList(name);
    }

}
