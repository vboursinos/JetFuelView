package headfront;

import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Hashtable;


public class JGraphLearning extends JFrame {
    /**
     * Create all required styles.
     * Create a three node graph with an inner node.
     * Extend the {@link mxGraphComponent} so that inner vertex cannot be dragged
     * outside their parent.
     */
    public JGraphLearning() {
        super("JGraphXLearning2");
        mxGraph graph = new mxGraph();

        configureGraph(graph);
        Object defaultParent = graph.getDefaultParent();
        mxStylesheet stylesheet = graph.getStylesheet();
        stylesheet.putCellStyle("user", createUserStyle());
        stylesheet.putCellStyle("group", createGroupStyle());
        stylesheet.putCellStyle("link", createAgentLinkStyle());
        stylesheet.putCellStyle("jetFuel", createJetfuelStyle());
        stylesheet.putCellStyle("ampsSever", createAmpServerStyle());
        graph.getModel().beginUpdate();
        Object user = graph.insertVertex(defaultParent, null, "ProcessFFT", 20, 20, 50, 50, "user");
        Object jetFuel = graph.insertVertex(defaultParent, null, "DeepakJetFuel", 150, 20, 50, 50, "jetFuel");
        Object group = graph.insertVertex(defaultParent, null, "Main", 240, 150, 10, 10, "group");
        Object ampsServer = graph.insertVertex(group, null, "PRIMARYLDN", 10, 35, 100, 20, "ampsSever");
        Object ampsServer2 = graph.insertVertex(group, null, "BACKUPLDN", 250, 35, 100, 20, "ampsSever");
//        graph.insertEdge(defaultParent, null, "", user, group, "agent");
        graph.insertEdge(defaultParent, "1", "", user, ampsServer, "link");
        graph.insertEdge(defaultParent, "2", "", user, jetFuel, "link");
        graph.insertEdge(defaultParent, "3", "", jetFuel, user, "link");
        graph.insertEdge(group, "4", "", ampsServer, ampsServer2, "link");
        graph.insertEdge(group, "5", "", ampsServer2, ampsServer, "link");
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
     * General graph settings.
     *
     * @param graph the graph to configure.
     */
    private void configureGraph(mxGraph graph) {
        graph.setResetEdgesOnMove(true);
        graph.setCellsResizable(false);
        graph.setAutoSizeCells(true);
        graph.setCellsEditable(false);
        graph.setEdgeLabelsMovable(false);
        graph.setDisconnectOnMove(false);
        graph.setKeepEdgesInForeground(true);
        graph.getSelectionModel().setSingleSelection(false);
        graph.setAllowDanglingEdges(false);

        graph.setEnabled(false);
        graph.setConstrainChildren(true);
        graph.setExtendParents(true);
        graph.setExtendParentsOnAdd(true);
        graph.setDefaultOverlap(0);
    }

    /**
     * General graph component settings.
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