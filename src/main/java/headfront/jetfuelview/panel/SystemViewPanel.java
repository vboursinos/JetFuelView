package headfront.jetfuelview.panel;

import com.mxgraph.swing.mxGraphComponent;
import headfront.graph.*;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.dnd.DnDConstants;


/**
 * Created by Deepak on 19/08/2018.
 */
public class SystemViewPanel {

    private static final Logger LOG = LoggerFactory.getLogger(SystemViewPanel.class);
    private BorderPane mainPane = new BorderPane();

    public SystemViewPanel() {
        JetFuelGraph graph = new JetFuelGraph();
        Styles.registerStyles(graph.getStylesheet());
        mxGraphComponent graphComponent = new JetFuelGraphComponent(graph);
        createMainPanel(graphComponent);
        JetFuelGraphRenderer render = new JetFuelGraphRenderer(graph);
        render.draw();
    }

    private void createMainPanel(mxGraphComponent graphComponent) {
        final SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(graphComponent);

        });

        Button save = new Button("Save");
        save.setOnAction(l -> {
            SwingUtilities.invokeLater(() -> {
                FileUtil.saveGraph(graphComponent, "layout.xml");
            });
        });
        Button load = new Button("Load");
        load.setOnAction(l -> {
            SwingUtilities.invokeLater(() -> {
                FileUtil.loadGraph(graphComponent, "layout.xml");
            });
        });

        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(5, 5, 5, 5));
        buttonBox.getChildren().addAll(save, load);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        buttonBox.spacingProperty().setValue(10);

        mainPane.setCenter(swingNode);
        mainPane.setBottom(buttonBox);
    }

    public BorderPane getMainPane() {
        return mainPane;
    }
}
