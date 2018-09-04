package headfront.jetfuelview.panel;

import headfront.guiwidgets.PopUpDialog;
import headfront.jetfuelview.util.TextUtils;
import headfront.utils.FileUtils;
import headfront.utils.StringUtils;
import headfront.utils.WebServiceRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.controlsfx.control.MaskerPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 19/08/2018.
 */
public abstract class AbstractLogonPanel {

    public final static ImageView jetfuelImage = new ImageView(new Image("images/icons/JetFuelMediumNoBg1.png"));
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLogonPanel.class);
    protected final TextField usernameTextField = new TextField();
    protected final PasswordField passwordTextField = new PasswordField();
    private final StackPane mainPanelWithMasker = new StackPane();
    private final MaskerPane maskerPane = new MaskerPane();
    private final BorderPane mainPane = new BorderPane();
    private Consumer<List<String>> validLogon;
    private boolean isJetFuelView;

    public AbstractLogonPanel(Runnable shutdownProcess, Consumer<List<String>> validLogon, boolean isJetFuelView) {
        this.validLogon = validLogon;
        this.isJetFuelView = isJetFuelView;
        final List<Path> files = FileUtils.getFiles("config", "properties");
        if (files.size() == 0) {
            PopUpDialog.showWarningPopup("No properties found", "No properties founds in folder config", 999999999);
        } else {
            String strText = isJetFuelView ? "Select Amps System" : "Select Amps instance";
            final Text text = TextUtils.createText(strText, "fancytextSmallest");
            BorderPane labelPane = new BorderPane();
            labelPane.setPadding(new Insets(10, 10, 0, 10));
            labelPane.setLeft(jetfuelImage);
            labelPane.setCenter(text);
            maskerPane.setText("Validating with AMPS ...");
            maskerPane.setVisible(false);
            mainPane.setTop(labelPane);
            Pane selectionPane = getCenterPane(files);

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> {
                shutdownProcess.run();
            });

            Button loginButton = new Button("Login");
            loginButton.setOnAction(e -> {
                final String selectedItem = getSelectedItem();
                if (selectedItem == null) {
                    PopUpDialog.showWarningPopup("Select Environment", "Please select an environment");
                    return;
                }
                final String username = usernameTextField.getText();
                if (username == null || username.length() == 0) {
                    PopUpDialog.showWarningPopup("Invalid Login Details Environment", "Please enter a username");
                    return;
                }
                final String password = passwordTextField.getText();
                if (password == null || password.length() == 0) {
                    PopUpDialog.showWarningPopup("Invalid Login Details Environment", "Please enter a password");
                    return;
                } else {
                    maskerPane.setVisible(true);
                    validate(selectedItem, username, password);
                }
            });
            HBox buttonBox = new HBox();
            buttonBox.getChildren().addAll(loginButton, cancelButton);
            buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
            buttonBox.spacingProperty().setValue(10);
            buttonBox.setPadding(new Insets(10, 10, 10, 10));
            BorderPane nestedPane = new BorderPane();
            nestedPane.setCenter(selectionPane);
            nestedPane.setBottom(buttonBox);
            mainPane.setCenter(nestedPane);
            mainPanelWithMasker.getChildren().addAll(mainPane, maskerPane);
        }
    }

    public abstract Pane getCenterPane(List<Path> files);

    public Pane getMainPane() {
        return mainPanelWithMasker;
    }


    private void validate(String env, String username, String password) {
        new Thread(() -> {
            String fileToLoad = "config/" + env;
            LOG.info("Loading " + fileToLoad);
            try {
                Properties properties = new Properties();
                properties.load(new FileReader(fileToLoad));
                final String servers = properties.getProperty("servers");
                final String adminPorts = properties.getProperty("adminports");
                final String environment = properties.getProperty("environment");
                checkValidProperties(servers, "servers should be set");
                checkValidProperties(adminPorts, "adminports should be set");
                checkValidProperties(environment, "environment should be set");
                final String[] allServers = servers.split(",");
                final String[] allAdminPorts = adminPorts.split(",");
                if (allServers.length != allAdminPorts.length) {
                    PopUpDialog.showWarningPopup("Invalid config", "Number of servers and adminports should be same in the config");
                    maskerPane.setVisible(false);
                    return;
                }
                if (!isJetFuelView) {
                    final String ampsNames = properties.getProperty("ampsNames");
                    checkValidProperties(ampsNames, "ampsNames should be set");
                    final String[] allAmpsNames = ampsNames.split(",");
                    if (allServers.length != allAmpsNames.length) {
                        PopUpDialog.showWarningPopup("Invalid config", "Number of servers and ampsNames should be same in the config");
                        maskerPane.setVisible(false);
                        return;
                    }
                }
                final String adminUrl = StringUtils.getAdminUrl(allServers[0], allAdminPorts[0]);
                WebServiceRequest request = new WebServiceRequest();
                CountDownLatch waitForReply = new CountDownLatch(1);
                AtomicBoolean loggedIn = new AtomicBoolean(false);
                new Thread(() -> {
                    final String reply = request.doWebRequests(adminUrl);
                    if (reply != null) {
                        loggedIn.set(true);
                    }
                    waitForReply.countDown();
                }).start();
                final boolean timeOut = waitForReply.await(5, TimeUnit.SECONDS);
                if (!timeOut) {
                    maskerPane.setVisible(false);
                    PopUpDialog.showWarningPopup("Amps unreachable", "JetFuelView cant connect to AMPS.");

                } else if (loggedIn.get()) {
                    maskerPane.setVisible(false);
                    List<String> logonDetails = new ArrayList<>();
                    logonDetails.add(env);
                    logonDetails.add(username);
                    logonDetails.add(password);
                    Platform.runLater(() -> {
                        validLogon.accept(logonDetails);
                    });
                } else {
                    maskerPane.setVisible(false);
                    PopUpDialog.showWarningPopup("Invalid Login Details Environment", "Please Try again");

                }
            } catch (Exception var3) {
                LOG.error("Unable to login to amps " + fileToLoad, var3);
            }
        }).start();

    }

    private void checkValidProperties(String properties, String message) {
        if (properties == null || properties.length() == 0) {
            PopUpDialog.showWarningPopup("Invalid config", message);
        }
    }

    public abstract String getSelectedItem() ;
}
