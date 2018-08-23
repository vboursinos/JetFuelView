package headfront.jetfuelview.panel;

import com.mxgraph.swing.mxGraphComponent;
import headfront.jetfuelview.graph.*;
import headfront.jetfuelview.util.JetFuelViewActions;
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
import org.controlsfx.control.NotificationPane;
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
    private String propertiesFile;
    private final String username;
    private final String credentials;
    public static final String FILE_SUFFIX = "-JetFuelView.xml";
    private JetFuelGraphModel jetFuelGraphModel = null;
    private JetFuelGraphComponent graphComponent;
    private JetFuelGraph graph;
    private JetFuelViewStatusBar jetFuelViewStatusBar;

    public SystemViewPanel(String environment, String propertiesFile, String username, String credentials,
                           JetFuelViewActions jetFuelViewActions) {
        this.environment = environment;
        this.propertiesFile = propertiesFile;
        this.username = username;
        this.credentials = credentials;
        graph = new JetFuelGraph();
        Styles.registerStyles(graph.getStylesheet());
        graphComponent = new JetFuelGraphComponent(graph);
        jetFuelViewStatusBar = new JetFuelViewStatusBar(new NotificationPane());
        jetFuelGraphModel = new JetFuelGraphModel(graph, propertiesFile, username, credentials, environment, jetFuelViewStatusBar);
        jetFuelViewActions.setJetFuelStatusBar(jetFuelViewStatusBar);
        graphComponent.setJetFuelGraphModel(jetFuelGraphModel);
        createMainPanel(graphComponent);
        jetFuelViewActions.loadFromDisk();
        FileUtil.loadGraph(graphComponent, environment + FILE_SUFFIX);
        jetFuelGraphModel.updateFromServer(false);
        jetFuelViewStatusBar.updateMessage("SystemView read from saved config");
    }

    private void createMainPanel(mxGraphComponent graphComponent) {
        final SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(graphComponent);

        });
        final Text text = TextUtils.createText("JetFuelView - System View", "fancytextSmaller");
        BorderPane labelPane = new BorderPane();
        labelPane.setPadding(new Insets(5, 5, 5, 5));
        labelPane.setCenter(text);
        mainPane.setTop(labelPane);
        final Node BorderedPane = Borders.wrap(swingNode)
                .lineBorder()
                .innerPadding(0).outerPadding(0)
                .color(Color.PURPLE)
                .thickness(1)
                .radius(5, 5, 5, 5)
                .build().build();
        BorderPane swingNodePane = new BorderPane();
        swingNodePane.setPadding(new Insets(1, 1, 1, 1));
        swingNodePane.setCenter(BorderedPane);
        mainPane.setCenter(swingNodePane);
        mainPane.setBottom(jetFuelViewStatusBar);
    }

    public BorderPane getMainPane() {
        return mainPane;
    }

    public mxGraphComponent getGraphComponent() {
        return graphComponent;
    }

    public JetFuelGraphModel getJetFuelGraphModel() {
        return jetFuelGraphModel;
    }
}
