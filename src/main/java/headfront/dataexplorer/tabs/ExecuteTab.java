package headfront.dataexplorer.tabs;

import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.RecordDisplay;
import headfront.guiwidgets.FunctionTester;
import headfront.guiwidgets.NarrowableList;
import headfront.guiwidgets.PopUpDialog;
import headfront.jetfuel.execute.FunctionAccessType;
import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.functions.FunctionParameter;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.tools.Borders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Deepak on 22/07/2016.
 */
public class ExecuteTab extends Tab {

    private NarrowableList functionsList = new NarrowableList(new ArrayList<>(), "Available Functions", "Select functions", true, false);

    private Consumer<Integer> recordCountListener = count -> {
    };
    private boolean sendUpdate = false;
    private int executedFunctions = 0;
    private int numberOfFunctions = 0;
    private JetFuelExecute executeService;
    private final boolean isProd;
    private TextArea descriptionTextArea = new TextArea();
    private JetFuelFunction selectedFunction;
    private TextField functionNameTextField = new TextField();
    private TextField functionPublisherTextField = new TextField();
    private TextField functionPublisherHostNameTextField = new TextField();
    private TextField functionPublishTimeTextField = new TextField();
    private TextField functionReturnTextField = new TextField();
    private TextField functionExecutionTypeTextField = new TextField();
    private TextField functionAccessTypeTextField = new TextField();
    private TextField functionAllowMultiExecuteTextField = new TextField();
    private Consumer<String> openNewJetFuelSelector;
    private Consumer<DataExplorerSelection> selectionListener;

    public ExecuteTab(String tabName, JetFuelExecute executeService, boolean isProd) {
        super(tabName);
        this.executeService = executeService;
        this.isProd = isProd;
        final String functionTopic = executeService.getFunctionTopic();
        if (functionTopic != null) {
            setTooltip(new Tooltip("Function Topic = " + executeService.getFunctionTopic() +
                    "\nFunction Bus Topic = " + executeService.getFunctionBusTopic()));
        } else {
            setTooltip(new Tooltip("Simulated JetFuelExecute"));
        }
//        executeService.setOnFunctionAddedListener(newFunction -> {
//            Platform.runLater(this::reloadFunctions);
//        });
//        executeService.setOnFunctionRemovedListener(newFunction -> {
//            Platform.runLater(this::reloadFunctions);
//        });
        Platform.runLater(this::reloadFunctions);
        createTab();
    }

    private void createTab() {
        setClosable(false);
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                sendUpdate = true;
                updateMessageSent();
            } else {
                sendUpdate = false;
            }
        });
        reloadFunctions();
        BorderPane borderPane = new BorderPane();
        functionsList.setWidthSize(380);
        BorderPane functionListBox = new BorderPane();
        functionListBox.setPadding(new Insets(10, 0, 10, 10));
        HBox box = new HBox();
        box.setPadding(new Insets(3, 3, 3, 3));
        Button button = new Button("Reload Functions");
        button.setOnAction(e -> reloadFunctions());
        box.setAlignment(Pos.BASELINE_CENTER);
        box.getChildren().addAll(button);
        functionListBox.setTop(box);
        functionListBox.setCenter(functionsList.createComponent());
        borderPane.setLeft(functionListBox);
        borderPane.setCenter(createDetailsPanel());
        setContent(borderPane);
        functionsList.addSelectionListener(this::updateSelection);
    }

    private void reloadFunctions() {
        functionsList.reset();
        Set<String> functions = executeService.getAvailableFunctions();
        ObservableList<String> listToUse = FXCollections.observableArrayList();
        listToUse.addAll(functions);
        FXCollections.sort(listToUse);
        functionsList.setAllDataList(listToUse);
        // clear existing fields
        descriptionTextArea.clear();
        functionNameTextField.clear();
        functionPublisherHostNameTextField.clear();
        functionPublisherTextField.clear();
        functionPublishTimeTextField.clear();
        functionExecutionTypeTextField.clear();
        functionAccessTypeTextField.clear();
        functionAllowMultiExecuteTextField.clear();
        functionReturnTextField.clear();
        numberOfFunctions = listToUse.size();
        updateMessageSent();
    }

    private void updateSelection(String selectedItem) {
        if (selectedItem != null) {
            JetFuelFunction functionsDetails = executeService.getFunction(selectedItem);
            if (functionsDetails != null) {

                descriptionTextArea.setText(getFullDescription(functionsDetails));
                functionNameTextField.setText(functionsDetails.getFullFunctionName());
                functionPublisherHostNameTextField.setText(functionsDetails.getPublisherHostname());
                functionPublisherTextField.setText(functionsDetails.getFunctionPublisherName());
                functionPublishTimeTextField.setText(functionsDetails.getPublisherTime());
                functionExecutionTypeTextField.setText(functionsDetails.getExecutionType().getText());
                functionAccessTypeTextField.setText(functionsDetails.getFunctionAccessType().getText());
                functionAllowMultiExecuteTextField.setText(""+functionsDetails.isAllowMultiExecute());
                String[] split = functionsDetails.getReturnType().toString().split("\\.");
                String returnValueType = split[split.length - 1];
                functionReturnTextField.setText(returnValueType);
                selectedFunction = functionsDetails;
            }
        }
    }

    private String getFullDescription(JetFuelFunction functionsDetails) {
        StringBuilder fullDescription = new StringBuilder();
        // Add Description
        fullDescription.append(functionsDetails.getFunctionDescription());
        fullDescription.append("\n\n");
        fullDescription.append("Return type " + functionsDetails.getReturnType().getSimpleName() + " - ");
        fullDescription.append(functionsDetails.getReturnTypeDescription());
        fullDescription.append("\n\n");
        String methodSingnature = functionsDetails.getReturnType().getSimpleName() + " " + functionsDetails.getFullFunctionName() + " ( ";
        fullDescription.append(methodSingnature);
        final List<FunctionParameter> functionParameters = functionsDetails.getFunctionParameters();
        if (functionParameters.size() == 0) {
            fullDescription.append(")");
        } else {
            fullDescription.append("\n");
            for (int i = 0; i < functionParameters.size(); i++) {
                String description = functionParameters.get(i).toStringWithDescription();
                if (i == (functionParameters.size() - 1)) {
                    description = description.replaceFirst(",", ")");
                }
                fullDescription.append("\t\t");
                fullDescription.append(description);
                fullDescription.append("\n");
            }
//            fullDescription.append("\t\t)");
        }
        return fullDescription.toString();
    }


    private Node createDetailsPanel() {
        BorderPane mainDetailsPanel = new BorderPane();
        mainDetailsPanel.setPadding(new Insets(5, 0, 0, 0));
        Node titledParameterDescriptionLabel = createTextArea("Full Description", 25, descriptionTextArea);
        Button testFunctionButton = new Button("Execute Function");
        testFunctionButton.setTooltip(new Tooltip("Click to test function"));
        testFunctionButton.setOnAction(e -> testFunction());
        Button statsButton = new Button("Show Stats [ 7 Days ]");
        statsButton.setTooltip(new Tooltip("Click to see the 7 days stats for this function"));
        statsButton.setOnAction(e -> showStats());
        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(5, 12, 0, 0));
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(testFunctionButton, statsButton);

        VBox topDescriptionBox = new VBox();

        GridPane gridPane = new GridPane();
        gridPane.add(createDescField("Function Name: ", functionNameTextField), 0, 0, 2, 1);
        gridPane.add(createDescField("Publisher Name: ", functionPublisherTextField), 0, 1);
        gridPane.add(createDescField("Publisher Host: ", functionPublisherHostNameTextField), 1, 1);
        gridPane.add(createDescField("Publish Time: ", functionPublishTimeTextField), 0, 2);
        gridPane.add(createDescField("Return Type: ", functionReturnTextField), 1, 2);
        gridPane.add(createDescField("Execution Type: ", functionExecutionTypeTextField), 0, 3);
        gridPane.add(createDescField("Access Type: ", functionAccessTypeTextField), 1, 3);
        gridPane.add(createDescField("Allow MultiExec: ", functionAllowMultiExecuteTextField), 0, 4);
        gridPane.add(buttonBox, 1, 4);
        topDescriptionBox.getChildren().addAll(gridPane);
        Node titledTopDescriptionBox = Borders.wrap(topDescriptionBox)
                .lineBorder()
                .title("Function Details").innerPadding(5).outerPadding(10)
                .color(Color.PURPLE)
                .thickness(2)
                .radius(5, 5, 5, 5)
                .build().build();
        titledTopDescriptionBox.minWidth(Double.MAX_VALUE);
        mainDetailsPanel.setTop(titledTopDescriptionBox);
        mainDetailsPanel.setCenter(titledParameterDescriptionLabel);
        return mainDetailsPanel;
    }

    private void showStats() {
        if (selectedFunction != null) {
            DataExplorerSelection dataExplorerSelection = new DataExplorerSelection(executeService.getFunctionTopic()+ "_STATS");
            dataExplorerSelection.setRecordDisplayType(RecordDisplay.VERTICAL);
            dataExplorerSelection.setSelectionType(NarrowableList.SelectionType.SHOW_ALL);
            dataExplorerSelection.setFieldSelectionType(NarrowableList.SelectionType.SHOW_ALL);
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            String dateTimeStr = sevenDaysAgo.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String filter = "/FunctionToCall = '" + selectedFunction.getFullFunctionName() +
                    "' AND /Date >= '" + dateTimeStr + "'";
            dataExplorerSelection.setFilter(filter);
            dataExplorerSelection.setShowHistory(false);
            dataExplorerSelection.setDeltaSubcribe(false);
            selectionListener.accept(dataExplorerSelection);
        } else {
            PopUpDialog.showErrorPopup("No Function Selected", "Please select a function to for which you need the stats.");
        }
    }

    private void testFunction() {
        if (selectedFunction != null) {
            if (isProd && selectedFunction.getFunctionAccessType() == FunctionAccessType.Write) {
                PopUpDialog.showErrorPopup("Function cant be executed",
                        "You are connected to PROD system and trying to execute a Write function. This is not allowed !!!");
            } else {
                new FunctionTester(selectedFunction, executeService, openNewJetFuelSelector,
                        this::updateExecutedMessagesCount);
            }
        } else {
            PopUpDialog.showErrorPopup("No Function Selected", "Please select a function to test.");
        }
    }

    private void updateExecutedMessagesCount() {
        executedFunctions++;
    }

    private Node createDescField(String title, TextField textField) {
        BorderPane fieldsBox = new BorderPane();
        fieldsBox.setPadding(new Insets(3, 3, 3, 3));
        Label label = new Label(title);
        HBox labelBox = new HBox();
        labelBox.setAlignment(Pos.CENTER);
        label.setMinWidth(120);
        label.setStyle("-fx-font-weight: bold;");
        labelBox.getChildren().addAll(label);
        textField.setEditable(false);
        fieldsBox.setLeft(labelBox);
        fieldsBox.setCenter(textField);
        textField.setMinWidth(250);
        return fieldsBox;
    }

    private Node createTextArea(String title, int height, TextArea textArea) {
        initialiseTextArea(height, textArea);

        Node titledFunctionDescriptionLabel = Borders.wrap(textArea)
                .lineBorder()
                .title(title).innerPadding(5).outerPadding(10)
                .color(Color.PURPLE)
                .thickness(2)
                .radius(5, 5, 5, 5)
                .build().build();
        titledFunctionDescriptionLabel.minWidth(Double.MAX_VALUE);
        return titledFunctionDescriptionLabel;
    }

    private void initialiseTextArea(int height, TextArea textArea) {
        textArea.setPrefRowCount(height);
        textArea.setPrefColumnCount(200);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-weight: bold;");
    }


    public void setRecordCountListener(Consumer<Integer> recordCountListener) {
        this.recordCountListener = recordCountListener;
        updateMessageSent();
    }

    private void updateMessageSent() {
        if (sendUpdate) {
            recordCountListener.accept(numberOfFunctions);
        }
    }

    public void setTransactionViewer(Consumer<String> openNewJetFuelSelector) {
        this.openNewJetFuelSelector = openNewJetFuelSelector;
    }

    public void setSelectionListener(Consumer<DataExplorerSelection> selectionListener) {
        this.selectionListener = selectionListener;
    }
}
