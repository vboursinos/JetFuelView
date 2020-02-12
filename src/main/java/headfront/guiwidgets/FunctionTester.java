package headfront.guiwidgets;

import headfront.dataexplorer.DataExplorer;
import headfront.execute.impl.test.TestJetFuelExecuteService;
import headfront.jetfuel.execute.FunctionExecutionType;
import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.JetFuelExecuteConstants;
import headfront.jetfuel.execute.functions.FunctionParameter;
import headfront.jetfuel.execute.functions.FunctionResponseListener;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;
import headfront.jetfuel.execute.utils.FunctionUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.tools.Borders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by Deepak on 23/05/2017.
 */
public class FunctionTester extends Stage {

    private TextArea outputTextArea = new TextArea();
    private JetFuelFunction function;
    private JetFuelExecute executeService;
    private final Consumer<String> openNewJetFuelSelector;
    private final Runnable updateStat;
    private List<TextField> textfields = new ArrayList();
    private Label idLabel = new Label();
    private CheckBox multiExecuteCheckBox;

    private AtomicBoolean windowShow = new AtomicBoolean(true);

    public FunctionTester(JetFuelFunction function, JetFuelExecute executeService,
                          Consumer<String> openNewJetFuelSelector, Runnable updateStat) {
        this.function = function;
        this.executeService = executeService;
        this.openNewJetFuelSelector = openNewJetFuelSelector;
        this.updateStat = updateStat;
        BorderPane pane = new BorderPane();
        BorderPane topPane = new BorderPane();
        topPane.setTop(createFunctionNameTextField());
        topPane.setCenter(createParametersFields());
        pane.setTop(topPane);
        pane.setCenter(createOutputArea());
        getIcons().add(DataExplorer.jetfuelTitlebarImage);
        setTitle("Calling function '" + function.getFullFunctionName() + "'");
        setWidth(650);
        setScene(new Scene(pane));
        setResizable(true);
        idLabel.setOnMouseClicked(e -> {
            openNewJetFuelSelector.accept(idLabel.getText());
        });
        show();
    }


    private Node createFunctionNameTextField() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        Label label = new Label();
        label.setFont(Font.font("Cambria", 15));
        label.setTextFill(Color.DARKBLUE);
        label.setStyle("-fx-font-weight: bold;");
        label.setText("Function '" + function.getFunctionName() + "'");
        pane.setLeft(label);
        if (function.isAllowMultiExecute()) {
            multiExecuteCheckBox = new CheckBox("Multi Execute [*." + function.getFunctionName() + "]");
            multiExecuteCheckBox.setFont(Font.font("Cambria", FontPosture.ITALIC, 10));
            multiExecuteCheckBox.setTooltip(new Tooltip("Selecting this option will execute all available function with name "
                    + function.getFunctionName()));
            pane.setRight(multiExecuteCheckBox);
        }
        return pane;
    }

    private Node createParametersFields() {
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(0, 10, 0, 10));
        Integer i = 0;
        for (FunctionParameter param : function.getFunctionParameters()) {
            Label label = new Label();
            label.setStyle("-fx-font-weight: bold;");
            label.setText(param.getParameterName() + " [" + param.getParameterType().getSimpleName() + "]");
            label.setMinWidth(290);
            label.setMaxWidth(290);
            TextField value = TextFields.createClearableTextField();
            fillDefaultValues(param, value);
            value.setMinWidth(300);
            Tooltip tooltip = new Tooltip(param.getDescription());
            value.setTooltip(tooltip);
            label.setTooltip(tooltip);
            textfields.add(value);
            HBox box = new HBox();
            box.setSpacing(8);
            box.setPadding(new Insets(3, 3, 3, 3));
            box.setAlignment(Pos.BASELINE_LEFT);
            box.getChildren().addAll(label, value);
            pane.add(box, 0, i++);
        }
        pane.add(createButtonPane(), 0, i++);
        Label noteLabel = new Label("Note: Enter List as [Buy,3] and Map as {Side=Buy,Price=100.55}");
        noteLabel.setStyle("-fx-font-weight: bold;");
        HBox noteBox = new HBox();
        noteBox.setSpacing(8);
        noteBox.setAlignment(Pos.BASELINE_CENTER);
        noteBox.setPadding(new Insets(10, 10, 0, 0));
        noteBox.getChildren().addAll(noteLabel);
        pane.add(noteBox, 0, i++);
        return pane;
    }

    private void fillDefaultValues(FunctionParameter param, TextField value) {
        final Class parameterType = param.getParameterType();
        if (parameterType == String.class) {
            value.setText(param.getParameterName());
        } else if (parameterType == Boolean.class) {
            value.setText("true");
        } else if (parameterType == Double.class) {
            value.setText("5.56");
        } else if (parameterType == Integer.class) {
            value.setText("10");
        } else if (parameterType == Long.class) {
            value.setText("1000");
        }
        if (param.getParameterName().toLowerCase().contains("trader")) {
            value.setText("Deepak");
        }
        if (param.getParameterName().toLowerCase().contains("inst")) {
            value.setText("DE00045245");
        }
        if (param.getParameterName().toLowerCase().contains("status") && parameterType == String.class) {
            value.setText("ON");
        }
    }


    private Node createOutputArea() {
        outputTextArea.setPrefRowCount(6);
        outputTextArea.setPrefColumnCount(200);
        outputTextArea.setWrapText(true);
        outputTextArea.setEditable(false);

        Node titledFunctionDescriptionLabel = Borders.wrap(outputTextArea)
                .lineBorder()
                .title("Output").innerPadding(5).outerPadding(10)
                .color(Color.PURPLE)
                .thickness(2)
                .radius(5, 5, 5, 5)
                .build().build();
        titledFunctionDescriptionLabel.minWidth(Double.MAX_VALUE);
        return titledFunctionDescriptionLabel;
    }

    private Node createButtonPane() {
        HBox idBox = new HBox();
        idBox.setSpacing(8);
        idBox.setAlignment(Pos.BASELINE_LEFT);
        idBox.setPadding(new Insets(10, 10, 0, 0));
        Label desLabel = new Label("ID: ");
        idLabel.setStyle("-fx-font-weight: bold;");
        desLabel.setStyle("-fx-font-weight: bold;");
        idBox.getChildren().addAll(desLabel, idLabel);
        HBox box = new HBox();
        box.setSpacing(8);
        box.setAlignment(Pos.BASELINE_RIGHT);
        box.setPadding(new Insets(10, 10, 0, 10));
        final List<String> idRef = new ArrayList<>();
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            idRef.forEach(i -> {
                executeService.cancelSubscriptionFunctionRequest(i);
            });
            windowShow.set(false);
            close();
        });
        Button cancelSubButton = new Button("Cancel Sub");
        cancelSubButton.setOnAction(e -> {
            if (!idRef.isEmpty()) {
                final String idToRemove = idRef.remove(0);
                executeService.cancelSubscriptionFunctionRequest(idToRemove);
            }
        });
        Button testButton = new Button("Execute");
        testButton.setOnAction(e -> {
            updateStat.run();
            printToOutputTextarea("\n------------- Starting ------------\n");
            final List<Class> parametersTypes = function.getParametersTypes();
            Object[] parametrs = new Object[textfields.size()];
            for (int i = 0; i < parametrs.length; i++) {
                final String text = textfields.get(i).getText().trim();
                try {
                    parametrs[i] = FunctionUtils.createCorrectTypeOfParam(parametersTypes.get(i), text);
                } catch (Exception de) {
                    printToOutputTextarea("Unable to convert " + text + " to type " + parametersTypes.get(i) + " due to  "
                            + de.getMessage() + "  " + de.toString());
                    return;
                }
            }
            String id = "";
            String fullFunctionName = function.getFullFunctionName();
            if (function.isAllowMultiExecute()) {
                if (multiExecuteCheckBox.isSelected()) {
                    fullFunctionName = "*." + function.getFunctionName();
                }
            }
            printToOutputTextarea("Calling function " + fullFunctionName + " with " + Arrays.toString(parametrs) + "\n");
            if (function.getExecutionType() == FunctionExecutionType.RequestResponse) {
                id = executeService.executeFunction(fullFunctionName, parametrs, new FunctionResponseListener() {
                    @Override
                    public void onCompleted(String s, Map<String, Object> map, Object o, Object o1) {
                        String replyFrom = "";
                        if (map.size() > 0) {
                            replyFrom = (String) map.get(JetFuelExecuteConstants.MSG_CREATION_NAME);
                        }
                        printToOutputTextarea("Got reply from " + replyFrom + " onCompleted '" + o + "' with '" + o1 + "'\n");
                    }


                    @Override
                    public void onError(String s, Map<String, Object> map, Object o, Object o1) {
                        String replyFrom = "";
                        if (map.size() > 0) {
                            replyFrom = (String) map.get(JetFuelExecuteConstants.MSG_CREATION_NAME);
                        }
                        printToOutputTextarea("Got reply from " + replyFrom + " onError '" + o + "' with '" + o1 + "'\n");
                    }
                });
            } else {
                id = executeService.executeSubscriptionFunction(fullFunctionName, parametrs, new SubscriptionFunctionResponseListener() {
                    @Override
                    public void onSubscriptionUpdate(String s, Map<String, Object> map, Object o, String s1) {
                        String replyFrom = "";
                        if (map.size() > 0) {
                            replyFrom = (String) map.get(JetFuelExecuteConstants.MSG_CREATION_NAME);
                        }
                        printToOutputTextarea("Got reply from " + replyFrom + " onSubscriptionUpdate '" + o + "' with '" + s1 + "'\n");
                    }

                    @Override
                    public void onSubscriptionStateChanged(String s, Map<String, Object> map, Object o, FunctionState functionState) {
                        String replyFrom = "";
                        if (map.size() > 0) {
                            replyFrom = (String) map.get(JetFuelExecuteConstants.MSG_CREATION_NAME);
                        }
                        printToOutputTextarea("Got reply from " + replyFrom + " onSubscriptionStateChanged '" + o + "' with '" + functionState + "'\n");
                    }

                    @Override
                    public void onCompleted(String s, Map<String, Object> map, Object o, Object o1) {
                        String replyFrom = "";
                        if (map.size() > 0) {
                            replyFrom = (String) map.get(JetFuelExecuteConstants.MSG_CREATION_NAME);
                        }
                        idRef.remove(0);
                        printToOutputTextarea("Got reply from " + replyFrom + " onCompleted '" + o + "' with '" + o1 + "'\n");
                    }


                    @Override
                    public void onError(String s, Map<String, Object> map, Object o, Object o1) {
                        String replyFrom = "";
                        if (map.size() > 0) {
                            replyFrom = (String) map.get(JetFuelExecuteConstants.MSG_CREATION_NAME);
                        }
                        idRef.remove(0);
                        printToOutputTextarea("Got reply from " + replyFrom + " onError '" + o + "' with '" + o1 + "'\n");
                    }
                });
                if (executeService instanceof TestJetFuelExecuteService) {
                    TestJetFuelExecuteService testJetFuelExecuteService = (TestJetFuelExecuteService) executeService;
                    testJetFuelExecuteService.getSubscriptionRegistry().registerActiveClientSubscription(id);
                }
                idRef.add(id);
            }
            printToOutputTextarea("Function ID '" + id + "'\n");
            idLabel.setText(id);
        });
        if (function.getExecutionType() == FunctionExecutionType.RequestResponse) {
            box.getChildren().addAll(testButton, closeButton);
        } else {
            box.getChildren().addAll(testButton, cancelSubButton, closeButton);
        }
        BorderPane pane = new BorderPane();
        pane.setLeft(idBox);
        pane.setRight(box);
        return pane;
    }

    private void printToOutputTextarea(String text) {
        Platform.runLater(() -> {
            if (windowShow.get()) {
                outputTextArea.appendText(text);
            }
        });
    }
}
