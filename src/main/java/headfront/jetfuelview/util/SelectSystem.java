package headfront.jetfuelview.util;

import headfront.guiwidgets.PopUpDialog;
import headfront.utils.FileUtils;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 19/08/2018.
 */
public class SelectSystem {

    private final ComboBox<String> comboBox = new ComboBox<>();
    private final TextField usernameTextField = new TextField();
    private final PasswordField passwordTextField = new PasswordField();
    private final BorderPane mainPane = new BorderPane();

    public SelectSystem() {
        final List<Path> files = FileUtils.getFiles("config", "properties");
        if (files.size() == 0) {
            PopUpDialog.showWarningPopup("No properties found", "No properties founds in folder config", 999999999);
        } else {
            final Text text = createText("Select system JetFuelView should connect to", "fancytextSmaller");
            BorderPane labelPane = new BorderPane();
            labelPane.setPadding(new Insets(5, 5, 0, 5));
            labelPane.setCenter(text);
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
                if (password == null || username.length() == 0) {
                    PopUpDialog.showWarningPopup("Invalid Login Details Environment", "Please enter a password");
                    return;
                } else {
                    boolean valid = validate(selectedItem, username, password);
                    if (valid) {
                        //go
                    } else {
                        PopUpDialog.showWarningPopup("Invalid Login Details Environment", "Please Try again");
                    }
                }

            });
            HBox buttonBox = new HBox();
            buttonBox.getChildren().addAll(loginButton, cancelButton);
            buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
            buttonBox.spacingProperty().setValue(10);
            selectionPane.add(buttonBox, 1, 3);
            mainPane.setCenter(selectionPane);

            Platform.runLater(() -> comboBox.requestFocus());
        }
    }

    public BorderPane getMainPane() {
        return mainPane;
    }

    private boolean validate(String selectedItem, String username, String password) {
        return username.equalsIgnoreCase("deepak");
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
