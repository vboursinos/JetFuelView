package headfront.dataexplorer.tabs;

import headfront.amps.AmpsConnection;
import headfront.convertor.MessageConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.bean.DataBean;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;

import java.util.*;

/**
 * Created by Deepak on 06/07/2016.
 */
public abstract class TableDataTab extends AbstractDataTab {

    protected ObservableList<DataBean> tableData;
    private TableView<DataBean> tableView;
    private Set<String> createdColumns;
    protected int dataCount = 1;
    protected Map<String, DataBean> allDataKeyedByID;

    public TableDataTab(String tabName, AmpsConnection connection, boolean showHistory, MessageConvertor messageConvertor,
                        DataExplorerSelection selection, List<String> recordIds) {
        super(tabName, connection, showHistory, messageConvertor, selection, recordIds);
    }

    @Override
    protected Node createContent() {
        //If I need to add the butons on pane;
//        BorderPane pane = new BorderPane();
//        HBox buttons = new HBox();
//        buttons.setPadding(new Insets(5,5,5,5));
//        buttons.setSpacing(5);
//        buttons.alignmentProperty().setValue(Pos.CENTER);
//        buttons.getChildren().addAll(new Button("aaaaa"),new Button("nnn"),new Button("nnn"));
//        pane.setTop(buttons);
//        pane.setCenter(createTable());
//        return pane;
        return createTable();
    }

    private TableView createTable() {
        tableData = FXCollections.observableArrayList();
        createdColumns = new HashSet<>();
        allDataKeyedByID = new HashMap<>();
        tableView = new TableView<>(tableData);
        tableView.setPlaceholder(new Label("Waiting for data from AMPS."));
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//        tableView.getSelectionModel().setCellSelectionEnabled(true);
        createMainColumn();
        setupCopyPaste();
        return tableView;
    }

    private void setupCopyPaste() {
        // Inspired from stack over flow
        tableView.setOnKeyPressed(event -> {
            KeyCodeCombination copyKeyCodeCompination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
            if (copyKeyCodeCompination.match(event)) {
                ObservableList<TablePosition> selectedCells = tableView.getSelectionModel().getSelectedCells();
                TablePosition position = selectedCells.get(0);
                int row = position.getRow();
                int col = position.getColumn();
                SimpleObjectProperty observableValue = (SimpleObjectProperty) tableView.getColumns().get(col).getCellObservableValue(row);
                String text = "";
                if (observableValue != null) {
                    text = observableValue.get().toString();
                    final ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(text);
                    Clipboard.getSystemClipboard().setContent(clipboardContent);
                }
                event.consume();
            }
        });
    }

    public void updateLastSubscriptionStatus(String message) {
        Platform.runLater(() -> {
            tableView.setPlaceholder(new Label(message));
            super.updateLastSubscriptionStatus(message);
        });
    }

    public abstract void createMainColumn();

    protected String getHistoricID(String currentID) {
        return currentID + "[" + dataCount++ + "]";
    }

    protected <T> void removeAllColumns() {
        ObservableList<TableColumn<DataBean, ?>> columns = tableView.getColumns();
        columns.remove(1, columns.size());
    }

    public boolean isHorizontal() {
        return true;
    }

    private boolean isOutOfFocus(DataBean item, String key) {
        if (item != null) {
            Property<Object> outOfFocus = item.getProperty(key);
            if (outOfFocus != null) {
                if (outOfFocus.getValue().toString().equalsIgnoreCase("true")) {
                    return true;
                }
            }
        }
        return false;
    }

    protected <T> void createTableColumn(String columnName) {
        TableColumn<DataBean, T> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new TableCellFactory<T>(columnName));
        highlightOutOfFocusRecords(column);
        column.setPrefWidth(120);
        tableView.getColumns().addAll(column);
        createdColumns.add(columnName);
    }

    private <T> void highlightOutOfFocusRecords(TableColumn<DataBean, T> column) {
        column.setCellFactory(cell -> new TableCell<DataBean, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || getItem() == null) ? "" : getItem().toString());
                setGraphic(null);
                TableRow<DataBean> currentRow = getTableRow();
                if (currentRow != null) {
                    if (!isEmpty()) {
                        if (isHorizontal()) {
                            if (isOutOfFocus(currentRow.getItem(), AmpsConnection.OOF_KEY)) {
                                currentRow.setStyle("-fx-background-color:lightcoral");
                            } else {
                                currentRow.setStyle("");
                            }
                        } else {
                            TableColumn<DataBean, T> tableColumn = getTableColumn();
                            DataBean outOfFocusBean = allDataKeyedByID.get(AmpsConnection.OOF_KEY);
                            String columnName = tableColumn.getText();
                            if (isOutOfFocus(outOfFocusBean, columnName)) {
                                setStyle("-fx-background-color:lightcoral");
                            } else {
                                setStyle("");
                            }
                        }
                    } else {
                        currentRow.setStyle("");
                    }
                }
            }
        });
    }

    protected void createColumnsIfRequired(Set<String> newColumns) {
        Set<String> columnsToCreate = new HashSet<>();
        newColumns.forEach(column -> {
            if (!createdColumns.contains(column)) {
                columnsToCreate.add(column);
                createdColumns.add(column);
            }
        });
        if (!columnsToCreate.isEmpty()) {
            Platform.runLater(() -> {
                columnsToCreate.forEach(this::createTableColumn);
            });
        }
    }

    protected void createColumnsIfRequired(String column) {
        if (!createdColumns.contains(column)) {
            createdColumns.add(column);
            Platform.runLater(() -> {
                createTableColumn(column);
            });
        }
    }

    public void clearData() {
        allDataKeyedByID.clear();
        tableData.clear();
        super.clearData();
    }

    protected void formatFields(Map<String, Object> dataMap) {
        String timestampField = "timestamp";
        String timestamp = (String) dataMap.get(timestampField);
        if (timestamp != null) {
            String[] parts = timestamp.split("");
            //20160722T230358.386810Z
            String formatedTime = parts[9] + parts[10] + ":"
                    + parts[11] + parts[12] + ":"
                    + parts[13] + parts[14] + "."
                    + parts[16] + parts[17] + parts[18] + parts[19] + parts[20] + parts[21];
            dataMap.put(timestampField, formatedTime);
        }
    }

    public TableView<DataBean> getTableView() {
        return tableView;
    }
}
