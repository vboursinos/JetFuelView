package headfront;

import com.mxgraph.swing.mxGraphComponent;
import headfront.dataexplorer.DataExplorer;
import headfront.graph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;


public class JetFuelView extends JFrame {
    private static final Logger LOG = LoggerFactory.getLogger(DataExplorer.class);
    public JetFuelView(String title) {
        super(title);
        LOG.info("Starting Data Explorer version " + title);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOG.error("Caught Exception on Thread " + t, e);
        });
        JetFuelGraph graph = new JetFuelGraph();
        Styles.registerStyles(graph.getStylesheet());
        mxGraphComponent graphComponent = new JetFuelGraphComponent(graph);
        setContentPane(createMainPanel(graphComponent));
        JetFuelGraphRenderer render = new JetFuelGraphRenderer(graph);
        render.draw();
    }

    private JPanel createMainPanel(mxGraphComponent graphComponent) {
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
        return mainPanel;
    }

    public static void main(String args[]) {
        Object version = 8;
        JetFuelView frame = new JetFuelView("JetFuelView  - " + version);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 820);
        frame.setVisible(true);
    }
}