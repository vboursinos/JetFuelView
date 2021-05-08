package headfront.utils;

import java.util.Map;

/**
 * Created by Deepak on 02/04/2016.
 */
public class MessageUtil {

    public static Object getLeafNode(Map map, String fieldName) {
        Object o = map.get(fieldName);
        if (o != null) {
            return o;
        }
        for (Object value : map.values()) {
            if (value instanceof Map) {
                Map newMap = (Map) value;
                return getLeafNode(newMap, fieldName);
            }
        }
        return null;
    }
    public static Object getActualLeafNode(Map map, String fieldName) {
        Object o = map.get(fieldName);
        if (o != null) {
            return o;
        }
        if (fieldName.contains("/")) {
            String[] parts = fieldName.split("/");
            Object value = map.get(parts[0]);
            if (value instanceof Map) {
                Map newMap = (Map) value;
                return getActualLeafNode(newMap, fieldName.replace(parts[0] + "/", ""));
            }
        }
        return null;
    }
    public static String removeHtml(String input) {
        return input.replace("</html>", "").replace("<html>", "").trim();

    }


}
