package headfront.guiwidgets;

import headfront.amps.services.TopicService;
import headfront.utils.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.tools.Borders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Deepak on 19/06/2016.
 */
public class NarrowableList {

    private Logger LOG = LoggerFactory.getLogger(NarrowableList.class);

    private List<String> allDataList;
    private int fullSize;
    private String title;
    private String prompt;
    private String filterUpto = null;
    private int widthSize = 200;
    private boolean createTitleFrame;
    private boolean multiSelect;
    private boolean showsize;
    private List<String> checkedItems = new ArrayList<>();
    private final TextField searchBox = TextFields.createClearableTextField();
    private final ListView<String> listView;
    private final Label label = new Label();
    private String lastSearchText = null;
    private Consumer<String> selectionListener;
    private String selectedTopic;
    private Consumer<String> moreRecordsListener;
    private volatile boolean ignoreSearchBox = false;
    private Task<Void> currentSearchTask = null;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean showSelectAllAllNewOptions = false;
    private boolean showSelectAllOptions = false;
    private SelectionType currentSelectionType = SelectionType.SHOW_SELECTED;
    private ToggleButton selectButton = new ToggleButton("Select");
    private ToggleButton allButton = new ToggleButton("ALL");
    private ToggleButton allNewButton = new ToggleButton("ALL New");
    private EventHandler counsumeAllEvents = javafx.event.Event::consume;
    private Map<String, BooleanProperty> allCheckableItems = new HashMap<>();
    private String defaultTooltip = "Showing number of Records / Total number of Records [Selected Records]";

    public enum SelectionType {
        SHOW_SELECTED("Select"),
        SHOW_ALL("Show All"),
        SHOW_NEW("Show New");
        private String desc;

        SelectionType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public NarrowableList(List<String> allDataList, String title, String prompt,
                          boolean createTitleFrame, boolean multiSelect) {
        LOG = LoggerFactory.getLogger(NarrowableList.class.getName() + "[" + title + ']');
        this.allDataList = allDataList;
        this.title = title;
        this.prompt = prompt;
        this.createTitleFrame = createTitleFrame;
        this.multiSelect = multiSelect;
        listView = new ListView<>();
        if (multiSelect) {
            installRightClickOptionsForMultiSelectionList();
            Callback<ListView<String>, ListCell<String>> listViewListCellCallback =
                    CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
                        @Override
                        public ObservableValue<Boolean> call(String item) {
                            BooleanProperty checkedBooleanItem = allCheckableItems.get(item);
                            if (checkedBooleanItem == null) {
                                checkedBooleanItem = new SimpleBooleanProperty();
                                allCheckableItems.put(item, checkedBooleanItem);
                                checkedBooleanItem.addListener((obs, wasSelected, isNowSelected) -> {
                                    if (isNowSelected) {
                                        if (!checkedItems.contains(item)) {
                                            checkedItems.add(item);
                                        }
                                    } else {
                                        checkedItems.remove(item);
                                    }
                                    updateLabel();
                                });
                            }
                            return checkedBooleanItem;
                        }
                    });
            listView.setCellFactory(listViewListCellCallback);
        } else {
            installRightOptionsForSingleSelectionList();
        }
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setSelectedTopic(newValue);
        });
        label.setTooltip(new Tooltip(defaultTooltip));
    }

    private void installRightOptionsForSingleSelectionList() {
        final ContextMenu rightClickContextMenu = new ContextMenu();
        rightClickContextMenu.getItems().addAll(createCopyRightClick());
        listView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                rightClickContextMenu.show(listView, event.getScreenX(), event.getScreenY());
            }
        });
    }

    public void setShowSelectAllAllNewOptions(boolean showSelectAllAllNewOptions) {
        this.showSelectAllAllNewOptions = showSelectAllAllNewOptions;
    }

    public void setShowSelectAllOptions(boolean showSelectAllOptions) {
        this.showSelectAllOptions = showSelectAllOptions;
    }

    private void setSelectedTopic(String newSelectedTopic) {
        if (newSelectedTopic != null && !newSelectedTopic.equals(selectedTopic)) {
            //  LOG.debug("Setting selected item to " + newSelectedTopic);
            this.selectedTopic = newSelectedTopic;
            if (selectionListener != null) {
                selectionListener.accept(selectedTopic);
            }
        }
    }

    public void setDoubleClickListener(Consumer<String> doubleClickListener) {
        listView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                String selectedItems = listView.getSelectionModel().getSelectedItem();
                doubleClickListener.accept(selectedItems);
            }
        });
    }

    private MenuItem createCopyRightClick() {
        MenuItem copyMenuItem = new MenuItem("Copy");
        copyMenuItem.setOnAction(event -> {
            String selectedItem = listView.getSelectionModel().getSelectedItem();
            java.awt.datatransfer.Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String parsedSelectedItem = StringUtils.removeBrackets(selectedItem);
            if (parsedSelectedItem.trim().length() == 0) {
                parsedSelectedItem = selectedItem;
            }
            systemClipboard.setContents(new StringSelection(parsedSelectedItem), null);
        });
        return copyMenuItem;
    }

    private void installRightClickOptionsForMultiSelectionList() {
        final ContextMenu rightClickContextMenu = new ContextMenu();
        MenuItem selectAllMenuItem = new MenuItem("Select All");
        selectAllMenuItem.setOnAction(event -> {
            listView.getItems().forEach(item -> {
                BooleanProperty booleanProperty = allCheckableItems.get(item);
                if (booleanProperty == null) {
                    booleanProperty = new SimpleBooleanProperty();
                    allCheckableItems.put(item, booleanProperty);
                }
                booleanProperty.set(true);
                if (!checkedItems.contains(item)) {
                    checkedItems.add(item);
                }
            });
            updateLabel();
        });
        MenuItem unSelectAllMenuItem = new MenuItem("Unselect All");
        unSelectAllMenuItem.setOnAction(event -> {
            clearAllChecks();
        });

        MenuItem copySelectedMenuItem = new MenuItem("Copy Selected");
        copySelectedMenuItem.setOnAction(event -> {
            List<String> selectedFields = getSelectedFields();
            Collections.sort(selectedFields);
            StringBuilder stringBuilder = new StringBuilder();
            selectedFields.forEach(field -> {
                stringBuilder.append(field);
                stringBuilder.append("\n");
            });
            java.awt.datatransfer.Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            systemClipboard.setContents(new StringSelection(stringBuilder.toString()), null);
        });
        MenuItem resetAllMenuItem = new MenuItem("Reset");
        resetAllMenuItem.setOnAction(event -> {
            final ArrayList<String> copyOfData = new ArrayList<>(allDataList);
            reset();
            setAllDataList(copyOfData);
        });

        rightClickContextMenu.getItems().addAll(createCopyRightClick(), copySelectedMenuItem, selectAllMenuItem, unSelectAllMenuItem, resetAllMenuItem);

        listView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                rightClickContextMenu.show(listView, event.getScreenX(), event.getScreenY());
            }
        });
    }

    public void clearAllChecks() {
        listView.getItems().forEach(item -> {
            BooleanProperty booleanProperty = allCheckableItems.get(item);
            if (booleanProperty == null) {
                booleanProperty = new SimpleBooleanProperty();
                allCheckableItems.put(item, booleanProperty);
            }
            booleanProperty.set(false);
            checkedItems.remove(item);
        });
        updateLabel();
    }

    public void addSelectionListener(Consumer<String> selectionListener) {
        this.selectionListener = selectionListener;
    }

    public Node createComponent() {
        GridPane mainGrid = new GridPane();
        mainGrid.setPadding(new Insets(5, 5, 5, 5));
        mainGrid.setHgap(0);
        mainGrid.setVgap(5);

        rebuildList(null);

        searchBox.setPromptText(prompt);
        searchBox.getStyleClass().add("search-box");
        searchBox.setMinWidth(widthSize);
        searchBox.setMaxWidth(widthSize);
        searchBox.textProperty().addListener(o -> {
            processSearchQuery();
        });
        GridPane.setMargin(searchBox, new Insets(5, 0, 0, 0));

        GridPane.setHgrow(listView, Priority.ALWAYS);
        GridPane.setVgrow(listView, Priority.ALWAYS);
        listView.setMinWidth(widthSize);
        listView.setMaxWidth(widthSize);

        if (createTitleFrame) {
            mainGrid.add(searchBox, 0, 0);
            mainGrid.add(listView, 0, 1);
            Node titledTopicSection = Borders.wrap(mainGrid)
                    .lineBorder()
                    .title(title).innerPadding(0).outerPadding(3)
                    .color(Color.BLUE)
                    .thickness(1)
                    .radius(5, 5, 5, 5)
                    .build().build();

            GridPane.setHgrow(titledTopicSection, Priority.NEVER);
            GridPane.setVgrow(titledTopicSection, Priority.ALWAYS);
            return titledTopicSection;
        } else {
            label.setText(title);
            label.setStyle("-fx-font-weight: bold;");
            if (showSelectAllAllNewOptions || showSelectAllOptions) {
                SegmentedButton selectionTypeButton = null;
                if (showSelectAllOptions) {
                    selectionTypeButton = new SegmentedButton(selectButton, allButton);
                } else {
                    selectionTypeButton = new SegmentedButton(selectButton, allButton, allNewButton);
                }
                ToggleGroup toggleGroup = new ToggleGroup();
                //ensure one toggle is always selected
                toggleGroup.selectedToggleProperty().addListener((ov, toggle, new_toggle) -> {
                    if (new_toggle == null) {
                        toggle.setSelected(true);
                    }
                });
                selectionTypeButton.setToggleGroup(toggleGroup);
                selectButton.setSelected(true);
                selectionTypeButton.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
                selectButton.setOnAction(e -> enableRecordSelection());
                allButton.setOnAction(e -> enableAllSelection());
                allNewButton.setOnAction(e -> enableAllNewSelection());
                GridPane.setHgrow(selectionTypeButton, Priority.ALWAYS);
                GridPane.setVgrow(selectionTypeButton, Priority.NEVER);
                mainGrid.add(selectionTypeButton, 0, 0);
            }
            mainGrid.add(label, 0, 1);
            mainGrid.add(searchBox, 0, 2);
            mainGrid.add(listView, 0, 3);
            GridPane.setHgrow(mainGrid, Priority.NEVER);
            GridPane.setVgrow(mainGrid, Priority.ALWAYS);
            updateLabel();
        }
        return mainGrid;
    }

    public void enableRecordSelection() {
        if (currentSelectionType != SelectionType.SHOW_SELECTED) {
            currentSelectionType = SelectionType.SHOW_SELECTED;
            ignoreSearchBox = false;
            setEnabled(true);
            searchBox.clear();
            clearAllChecks();
        }
    }

    private void enableAllSelection() {
        if (currentSelectionType != SelectionType.SHOW_ALL) {
            currentSelectionType = SelectionType.SHOW_ALL;
            ignoreSearchBox = true;
            setEnabled(false);
            searchBox.setText(SelectionType.SHOW_ALL.getDesc());
            clearAllChecks();
        }
    }

    private void enableAllNewSelection() {
        if (currentSelectionType != SelectionType.SHOW_NEW) {
            currentSelectionType = SelectionType.SHOW_NEW;
            ignoreSearchBox = true;
            setEnabled(false);
            searchBox.setText(SelectionType.SHOW_NEW.getDesc());
            clearAllChecks();
        }
    }

    private void processSearchQuery() {
        if (!ignoreSearchBox) {
            if (currentSearchTask != null) {
                currentSearchTask.cancel();
            }
            currentSearchTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(500);
                    if (!isCancelled()) {
                        Platform.runLater(() -> {
                            try {
                                rebuildList(searchBox.getText());
                            } catch (Exception e) {
                                LOG.error("Exception thrown while execting search " + searchBox.getText(), e);
                            }
                        });
                    }
                    return null;
                }
            };
            executorService.submit(currentSearchTask);
            if (LOG.isDebugEnabled()) {
                currentSearchTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observable,
                                        Worker.State oldValue, Worker.State newState) {
                        LOG.debug("SearchTask oldState  " + oldValue + " new state " + newState);
                    }
                });
            }
        }
    }

    private void updateLabel() {
        if (showsize) {
            String showing = StringUtils.formatNumber(listView.getItems().size());
            String all = StringUtils.formatNumber(fullSize);
            String selected = StringUtils.formatNumber(checkedItems.size());
            label.setText(showing + " / " + all + " [Selected " + selected + "] " + title);
            StringBuilder builder = new StringBuilder();
            if (checkedItems.size() == 0) {
                builder.append("\n\nNo fields selected");
            } else if (checkedItems.size() < 200) {
                builder.append("\n\nSelected Fields\n");
                checkedItems.forEach(item -> builder.append(item + "\n"));
            } else {
                builder.append("\n\nMore than 200 fields selected");
            }
            label.setTooltip(new Tooltip(defaultTooltip + builder.toString()));
        }
    }

    public int getListSize() {
        return listView.getItems().size();
    }

    public void setFilterUpto(String filterUpto) {
        this.filterUpto = filterUpto;
    }

    public void setShowsize(boolean showsize) {
        this.showsize = showsize;
    }

    public void setWidthSize(int widthSize) {
        this.widthSize = widthSize;
    }

    private void rebuildList(String text) {
        if (text == lastSearchText || (text != null && text.equals(lastSearchText))) {
            return;
        }
        if (fullSize > 0) {
            int currentSize = allDataList.size();
            if (fullSize < TopicService.NO_OF_RECORDS_TO_QUERY) {
                narrowLocalList(text);
            } else if (currentSize < TopicService.NO_OF_RECORDS_TO_QUERY && isNewSearchNarrow(text)) {
                narrowLocalList(text);
            } else {
                if (currentSelectionType == SelectionType.SHOW_SELECTED) {
                    requestMoreRecords(text);
                }
            }
        }
        lastSearchText = text;
    }

    private boolean isNewSearchNarrow(String newSearch) {
        if (StringUtils.isValidString(lastSearchText) && StringUtils.isValidString(newSearch)) {
            if (newSearch.length() > lastSearchText.length()) {
                return true;
            }
        }
        return false;
    }

    private void requestMoreRecords(String text) {
        if (moreRecordsListener != null) {
            moreRecordsListener.accept(text);
        } else {
            narrowLocalList(text);
        }
    }


    public String getLastSearchText() {
        return lastSearchText;
    }

    public void setRequestMoreRecordsListener(Consumer<String> moreRecordsListener) {
        this.moreRecordsListener = moreRecordsListener;
    }

    public void narrowLocalList(String text) {
        ObservableList<String> listToPopulate = FXCollections.observableArrayList();
        allDataList.forEach(listEntry -> {
            String entry = listEntry;
            if (filterUpto != null) {
                //@todo use regular expression
                if (entry.contains(filterUpto)) {
                    entry = entry.split(filterUpto)[0];
                }
            }
            if (text == null || entry.toUpperCase().contains(text.toUpperCase())) {
                listToPopulate.add(listEntry);
            }
        });
        FXCollections.sort(listToPopulate);
        if (multiSelect) {
            listView.setItems(listToPopulate);
            checkedItems.forEach(selectedItem -> {
                if (listView.getItems().contains(selectedItem)) {
                    BooleanProperty booleanProperty = allCheckableItems.get(selectedItem);
                    if (booleanProperty != null) {
                        booleanProperty.set(true);
                    }
                }
            });
        } else {
            listView.setItems(listToPopulate);
        }
        if (selectedTopic != null) {
            if (listView.getItems().contains(selectedTopic)) {
                listView.getSelectionModel().select(selectedTopic);
            }
        }
        updateLabel();
    }

    public void selectFirstFromList() {
        listView.getSelectionModel().selectFirst();
    }

    public void clearSelection() {
        listView.getSelectionModel().clearSelection();
    }

    public void clearText() {
        searchBox.clear();
    }

    public void reset() {
        selectedTopic = null;
        allDataList.clear();
        fullSize = 0;
        checkedItems.clear();
        selectButton.setSelected(true);
        ignoreSearchBox = true;
        searchBox.clear();
        ignoreSearchBox = false;
        clearListSelection();
        listView.getItems().clear();
        updateLabel();
        allCheckableItems.clear();
        enableRecordSelection();
    }

    private void clearListSelection() {
        clearAllChecks();
        listView.getSelectionModel().clearSelection();
    }

    public void setAllDataList(List<String> allDataList, int fullSize) {
        clearSelection();
        this.allDataList = allDataList;
        this.fullSize = fullSize;
        narrowLocalList(null);
    }

    public void setAllDataList(List<String> allDataList) {
        selectedTopic = null;
        clearSelection();
        setAllDataList(allDataList, allDataList.size());
    }

    public List<String> getAllDataList() {
        return allDataList;
    }

    public List<String> getSelectedFields() {
        if (multiSelect) {
            List<String> selectedFields = new ArrayList<>();
            selectedFields.addAll(checkedItems);
            return selectedFields;
        } else {
            return listView.getSelectionModel().getSelectedItems();
        }
    }

    public boolean hasSelectedFields() {
        if (multiSelect) {
            return checkedItems.size() > 0;
        } else {
            return listView.getSelectionModel().getSelectedItems() != null;
        }

    }

    public void selectedFields(List<String> fields) {
        selectedTopic = null;
        if (!fields.isEmpty()) {
            if (multiSelect) {
            } else {
                listView.getSelectionModel().select(fields.get(0));
                setSelectedTopic(fields.get(0));
            }
        }
    }

    public void setEnabled(boolean enable) {
        searchBox.setEditable(enable);
        listView.setEditable(enable);
        if (!enable) {
            listView.addEventFilter(MouseEvent.ANY, counsumeAllEvents);
        } else {
            listView.removeEventFilter(MouseEvent.ANY, counsumeAllEvents);
        }
    }

    public SelectionType getCurrentSelectionType() {
        return currentSelectionType;
    }

    public boolean manuallyEnteredSelection() {
        if (currentSelectionType == SelectionType.SHOW_SELECTED) {
            if (getListSize() == 0) {
                String lastText = getLastSearchText();
                if (lastText != null && lastText.trim().length() >= 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
