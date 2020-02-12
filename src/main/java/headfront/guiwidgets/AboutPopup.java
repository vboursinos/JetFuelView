package headfront.guiwidgets;

import headfront.JetFuel;
import headfront.dataexplorer.DataExplorer;
import headfront.dataexplorer.JetFuelDataExplorerProperties;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Created by Deepak on 31/07/2016.
 */
public class AboutPopup {
    private static final String contactEmail = "deepakcdo@gmail.com";
    private HostServices hostServices;

    public AboutPopup(HostServices hostServices) {
        this.hostServices = hostServices;
        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.getIcons().add(DataExplorer.jetfuelTitlebarImage);
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setTop(JetFuel.jetfuelImage);
        VBox flow = new VBox();
        flow.setPadding(new Insets(5, 5, 5, 5));
        Text jetFuelVersion = new Text("JetFuel DataExplorer Version " + JetFuelDataExplorerProperties.getInstance().getProperty("version"));
        HBox jetFuelVersionbox = new HBox();
        jetFuelVersionbox.getChildren().add(jetFuelVersion);
        jetFuelVersionbox.alignmentProperty().setValue(Pos.CENTER);
        jetFuelVersion.setStyle("-fx-font-weight: bold");
        Text thirdPartyLib = new Text("Special thanks to the following projects: - ");
        thirdPartyLib.setTextAlignment(TextAlignment.LEFT);
        Node controlFx = createLink("\t - Gui Widgets", "http://www.fxexperience.com/controlsfx/");
        Node email = createLink("Email for info ", "mailto:" + contactEmail);
        flow.getChildren().addAll(jetFuelVersionbox, thirdPartyLib, controlFx, email);
        pane.setCenter(flow);
        Button okButton = new Button("OK");
        BorderPane buttonPane = new BorderPane();
        buttonPane.setRight(okButton);
        okButton.setOnAction(e -> stage.hide());
        pane.setBottom(buttonPane);
        stage.setScene(new Scene(pane));
        stage.show();
    }

    private Node createLink(String text, String link) {
        HBox box = new HBox();
        Label label = new Label(text);
        box.alignmentProperty().setValue(Pos.CENTER_LEFT);
        Hyperlink hyperLink = new Hyperlink(link);
        hyperLink.setOnAction(e ->
        {
            hostServices.showDocument(hyperLink.getText());
        });
        box.getChildren().addAll(label, hyperLink);
        return box;
    }
}
