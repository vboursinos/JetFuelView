package headfront.jetfuelview.panel;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Deepak on 07/07/2016.
 */
public class JetFuelViewStatusBar extends StatusBar {

    private static final Label activeCountButton = new Label("-");
    private static final Label inactiveCountButton = new Label("-");

    private int activeCount = 0;
    private int inActiveCount = 0;

    public JetFuelViewStatusBar(NotificationPane notificationPane) {
        activeCountButton.setTooltip(new Tooltip("Number of active Servers"));
        activeCountButton.setMaxSize(Double.MAX_VALUE, 24);
        activeCountButton.setMinSize(35, 24);
        activeCountButton.setAlignment(Pos.CENTER);
        activeCountButton.setPadding(new Insets(0, 5, 0, 5));
        activeCountButton.setBackground(new Background(new BackgroundFill(Color.GREEN,
                new CornerRadii(5), new Insets(0, 5, 0, 0))));
        inactiveCountButton.setTooltip(new Tooltip("Number of Inactive Servers"));
        inactiveCountButton.setMaxSize(Double.MAX_VALUE, 24);
        inactiveCountButton.setMinSize(35, 24);
        inactiveCountButton.setAlignment(Pos.CENTER);
        inactiveCountButton.setPadding(new Insets(0, 5, 0, 5));
        inactiveCountButton.setBackground(new Background(new BackgroundFill(Color.RED,
                new CornerRadii(5), new Insets(0, 5, 0, 0))));

        HBox recordCountBox = new HBox();
        recordCountBox.setAlignment(Pos.CENTER);
        recordCountBox.getChildren().addAll(inactiveCountButton, activeCountButton);
        HBox.setHgrow(activeCountButton, Priority.ALWAYS);
        getRightItems().add(recordCountBox);
        showWelcomeMessage();
    }


    public void incrementActiveCount() {
        Platform.runLater(() -> {
            activeCount++;
            activeCountButton.setText("" + activeCount);
        });
    }

    public void decrementActiveCount() {
        Platform.runLater(() -> {
            activeCount--;
            activeCountButton.setText("" + activeCount);
        });
    }

    public void incrementInactiveCount() {
        Platform.runLater(() -> {
            inActiveCount++;
            inactiveCountButton.setText("" + inActiveCount);
        });
    }

    public void decrementInactiveCount() {
        Platform.runLater(() -> {
            inActiveCount--;
            inactiveCountButton.setText("" + inActiveCount);
        });
    }

    public void showWelcomeMessage() {
        Platform.runLater(() -> {
            setText("Welcome to JetFuelView!");
            activeCountButton.setText("0");
            inactiveCountButton.setText("0");
        });
    }

    public void clearCount() {
        inActiveCount = 0;
        activeCount = 0;
    }

    public void updateMessage(String message) {
        Platform.runLater(() -> {
            setText(message + " - [" + DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm:ss")
                    .format(LocalDateTime.now()) + "]");
        });
    }
}
