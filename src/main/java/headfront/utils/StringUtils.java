package headfront.utils;

import javafx.scene.control.TreeItem;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by Deepak on 24/07/2016.
 */
public class StringUtils {

    private static DecimalFormat decimalFormat = new DecimalFormat("#,###,###,###");
    public static final String KEY_SEPERATOR = "±";
    private static final Map<String, String> anonymiseStrings = new HashMap<>();

    static {
        String[] from = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "a", "E", "e", "I", "i", "O", "o", "U", "u", "R", "r", "S", "s", "T", "t", "M", "m", "N", "n", "D", "d", "P", "p", "B", "b", "K", "k", "L", "l"};
        String[] to = {"2", "9", "0", "1", "2", "3", "6", "4", "5", "1", "e", "f", "s", "a", "w", "w", "t", "t", "M", "e", "Y", "T", "A", "a", "o", "e", "K", "f", "t", "F", "k", "r", "S", "z", "t", "F", "k", "r", "S", "z"};
        for (int i = 0; i < from.length; i++) {
            anonymiseStrings.put(":" + from[i], ":" + to[i]);
            anonymiseStrings.put(":\"" + from[i], ":\"" + to[i]);
            anonymiseStrings.put(from[i] + ",", to[i] + ",");
            anonymiseStrings.put(from[i] + "\",", to[i] + "\",");
        }
    }

    public static String formatToReadableDate(String isoDate) {
        StringBuilder builder = new StringBuilder();
        builder.append(isoDate.substring(0, 4));
        builder.append("/");
        builder.append(isoDate.substring(4, 6));
        builder.append("/");
        builder.append(isoDate.substring(6, 8));
        builder.append(" ");
        builder.append(isoDate.substring(9, 11));
        builder.append(":");
        builder.append(isoDate.substring(11, 13));
        builder.append(":");
        builder.append(isoDate.substring(13, 15));
        builder.append("-");
        builder.append(isoDate.substring(16, 22));

        return builder.toString();

    }

    //2016-09-27T21:38:45.4549750+01:00
    public static String formatToLogDate(String isoDate) {
        StringBuilder builder = new StringBuilder();
        builder.append(isoDate.substring(0, 4));
        builder.append("-");
        builder.append(isoDate.substring(4, 6));
        builder.append("-");
        builder.append(isoDate.substring(6, 8));
        builder.append("T");
        builder.append(isoDate.substring(9, 11));
        builder.append(":");
        builder.append(isoDate.substring(11, 13));
        builder.append(":");
        builder.append(isoDate.substring(13, 15));
        builder.append(".");
        builder.append(isoDate.substring(16, 22));
        builder.append("0+01:00");

        return builder.toString();

    }

    public static String removeBrackets(String source) {
        if (source.contains("[")) {
            source = source.split("\\[")[0].trim();
        }
        return source;
    }

    public static String getFieldName(String source) {
        String[] split = source.split("/");
        return "/" + split[split.length - 1];
    }

    public static String anonymiseData(String input) {
        for (Map.Entry entry : anonymiseStrings.entrySet()) {
            input = input.replace(entry.getKey().toString(), entry.getValue().toString());
        }
        return input;
    }

    public static String createRecordKey(List<String> keys) {
        if (keys.size() > 1) {
            StringJoiner joiner = new StringJoiner(KEY_SEPERATOR);
            keys.forEach(k -> {
                String process = k.replace("/", "");
                joiner.add(process);
            });
            return joiner.toString();
        }
        return keys.get(0);
    }

    public static boolean isValidString(final String text) {
        if (text != null) {
            if (text.trim().length() > 0) {
                return true;
            }
        }
        return false;
    }

    public static String getPaddedString(String originalText, int stringLength, String padValue) {
        final int currentLength = originalText.length();
        if (originalText.length() == stringLength) {
            return originalText;
        } else {
            int requiredPadding = stringLength - currentLength;
            String pad = "";
            for (int i = 0; i < requiredPadding; i++) {
                pad = pad + padValue;
            }
            return pad + originalText;
        }
    }

    public static String removePassword(String connectionURI) {
        return connectionURI.replaceAll(":[^////].*@", ":*****@");
    }

    public static String formatNumber(Number n) {
        return decimalFormat.format(n);
    }

    public static String formatNumber(String n) {
        Double d = Double.parseDouble(n);
        return decimalFormat.format(d);
    }

    public static String getAmpsUrl(String connectionsStr, String adminPortStr, boolean useSecureHttp) {
        String server = connectionsStr.replace("tcp://", "");
        server = server.replace("tcps://", "");
        server = server.substring(0, server.lastIndexOf(":"));
        String prefix = useSecureHttp ? "https://" : "http://";
        return prefix + server + ":" + adminPortStr;
    }

    public static String getAmpsAdminUrlWithCredential(String connectionsStr, String adminPortStr, String username,
                                                       String credential, boolean useSecureHttp) {
        String server = connectionsStr.replace("tcp://", "");
        server = server.replace("tcps://", "");
        server = server.substring(0, server.lastIndexOf(":"));
        String prefix = useSecureHttp ? "https://" : "http://";
        return prefix + username + ":" + credential + "@" + server + ":" + adminPortStr + "/amps.json";
    }

    public static String getAdminUrl(String connectionsStr, String adminPortStr, boolean useSecureHttp) {
        return getAmpsUrl(connectionsStr, adminPortStr, useSecureHttp) + "/amps";
    }

    public static String removePasswordsFromUrl(String url) {
        String httpProtocol = url.substring(0, url.indexOf("//")) + "//";
        final int index = url.indexOf("@");
        if (index > 0) {
            url = httpProtocol + url.substring(index + 1);
        }
        return url;
    }

    public static String getGalvanometerUrl(String connectionsStr, String adminPortStr, boolean useSecureHttp) {
        return getAmpsUrl(connectionsStr, adminPortStr, useSecureHttp);
    }

    public static String getConfigUrl(String connectionsStr, String adminPortStr, boolean useSecureHttp) {
        return getAdminUrl(connectionsStr, adminPortStr, useSecureHttp) + "/instance/config.xml";
    }

    public static String getServerAndPortFromUrl(String fullUrl) {
        String url = removeProtocolHeaders(fullUrl);
        int index = url.indexOf("@");
        if (index > -1) {
            url = url.substring(index + 1);
        }
        index = url.indexOf("/");
        if (index > -1) {
            url = url.substring(0, index);
        }
        return url;
    }

    public static String getAmpsJsonConnectionStringWithCredentials(String url, String username, String credential) {
        String connectionString = removeProtocolHeaders(url);
        if (username != null) {
            connectionString = username + ":" + credential + "@" + connectionString;
        }
        String prefix = url.substring(0, url.indexOf("//")) + "//";
        connectionString = prefix + connectionString;
        return connectionString;
    }

    public static String getAmpsJsonConnectionString(String url, String username, String credential, String port) {
        String connectionString = getServerAndPortFromUrl(url);
        int index = connectionString.indexOf(":");
        if (index > -1) {
            connectionString = connectionString.substring(0, index);
        }
        if (username != null) {
            connectionString = username + ":" + credential + "@" + connectionString;
        }
        String prefix = url.substring(0, url.indexOf("//")) + "//";
        connectionString = prefix + connectionString + ":" + port + "/amps/json";
        return connectionString;
    }

    public static String getShortServerAndPortFromUrl(String fullUrl) {
        String url = getServerFromUrl(fullUrl);
        String restOfUrl = removeProtocolHeaders(fullUrl);
        int index = restOfUrl.indexOf("@");
        if (index > -1) {
            restOfUrl = restOfUrl.substring(index + 1);
        }
        restOfUrl = restOfUrl.split(":")[1];
        final int removeIndex = restOfUrl.indexOf("/");
        String port = "";
        if (removeIndex > -1) {
            port = ":" + restOfUrl.substring(0, removeIndex);
        }
        return url + port;
    }

    public static String getServerFromUrl(String fullUrl) {
        String url = removeProtocolHeaders(fullUrl);
        int index = url.indexOf("@");
        if (index > -1) {
            url = url.substring(index + 1);
        }
        index = url.indexOf(":");
        if (index > -1) {
            url = url.substring(0, index);
        }
        // if we have a hostname then just return the host name
        final String[] split = url.split("\\.");
        if (split.length > 0) {
            final String firstToken = split[0];
            if (!isNumeric(firstToken)) {
                url = firstToken;
            }
        }
        return url;
    }

    private static String removeProtocolHeaders(String fullUrl) {
        String url = fullUrl.replaceAll("http://", "");
        url = url.replaceAll("https://", "");
        url = url.replaceAll("tcp://", "");
        url = url.replaceAll("tcps://", "");
        return url;
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public static String removeInvalidCharFromJson(String dataToProcess) {
        String processedData = dataToProcess.replace("nan", "null");
        processedData = processedData.replaceAll("\\n", "");
        return processedData;

    }

    public static String getFullTreePath(TreeItem<String> treeItem) {
        String data = treeItem.getValue();
        TreeItem<String> parent = treeItem.getParent();
        while (parent != null) {
            data = parent.getValue() + "/" + data;
            parent = parent.getParent();
        }
        return data;
    }
}
