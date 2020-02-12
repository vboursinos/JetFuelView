package headfront.dataexplorer.bean;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Deepak on 06/07/2016.
 */
public class DataBean {

    private Map<String, Property> properties = new HashMap<>();
    private Object beanID;

    public DataBean(Object beanID) {
        this.beanID = beanID;
    }

    public DataBean(Object beanID, Map values) {
        this.beanID = beanID;
        updateProperties(values);
    }

    public boolean updateProperties(Map values) {
        int oldSize = properties.size();
        values.entrySet().forEach(o -> {
            Map.Entry e = (Map.Entry) o;
            setPropertyValue(e.getKey().toString(), e.getValue());
        });
        return properties.size() > oldSize;
    }

    public Object getPropertyValue(String propertyName) {
        Property property = getProperty(propertyName);
        return property == null ? null : property.getValue();
    }

    public <T> Property<T> getProperty(String propertyName) {
        return properties.get(propertyName);
    }


    public boolean setPropertyValue(String propertyName, final Object newValue) {
        boolean added = false;
        Property property = properties.get(propertyName);
        if (property == null) {
            property = new SimpleObjectProperty<>();
            properties.put(propertyName, property);
            added = true;
        }
        setPropertyValue(property, newValue);
        return added;
    }


    public void setPropertyValue(final Property property, final Object newValue) {
        if (property != null) {
            property.setValue(newValue);
        }
    }

    public Object getBeanID() {
        return beanID;
    }
}
