package headfront.guiwidgets;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by Deepak on 06/04/2016.
 */
public class AlertHandler implements EventHandler<WebEvent<String>> {


    private Stage parent;

    public AlertHandler(final Stage parent) {
        this.parent = parent;
    }

    @Override
    public void handle(WebEvent<String> event) {

        final Stage dialog = new Stage(StageStyle.DECORATED);
        dialog.initOwner(parent);
        dialog.initModality(Modality.WINDOW_MODAL);
        Label dataLabel = new Label();
        dataLabel.setText(event.getData());
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> dialog.close());

        VBox vbox = new VBox(10); // 10 is the spacing between children, adjust as needed
        vbox.getChildren().addAll(dataLabel, okButton);

        Scene scene = new Scene(vbox, Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

}
