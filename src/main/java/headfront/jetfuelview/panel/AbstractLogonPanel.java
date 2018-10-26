package headfront.jetfuelview.panel;

import headfront.guiwidgets.PopUpDialog;
import headfront.jetfuelview.util.TextUtils;
import headfront.utils.FileUtils;
import headfront.utils.StringUtils;
import headfront.utils.WebServiceRequest;
import javafx.application.Platform;
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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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
    private Runnable shutdownProcess;
    private Consumer<List<String>> validLogon;
    private boolean isJetFuelView;
    private String resourceDir = "";

    public AbstractLogonPanel(Runnable shutdownProcess, Consumer<List<String>> validLogon, boolean isJetFuelView) {
        this.shutdownProcess = shutdownProcess;
        this.validLogon = validLogon;
        this.isJetFuelView = isJetFuelView;
    }

    public abstract Pane getCenterPane(List<Path> files);

    public String getResourceDir() {
        return resourceDir;
    }

    public Pane getMainPane() {
        File configLocation = new File("resource");
        if (!configLocation.exists()) {
            configLocation = new File("resources");
            if (!configLocation.exists()) {
                configLocation = new File("config");
            }
        }
        resourceDir = configLocation.getName();
        final List<Path> files = FileUtils.getFiles(resourceDir, "properties");
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
            loginButton.setDefaultButton(true);
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
        return mainPanelWithMasker;
    }

    private void validate(String env, String username, String password) {
        new Thread(() -> {
            try {
                final String adminUrl = getAdminUrl(env, username, password, maskerPane);
                if (adminUrl != null) {
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
                        String useSecureHttp = adminUrl.contains("https://") ? "true" : "false";
                        List<String> logonDetails = getLoginDetails(env, username, password, useSecureHttp);
                        Platform.runLater(() -> {
                            validLogon.accept(logonDetails);
                        });
                    } else {
                        maskerPane.setVisible(false);
                        PopUpDialog.showWarningPopup("Invalid Login Details Environment", "Please Try again");
                    }
                }
            } catch (Exception var3) {
                LOG.error("Unable to login to amps " + env, var3);
                maskerPane.setVisible(false);
                PopUpDialog.showWarningPopup("Invalid selection " + env, var3.getMessage());
            }
        }).start();
    }

    protected abstract List<String> getLoginDetails(String env, String username, String password, String useSecureHttp);

    protected abstract String getAdminUrl(String env, String username, String password, MaskerPane maskerPane);

    protected void checkValidProperties(String properties, String message) {
        if (properties == null || properties.length() == 0) {
            PopUpDialog.showWarningPopup("Invalid config", message + " in the config.", 888);
        }
    }

    public abstract String getSelectedItem();
}
