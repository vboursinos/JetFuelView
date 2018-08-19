package headfront.jetfuelview.graph;

import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxStylesheet;

import java.awt.*;
import java.util.Hashtable;

/**
 * Created by Deepak on 19/08/2018.
 */
public class Styles {

    public static final String USER_CONNECTED = "USER_CONNECTED";
    public static final String UER_DISCONNECTED = "UER_DISCONNECTED";
    public static final String JETFUEL_CONNECTED = "JETFUEL_CONNECTED";
    public static final String JETFUEL_DISCONNECTED = "JETFUEL_DISCONNECTED";
    public static final String AMPS_LINK_GOOD = "AMPS_LINK_GOOD";
    public static final String AMPS_LINK_BAD = "AMPS_LINK_BAD";
    public static final String AMPS_SERVER_GOOD = "AMPS_SERVER_GOOD";
    public static final String AMPS_SERVER_BAD = "AMPS_SERVER_BAD";
    public static final String AMPS_GROUP = "AMPS_GROUP";
    public static final String AMPS_COMPONENT_GOOD = "AMPS_COMPONENT_GOOD";
    public static final String AMPS_COMPONENT_BAD = "AMPS_COMPONENT_BAD";


    public static void registerStyles(mxStylesheet stylesheet) {
        stylesheet.putCellStyle(USER_CONNECTED, createConnectedUserStyle());
        stylesheet.putCellStyle(AMPS_GROUP, createAmpsGroupStyle());
        stylesheet.putCellStyle(AMPS_LINK_GOOD, createAmpsGoodLinktyle());
        stylesheet.putCellStyle(JETFUEL_CONNECTED, createConnectedJetfuelStyle());
        stylesheet.putCellStyle(AMPS_SERVER_GOOD, createAmpsGoodServerStyle());
        stylesheet.putCellStyle(AMPS_SERVER_BAD, createAmpsBadServerStyle());
        stylesheet.putCellStyle(AMPS_COMPONENT_GOOD, createComponentGoodStyle());
    }

    private static Hashtable<String, Object> createConnectedUserStyle() {
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 170)));
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
        // label position
        style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        // font
        style.put(mxConstants.STYLE_FONTSIZE, 14);
        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        //image
        style.put(mxConstants.SHAPE_IMAGE, "/images/client.png");
        return style;
    }

    private static Hashtable<String, Object> createComponentGoodStyle() {
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 170)));
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
        // label position
        style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        // font
        style.put(mxConstants.STYLE_FONTSIZE, 14);
        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        //image
        style.put(mxConstants.SHAPE_IMAGE, "/images/component.png");
        return style;
    }



    private static Hashtable<String, Object> createConnectedJetfuelStyle() {
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 170)));
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
        // label position
        style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        // font
        style.put(mxConstants.STYLE_FONTSIZE, 14);
        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        //image
        style.put(mxConstants.SHAPE_IMAGE, "/images/JetFuelMediumNoBg.png");
        return style;
    }

    private static Hashtable<String, Object> createAmpsGroupStyle() {
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(229, 250, 248)));
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 170)));
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_AUTOSIZE, 1);
        style.put(mxConstants.STYLE_RESIZABLE, 1);
        // label position
        style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_TOP);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_BOTTOM);
        // font
        style.put(mxConstants.STYLE_FONTSIZE, 18);
        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        return style;
    }

    private static Hashtable<String, Object> createObjectStyle() {
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 110, 0)));
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        return style;
    }

    private static Hashtable<String, Object> createAmpsGoodServerStyle() {
        Hashtable<String, Object> style = new Hashtable<>();

        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 170)));
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        // label position
        style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        // font
        style.put(mxConstants.STYLE_FONTSIZE, 14);
        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        //image
        style.put(mxConstants.SHAPE_IMAGE, "/images/ampsServer.png");

        return style;
    }

    private static Hashtable<String, Object> createAmpsBadServerStyle() {
        Hashtable<String, Object> style = new Hashtable<>();

        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 0, 170)));
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        // label position
        style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        // font
        style.put(mxConstants.STYLE_FONTSIZE, 14);
        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        //image
        style.put(mxConstants.SHAPE_IMAGE, "/images/ampsServerOff2.png");

        return style;
    }

    private static Hashtable<String, Object> createAmpsGoodLinktyle() {
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.BLACK));
        style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        style.put(mxConstants.STYLE_ENDSIZE, 10);
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        return style;
    }
}
