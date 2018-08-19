package headfront;

import com.mxgraph.swing.mxGraphComponent;
import headfront.graph.*;

import javax.swing.*;
import java.awt.*;


public class JGraphLearning extends JFrame {

    public JGraphLearning() {
        super("JGraphXLearning2");
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
        JGraphLearning frame = new JGraphLearning();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 820);
        frame.setVisible(true);
    }
}