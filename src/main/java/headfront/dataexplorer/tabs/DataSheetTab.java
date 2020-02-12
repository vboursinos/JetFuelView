package headfront.dataexplorer.tabs;

import headfront.amps.AmpsConnection;
import headfront.amps.services.TopicService;
import headfront.dataexplorer.DataExplorerChooserPanel;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.controlfx.DataSpreadSheetView;
import headfront.dataexplorer.tabs.datasheet.DataSheetPopulator;
import headfront.guiwidgets.NarrowableList;
import headfront.utils.GuiUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TablePosition;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Deepak on 03/06/2017.
 */
public class DataSheetTab extends Tab {

    private static final Logger LOG = LoggerFactory.getLogger(DataSheetTab.class);
    private final String tabName;
    private final Stage dataChooserStage;
    private final DataExplorerChooserPanel explorerChooserPanel;
    private final TopicService topicService;
    private AmpsConnection connection;
    private GridBase grid;
    private DataSpreadSheetView spreadSheetView;
    private static long counter = 1;
    private String lastSubscriptionStatus = "New Window";
    private long receivedMessagesCount = 0;
    private int recordCount = 0;
    private Consumer<Integer> recordCountListener = s -> {
    };
    private Consumer<Long> msgCountListener = s -> {
    };
    private Consumer<String> subscriptionStatusListener = s -> {
    };
    private boolean sendUpdate;

    private int rowCount = 31; //Will be re-calculated after if incorrect.
    private int columnCount = 100;

    private int lastColumnId;
    private int lastrowId;
    private List<DataSheetPopulator> activePopulators = new ArrayList<>();

    public DataSheetTab(String tabName, Stage dataChooserStage, DataExplorerChooserPanel explorerChooserPanel,
                        TopicService topicService, AmpsConnection connection) {
        super(tabName + "_" + (counter++));
        this.tabName = tabName;
        this.dataChooserStage = dataChooserStage;
        this.explorerChooserPanel = explorerChooserPanel;
        this.topicService = topicService;
        this.connection = connection;
        setClosable(false);
        createTab();
    }

    public void selectionListener(DataExplorerSelection selection) {
        LOG.info("Got request in DataSheet tab " + selection);
        grid.setCellValue(lastrowId, lastColumnId, selection.toBriefString());
        DataSheetPopulator dataSheetPopulator = new DataSheetPopulator(connection, this, lastrowId, lastColumnId, selection, topicService);
        activePopulators.add(dataSheetPopulator);
        dataChooserStage.hide();

    }

    protected void createTab() {
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(createContent());
        setContent(mainPane);
        setOnClosed(val -> {
            stopSubscription();
        });
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startSendingUpdates();
            } else {
                stopSendingUpdate();
            }
        });
        setTooltip(new Tooltip("Data Sheet"));
    }

    public void stopSubscription() {
        activePopulators.forEach(pop -> pop.stop());
        activePopulators.clear();

    }

    private void stopSendingUpdate() {
        sendUpdate = false;
    }

    private void startSendingUpdates() {
        sendUpdate = true;
        msgCountListener.accept(receivedMessagesCount);
        recordCountListener.accept(recordCount);
        subscriptionStatusListener.accept(lastSubscriptionStatus);
    }

    public void setRecordCountListener(Consumer<Integer> recordCountListener) {
        this.recordCountListener = recordCountListener;
        updateMessageSent();
    }

    protected Node createContent() {
        grid = new GridBase(rowCount, columnCount);
        buildGrid(grid);

        spreadSheetView = new DataSpreadSheetView(grid, this::handledSelected, this::handleNewRecordsSelected);
        spreadSheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadSheetView.getColumns().forEach(c -> c.setPrefWidth(100));

        GridPane centerPane = new GridPane();
        centerPane.add(spreadSheetView, 0, 0);
        GridPane.setHgrow(spreadSheetView, Priority.ALWAYS);
        GridPane.setVgrow(spreadSheetView, Priority.ALWAYS);
        return centerPane;
    }

    private void processShowAllNewFromTopic(String topic) {
        DataExplorerSelection dataExplorerSelection = new DataExplorerSelection(topic);
        dataExplorerSelection.setSelectionType(NarrowableList.SelectionType.SHOW_NEW);
        dataExplorerSelection.setFieldSelectionType(NarrowableList.SelectionType.SHOW_ALL);
        selectionListener(dataExplorerSelection);
    }

    private void handleNewRecordsSelected() {
        TablePosition selectedCell = spreadSheetView.getSelectionModel().getFocusedCell();
        if (selectedCell != null) {
            lastColumnId = selectedCell.getColumn();
            lastrowId = selectedCell.getRow();
        }
        GuiUtil.showSelectionList(connection.getSowStatusService().getAllTopic(),
                "Choose a Topic", "Select a topic from list below to show all new records.", this::processShowAllNewFromTopic);

    }

    private void handledSelected() {
        explorerChooserPanel.setDataSheetmode(true);
        dataChooserStage.show();
        TablePosition selectedCell = spreadSheetView.getSelectionModel().getFocusedCell();
        if (selectedCell != null) {
            lastColumnId = selectedCell.getColumn();
            lastrowId = selectedCell.getRow();
        }
    }

    private void buildGrid(GridBase grid) {
        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<>(grid.getRowCount());
        for (int column = 0; column < columnCount; column++) {
            rows.add(getSeparator(grid, column));
        }
        grid.setRows(rows);
    }

    private void updateMessageSent() {
        startSendingUpdates();
    }

    public void setData(int row, int column, Object value) {
        grid.setCellValue(row, column, value);

    }

    private ObservableList<SpreadsheetCell> getSeparator(GridBase grid, int row) {
        final ObservableList<SpreadsheetCell> separator = FXCollections.observableArrayList();
        for (int column = 0; column < grid.getColumnCount(); ++column) {
            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
            cell.setEditable(true);
            cell.getStyleClass().add("separator");
            separator.add(cell);
        }
        return separator;
    }

    public void clear() {
        for (int row = 0; row < grid.getRowCount(); row++) {
            for (int column = 0; column < grid.getColumnCount(); column++) {
                setData(row, column, "");
            }
        }
        stopSubscription();
    }
}
