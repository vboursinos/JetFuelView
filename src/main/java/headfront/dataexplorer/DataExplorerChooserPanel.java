package headfront.dataexplorer;

import headfront.amps.services.TopicMetaData;
import headfront.amps.services.TopicService;
import headfront.dataexplorer.controlfx.AutoCompletionTextAreaBinding;
import headfront.dataexplorer.tabs.dialog.JetFuelDateSelectorDialog;
import headfront.guiwidgets.NarrowableList;
import headfront.guiwidgets.PopUpDialog;
import headfront.guiwidgets.TextAreaDialog;
import headfront.utils.FileUtils;
import headfront.utils.StringUtils;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.util.Callback;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.tools.Borders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Deepak on 08/03/2016..
 */
public class DataExplorerChooserPanel {

    private static final Logger LOG = LoggerFactory.getLogger(DataExplorerChooserPanel.class);

    private MaskerPane maskerPane = new MaskerPane();
    private TopicService topicService;
    private TabPane tabPane = new TabPane();
    private Consumer<DataExplorerSelection> selectionListener;
    private Function<DataExplorerSelection, String> getSow;
    private Function<DataExplorerSelection, String> getCount;
    private Runnable closeWindow;
    private NarrowableList topicsNarrowableList;
    private NarrowableList recordsNarrowableList;
    private NarrowableList fieldsNarrowableList;
    private NarrowableList queryEgNarrowableList;
    private GridPane topicDetailsGridPane;
    private Map<String, Label> detailsLabels = new HashMap<>();
    private TextArea filterTextArea = new TextArea();
    private TextArea optionsTextArea = new TextArea();
    private TextField orderByTextBox = TextFields.createClearableTextField();
    private String selectedTopic;
    private ComboBox<RecordDisplay> displayOptions = new ComboBox<>();
    private ToggleSwitch showRecordHistorySwitch = new ToggleSwitch("Show Record History");
    private ToggleSwitch showDeltatSubscribeSwitch = new ToggleSwitch("Delta Subscribe");
    private Image trashImage = new Image("images/icons/trash.png");
    private Image helpImage = new Image("images/icons/Help.png");
    private Image saveImage = new Image("images/icons/Save.png");
    private Image openImage = new Image("images/icons/open.png");
    private AutoCompletionTextAreaBinding<String> autoCompleteBindingForFilter = null;
    private AutoCompletionTextAreaBinding<String> autoCompleteBindingForOptions = null;
    private AutoCompletionBinding<String> autoCompleteBindingForGroupBy = null;
    private boolean dataSheetmode = false;
    private GridPane mainGrid = new GridPane();
    private Node dataSheetOptionsSection;
    private Node standardOptionsSection;
    private Consumer<DataExplorerSelection> dataSheetSelectionListener;
    private String PROP_FILTER = "FILTER";
    private String PROP_OPTIONS = "OPTION";
    private String PROP_ORDERBY = "ORDERBY";

    public DataExplorerChooserPanel(TopicService topicService, Consumer<DataExplorerSelection> selectionListener,
                                    Function<DataExplorerSelection, String> getSow, Runnable closeWindow,
                                    Function<DataExplorerSelection, String> getCount) {
        this.topicService = topicService;
        this.selectionListener = selectionListener;
        this.getSow = getSow;
        this.getCount = getCount;
        this.closeWindow = closeWindow;
        showDeltatSubscribeSwitch.setSelected(true);
    }

    public Parent createPanel() {
        dataSheetOptionsSection = createDataSheetOptionsSection();
        standardOptionsSection = createStandardOptionsSection();
        StackPane mainPanelWithMasker = new StackPane();
        mainGrid.setPadding(new Insets(10, 10, 10, 10));
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.add(createTopicSection(), 0, 0);
        mainGrid.add(createRecordSelectionSection(), 1, 0);
        mainGrid.add(createFieldListTab(), 2, 0);
        mainGrid.add(standardOptionsSection, 3, 0);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mainGrid);
        borderPane.setBottom(createInfoSection());
        maskerPane.setText("Requesting data from AMPS ...");
        maskerPane.setVisible(false);
        mainPanelWithMasker.getChildren().addAll(borderPane, maskerPane);
        return mainPanelWithMasker;
    }

    public void selectFirstTopic() {
        reset(true);
//        topicsNarrowableList.selectFirstFromList();
    }

    public void selectNoTopic() {
        reset(true);
        topicsNarrowableList.clearText();
        topicsNarrowableList.clearSelection();
    }

    public void setDataExplorerSelection(DataExplorerSelection dataExplorerSelection) {
        reset(true);
        topicsNarrowableList.selectedFields(Arrays.asList(dataExplorerSelection.getTopic()));
        recordsNarrowableList.selectedFields(dataExplorerSelection.getRecords());
        fieldsNarrowableList.selectedFields(dataExplorerSelection.getFields());
        filterTextArea.setText(dataExplorerSelection.getFilter());
    }


    private void showDetailsPanel(Map<String, Object> allTopicDetials) {
        allTopicDetials.forEach((key, value) -> {
            Label label = detailsLabels.get(key);
            if (label != null && value != null) {
                String text = value.toString();
                if (value instanceof Number) {
                    text = StringUtils.formatNumber((Number) value);
                }
                label.setText(text);
            }
        });
    }

    private Node createInfoSection() {
        topicDetailsGridPane = new GridPane();
        topicDetailsGridPane.setPadding(new Insets(5, 5, 5, 5));
        topicDetailsGridPane.setHgap(20);
        createTopicDetailsLabels(TopicService.TOPIC_NAME, 0, 0, true);
        createTopicDetailsLabels(TopicService.TOPIC_KEY, 0, 1, true);
        createTopicDetailsLabels(TopicService.TOPIC_TYPE, 2, 0, false);
        createTopicDetailsLabels(TopicService.TOPIC_MESSAGE_TYPE, 2, 1, false);
        createTopicDetailsLabels(TopicService.TOPIC_SOW_ENABLED, 4, 0, false);
        createTopicDetailsLabels(TopicService.TOPIC_TXN_ENABLED, 4, 1, false);
        createTopicDetailsLabels(TopicService.TOPIC_NO_OF_RECORDS, 6, 0, false);
        createTopicDetailsLabels(TopicService.TOPIC_EXPIRY, 6, 1, false);
//        createTopicDetailsLabels(TopicService.TOPIC_SOW_ENABLED2, 4, 2, false);
//        createTopicDetailsLabels(TopicService.TOPIC_TNX_ENABLED2, 6, 0, false);
//        createTopicDetailsLabels(TopicService.TOPIC_NO_OF_RECORDS2, 6, 1, false);
//        createTopicDetailsLabels(TopicService.TOPIC_EXPIRY2, 6, 2, false);
        Node titledTopicSection = Borders.wrap(topicDetailsGridPane)
                .lineBorder()
                .title("Details of Selection").innerPadding(0).outerPadding(5)
                .color(Color.PURPLE)
                .thickness(2)
                .radius(5, 5, 5, 5)
                .build().build();
        titledTopicSection.maxHeight(100);
        return titledTopicSection;
    }

    private void createTopicDetailsLabels(String name, int x, int y, boolean wide) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold;");
        nameLabel.setMinWidth(80);
        topicDetailsGridPane.add(nameLabel, x, y);
        Label valueLabel = new Label("");
        if (wide) {
            valueLabel.setMinWidth(300);
        } else {
            valueLabel.setMinWidth(130);
        }
        topicDetailsGridPane.add(valueLabel, x + 1, y);
        detailsLabels.put(name, valueLabel);
    }

    private Node createRecordSelectionSection() {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        tabPane.setMinWidth(365);
        tabPane.setMaxWidth(365);
        GridPane.setHgrow(tabPane, Priority.ALWAYS);
        GridPane.setVgrow(tabPane, Priority.ALWAYS);
        tabPane.getTabs().setAll(createRecordTab(), createFilterTab());
        return tabPane;
    }

    private Tab createFilterTab() {
        GridPane mainRecordsGrid = new GridPane();
        mainRecordsGrid.setPadding(new Insets(0, 0, 0, 0));
        mainRecordsGrid.setHgap(0);
        mainRecordsGrid.setVgap(0);
        List<String> queryExamples = ContentProperties.getInstance().getPropertyList("FilterExamples");
        Collections.sort(queryExamples);
        queryEgNarrowableList = new NarrowableList(queryExamples, "Example Queries", "Enter Field", false, false);
        queryEgNarrowableList.setAllDataList(queryExamples, 100);
        queryEgNarrowableList.setWidthSize(350);
        Label label = new Label("Create Filter");
        label.setStyle("-fx-font-weight: bold;");
        Label label2 = new Label(" [Must Match Case]");
        label2.setStyle("-fx-font-size: 11;");
        HBox labelBox = new HBox();
        labelBox.alignmentProperty().setValue(Pos.CENTER);
        labelBox.getChildren().addAll(label, label2);
        Button openButton = new Button("", new ImageView(openImage));
        openButton.setTooltip(new Tooltip("Open saved filter"));
        openButton.setOnAction(e -> {
            Properties properties = FileUtils.readProperties();
            if (properties.containsKey(PROP_FILTER) || properties.containsKey(PROP_OPTIONS)) {
                filterTextArea.clear();
                filterTextArea.setText(properties.get(PROP_FILTER).toString());
                optionsTextArea.clear();
                optionsTextArea.setText(properties.get(PROP_OPTIONS).toString());
                orderByTextBox.clear();
                orderByTextBox.setText(properties.get(PROP_ORDERBY).toString());
            } else {
                filterTextArea.clear();
                optionsTextArea.clear();
                orderByTextBox.clear();
                final String strVal = properties.toString();
                final String substring = strVal.substring(1, strVal.length() - 1);
                filterTextArea.setText(substring);
            }
        });
        Button saveButton = new Button("", new ImageView(saveImage));
        saveButton.setOnAction(e -> {
            Properties properties = new Properties();
            properties.put(PROP_FILTER, filterTextArea.getText());
            properties.put(PROP_OPTIONS, optionsTextArea.getText());
            properties.put(PROP_ORDERBY, orderByTextBox.getText());
            FileUtils.saveFile(properties, false);
        });
        saveButton.setTooltip(new Tooltip("Save filter for future"));
        Button clearButton = new Button("", new ImageView(trashImage));
        clearButton.setTooltip(new Tooltip("Clear filter"));
        clearButton.setOnAction(e -> {
            filterTextArea.clear();
            optionsTextArea.clear();
            orderByTextBox.clear();
            queryEgNarrowableList.clearText();
        });
        Button helpButton = new Button("", new ImageView(helpImage));
        helpButton.setTooltip(new Tooltip("Show help for filter"));
        helpButton.setOnAction(e -> {
            String topHeader = "AMPS Filter Help";
            String filterHelp = (String) ContentProperties.getInstance().getProperty("FilterHelp");
            TextAreaDialog dlg = createAlert(filterHelp);
            dlg.setTitle("Amps filter Help");
            dlg.getDialogPane().setContentText("");
            dlg.getDialogPane().setHeaderText(topHeader);
            dlg.show();
        });
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(0);

        buttonBox.getChildren().addAll(openButton, saveButton, clearButton, helpButton);
        BorderPane topPane = new BorderPane();
        label.alignmentProperty().setValue(Pos.BOTTOM_LEFT);
        topPane.setPadding(new Insets(5, 5, 5, 5));
        topPane.setRight(buttonBox);
        topPane.setLeft(labelBox);
        mainRecordsGrid.add(topPane, 0, 0, 3, 1);
//        filterTextArea.setPrefColumnCount(25);
        filterTextArea.setPrefRowCount(8);
        filterTextArea.setPromptText("Enter custom filter e.g /ID='DE001234565'. Use fields list below to see available fields on this topic. " +
                "Also and see examples below. For more Help click on the ? above.");

        Label optionsLabel = new Label("Options ");
        optionsTextArea.setPrefRowCount(4);
        optionsTextArea.setPromptText("Enter custom projection. For more Help click on the ? above.");
        VBox optionsBox = new VBox();
        optionsBox.setPadding(new Insets(5, 5, 5, 5));
        optionsBox.getChildren().addAll(optionsLabel, optionsTextArea);

        Label orderByLabel = new Label("Order By ");
        orderByTextBox.setPromptText("e.g /Market ASC or /Market DESC");
        VBox groupByBox = new VBox();
        groupByBox.setPadding(new Insets(5, 5, 5, 5));
        groupByBox.getChildren().addAll(orderByLabel, orderByTextBox);

        label.setStyle("-fx-font-weight: bold;");
        HBox textAreaHBox = new HBox();
        textAreaHBox.setPadding(new Insets(5, 5, 5, 5));
        textAreaHBox.getChildren().add(filterTextArea);
        mainRecordsGrid.add(textAreaHBox, 0, 1, 3, 8);
        mainRecordsGrid.add(optionsBox, 0, 9, 3, 4);
        mainRecordsGrid.add(groupByBox, 0, 14, 3, 2);
        mainRecordsGrid.add(queryEgNarrowableList.createComponent(), 0, 16, 3, 20);
        Tab filtersTab = new Tab("Select by Filters");
        filtersTab.setContent(mainRecordsGrid);
        return filtersTab;
    }

    private TextAreaDialog createAlert(String data) {
        TextAreaDialog dlg = new TextAreaDialog(data);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(null);
        return dlg;
    }

    private Node createFieldListTab() {
        fieldsNarrowableList = new NarrowableList(new ArrayList<>(), "Fields", "Enter Field", false, true);
        fieldsNarrowableList.setShowsize(true);
        fieldsNarrowableList.setWidthSize(280);
        fieldsNarrowableList.setShowSelectAllOptions(true);
        Node titledFieldSection = Borders.wrap(fieldsNarrowableList.createComponent())
                .lineBorder()
                .title("Fields").innerPadding(5).outerPadding(3)
                .color(Color.BLUE)
                .thickness(1)
                .radius(5, 5, 5, 5)
                .build().build();
        return titledFieldSection;
    }


    private Tab createRecordTab() {
        recordsNarrowableList = new NarrowableList(new ArrayList<>(), "Records", "Enter Record", false, true);
        recordsNarrowableList.setRequestMoreRecordsListener(recordSearch -> newTopicSelected(selectedTopic, recordSearch, true));
        recordsNarrowableList.setWidthSize(350);
        recordsNarrowableList.setShowsize(true);
        recordsNarrowableList.setShowSelectAllAllNewOptions(true);
        GridPane mainRecordsGrid = new GridPane();
        mainRecordsGrid.setPadding(new Insets(0, 0, 0, 0));
        mainRecordsGrid.setHgap(0);
        mainRecordsGrid.setVgap(0);
        mainRecordsGrid.add(recordsNarrowableList.createComponent(), 0, 0);

        Tab recordsTab = new Tab("Select by Records");
        recordsTab.setContent(mainRecordsGrid);
        return recordsTab;
    }

    private Node createDataSheetOptionsSection() {
        Button selectButton = new Button("JetFuel Sheet Selector");
        selectButton.setMinWidth(180);
        selectButton.setId("choosebutton");
        selectButton.setOnAction(e -> {
            String failureReason = isValidSelection();
            if (failureReason == null) {
                DataExplorerSelection dataExplorerSelection = createDataExplorerSelection();
                dataSheetSelectionListener.accept(dataExplorerSelection);
            } else {
                PopUpDialog.showWarningPopup("Invalid Selection", failureReason);
            }

        });
        Button resetButton = new Button("Reset/Refresh Selection");
        resetButton.setOnAction(e -> {
            selectFirstTopic();
        });
        resetButton.setMinWidth(180);
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            selectionListener.accept(null);
        });
        cancelButton.setMinWidth(180);
        VBox allButtons = new VBox();
        allButtons.setPadding(new Insets(10, 10, 10, 10));
        allButtons.setSpacing(10);
        allButtons.alignmentProperty().setValue(Pos.TOP_CENTER);
        allButtons.getChildren().addAll(resetButton, selectButton, cancelButton);
        HBox.setHgrow(selectButton, Priority.ALWAYS);

        Node titledOptionSection = Borders.wrap(allButtons)
                .lineBorder()
                .title("Options").innerPadding(0).outerPadding(3)
                .color(Color.BLUE)
                .thickness(1)
                .radius(5, 5, 5, 5)
                .build().build();
        return titledOptionSection;
    }

    private Node createStandardOptionsSection() {
        GridPane allOptions = new GridPane();
        allOptions.setPadding(new Insets(10, 10, 10, 10));
        allOptions.setHgap(10);
        allOptions.setVgap(10);
        displayOptions.setItems(FXCollections.observableArrayList(RecordDisplay.TEXTAREA, RecordDisplay.TREE, RecordDisplay.VERTICAL, RecordDisplay.HORIZONTAL));
        displayOptions.getSelectionModel().select(RecordDisplay.HORIZONTAL);
        displayOptions.setCellFactory(new Callback<ListView<RecordDisplay>, ListCell<RecordDisplay>>() {
            @Override
            public ListCell<RecordDisplay> call(ListView<RecordDisplay> param) {
                return new ListCell<RecordDisplay>() {
                    protected void updateItem(RecordDisplay item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getDisplayName());
                            setGraphic(item.getIcon());
                        }
                    }
                };
            }
        });

        displayOptions.setButtonCell(new ListCell<RecordDisplay>() {
            @Override
            protected void updateItem(RecordDisplay item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getDisplayName());
                    setGraphic(item.getIcon());
                }
            }
        });

        Button resetButton = new Button("Reset/Refresh Selection");
        resetButton.setOnAction(e -> {
            selectFirstTopic();
        });
        Button jetFuelSelectorButton = new Button("Scrollable History", new ImageView(DataExplorer.jetfuelButtonImage));
        jetFuelSelectorButton.setTooltip(new Tooltip("Show history of a record and then show real time updates."));
        jetFuelSelectorButton.setOnAction(e -> {
            showJetFuelSelector();
        });
        Button chooseButton = new Button("Show Data");
        chooseButton.setId("choosebutton");
        chooseButton.setOnAction(e -> {
            String failureReason = isValidSelection();
            if (failureReason == null) {
                if (showRecordHistorySwitch.isSelected()) {
                    JetFuelDateSelectorDialog jetFuelDateSelectorDialog = new JetFuelDateSelectorDialog("Select the time from which JetFuel should start reading the journal");
                    jetFuelDateSelectorDialog.initModality(Modality.APPLICATION_MODAL);
                    jetFuelDateSelectorDialog.initOwner(null);
                    Optional<String> selectedValue = jetFuelDateSelectorDialog.showAndWait();
                    if (selectedValue.isPresent()) {
                        DataExplorerSelection dataExplorerSelection = createDataExplorerSelection();
                        dataExplorerSelection.setShowHistory(true);
                        dataExplorerSelection.setJetFuelSelectorStart(selectedValue.get());
                        dataExplorerSelection.setJetFuelSelectorEnd("END of journal"); // this is wrong
                        selectionListener.accept(dataExplorerSelection);
                    }
                } else {
                    DataExplorerSelection dataExplorerSelection = createDataExplorerSelection();
                    selectionListener.accept(dataExplorerSelection);
                }
            } else {
                PopUpDialog.showWarningPopup("Invalid Selection", failureReason);
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            selectionListener.accept(null);
        });
        Button countButton = new Button("Count");
        countButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        countButton.setOnAction(e -> {
            String failureReason = isValidCountSelection();
            if (failureReason == null) {
                DataExplorerSelection dataExplorerSelection = createDataExplorerSelection();
                getCount(dataExplorerSelection);
            } else {
                PopUpDialog.showWarningPopup("Invalid Selection", failureReason);
            }
        });

        Button reloadCacheButton = new Button("Reload Data Cache");
        reloadCacheButton.setTooltip(new Tooltip("This clears the data JetFuel has cached for the chooser"));
        reloadCacheButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        reloadCacheButton.setOnAction(e -> {
            topicService.clearTopicMetaData();
        });

        Button saveSowToFileButton = new Button("Save SOW to File");
        saveSowToFileButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        saveSowToFileButton.setOnAction(e -> {
            String failureReason = isValidSowToFileSelection();
            if (failureReason == null) {
                DataExplorerSelection dataExplorerSelection = createDataExplorerSelection();
                writeSowToFile(dataExplorerSelection);
            } else {
                PopUpDialog.showWarningPopup("Invalid Selection", failureReason);
            }
        });
        HBox okCancelBox = new HBox();
        okCancelBox.setSpacing(10);
        okCancelBox.alignmentProperty().setValue(Pos.CENTER);
        okCancelBox.getChildren().addAll(chooseButton, cancelButton);
        HBox.setHgrow(chooseButton, Priority.ALWAYS);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);

        displayOptions.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        resetButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        jetFuelSelectorButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        chooseButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cancelButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane pane = new BorderPane();
        HBox displayOptionBox = new HBox();
        displayOptionBox.setSpacing(10);
        displayOptionBox.alignmentProperty().setValue(Pos.CENTER);
        displayOptionBox.getChildren().addAll(displayOptions);
        allOptions.add(resetButton, 0, 0);
        allOptions.add(displayOptionBox, 0, 1);
        allOptions.add(showRecordHistorySwitch, 0, 2);
        allOptions.add(showDeltatSubscribeSwitch, 0, 3);
        allOptions.add(jetFuelSelectorButton, 0, 4);
        allOptions.add(okCancelBox, 0, 5);
        pane.setTop(allOptions);

        VBox extraOptionBox = new VBox();
        extraOptionBox.setPadding(new Insets(10, 10, 10, 10));
        extraOptionBox.setSpacing(10);
        extraOptionBox.alignmentProperty().setValue(Pos.CENTER);
        extraOptionBox.getChildren().addAll(saveSowToFileButton, countButton, reloadCacheButton);
        HBox.setHgrow(saveSowToFileButton, Priority.ALWAYS);
        HBox.setHgrow(countButton, Priority.ALWAYS);

        pane.setBottom(extraOptionBox);
        Node titledOptionSection = Borders.wrap(pane)
                .lineBorder()
                .title("Options").innerPadding(0).outerPadding(3)
                .color(Color.BLUE)
                .thickness(1)
                .radius(5, 5, 5, 5)
                .build().build();
        return titledOptionSection;
    }

    private void getCount(DataExplorerSelection dataExplorerSelection) {
        maskerPane.setVisible(true);
        new Thread(() -> {
            Object apply = getCount.apply(dataExplorerSelection);
            if (apply != null) {
                Platform.runLater(() -> {
                    PopUpDialog.showInfoPopup(" Count Selection", "Amps returned " + apply + " records ", 5000);
                    maskerPane.setVisible(false);
                });
            } else {
                PopUpDialog.showWarningPopup("Invalid count Selection", "Unable to get count for " + dataExplorerSelection);
            }
        }).start();
    }

    private void writeSowToFile(DataExplorerSelection dataExplorerSelection) {
        maskerPane.setVisible(true);
        new Thread(() -> {
            Object apply = getSow.apply(dataExplorerSelection);
            if (apply != null) {
                Platform.runLater(() -> {
                    FileUtils.saveFile(apply.toString(), true);
                    maskerPane.setVisible(false);
                    closeWindow.run();
                });
            } else {
                PopUpDialog.showWarningPopup("Invalid Sow Selection", "Unable to get Sow for " + dataExplorerSelection);
            }
        }).start();
    }

    private DataExplorerSelection createDataExplorerSelection() {
        String topicToUse = selectedTopic;
        if (topicsNarrowableList.getSelectedFields().size() == 0) {
            String lastSearchText = topicsNarrowableList.getLastSearchText();
            if (lastSearchText != null && lastSearchText.trim().length() > 0) {
                topicToUse = lastSearchText;
            }
        }
        DataExplorerSelection dataExplorerSelection = new DataExplorerSelection(topicToUse);
        dataExplorerSelection.setRecordDisplayType(displayOptions.getValue());
        final boolean showAllFields = fieldsNarrowableList.getCurrentSelectionType() == NarrowableList.SelectionType.SHOW_ALL;
        if (showAllFields) {
            dataExplorerSelection.setFields(new ArrayList<>());
        } else {
            dataExplorerSelection.setFields(fieldsNarrowableList.getSelectedFields());
        }
        String filterText = filterTextArea.getText();
        dataExplorerSelection.setFilter(filterText);
        dataExplorerSelection.setShowHistory(showRecordHistorySwitch.isSelected());
        String orderByText = orderByTextBox.getText();
        if (orderByText != null && orderByText.trim().length() > 0) {
            dataExplorerSelection.setOrderBy(orderByText);
        }
        List<String> selectedFields = recordsNarrowableList.getSelectedFields();
        if (selectedFields.size() > 0) {
            dataExplorerSelection.setRecords(selectedFields);
        } else {
            if (recordsNarrowableList.getCurrentSelectionType() == NarrowableList.SelectionType.SHOW_SELECTED) {
                String enteredRecord = recordsNarrowableList.getLastSearchText();
                if (enteredRecord != null && enteredRecord.trim().length() > 0) {
                    dataExplorerSelection.setRecords(Arrays.asList(enteredRecord));
                }
            }
        }
        if (filterText != null && filterText.trim().length() > 1) {
            dataExplorerSelection.setSelectionType(NarrowableList.SelectionType.SHOW_ALL);
        } else {
            dataExplorerSelection.setSelectionType(recordsNarrowableList.getCurrentSelectionType());
        }
        dataExplorerSelection.setFieldSelectionType(fieldsNarrowableList.getCurrentSelectionType());
        String tabName = tabPane.getSelectionModel().getSelectedItem().textProperty().get();
        if (tabName.toLowerCase().contains("filter")) {
            String options = optionsTextArea.getText().trim();
            if (options.length() > 0) {
                dataExplorerSelection.setOptions(options);
            }
        }
        dataExplorerSelection.setDeltaSubcribe(showDeltatSubscribeSwitch.isSelected());
        return dataExplorerSelection;
    }

    private void showJetFuelSelector() {
        String failureReason = isValidJetFuelSelection();
        if (failureReason == null) {
            JetFuelDateSelectorDialog jetFuelDateSelectorDialog = new JetFuelDateSelectorDialog("Select the time from which JetFuel should start reading the journal");
            jetFuelDateSelectorDialog.initModality(Modality.APPLICATION_MODAL);
            jetFuelDateSelectorDialog.initOwner(null);
            Optional<String> selectedValue = jetFuelDateSelectorDialog.showAndWait();
            if (selectedValue.isPresent()) {
                DataExplorerSelection dataExplorerSelection = createDataExplorerSelection();
                dataExplorerSelection.setJetFuelSelector(true);
                dataExplorerSelection.setShowHistory(true);
                dataExplorerSelection.setJetFuelSelectorStart(selectedValue.get());
                dataExplorerSelection.setJetFuelSelectorEnd("END of journal"); // this is wrong
                selectionListener.accept(dataExplorerSelection);
            }

        } else {
            PopUpDialog.showWarningPopup("Invalid Jetfuel Selection", failureReason);
        }
    }

    private Node createTopicSection() {
        topicsNarrowableList = new NarrowableList(topicService.getAllTopics(),
                "Topic / View / Queue", "Enter Topic, View or Queue", true, false);
        topicsNarrowableList.setWidthSize(300);
        topicsNarrowableList.addSelectionListener(selected -> newTopicSelected(selected, null, false));
        return topicsNarrowableList.createComponent();
    }

    private void newTopicSelected(String newTopic, String recordSearch, boolean forced) {
        if (newTopic != null && newTopic.trim().length() > 0) {
            selectedTopic = newTopic.split("-")[0].trim();
            maskerPane.setVisible(true);
            if (!forced) {
                reset(false);
            }
            new Thread(() -> {
                TopicMetaData topicMetaData = topicService.getTopicMetaData(selectedTopic, recordSearch);
                Platform.runLater(() -> {
                    maskerPane.setVisible(false);
                    if (topicMetaData != null) {
                        if (recordsNarrowableList != null) {
                            recordsNarrowableList.setAllDataList(topicMetaData.getRecords(), topicMetaData.getRecordCount());
                            recordsNarrowableList.enableRecordSelection();
                            fieldsNarrowableList.setAllDataList(topicMetaData.getFields());
                            showDetailsPanel(topicMetaData.getTopicDetails());
                            setupFilterAutoComplete(topicMetaData.getFields());
                        }
                    } else {
                        PopUpDialog.showWarningPopup("No Topic Details", "Could not find topic details for topic " + selectedTopic);
                    }
                });
            }).start();
        }
    }

    private void setupFilterAutoComplete(List<String> fields) {
        if (autoCompleteBindingForFilter != null) {
            autoCompleteBindingForFilter.dispose();
        }
        if (autoCompleteBindingForOptions != null) {
            autoCompleteBindingForOptions.dispose();
        }
        if (autoCompleteBindingForGroupBy != null) {
            autoCompleteBindingForGroupBy.dispose();
        }
        List<String> groupByOptions = new ArrayList<>();
        Map<String, String> mappings = new HashMap<>();
        fields.forEach(field -> {
            String[] split = field.split(" ");
            String speechMarks = "";
            if (field.toLowerCase().contains("string")) {
                speechMarks = "\"\"";
            }
            mappings.put(field, " /" + split[0] + " = " + speechMarks);
            groupByOptions.add("/" + split[0] + " DESC");
        });
        autoCompleteBindingForFilter = new AutoCompletionTextAreaBinding<>(filterTextArea,
                SuggestionProvider.create(mappings.keySet()), mappings);
        autoCompleteBindingForOptions = new AutoCompletionTextAreaBinding<>(optionsTextArea,
                SuggestionProvider.create(mappings.keySet()), mappings);
        autoCompleteBindingForGroupBy = TextFields.bindAutoCompletion(orderByTextBox, groupByOptions);
    }

    public void reset(boolean resetTopic) {
        if (resetTopic) {
            topicsNarrowableList.clearText();
            topicsNarrowableList.clearSelection();
            topicsNarrowableList.setAllDataList(topicService.getAllTopics());
        }
        recordsNarrowableList.reset();
        fieldsNarrowableList.reset();
        queryEgNarrowableList.clearText();
        filterTextArea.clear();
        optionsTextArea.clear();
        orderByTextBox.clear();
        displayOptions.getSelectionModel().select(RecordDisplay.HORIZONTAL);
        showRecordHistorySwitch.selectedProperty().setValue(false);
        showDeltatSubscribeSwitch.selectedProperty().setValue(true);
    }

    public String isValidJetFuelSelection() {
        String tabName = tabPane.getSelectionModel().getSelectedItem().textProperty().get();
        if (!tabName.toLowerCase().contains("filter")) {
            int availableRecords = recordsNarrowableList.getListSize();
            int recordSize = recordsNarrowableList.getSelectedFields().size();
            if (availableRecords > 0 && recordSize == 0) {
                return "Please select atleast one record";
            }
            String lastSearchText = recordsNarrowableList.getLastSearchText();
            if (availableRecords == 0 && (lastSearchText == null || lastSearchText.trim().length() == 0)) {
                return "Please select atleast a record or enter a record id.";
            }
            if (fieldsNarrowableList.getCurrentSelectionType() != NarrowableList.SelectionType.SHOW_ALL) {
                int fieldSize = fieldsNarrowableList.getSelectedFields().size();
                if (fieldSize == 0 && fieldsNarrowableList.getListSize() > 0) {
                    return "Please select atleast one field or select all fields";
                }
            }
        } else {
            int filterLength = filterTextArea.getText().length();
            int optionsLength = optionsTextArea.getText().length();
            if (filterLength == 0) {
                return "Please enter a filter";
            }
            if (optionsLength != 0) {
                return "Options are not allowed in JetFuel Selection";
            }
            if (fieldsNarrowableList.getCurrentSelectionType() != NarrowableList.SelectionType.SHOW_ALL) {
                if (!fieldsNarrowableList.hasSelectedFields()) {
                    return "Please select atleast one field, or select display type of TextArea or select all fields";
                }
            }
        }

        return null;
    }

    public String isValidSowToFileSelection() {
        if (topicsNarrowableList.getListSize() != 1) {
            if (topicsNarrowableList.getListSize() == 0) {
                String lastText = topicsNarrowableList.getLastSearchText();
                if (lastText == null || lastText.trim().length() == 0) {
                    return "Please select a topic or enter one";
                }
            }
        }
        if (filterTextArea.getText().length() > 0 && recordsNarrowableList.hasSelectedFields()) {
            return "You can query using a filter or select records, not both.";
        }

        String tabName = tabPane.getSelectionModel().getSelectedItem().textProperty().get();
        if (tabName.toLowerCase().contains("filter")) {
            int filterLength = filterTextArea.getText().length();
            if (filterLength == 0) {
                return "Please enter a filter";
            }
            if (fieldsNarrowableList.hasSelectedFields()) {
                return "Please don't select fields as the SOW will write all fields.";
            }
        } else {
            if (recordsNarrowableList.getCurrentSelectionType() == NarrowableList.SelectionType.SHOW_SELECTED) {
                if (!recordsNarrowableList.hasSelectedFields()) {
                    return "Please select atleast one record";
                }
            }
            if (recordsNarrowableList.getCurrentSelectionType() == NarrowableList.SelectionType.SHOW_NEW) {
                return "You cannot select 'ALL New Records' for a sow.";
            }
            if (fieldsNarrowableList.hasSelectedFields()) {
                return "Please don't select fields as the SOW will write all fields.";
            }
        }
        return null;
    }

    public String isValidCountSelection() {
        if (topicsNarrowableList.getListSize() != 1) {
            if (topicsNarrowableList.getListSize() == 0) {
                String lastText = topicsNarrowableList.getLastSearchText();
                if (lastText == null || lastText.trim().length() == 0) {
                    return "Please select a topic or enter one";
                }
            }
        }
        if (filterTextArea.getText().length() > 0 && recordsNarrowableList.hasSelectedFields()) {
            return "You can query using a filter or select records, not both.";
        }

        String tabName = tabPane.getSelectionModel().getSelectedItem().textProperty().get();
        if (tabName.toLowerCase().contains("filter")) {
            int filterLength = filterTextArea.getText().length();
            if (filterLength == 0) {
                return "Please enter a filter";
            }
        } else {
            if (recordsNarrowableList.getCurrentSelectionType() == NarrowableList.SelectionType.SHOW_SELECTED) {
                if (!recordsNarrowableList.hasSelectedFields()) {
                    return "Please select atleast one record";
                }
            }
            if (recordsNarrowableList.getCurrentSelectionType() == NarrowableList.SelectionType.SHOW_NEW) {
                return "You cannot select 'ALL New Records' for a count.";
            }

        }
        return null;
    }

    public String isValidSelection() {
        boolean manuallyEnteredTopic = false;
        if (topicsNarrowableList.getListSize() == 0) {
            String lastText = topicsNarrowableList.getLastSearchText();
            if (lastText == null || lastText.trim().length() == 0) {
                return "Please select a topic or enter one";
            } else {
                manuallyEnteredTopic = true;
            }
        }
        if (displayOptions.getValue() == RecordDisplay.TREE && showRecordHistorySwitch.isSelected()) {
            return "Tree view is not availabe for historical records";
        }
        if (filterTextArea.getText().length() > 0 && recordsNarrowableList.hasSelectedFields()) {
            return "You can query using a filter or select records, not both.";
        }

        String tabName = tabPane.getSelectionModel().getSelectedItem().textProperty().get();
        if (tabName.toLowerCase().contains("filter")) {
            int filterLength = filterTextArea.getText().length();
            int optionsLength = optionsTextArea.getText().length();
            if (filterLength == 0 && optionsLength == 0) {
                return "Please enter a filter or some options";
            }
            if (optionsLength != 0 && fieldsNarrowableList.getCurrentSelectionType() != NarrowableList.SelectionType.SHOW_ALL) {
                return "Please select ALL field when entering options.";
            }
            return checkValidRecordAndFields(manuallyEnteredTopic, true);
        } else {
            if (recordsNarrowableList.getCurrentSelectionType() == NarrowableList.SelectionType.SHOW_SELECTED) {
                if (!recordsNarrowableList.hasSelectedFields() && !recordsNarrowableList.manuallyEnteredSelection()) {
                    return "Please select atleast one record";
                }
                return checkValidRecordAndFields(manuallyEnteredTopic, false);
            }
        }
        return null;
    }

    private String checkValidRecordAndFields(boolean manullayEnteredTopic, boolean filterChoosen) {
        if (!manullayEnteredTopic) {
            if (displayOptions.getSelectionModel().getSelectedItem() != RecordDisplay.TEXTAREA) {
                if (fieldsNarrowableList.getCurrentSelectionType() != NarrowableList.SelectionType.SHOW_ALL) {
                    int fieldSize = fieldsNarrowableList.getSelectedFields().size();
                    if (fieldSize == 0 && fieldsNarrowableList.getListSize() > 0) {
                        return "Please select atleast one field, or select display type of TextField or select all fields";
                    }
                }
            }
        } else {
            if (!filterChoosen) {
                if (recordsNarrowableList.getCurrentSelectionType() == NarrowableList.SelectionType.SHOW_SELECTED) {
                    return "For a manually entered topic you need to select 'All' or 'All New' in the record sections.";

                }
            }
        }
        return null;
    }

    public void forceLoadTopicIfRequired() {
        String lastSearchText = topicsNarrowableList.getLastSearchText();
        if (lastSearchText == null || lastSearchText.trim().length() == 0) {
            if (topicsNarrowableList.getListSize() == 0) {
                new Thread(() -> {
                    Platform.runLater(() -> maskerPane.setVisible(true));
                    LOG.info("Creating a thread to load topic");
                    if (topicsNarrowableList.getListSize() == 0) {
                        try {
                            Platform.runLater(() -> {
                                        reset(true);
                                        maskerPane.setVisible(false);
                                    }
                            );
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            LOG.error("Exception thrown while loading topic", e);
                            maskerPane.setVisible(false);
                        }
                    }
                    LOG.info("Created topics");
                }).start();
            }
        }
    }

    public void setDataSheetmode(boolean newDataSheetmode) {
        if (newDataSheetmode != this.dataSheetmode) {
            if (newDataSheetmode) {
                mainGrid.getChildren().remove(standardOptionsSection);
                mainGrid.add(dataSheetOptionsSection, 3, 0);
            } else {
                mainGrid.getChildren().remove(dataSheetOptionsSection);
                mainGrid.add(standardOptionsSection, 3, 0);
            }
            this.dataSheetmode = newDataSheetmode;
        }

    }

    public void setDataSheetSelectionListener(Consumer<DataExplorerSelection> dataSheetSelectionListener) {
        this.dataSheetSelectionListener = dataSheetSelectionListener;
    }
}
