package headfront.convertor;

import java.util.Map;

/**
 * Created by Deepak on 30/03/2016.
 */
public interface MessageConvertor {

    String convertToString(Map<String, Object> data);

    Map<String, Object> convertToMap(String data);
}
