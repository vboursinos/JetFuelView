package headfront.jetfuelview.util;

import headfront.guiwidgets.PopUpDialog;
import headfront.jetfuelview.JetFuelView;
import headfront.utils.FileUtils;
import headfront.utils.StringUtils;
import headfront.utils.WebServiceRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
public class SelectSystem {

    private static final Logger LOG = LoggerFactory.getLogger(JetFuelView.class);
    private final ComboBox<String> comboBox = new ComboBox<>();
    private final TextField usernameTextField = new TextField();
    private final PasswordField passwordTextField = new PasswordField();
    private final StackPane mainPanelWithMasker = new StackPane();
    private final MaskerPane maskerPane = new MaskerPane();
    private final BorderPane mainPane = new BorderPane();
    private Consumer<List<String>> validLogon;

    public SelectSystem(Runnable shutdownProcess, Consumer<List<String>> validLogon) {
        this.validLogon = validLogon;
        final List<Path> files = FileUtils.getFiles("config", "properties");
        if (files.size() == 0) {
            PopUpDialog.showWarningPopup("No properties found", "No properties founds in folder config", 999999999);
        } else {
            final Text text = createText("Select system JetFuelView should connect to", "fancytextSmaller");
            BorderPane labelPane = new BorderPane();
            labelPane.setPadding(new Insets(5, 5, 0, 5));
            labelPane.setCenter(text);
            maskerPane.setText("Validating with AMPS ...");
            maskerPane.setVisible(false);
            mainPane.setTop(labelPane);
            GridPane selectionPane = new GridPane();
            final List<String> names = files.stream().map(f -> f.toFile().getName()).collect(Collectors.toList());
            ObservableList<String> listToUse = FXCollections.observableArrayList();
            listToUse.addAll(names);
            FXCollections.sort(listToUse);
            selectionPane.setHgap(10);
            selectionPane.setVgap(10);
            selectionPane.setPadding(new Insets(5, 5, 0, 5));
            selectionPane.setMaxWidth(Double.MAX_VALUE);
            selectionPane.setAlignment(Pos.CENTER_LEFT);
            comboBox.getItems().addAll(listToUse);
            comboBox.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(comboBox, Priority.ALWAYS);
            GridPane.setFillWidth(comboBox, true);

            selectionPane.add(new Label("Environment"), 0, 0);
            selectionPane.add(comboBox, 1, 0);
            selectionPane.add(new Label("Username"), 0, 1);
            selectionPane.add(usernameTextField, 1, 1);
            selectionPane.add(new Label("Password"), 0, 2);
            selectionPane.add(passwordTextField, 1, 2);

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> {
                shutdownProcess.run();
            });

            Button loginButton = new Button("Login");
            loginButton.setOnAction(e -> {
                final String selectedItem = comboBox.getSelectionModel().getSelectedItem();
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
            selectionPane.add(buttonBox, 1, 3);
            mainPane.setCenter(selectionPane);
            mainPanelWithMasker.getChildren().addAll(mainPane, maskerPane);
            Platform.runLater(() -> comboBox.requestFocus());
        }
    }

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
                    PopUpDialog.showWarningPopup("Invalid config", "Length of servers and adminports should be same in the config");
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

    private Text createText(String textToUse, String id) {
        Blend blend = new Blend();
        blend.setMode(BlendMode.MULTIPLY);

        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(254, 235, 66, 0.3));
        ds.setOffsetX(5);
        ds.setOffsetY(5);
        ds.setRadius(5);
        ds.setSpread(0.2);

        blend.setBottomInput(ds);

        DropShadow ds1 = new DropShadow();
        ds1.setColor(Color.web("#7b68ee"));
        ds1.setRadius(10);
        ds1.setSpread(0.2);

        Blend blend2 = new Blend();
        blend2.setMode(BlendMode.MULTIPLY);

        InnerShadow is = new InnerShadow();
        is.setColor(Color.web("#00AEDB"));
        is.setRadius(9);
        is.setChoke(0.8);
        blend2.setBottomInput(is);

        InnerShadow is1 = new InnerShadow();
        is1.setColor(Color.web("#f13a00"));
        is1.setRadius(5);
        is1.setChoke(0.4);
        blend2.setTopInput(is1);

        Blend blend1 = new Blend();
        blend1.setMode(BlendMode.MULTIPLY);
        blend1.setBottomInput(ds1);
        blend1.setTopInput(blend2);

        blend.setTopInput(blend1);
        Text text = new Text();
        text.setText(textToUse);
        text.setId(id);
        text.setEffect(blend);
        return text;
    }

}
