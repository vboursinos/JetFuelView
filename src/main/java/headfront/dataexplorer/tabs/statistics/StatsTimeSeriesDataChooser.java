package headfront.dataexplorer.tabs.statistics;

import headfront.amps.services.TopicService;
import headfront.dataexplorer.DataExplorer;
import headfront.guiwidgets.DateTimePicker;
import headfront.guiwidgets.NarrowableList;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.StringUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.CheckModel;
import org.controlsfx.control.CheckTreeView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 25/10/2016.
 */
public class StatsTimeSeriesDataChooser extends Stage {

    private DateTimePicker startDatePicker = new DateTimePicker();
    private DateTimePicker toDatePicker = new DateTimePicker();
    private CheckTreeView<String> checkTreeView = new CheckTreeView<>();
    private TopicService topicService;
    private boolean userSelectedValidSettings = false;
    private boolean allowSingleSelection;
    private Runnable reloadTreeListener;
    private NarrowableList statsList = new NarrowableList(new ArrayList<>(), "Amps Stats", "Enter Stats name", false, true);
    private Map<String, TreeItem<String>> selectedCache = new HashMap<>();
    private boolean treeFlattend = false;
    private TabPane tabPane = new TabPane();

    public StatsTimeSeriesDataChooser(boolean allowSingleSelection, Runnable reloadTreeListener) {
        this.allowSingleSelection = allowSingleSelection;
        this.reloadTreeListener = reloadTreeListener;
        BorderPane pane = new BorderPane();
        HBox datePane = new HBox();
        datePane.setAlignment(Pos.BASELINE_CENTER);
        initModality(Modality.APPLICATION_MODAL);
        datePane.setSpacing(10);
        datePane.setPadding(new Insets(5, 5, 5, 5));
        Label fromLabel = new Label("From Date");
        fromLabel.setStyle("-fx-font-weight: bold;");
        Label toLabel = new Label("To Date");
        toLabel.setStyle("-fx-font-weight: bold;");
        datePane.getChildren().addAll(fromLabel, startDatePicker, toLabel, toDatePicker);
        pane.setTop(datePane);
        checkTreeView.setMinWidth(540);
        checkTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        HBox viewPane = new HBox();
        viewPane.setAlignment(Pos.BASELINE_CENTER);
        viewPane.setSpacing(10);
        viewPane.setPadding(new Insets(5, 5, 5, 5));
        viewPane.getChildren().addAll(checkTreeView);

        Button reloadTree = new Button("Reload Tree");
        reloadTree.setOnAction(e -> {
            topicService.addMetaDataListener(() -> {
                Platform.runLater(() -> {
                    rebuildTree();
                    resetTree();
                });
            });
            reloadTreeListener.run();
        });
        Button clearButton = new Button("Clear Selection");
        clearButton.setOnAction(e -> {
            resetTree();
        });
        Button okButton = new Button("Show Stats");
        okButton.setOnAction(this::okButtonPressed);
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            hide();
        });

        Tab treeSelectionTab = new Tab("Tree View");
        treeSelectionTab.setClosable(false);
        treeSelectionTab.setContent(viewPane);
        tabPane.getTabs().add(treeSelectionTab);
        Tab searchSelectionTab = new Tab("Search View");
        searchSelectionTab.setClosable(false);
        statsList.setWidthSize(540);
        HBox statListPane = new HBox();
        statListPane.setAlignment(Pos.BASELINE_CENTER);
        statListPane.getChildren().add(statsList.createComponent());
        searchSelectionTab.setContent(statListPane);
        tabPane.getTabs().add(searchSelectionTab);
        pane.setCenter(tabPane);
        HBox buttonPane = new HBox();
        buttonPane.setSpacing(10);
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(new Insets(5, 5, 5, 5));
        buttonPane.getChildren().addAll(reloadTree, clearButton, cancelButton, okButton);
        pane.setBottom(buttonPane);
        getIcons().add(DataExplorer.jetfuelTitlebarImage);
        setTitle("Statistics chooser");
        setWidth(570);
        setScene(new Scene(pane));
    }

    private void resetTree() {
        checkTreeView.getCheckModel().clearChecks();
        statsList.reset();
        List<String> newList = new ArrayList<String>();
        newList.addAll(selectedCache.keySet());
        statsList.setAllDataList(newList);
    }


    private void okButtonPressed(ActionEvent e) {
        if (startDatePicker.getValue() == null) {
            PopUpDialog.showWarningPopup("Start date not selected", "Please select a valid Start Date.");
            return;
        }
        if (toDatePicker.getValue() == null) {
            PopUpDialog.showWarningPopup("To date not selected", "Please select a valid To Date.");
            return;
        }
        if (startDatePicker.getDateTimeValue().isAfter(toDatePicker.getDateTimeValue())) {
            PopUpDialog.showWarningPopup("From date afer to data", "Please select a 'To' data that is after 'From' Date.");
            return;
        }
        CheckModel<TreeItem<String>> checkModel = checkTreeView.getCheckModel();
        if (isTextSearch()) {// text search)
            checkModel.clearChecks();
            List<String> selectedFields = statsList.getSelectedFields();
            selectedFields.forEach(field -> {
                TreeItem<String> treeItem = selectedCache.get(field);
                checkModel.getCheckedItems().add(treeItem);
            });
        }

        if (checkModel.getCheckedItems().size() == 0) {
            PopUpDialog.showWarningPopup("No Stat selected", "Please select one stat to show.");
            return;
        }
        if (checkModel.getCheckedItems().get(0).getChildren().size() > 1) {
            PopUpDialog.showWarningPopup("Invalid selection", "Please only leaf nodes.");
            return;
        }

        if (allowSingleSelection) {
            if (checkModel.getCheckedItems().size() > 1) {
                PopUpDialog.showWarningPopup("No Stat selected", "Please select only one stat to show.");
                return;
            }
        }
        // all good
        userSelectedValidSettings = true;
        hide();
    }

    private boolean isTextSearch() {
        String tabName = tabPane.getSelectionModel().getSelectedItem().textProperty().get();
        if (tabName.toLowerCase().contains("search")) {
            return true;
        }
        return false;
    }

    @Override
    public void showAndWait() {
        rebuildTree();
        super.showAndWait();
    }

    private void rebuildTree() {
        userSelectedValidSettings = false;
        CheckBoxTreeItem<String> ampsStatsMetaData = topicService.getAmpsStatsMetaData();
        checkTreeView.setRoot(ampsStatsMetaData);
        if (!treeFlattend) {
            statsList.setAllDataList(getAllItems(ampsStatsMetaData));
            treeFlattend = true;
        }
    }

    private List<String> getAllItems(CheckBoxTreeItem<String> ampsStatsMetaData) {
        List<String> allItems = new ArrayList<>();
        addChild(allItems, ampsStatsMetaData);
        return allItems;
    }

    private void addChild(List<String> allItems, TreeItem<String> treeItem) {
        if (treeItem.isLeaf()) {
            String stringTreePath = StringUtils.getFullTreePath(treeItem);
            selectedCache.put(stringTreePath, treeItem);
            allItems.add(stringTreePath);
        } else {
            treeItem.getChildren().forEach(childTreeItem -> addChild(allItems, childTreeItem));
        }
    }

    public boolean isUserSelectedValidSettings() {
        return userSelectedValidSettings;
    }

    public LocalDateTime getStartDate() {
        return startDatePicker.getDateTimeValue();
    }

    public LocalDateTime getToDate() {
        return toDatePicker.getDateTimeValue();
    }

    public TreeItem<String> getStats() {
        return checkTreeView.getCheckModel().getCheckedItems().get(0);
    }

    public ObservableList<TreeItem<String>> getAllStats() {
        return checkTreeView.getCheckModel().getCheckedItems();
    }


    public void setTopicService(TopicService topicService) {
        this.topicService = topicService;
    }
}
