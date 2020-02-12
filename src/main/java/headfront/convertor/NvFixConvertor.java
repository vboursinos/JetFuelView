package headfront.convertor;

import com.crankuptheamps.client.NVFIXBuilder;
import com.crankuptheamps.client.NVFIXShredder;
import com.crankuptheamps.client.exception.CommandException;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 30/03/2016.
 */
public class NvFixConvertor implements MessageConvertor {

    private static byte FIELD_SEPARATOR = (byte) 1;
    private final NVFIXShredder nvfixShredder = new NVFIXShredder(FIELD_SEPARATOR);

    @Override
    public String convertToString(Map data) {
        try {
            NVFIXBuilder builder = new NVFIXBuilder(1024, FIELD_SEPARATOR);
            data.entrySet().forEach(obj -> {
                Entry e = (Entry) obj;
                try {
                    builder.append(e.getKey().toString(), e.getValue().toString());
                } catch (CommandException e1) {
                    e1.printStackTrace();
                }
            });
            return new String(builder.getBytes());

        } catch (Exception e) {
            throw new RuntimeException("Unable to create nvfix for " + data, e);
        }
    }

    @Override
    public Map<String, Object> convertToMap(String data) {
        try {
            Map<CharSequence, CharSequence> charSequenceCharSequenceMap = nvfixShredder.toNVMap(data);
            return charSequenceCharSequenceMap
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(p -> p.getKey().toString(), Entry::getValue));
        } catch (Exception e) {
            throw new RuntimeException("Unable to decode to nvfix data " + data, e);
        }
    }
}
