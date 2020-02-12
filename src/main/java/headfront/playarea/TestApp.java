package headfront.playarea;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deepak on 25/10/2016.
 */
public class TestApp extends Application {

    private DatePicker startDatePicker = new DatePicker();
    private DatePicker toDatePicker = new DatePicker();
//    private ListSelectionView<String> view = new ListSelectionView<>();

    private ValidationSupport validationSupport = new ValidationSupport();
    List<BooleanProperty> allItems = new ArrayList<>();

    public Scene createScene() {
//        view.getSourceItems().addAll("Katja", "Dirk", "Philip", "Jule", "Armin");
        BorderPane pane = new BorderPane();

        validationSupport.registerValidator(startDatePicker, false, (Control c, LocalDate newValue) ->
                ValidationResult.fromWarningIf(startDatePicker, "The date should set", newValue == null));
        validationSupport.registerValidator(toDatePicker, false, (Control c, LocalDate newValue) ->
                ValidationResult.fromWarningIf(toDatePicker, "The date should set", newValue == null));

        HBox datePane = new HBox();
        datePane.setAlignment(Pos.BASELINE_CENTER);
        datePane.setSpacing(10);
        datePane.setPadding(new Insets(5, 5, 5, 5));
        Label fromLabel = new Label("From Date");
        fromLabel.setStyle("-fx-font-weight: bold;");
        Label toLabel = new Label("To Date");
        toLabel.setStyle("-fx-font-weight: bold;");
        datePane.getChildren().addAll(fromLabel, startDatePicker, toLabel, toDatePicker);
        pane.setTop(datePane);
//        view.setPadding(new Insets(0, 5, 0, 5));

        ListView<String> view = new ListView<>();
        for (int i = 1; i <= 10000; i++) {
            String item = "Item " + i;
            view.getItems().add(item);
        }


        Callback<ListView<String>, ListCell<String>> listViewListCellCallback =
                CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(String item) {
                        BooleanProperty observable = new SimpleBooleanProperty();
                        allItems.add(observable);
                        observable.addListener((obs, wasSelected, isNowSelected) ->
                                System.out.println("Check box for " + item + " changed from " + wasSelected + " to " + isNowSelected)
                        );
                        return observable;
                    }
                });
//view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        view.setCellFactory(listViewListCellCallback);
//        listViewListCellCallback.

        installRightClickOptionsForMultiSelectionList(view);

        pane.setCenter(view);
        Button okButton = new Button("Show Stats");
        okButton.setOnAction(e -> {

        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
//            hide();
        });
        HBox buttonPane = new HBox();
        buttonPane.setSpacing(10);
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(new Insets(5, 5, 5, 5));
        buttonPane.getChildren().addAll(cancelButton, okButton);
        pane.setBottom(buttonPane);
        return new Scene(pane);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Graphical Statistics chooser");
        primaryStage.setWidth(600);
        primaryStage.setScene(createScene());
        primaryStage.show();
    }

    private void installRightClickOptionsForMultiSelectionList(ListView view) {
        final ContextMenu rightClickContextMenu = new ContextMenu();
        MenuItem selectAllMenuItem = new MenuItem("Select All");
        selectAllMenuItem.setOnAction(event -> {
//            view.getSelectionModel().selectAll();
//            getCheckModel().checkAll();
            allItems.forEach(e -> e.set(true));
        });
        MenuItem unSelectAllMenuItem = new MenuItem("Unselect All");
        unSelectAllMenuItem.setOnAction(event -> {
            allItems.forEach(e -> e.set(false));
//            view.getCellFactory().call(view);
        });
        rightClickContextMenu.getItems().addAll(selectAllMenuItem, unSelectAllMenuItem);

        view.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                rightClickContextMenu.show(view, event.getScreenX(), event.getScreenY());
            }
        });
    }


    public LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    public LocalDate getToDate() {
        return toDatePicker.getValue();
    }

//    public ObservableList<String> getStats() {
//        return view.getTargetItems();
//    }
}
