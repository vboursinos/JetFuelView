package headfront.guiwidgets;

import headfront.services.ProcessService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.BiConsumer;

/**
 * Created by Deepak on 03/04/2016.
 */
public class ConfirmHandler implements Callback<String, Boolean> {

    private Stage parent;

    @Autowired
    private ProcessService processService;

    public ConfirmHandler(final Stage parent) {
        this.parent = parent;
    }

    @Override
    public Boolean call(String msg) {
        final SimpleStringProperty confirmationResult = new SimpleStringProperty();
        // initialize the confirmation dialog
        final Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initOwner(parent);
        dialog.initModality(Modality.WINDOW_MODAL);

        Label ampsAdminPageLabel = createOption("Amps Admin Page", msg, dialog, this::openWebLink);
        Label dataViewer = createOption("DataViewer", msg, dialog, this::startJavaProcess);
        Label closeLabel = createOption("Cancel", msg, dialog, (name, mmsg) -> dialog.close());

        VBox vbox = new VBox(10); // 10 is the spacing between children, adjust as needed

        vbox.getChildren().addAll(ampsAdminPageLabel, dataViewer, closeLabel);
        Scene scene = new Scene(vbox, Color.TRANSPARENT);
        dialog.setScene(scene);

        String[] split = msg.split(",");
        dialog.setX(Integer.parseInt(split[2]));
        dialog.setY(Integer.parseInt(split[3]) + 50);
        dialog.showAndWait();
        confirmationResult.get();
        return true;
    }

    private Label createOption(String optionName, String fullCommand, Stage dialog, BiConsumer<String, String> consumer) {
        Label label = new Label(optionName);
        label.setOnMouseClicked(e -> {
            dialog.close();
            consumer.accept(fullCommand, optionName);

        });
        label.setOnMouseEntered(e -> {
            label.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        });
        label.setOnMouseExited(e -> {
            label.setBackground(null);
        });
        return label;
    }

    private void openWebLink(String fullCommand, String optionName) {
        String[] split = fullCommand.split(",");
        Stage stage = new Stage();
        String url = split[1];
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        stage.setScene(new Scene(browser));
        stage.setTitle(split[0] + " " + split[1]);
        stage.setX(100);
        stage.setY(100);
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.show();
        stage.toFront();
    }

    private void startJavaProcess(String fullCommand, String optionName) {

        processService.launchJavaProcess(optionName);
    }
}
