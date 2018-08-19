package headfront.jetfuelview.panel;

import com.mxgraph.swing.mxGraphComponent;
import headfront.graph.*;
import headfront.jetfuelview.util.TextUtils;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.controlsfx.tools.Borders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;


/**
 * Created by Deepak on 19/08/2018.
 */
public class SystemViewPanel {

    private static final Logger LOG = LoggerFactory.getLogger(SystemViewPanel.class);
    private BorderPane mainPane = new BorderPane();
    private String environment;
    private static final String FILE_SUFFIX = "-JetFuelView.xml";

    public SystemViewPanel(String env) {
        this.environment = env;
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
                FileUtil.saveGraph(graphComponent, environment + FILE_SUFFIX);
            });
        });
        Button load = new Button("Load");
        load.setOnAction(l -> {
            SwingUtilities.invokeLater(() -> {
                FileUtil.loadGraph(graphComponent, environment + FILE_SUFFIX);
            });
        });

        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(5, 5, 5, 5));
        buttonBox.getChildren().addAll(save, load);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        buttonBox.spacingProperty().setValue(10);
        final Text text = TextUtils.createText("JetFuelView showing system view", "fancytextLarge");
        BorderPane labelPane = new BorderPane();
        labelPane.setPadding(new Insets(5, 5, 10, 5));
        labelPane.setCenter(text);
        mainPane.setTop(labelPane);
        final Node BorderedPane = Borders.wrap(swingNode)
                .lineBorder()
                .innerPadding(0).outerPadding(0)
                .color(Color.BLUE)
                .thickness(3)
                .radius(5, 5, 5, 5)
                .build().build();
        BorderPane swingNodePane = new BorderPane();
        swingNodePane.setPadding(new Insets(5, 5, 10, 5));
        swingNodePane.setCenter(BorderedPane);
        mainPane.setCenter(swingNodePane);
        mainPane.setBottom(buttonBox);
    }

    public BorderPane getMainPane() {
        return mainPane;
    }
}
