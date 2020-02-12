package headfront.guiwidgets;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;

/**
 * Created by Deepak on 07/07/2016.
 */
public class AmpsStatusBar extends StatusBar {

    private static final ImageView ampsConnectedImage = new ImageView(new Image("images/icons/tick-icon.png"));
    private static final ImageView ampsDisconnectedImage = new ImageView(new Image("images/icons/cross-icon.png"));
    private static final ImageView informationImage = new ImageView(new Image("images/icons/information.png"));
    private static final ImageView warningImage = new ImageView(new Image("images/icons/warning.png"));
    private static final ImageView errorImage = new ImageView(new Image("images/icons/error.png"));
    private static final Button connectedButton = new Button("", ampsConnectedImage);
    private static final Button disconnectedButton = new Button("", ampsDisconnectedImage);
    private static final Label rowCountButton = new Label("-");

    private NotificationPane notificationPane;
    private boolean ampsRequiresRestart = false;

    public AmpsStatusBar(NotificationPane notificationPane) {
        this.notificationPane = notificationPane;
        connectedButton.setTooltip(new Tooltip("Connected to Amps"));
        connectedButton.setPadding(Insets.EMPTY);
        disconnectedButton.setTooltip(new Tooltip("Disconnected from Amps"));
        disconnectedButton.setPadding(Insets.EMPTY);
        rowCountButton.setTooltip(new Tooltip("Number of Records"));
        rowCountButton.setMaxSize(Double.MAX_VALUE, 24);
        rowCountButton.setMinSize(35, 24);
        rowCountButton.setAlignment(Pos.CENTER);
        rowCountButton.setPadding(new Insets(0, 10, 0, 10));
        rowCountButton.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                new CornerRadii(5), new Insets(0, 5, 0, 5))));
        HBox recordCountBox = new HBox();
        recordCountBox.setSpacing(15);
        recordCountBox.setAlignment(Pos.CENTER);
        recordCountBox.getChildren().add(rowCountButton);
        HBox.setHgrow(rowCountButton, Priority.ALWAYS);
        getRightItems().add(recordCountBox);
        getRightItems().add(disconnectedButton);
    }

    public void setAmpsConnectionStatus(boolean connected, String message) {
        if (!ampsRequiresRestart) {
            Platform.runLater(() -> {
                if (connected) {
                    getRightItems().set(1, connectedButton);
                    notificationPane.setGraphic(informationImage);
                } else {
                    getRightItems().set(1, disconnectedButton);
                    notificationPane.setGraphic(errorImage);
                }
                notificationPane.setText(message);
                notificationPane.hide();
                if (!notificationPane.isShowing()) {
                    notificationPane.show();
                    // remove after 3 sec
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                        Platform.runLater(() -> notificationPane.hide());
                    }).start();
                }
                setText(message);
            });
        }
    }

    public void updateMessageCount(Long count) {

    }

    public void updateRowCount(int count) {
        Platform.runLater(() -> {
            rowCountButton.setText("" + count);
        });

    }

    public void updateSubscriptionStatus(String status) {
        if (!ampsRequiresRestart) {
            Platform.runLater(() -> {
                setText(status);
            });
        }
    }

    public void showWelcomeMessage() {
        setText("Welcome to JetFuel!");
        rowCountButton.setText("-");
    }

    public void restartAmps() {
        setAmpsConnectionStatus(false, "Please Restart JetFuel ...");
        ampsRequiresRestart = true;
    }
}
