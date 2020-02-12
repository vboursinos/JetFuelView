package headfront.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Deepak on 24/07/2016.
 */
public class PrimativeClassUtil {

    private static final Map<Class, String> BUILT_IN_MAP = new HashMap<>();

    static {
        BUILT_IN_MAP.put(String.class, " [String]");
        BUILT_IN_MAP.put(Boolean.class, " [Boolean]");
        BUILT_IN_MAP.put(Integer.class, " [Integer]");
        BUILT_IN_MAP.put(Long.class, " [Long]");
        BUILT_IN_MAP.put(Double.class, " [Double]");
        BUILT_IN_MAP.put(Long.class, " [Long]");
        BUILT_IN_MAP.put(Short.class, " [Short]");
        BUILT_IN_MAP.put(Byte.class, " [Byte]");
        BUILT_IN_MAP.put(Float.class, " [Float]");
        BUILT_IN_MAP.put(Character.class, " [Char]");
    }

    public static String getPrimativeType(Object obj) {
        if (obj == null) {
            return " [null]";
        }
        Class aClass = obj.getClass();
        String c = BUILT_IN_MAP.get(aClass);
        if (c == null) {
            return " [" + aClass.getSimpleName() + "]";
        }
        return c;
    }
}
