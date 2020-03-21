package headfront.dataexplorer.tabs.dialog;

import com.crankuptheamps.client.Client;
import headfront.guiwidgets.DateTimePicker;
import headfront.guiwidgets.PopUpDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class JetFuelDateSelectorDialog extends Dialog<String> {

    private ButtonType okButtonType;
    private DateTimePicker startDatePicker = new DateTimePicker();
    private DateTimePicker toDatePicker = new DateTimePicker();
    private ComboBox selectionComboBox;
    private String ALL = "ALL";
    private String START_OF_DAY = "Start of Day";
    private String DATE_SELECTION = "Select Date";
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private String TODAY_DATE = "";

    @SuppressWarnings("deprecation")
    public JetFuelDateSelectorDialog(String title) {
        LocalDateTime now = LocalDateTime.now().with(LocalTime.MIN);
        TODAY_DATE = formatter.format(now);
        final DialogPane dialogPane = getDialogPane();
        setTitle(title);
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL);
        startDatePicker.setDateTimeValue(LocalDateTime.now().minusDays(1));
        toDatePicker.setDateTimeValue(LocalDateTime.now().plusMinutes(1));

        Label lbMessage = new Label("Select All or enter date range");  //$NON-NLS-1$
        lbMessage.setStyle("-fx-font-weight: bold;");

        final VBox content = new VBox(10);
        content.getChildren().add(lbMessage);
        ObservableList<String> options = FXCollections.observableArrayList(ALL, START_OF_DAY, DATE_SELECTION);
        selectionComboBox = new ComboBox(options);

        content.getChildren().add(createDescField("Show: ", selectionComboBox));
        content.getChildren().add(createDescField("From Date: ", startDatePicker));
        content.getChildren().add(createDescField("To Date: ", toDatePicker));

        selectionComboBox.getSelectionModel().selectedItemProperty().addListener((value, oldValue, newValue) -> {
            if (newValue.equals(DATE_SELECTION)) {
                startDatePicker.setDisable(false);
                toDatePicker.setDisable(true); // we dont use this for now
            } else {
                startDatePicker.setDisable(true);
                toDatePicker.setDisable(true); // we dont use this for now
            }
        });
        selectionComboBox.getSelectionModel().select(START_OF_DAY);

        okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(okButtonType);
        dialogPane.setContent(content);


        setResultConverter(dialogButton -> {
            if (dialogButton.getText().equalsIgnoreCase("cancel")) {
                return null;
            } else {
                String selectedItem = selectionComboBox.getSelectionModel().getSelectedItem().toString();
                if (selectedItem.equalsIgnoreCase(DATE_SELECTION)) {
                    if (startDatePicker.getValue() == null) {
                        PopUpDialog.showWarningPopup("Start date not selected", "Please select a valid Start Date.");
                        return null;
                    }
//                    if (toDatePicker.getDateTimeValue() == null) {
//                        PopUpDialog.showWarningPopup("To date not selected", "Please select a valid To Date.");
//                        return null;
//                    }
//                    if (startDatePicker.getDateTimeValue().isAfter(toDatePicker.getDateTimeValue())) {
//                        PopUpDialog.showWarningPopup("From date after to data", "Please select a 'To' data that is after 'From' Date.");
//                        return null;
//                    }
                }
                if (selectedItem.equalsIgnoreCase(ALL)) {
                    return Client.Bookmarks.EPOCH;
                } else if (selectedItem.equalsIgnoreCase(START_OF_DAY)) {
                    return TODAY_DATE;
                } else {
                    return formatter.format(startDatePicker.getDateTimeValue());
                }
            }
        });
    }


    /**************************************************************************
     *
     * Support classes
     *
     **************************************************************************/
    private Node createDescField(String title, Node node) {
        BorderPane fieldsBox = new BorderPane();
//        fieldsBox.setPadding(new Insets(5, 5, 5, 5));
        Label label = new Label(title);
        HBox labelBox = new HBox();
        labelBox.setAlignment(Pos.CENTER);
        label.setMinWidth(120);
//        label.setStyle("-fx-font-weight: bold;");
        labelBox.getChildren().addAll(label);
        fieldsBox.setLeft(labelBox);
        fieldsBox.setCenter(node);
        return fieldsBox;
    }
}
