package headfront.convertor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is a very very slow parser. Only used for reading config for now
 */
public class XmlConvertor implements MessageConvertor {

    private boolean keysInLowerCase;

    public XmlConvertor(boolean keysInLowerCase) {
        this.keysInLowerCase = keysInLowerCase;
    }

    @Override
    public String convertToString(Map<String, Object> data) {
        return null;
    }

    @Override
    public Map<String, Object> convertToMap(String data) {
        try {
            InputStream is = new StringBufferInputStream(data);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(is);
            return (Map<String, Object>) createMap(document.getDocumentElement());
        } catch (Exception excepion) {
            excepion.printStackTrace();
        }
        return null;
    }

    private Object createMap(Node node) {
        Map<String, Object> map = new HashMap<String, Object>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            String name = currentNode.getNodeName();
            if (keysInLowerCase){
                name = name.toLowerCase();
            }
            String trim = currentNode.getTextContent().trim();
            if (trim.length() == 0) {
                continue;
            }
            Object value = null;
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                value = createMap(currentNode);
            } else if (currentNode.getNodeType() == Node.TEXT_NODE) {
                return currentNode.getTextContent();
            }
            if (map.containsKey(name)) {
                Object os = map.get(name);
                if (os instanceof List) {
                    ((List<Object>) os).add(value);
                } else {
                    List<Object> objs = new LinkedList<Object>();
                    objs.add(os);
                    objs.add(value);
                    map.put(name, objs);
                }
            } else {
                if (keysInLowerCase) {
                    map.put(name.toLowerCase(), value);
                } else {
                    map.put(name, value);
                }
            }
        }
        return map;
    }
}
