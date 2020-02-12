package headfront.dataexplorer.tabs;

import com.crankuptheamps.client.Message;
import headfront.amps.AmpsConnection;
import headfront.convertor.MessageConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.bean.DataBean;
import headfront.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 06/07/2016.
 */
public class HorizontalDataTableTab extends TableDataTab {

    private boolean showHistory;
    private String recordIdField;
    private List<String> recordIds;
    private String ORIGINAL_ID = "OriginalID";
    private String COUNT = "Count";

    public HorizontalDataTableTab(String tabName, AmpsConnection connection, boolean showHistory,
                                  MessageConvertor messageConvertor, List<String> recordIds, DataExplorerSelection selection) {
        super(tabName, connection, showHistory, messageConvertor, selection, recordIds);
        this.showHistory = showHistory;
        this.recordIdField = StringUtils.createRecordKey(recordIds);
        this.recordIds = recordIds;
        addInterestedFields();
        createTab();
        interestedFields.forEach(col -> {
            createColumnsIfRequired(col);
        });
    }

    @Override
    protected void processMessage(Message message) {
        if (showHistory) {
            if (message.getCommand() == Message.Command.Ack && message.getAckType() == Message.AckType.Completed) {
                updateLastSubscriptionStatus("Got all historical data from Amps. Now waiting for real time data.");
            }
        }
        super.processMessage(message);
    }

    public void updateModel(List<? extends Object> messages) {
        messages.forEach(msg -> {
            Map<String, Object> dataMap = (Map<String, Object>) msg;
            Map<String, Object> filteredMap = filterInterestedFields(dataMap);
            Object idObject;
            if (recordIds.size() == 1) {
                idObject = filteredMap.get(recordIdField);
            } else {
                String createdId = "";
                for (String recId : recordIds) {
                    createdId = createdId + filteredMap.get(recId) + StringUtils.KEY_SEPERATOR;
                }
                idObject = createdId.substring(0, createdId.length() - 1);
            }
            if (idObject != null) {
                String rawId = idObject.toString();
                final String id;
                if (showHistory) {
                    id = getHistoricID(rawId);
                    filteredMap.put(ORIGINAL_ID, rawId);
                    filteredMap.put(COUNT, (dataCount - 1));
                } else {
                    id = rawId;
                }
                createColumnsIfRequired(filteredMap.keySet());
                DataBean oldBean = allDataKeyedByID.get(id);
                if (oldBean == null) {
                    oldBean = new DataBean(id, filteredMap);
                    if (recordIds.size() > 0) {
                        oldBean.setPropertyValue(recordIdField, id);
                    }
                    tableData.add(oldBean);
                    allDataKeyedByID.put(id, oldBean);
                    sendRecordCount(allDataKeyedByID.size());
                } else {
                    boolean addedProperty = oldBean.updateProperties(filteredMap);
                    if (addedProperty) {
                        int index = tableData.indexOf(oldBean);
                        if (index > -1) {
                            tableData.set(index, oldBean);
                        }
                    }
                }
                if (showHistory) {
                    oldBean.setPropertyValue(recordIdField, id);
                }
            } else {
                LOG.info("Ignoring message as it did not have and ID  with field " + recordIds + " data was " + filteredMap);
                updateRecordsWithNoMessageCount();
            }
        });
    }

    @Override
    public void createMainColumn() {
        createTableColumn(recordIdField);
    }

}
