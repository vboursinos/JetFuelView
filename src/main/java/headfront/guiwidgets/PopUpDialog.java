package headfront.guiwidgets;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * Created by Deepak on 30/07/2016.
 */
public class PopUpDialog {

    public static void showInfoPopup(final String title, String message) {
        Platform.runLater(() -> {
            Notifications notificationBuilder = getNotifications(title, message, 5);
            notificationBuilder.showInformation();
        });
    }

    public static void showInfoPopup(final String title, String message, int dismiss) {
        Platform.runLater(() -> {
            Notifications notificationBuilder = getNotifications(title, message, dismiss);
            notificationBuilder.showInformation();
        });
    }

    public static void showWarningPopup(final String title, String message) {
        Platform.runLater(() -> {
            Notifications notificationBuilder = getNotifications(title, message, 5);
            notificationBuilder.showWarning();
        });
    }

    public static void showWarningPopup(final String title, String message, int dismiss) {
        Platform.runLater(() -> {
            Notifications notificationBuilder = getNotifications(title, message, dismiss);
            notificationBuilder.showWarning();
        });
    }

    private static Notifications getNotifications(String title, String message, int dismiss) {
        return Notifications.create()
                .title(title)
                .text(message)
                .hideAfter(Duration.seconds(dismiss))
                .position(Pos.CENTER);
    }

    public static void showErrorPopup(final String title, String message) {
        Platform.runLater(() -> {
            Notifications notificationBuilder = getNotifications(title, message, 5);
            notificationBuilder.showError();
        });
    }
}
