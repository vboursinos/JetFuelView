package headfront.jetfuelview.panel;

import headfront.utils.FileUtils;
import headfront.utils.GuiUtil;
import headfront.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Deepak on 31/08/2018.
 */
public class JetFuelExplorerLogonPanel extends AbstractLogonPanel {

    private static final Logger LOG = LoggerFactory.getLogger(JetFuelExplorerLogonPanel.class);

    public JetFuelExplorerLogonPanel(Runnable shutdownProcess, Consumer<List<String>> validLogon) {
        super(shutdownProcess, validLogon, false);
    }

    @Override
    public Pane getCenterPane(List<Path> files) {
        BorderPane selectionPane = new BorderPane();
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        for (Path path : files) {
            tabPane.getTabs().add(createTab(path));
        }
        selectionPane.setCenter(tabPane);
        selectionPane.setMaxWidth(Double.MAX_VALUE);
        GridPane loginPane = new GridPane();
        loginPane.setPadding(new Insets(0, 10, 0, 10));
        loginPane.setHgap(5);
        loginPane.setVgap(5);
        GridPane.setHgrow(usernameTextField, Priority.ALWAYS);
        GridPane.setFillWidth(usernameTextField, true);
        loginPane.add(new Label("Username"), 0, 1);
        loginPane.add(usernameTextField, 1, 1);
        loginPane.add(new Label("Password"), 0, 2);
        loginPane.add(passwordTextField, 1, 2);
        selectionPane.setBottom(loginPane);
        return selectionPane;
    }

    private Tab createTab(Path file) {
        try {
            final String name = file.toFile().getName();
            Properties properties = new Properties();
            properties.load(new FileReader(file.toAbsolutePath().toString()));
            final String servers = properties.getProperty("servers");
            final String adminPorts = properties.getProperty("adminports");
            final String environment = properties.getProperty("environment");
            final String ampsNames = properties.getProperty("ampsNames");

            final String[] allServers = servers.split(",");
            final String[] allAdminPorts = adminPorts.split(",");
            final String[] allAmpsNames = ampsNames.split(",");
            GridPane tabGrid = new GridPane();
            tabGrid.setHgap(10);
            tabGrid.setPadding(new Insets(10, 10, 10, 10));
            Label textLabel = new Label("Amps      ");
            textLabel.setStyle("-fx-font-weight: bold");
            ComboBox<String> serverOptions = new ComboBox<>();
            GridPane.setHgrow(serverOptions, Priority.ALWAYS);
            serverOptions.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(serverOptions, true);
            final List<String> strings = Arrays.asList(allAmpsNames);
            ObservableList<String> listToUse = FXCollections.observableArrayList();
            listToUse.addAll(strings);
            FXCollections.sort(listToUse);
            serverOptions.setItems(listToUse);

            tabGrid.add(textLabel, 0, 0);
            tabGrid.add(serverOptions, 1, 0);

            serverOptions.setEditable(false);
            Tab newTab = new Tab(name.replace(".properties",""), tabGrid);
            if (GuiUtil.isProd(environment)) {
                newTab.setStyle("-fx-background-color: #CD5C5C;");
                tabGrid.setStyle("-fx-background-color: #CD5C5C;");
            } else {
                newTab.setStyle("-fx-background-color: #008000");
                tabGrid.setStyle("-fx-background-color: #008000;");
            }
            return newTab;
        } catch (Exception e) {
            LOG.error("Unable to load file " + file + e);
            return null;
        }
    }

    @Override
    public String getSelectedItem() {
        return null;
    }
}
