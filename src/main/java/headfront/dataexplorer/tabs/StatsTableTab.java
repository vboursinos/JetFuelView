package headfront.dataexplorer.tabs;

import headfront.amps.AmpsConnection;
import headfront.amps.services.AmpsStatsLoader;
import headfront.convertor.JacksonJsonConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.bean.DataBean;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.MessageUtil;
import javafx.scene.control.TreeItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 22/07/2016.
 */
public class StatsTableTab extends TableDataTab {

    private String idField;
    private String dataField;
    private List<String> columnsToCreate;
    private AmpsStatsLoader statsLoader = null;
    private JacksonJsonConvertor messageConvertor = new JacksonJsonConvertor();
    private boolean messageStatusUpdate = false;
    private boolean addUpdateTime = false;

    public StatsTableTab(String tabName, AmpsConnection connection,
                         List<String> idFields, String dataField, List<String> columnsToCreate,
                         DataExplorerSelection selection, String connectionsStr, String adminPortStr,
                         int refreshTimeInSec, boolean useSecureHttp) {
        super(tabName, connection, false, null, selection, idFields);
        this.idField = idFields.get(0);
        this.dataField = dataField;
        this.columnsToCreate = columnsToCreate;
        addUpdateTime = true;
        createTab();
        statsLoader = new AmpsStatsLoader(connectionsStr, adminPortStr, dataField, this::processMessage,
                refreshTimeInSec,useSecureHttp);
    }

    public StatsTableTab(String tabName, AmpsConnection connection,
                         List<String> idFields, TreeItem<String> treeItem,
                         LocalDateTime startDate, LocalDateTime stopDate, List<String> columnsToCreate,
                         DataExplorerSelection selection, String connectionsStr, String adminPortStr, int refreshTimeInSec,
                         boolean useSecureHttp) {
        super(tabName, connection, false, null, selection, idFields);
        this.idField = idFields.get(0);
        this.dataField = treeItem.getValue();
        this.columnsToCreate = columnsToCreate;
        createTab();
        statsLoader = new AmpsStatsLoader(connectionsStr, adminPortStr, this::processMessage,
                refreshTimeInSec, treeItem, startDate, stopDate,useSecureHttp);
    }

    @Override
    public void stopSubscription() {
        statsLoader.stopRunning();
    }

    @Override
    public void createMainColumn() {
        createTableColumn(idField);
        columnsToCreate.forEach(col -> {
            createTableColumn(col);
        });
    }

    private void processMessage(String json) {
        if (json != null) {
            Map<String, Object> data = messageConvertor.convertToMap(json);
            Object dataToProcess = MessageUtil.getLeafNode(data, dataField);
            if (dataToProcess instanceof List) {
                updateModel((List) dataToProcess);
            }
        } else {
            PopUpDialog.showWarningPopup("Unable to get stats", "Amps was not able to provide the required data");
        }
    }

    @Override
    public void updateModel(List<? extends Object> messages) {
        List<String> oldBeanIds = new ArrayList<>();
        oldBeanIds.addAll(allDataKeyedByID.keySet());
        messages.forEach(msg -> {
            Map<String, Object> dataMap = (Map<String, Object>) msg;
            if (addUpdateTime) {
                dataMap.put("UpdateTime", LocalDateTime.now());
            }
            Object idObject = dataMap.get(idField);
            if (idObject != null) {
                final String id = idObject.toString();
                createColumnsIfRequired(dataMap.keySet());
                DataBean oldBean = allDataKeyedByID.get(id);
                oldBeanIds.remove(id);
                if (oldBean == null) {
                    oldBean = new DataBean(id, dataMap);
                    tableData.add(oldBean);
                    allDataKeyedByID.put(id, oldBean);
                    sendRecordCount(allDataKeyedByID.size());
                } else {
                    oldBean.updateProperties(dataMap);
                }
            } else {
                LOG.info("Ignoring message as it did not have and ID  with field " + idField + " data was " + dataMap);
                updateRecordsWithNoMessageCount();
            }
        });
        oldBeanIds.forEach(id -> {
            DataBean remove = allDataKeyedByID.remove(id);
            tableData.remove(remove);
        });
        if (!messageStatusUpdate) {
            updateLastSubscriptionStatus("Getting data from Amps");
            messageStatusUpdate = true;
        }
    }

}
