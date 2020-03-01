package headfront.jetfuelview.panel;

import headfront.guiwidgets.PopUpDialog;
import headfront.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.MaskerPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 31/08/2018.
 */
public class JetFuelViewLogonPanel extends AbstractLogonPanel {

    private static final Logger LOG = LoggerFactory.getLogger(JetFuelViewLogonPanel.class);

    private ComboBox<String> systemsComboBox = new ComboBox<>();

    public JetFuelViewLogonPanel(Runnable shutdownProcess, Consumer<List<String>> validLogon) {
        super(shutdownProcess, validLogon, true);
    }

    private List<String> getOptionsList(List<Path> files) {
        return files.stream().map(f -> f.toFile().getName()).collect(Collectors.toList());
    }

    @Override
    public Pane getCenterPane(List<Path> files) {
        GridPane selectionPane = new GridPane();
        final List<String> names = getOptionsList(files);
        ObservableList<String> listToUse = FXCollections.observableArrayList();
        listToUse.addAll(names);
        //FXCollections.sort(listToUse);
        selectionPane.setHgap(10);
        selectionPane.setVgap(10);
        selectionPane.setPadding(new Insets(5, 5, 0, 5));
        selectionPane.setMaxWidth(Double.MAX_VALUE);
        selectionPane.setAlignment(Pos.CENTER_LEFT);
        systemsComboBox.getItems().addAll(listToUse);
        systemsComboBox.setMaxWidth(Double.MAX_VALUE);
        systemsComboBox.getSelectionModel().select(0);
        GridPane.setHgrow(systemsComboBox, Priority.ALWAYS);
        GridPane.setFillWidth(systemsComboBox, true);

        selectionPane.add(new Label("Environment"), 0, 0);
        selectionPane.add(systemsComboBox, 1, 0);
        String disableAuth = System.getProperty("DisableAuth");
        if(disableAuth == null) {
            selectionPane.add(new Label("Username"), 0, 1);
            selectionPane.add(usernameTextField, 1, 1);
            selectionPane.add(new Label("Password"), 0, 2);
            selectionPane.add(passwordTextField, 1, 2);
        }
        return selectionPane;
    }

    @Override
    public String getSelectedItem() {
        return systemsComboBox.getSelectionModel().getSelectedItem();
    }

    @Override
    protected String getAdminUrl(String env, String username, String password, MaskerPane maskerPane) {
        String fileToLoad = getResourceDir() + File.separator + env;
        LOG.info("Loading " + fileToLoad);
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(fileToLoad));
            final String servers = properties.getProperty("servers");
            final String adminPorts = properties.getProperty("adminports");
            final String environment = properties.getProperty("environment");
            final String securehttps = properties.getProperty("securehttp");
            final String overrideEnvironment = properties.getProperty("overrideEnvironment");
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
            String[] allOverrideEnvironment = null;
            if (overrideEnvironment != null) {
                allOverrideEnvironment  = overrideEnvironment.split(",");
                if (allServers.length != allOverrideEnvironment.length) {
                    PopUpDialog.showWarningPopup("Invalid config", "Number of servers and overrideEnvironment should be same in the config");
                    maskerPane.setVisible(false);
                    return null;
                }
            }
            return StringUtils.getAmpsAdminUrlWithCredential(allServers[0], allAdminPorts[0], username, password,
                    Boolean.parseBoolean(securehttp[0]));

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
        logonDetails.add(env);
        logonDetails.add(useSecureHttp);
        return logonDetails;
    }
}
