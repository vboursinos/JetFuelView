package headfront.component;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

/**
 * Created by Deepak on 31/07/2016.
 */
public class ComponentInstaller extends Application {

    private ComponentActions actions = new ComponentActions();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene = new Scene(new Group());
        scene.getStylesheets().add("fx.css");
        Wizard wizard = new Wizard(scene);
        wizard.setTitle("Create new Component");
        wizard.setFlow(new Wizard.LinearFlow(createComponentPane(), createSourcePane(),
                createDestinationPane(), createTransformationPane()));
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                System.out.println("Wizard finished, settings: " + wizard.getSettings());
            }
        });

    }

    private WizardPane createComponentPane() {
        GridPane createComponetPane = new GridPane();
        createComponetPane.setVgap(10);
        createComponetPane.setHgap(10);
        int row = 0;

        createComponetPane.add(new Label("Component Name:"), 0, row);
        TextField componentName = createTextField();
        createComponetPane.add(componentName, 1, row++);

        createComponetPane.add(new Label("Start Time [cron Format]:"), 0, row);
        TextField componentStartTime = createTextField();
        createComponetPane.add(componentStartTime, 1, row++);
        componentStartTime.setPromptText("30 23 * * 0-6");

        createComponetPane.add(new Label("Stop Time [cron Format]:"), 0, row);
        TextField componentStopTime = createTextField();
        createComponetPane.add(componentStopTime, 1, row++);
        componentStopTime.setPromptText("25 23 * * 0-6");

        WizardPane page1 = new WizardPane();
        page1.setHeaderText("Starting to create a new component");
        page1.setContent(createComponetPane);
        return page1;
    }

    private WizardPane createSourcePane() {
        return createSelectionPane("Choose data source", "Source", "Source Query :");
    }

    private WizardPane createDestinationPane() {
        return createSelectionPane("Choose destination", "Destination:", "Destination point :");
    }

    private WizardPane createSelectionPane(String title, String choose, String extraField) {
        GridPane createComponetPane = new GridPane();
        createComponetPane.setVgap(10);
        createComponetPane.setHgap(10);
        int row = 0;
        createComponetPane.add(new Label(choose), 0, row);
        ObservableList<String> listToUse = FXCollections.observableArrayList();
        listToUse.add("DB - Sybase - UAT uatServer.headfront.co.uk:8080");
        listToUse.add("DB - Sybase - Dev devServer.headfront.co.uk:8080");
        listToUse.add("AMPS - UAT uatserver.headfront.co.uk:8080/amps/json");
        ComboBox<String> options = new ComboBox<>(listToUse);
        createComponetPane.add(options, 1, row++);
        TextField pollTime = createTextField();

        CheckBox realTime = new CheckBox("");
        realTime.selectedProperty().addListener(t -> {
            if (realTime.isSelected()) {
                pollTime.setEditable(true);
            } else {
                pollTime.setEditable(false);
                pollTime.clear();
            }
        });
        createComponetPane.add(new Label("RealTime"), 0, row);
        createComponetPane.add(realTime, 1, row++);

        createComponetPane.add(new Label(extraField), 0, row);
        createComponetPane.add(pollTime, 1, row);

        WizardPane page1 = new WizardPane();
        page1.setHeaderText(title);
        page1.setContent(createComponetPane);
        return page1;
    }

    private WizardPane createTransformationPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(5, 5, 5, 5));
        BorderPane listPane = new BorderPane();
        listPane.setTop(new Label("Transformations"));
        ObservableList data = FXCollections.observableArrayList();
        ListView<String> transformationsList = new ListView<>(data);
        listPane.setCenter(transformationsList);
        pane.setCenter(listPane);
        pane.setRight(actions.createActionPane());

        actions.setListView(data);
        WizardPane page1 = new WizardPane();
        page1.setHeaderText("Create Transformations");
        page1.setContent(pane);
        page1.setPrefSize(500, 500);
        return page1;
    }

    private TextField createTextField() {
        TextField textField = TextFields.createClearableTextField();
        GridPane.setHgrow(textField, Priority.ALWAYS);
        return textField;
    }
}
