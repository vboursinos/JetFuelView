package headfront;

import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import headfront.graph.JetFuelGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class JGraphLearning extends JFrame {

    public JGraphLearning() {
        super("JGraphXLearning2");
        mxGraph graph = new JetFuelGraph();

        Object defaultParent = graph.getDefaultParent();
        mxStylesheet stylesheet = graph.getStylesheet();
        stylesheet.putCellStyle("user", createUserStyle());
        stylesheet.putCellStyle("group", createGroupStyle());
        stylesheet.putCellStyle("link", createAgentLinkStyle());
        stylesheet.putCellStyle("jetFuel", createJetfuelStyle());
        stylesheet.putCellStyle("ampsSever", createAmpServerStyle());
        graph.getModel().beginUpdate();
        Map<String, Object> createdServers = new HashMap<>();
        int startx = 20;
        int starty = 35;
        int width = 200;
        int height = 40;
        int xPadding = 50;
        int yPadding = 50;
        String[] allAmpsServer = {"PRIMARYLDN", "BACKUPLDN", "PRIMARYFFT", "BACKUPFFT"};
        Object user = graph.insertVertex(defaultParent, null, "ProcessFFT", 20, 20, 50, 50, "user");
        Object jetFuel = graph.insertVertex(defaultParent, null, "DeepakJetFuel", 150, 20, 50, 50, "jetFuel");
        Object group = graph.insertVertex(defaultParent, null, "Main", 240, 150,
                (width + yPadding) * 2,
                (((allAmpsServer.length + 1) / 2) * (height + xPadding)) + (starty), "group");
        int count = 0;
        for (int i = 0; i < allAmpsServer.length; i++) {
            final Object ampsSever = graph.insertVertex(group, allAmpsServer[i], allAmpsServer[i], startx, starty, width, height, "ampsSever");
            count++;
            if (count == 2) {
                count = 0;
                startx = 20;
                starty = starty + height + yPadding;
            } else {
                startx = startx + width + xPadding;
            }
            createdServers.put(allAmpsServer[i], ampsSever);
        }

        graph.insertEdge(defaultParent, "1", "", user, createdServers.get(allAmpsServer[0]), "link");
        graph.insertEdge(defaultParent, "2", "", user, jetFuel, "link");
        graph.insertEdge(defaultParent, "3", "", jetFuel, user, "link");
        graph.insertEdge(group, "4", "", createdServers.get(allAmpsServer[0]), createdServers.get(allAmpsServer[1]), "link");
        graph.insertEdge(group, "5", "", createdServers.get(allAmpsServer[1]), createdServers.get(allAmpsServer[0]), "link");

        graph.getModel().endUpdate();
        mxGraphComponent graphComponent = new mxGraphComponent(graph) {
            private static final long serialVersionUID = 1821677322838455152L;

            @Override
            public mxGraphHandler createGraphHandler() {
                return new mxGraphHandler(this) {
                    @Override
                    protected boolean shouldRemoveCellFromParent(Object parent, Object[] cells, MouseEvent e) {
                        return false;
                    }
                };
            }
        };
        configureGraphComponent(graphComponent);

        JPanel canvasPanel = new JPanel(new BorderLayout());
        canvasPanel.add(graphComponent, BorderLayout.CENTER);

        JButton save = new JButton("Save");
        save.addActionListener(l -> {
            FileUtil.saveGraph(graphComponent, "layout.xml");
        });
        JButton load = new JButton("Load");
        load.addActionListener(l -> {
            FileUtil.loadGraph(graphComponent, "layout.xml");
        });


        FlowLayout buttonLayout = new FlowLayout();
        JPanel buttonPanel = new JPanel(buttonLayout);
        buttonPanel.add(save);
        buttonPanel.add(load);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(canvasPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
        setContentPane(mainPanel);
    }

    /**
     * General JetFuelGraph component settings.
     *
     * @param graphComponent
     */
    private void configureGraphComponent(mxGraphComponent graphComponent) {
        graphComponent.getViewport().setOpaque(true);
        graphComponent.getViewport().setBackground(Color.WHITE);
        graphComponent.setConnectable(false);
        graphComponent.setToolTips(true);
        ToolTipManager.sharedInstance().registerComponent(graphComponent);
    }

    /**
     * Create a new style for process vertices.
     *
     * @return the created style.
     */
    private Hashtable<String, Object> createUserStyle() {
        Hashtable<String, Object> style = new Hashtable<String, Object>();
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

    private Hashtable<String, Object> createJetfuelStyle() {
        Hashtable<String, Object> style = new Hashtable<String, Object>();
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


    private Hashtable<String, Object> createGroupStyle() {
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

    /**
     * Create a new style for object vertices.
     *
     * @return the created style.
     */
    private Hashtable<String, Object> createObjectStyle() {
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(0, 110, 0)));
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        return style;
    }

    /**
     * Create a new style for state vertices.
     *
     * @return the created style.
     */
    private Hashtable<String, Object> createAmpServerStyle() {
        Hashtable<String, Object> style = new Hashtable<String, Object>();

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

    /**
     * Create a new style for agent links.
     *
     * @return the created style.
     */
    private Hashtable<String, Object> createAgentLinkStyle() {
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.BLACK));
        style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        style.put(mxConstants.STYLE_ENDSIZE, 10);
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        return style;
    }

    /**
     * Execute the program.
     *
     * @param args ignored.
     */
    public static void main(String args[]) {
        JGraphLearning frame = new JGraphLearning();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 820);
        frame.setVisible(true);
    }
}