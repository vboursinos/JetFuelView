package headfront.dataexplorer.tabs;

import headfront.amps.AmpsConnection;
import headfront.convertor.MessageConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.bean.DataBean;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Deepak on 22/07/2016.
 */
public class SimpleStatusTableTab extends TableDataTab {

    private String idField;
    private String dataField;
    private List<String> columnsToCreate;
    private boolean clearOnUpdate;
    private volatile boolean recievedFirstMessage = false;

    public SimpleStatusTableTab(String tabName, AmpsConnection connection, MessageConvertor messageConvertor,
                                List<String> idFields, String dataField, List<String> columnsToCreate,
                                DataExplorerSelection selection, boolean clearOnUpdate) {
        super(tabName, connection, false, messageConvertor, selection, idFields);
        this.idField = idFields.get(0);
        this.dataField = dataField;
        this.columnsToCreate = columnsToCreate;
        this.clearOnUpdate = clearOnUpdate;
        createTab();
    }

    @Override
    public void createMainColumn() {
        createTableColumn(idField);
        columnsToCreate.forEach(col -> {
            createTableColumn(col);
        });

    }

    @Override
    public void updateModel(List<? extends Object> messages) {
        Set<String> keysToRemove = new HashSet<>(allDataKeyedByID.keySet());
        messages.forEach(msg -> {
            Map<String, Object> sourceData = (Map<String, Object>) msg;
            Map<String, Object> dataMap = sourceData;
            if (dataField != null) {
                dataMap = (Map<String, Object>) sourceData.get(dataField);
            }
            formatFields(dataMap);
            Object idObject = dataMap.get(idField);
            if (idObject != null) {
                final String id = idObject.toString();
                createColumnsIfRequired(dataMap.keySet());
                DataBean oldBean = allDataKeyedByID.get(id);
                keysToRemove.remove(id);
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
        if (clearOnUpdate) {
            keysToRemove.forEach(idToRemove -> {
                DataBean beanToRemove = allDataKeyedByID.remove(idToRemove);
                tableData.remove(beanToRemove);
            });
            sendRecordCount(allDataKeyedByID.size());
        }
        if (!recievedFirstMessage) {
            recievedFirstMessage = true;
            updateLastSubscriptionStatus("Received snapshot of data from amps. Will update as new data comes in.");
        }
    }

}
