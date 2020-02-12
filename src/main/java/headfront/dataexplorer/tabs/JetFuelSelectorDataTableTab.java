package headfront.dataexplorer.tabs;

import com.crankuptheamps.client.Message;
import headfront.amps.AmpsConnection;
import headfront.convertor.MessageConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.bean.DataBean;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 06/07/2016.
 */
public class JetFuelSelectorDataTableTab extends TableDataTab {

    private String recordIdField;
    private String fieldColumn = "Field";
    private String valueColumn = "Value";
    private List<List<DataBean>> allDataFromAmps = new ArrayList<>();
    private int count = 1;
    private Slider slider = new Slider();
    private Label sliderValue = new Label();

    public JetFuelSelectorDataTableTab(String tabName, AmpsConnection connection, boolean showHistory,
                                       MessageConvertor messageConvertor, List<String> recordIds,
                                       DataExplorerSelection selection) {
        super(tabName, connection, showHistory, messageConvertor, selection, recordIds);
        this.recordIdField = recordIds.get(0);
        addInterestedFields();
        createTab();
        setupSlider();
    }

    private void setupSlider() {
        slider.setMinSize(500, 10);
        slider.setMin(1);
        slider.setValue(2);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(5);
        slider.setMinorTickCount(1);
        slider.setBlockIncrement(1);
        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            int indexToUse = new_val.intValue() - 1;
            if (allDataFromAmps.size() > indexToUse) {
                List<DataBean> data = allDataFromAmps.get(indexToUse);
                sliderValue.setText("" + (indexToUse + 1));
                tableData.clear();
                tableData.addAll(data);
            }
        });
    }

    public boolean isHorizontal() {
        return false;
    }

    @Override
    protected void processMessage(Message message) {
        if (message.getCommand() == Message.Command.Ack && message.getAckType() == Message.AckType.Completed) {
            updateLastSubscriptionStatus("Got all historical data from Amps. Now waiting for real time data.");
        }
        super.processMessage(message);
    }

    public void updateModel(List<? extends Object> messages) {
        messages.forEach(msg -> {
            List<DataBean> data = new ArrayList<>();
            Map<String, Object> dataMap = (Map<String, Object>) msg;
            Map<String, Object> filteredMap = filterInterestedFields(dataMap);
            String id = filteredMap.get(recordIdField).toString();
            if (id != null) {
                filteredMap.forEach((key, value) -> {
                    DataBean newBean = new DataBean(id);
                    newBean.setPropertyValue(fieldColumn, key);
                    newBean.setPropertyValue(valueColumn, value);
                    data.add(newBean);
                });

            } else {
                LOG.warn("Got a map with no ID, id key = " + recordIdField + " data we got " + filteredMap);
            }
            if (data.size() > 0) {
                allDataFromAmps.add(data);
                sendRecordCount(count++);
            }
        });
        Platform.runLater(() -> {
            updateSlider();
            slider.adjustValue(0);
        });
    }

    protected Node createBottomPanel() {
        BorderPane pane = new BorderPane();
        sliderValue.setStyle("-fx-font-weight: bold;");
        Label label = new Label("  Move the slider to see the record changing in time. Showing record ");
        label.setStyle("-fx-font-weight: bold;");
        HBox labelBox = new HBox();
        labelBox.alignmentProperty().setValue(Pos.CENTER);
        labelBox.getChildren().addAll(label, sliderValue);
        pane.setLeft(labelBox);
        pane.setRight(slider);
        return pane;
    }

    private void updateSlider() {
        System.out.println("-----");
        slider.setMax(count - 1);
    }

    @Override
    public void createMainColumn() {
        createTableColumn(fieldColumn);
        createTableColumn(valueColumn);
    }
}