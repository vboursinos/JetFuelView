package headfront.jetfuelview.panel;

import headfront.guiwidgets.PopUpDialog;
import headfront.utils.GuiUtil;
import headfront.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import org.controlsfx.control.MaskerPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by Deepak on 31/08/2018.
 */
public class JetFuelExplorerLogonPanel extends AbstractLogonPanel {

    private static final Logger LOG = LoggerFactory.getLogger(JetFuelExplorerLogonPanel.class);

    private final Map<String, ComboBox> allCombobBoxes = new ConcurrentHashMap<>();
    private final TabPane tabPane = new TabPane();
    private String selectedAmpsConnectionUrl = "";
    private String selecteAdminPort = "";
    private String selecteEnvironment = "";
    private boolean selectedSecureHttp = false;

    public JetFuelExplorerLogonPanel(Runnable shutdownProcess, Consumer<List<String>> validLogon) {
        super(shutdownProcess, validLogon, false);
    }

    @Override
    public Pane getCenterPane(List<Path> files) {
        BorderPane selectionPane = new BorderPane();
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
            final String environment = properties.getProperty("environment");
            final String ampsNames = properties.getProperty("ampsNames");

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
            serverOptions.getSelectionModel().select(0);
            final String tabName = name.replace(".properties", "");
            allCombobBoxes.put(tabName, serverOptions);
            Tab newTab = new Tab(tabName, tabGrid);
            if (GuiUtil.isProd(environment)) {
                newTab.setStyle("-fx-background-color: #CD5C5C;");
                tabGrid.setStyle("-fx-background-color: #CD5C5C;");
            } else {
                newTab.setStyle("-fx-background-color: #008000");
                tabGrid.setStyle("-fx-background-color: #008000;");
            }
            return newTab;
        } catch (Exception e) {
            LOG.error("Unable to load file " + file + e, e);
            return null;
        }
    }

    @Override
    public String getSelectedItem() {
        final ComboBox comboBox = allCombobBoxes.get(tabPane.getSelectionModel().getSelectedItem().getText());
        if (comboBox != null) {
            return comboBox.getSelectionModel().getSelectedItem().toString();
        }
        return null;
    }

    @Override
    protected String getAdminUrl(String env, String username, String password, MaskerPane maskerPane) {
        String fileToLoad = getResourceDir() + File.separator + tabPane.getSelectionModel().getSelectedItem().getText() + ".properties";
        LOG.info("Loading " + fileToLoad);
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(fileToLoad));
            final String servers = properties.getProperty("servers");
            final String adminPorts = properties.getProperty("adminports");
            final String environment = properties.getProperty("environment");
            final String securehttps = properties.getProperty("securehttp");
            checkValidProperties(servers, "servers should be set");
            checkValidProperties(adminPorts, "adminports should be set");
            checkValidProperties(environment, "environment should be set");
            checkValidProperties(securehttps, "securehttp should be set");
            final String[] allServers = servers.split(",");
            final String[] allAdminPorts = adminPorts.split(",");
            final String[] securehttp = securehttps.split(",");
            if (allServers.length != allAdminPorts.length) {
                PopUpDialog.showWarningPopup("Invalid config", "Number of servers and adminports should be same in the config");
                maskerPane.setVisible(false);
                return null;
            }
            if (allServers.length != securehttp.length) {
                PopUpDialog.showWarningPopup("Invalid config", "Number of servers and securehttp should be same in the config");
                maskerPane.setVisible(false);
                return null;
            }
            final String ampsNames = properties.getProperty("ampsNames");
            checkValidProperties(ampsNames, "ampsNames should be set");
            final String[] allAmpsNames = ampsNames.split(",");
            if (allServers.length != allAmpsNames.length) {
                PopUpDialog.showWarningPopup("Invalid config", "Number of servers and ampsNames should be same in the config");
                maskerPane.setVisible(false);
                return null;
            }

            for (int i = 0; i < allAmpsNames.length; i++) {
                if (allAmpsNames[i].equals(env)) {
                    selectedAmpsConnectionUrl = allServers[i];
                    selecteAdminPort = allAdminPorts[i];
                    selecteEnvironment = environment;
                    selectedSecureHttp = Boolean.parseBoolean(securehttp[i]);
                    break;
                }
            }
            return StringUtils.getAmpsAdminUrlWithCredential(selectedAmpsConnectionUrl, selecteAdminPort, username, password, selectedSecureHttp);
        } catch (Exception var3) {
            LOG.error("Unable to login to amps " + fileToLoad, var3);
            maskerPane.setVisible(false);
            PopUpDialog.showWarningPopup("Invalid selection", var3.getMessage());

        }
        return null;
    }

    @Override
    protected List<String> getLoginDetails(String env, String username, String password, String useSecureHttp) {
        List<String> logonDetails = new ArrayList<>();
        logonDetails.add(username);
        logonDetails.add(password);
        final String ampsJsonConnectionStringWithCredentials = StringUtils.getAmpsJsonConnectionStringWithCredentials(selectedAmpsConnectionUrl, username, password);
        logonDetails.add(ampsJsonConnectionStringWithCredentials);
        logonDetails.add(selecteAdminPort);
        logonDetails.add(selecteEnvironment);
        logonDetails.add(useSecureHttp);
        return logonDetails;
    }
}
