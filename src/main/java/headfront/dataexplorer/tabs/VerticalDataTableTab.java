package headfront.dataexplorer.tabs;

import com.crankuptheamps.client.Message;
import headfront.amps.AmpsConnection;
import headfront.convertor.MessageConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.bean.DataBean;
import headfront.utils.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Deepak on 06/07/2016.
 */
public class VerticalDataTableTab extends TableDataTab {

    private boolean showHistory;
    private String recordIdField;
    private List<String> recordIds;
    private Set<String> recordKeys = new HashSet<>();
    private String recordKey = "Field";

    public VerticalDataTableTab(String tabName, AmpsConnection connection, boolean showHistory,
                                MessageConvertor messageConvertor, List<String> recordIds, DataExplorerSelection selection) {
        super(tabName, connection, showHistory, messageConvertor, selection, recordIds);
        this.showHistory = showHistory;
        this.recordIdField = recordIds.get(0);
        this.recordIds = recordIds;
        addInterestedFields();
        createTab();
        createColumns();
    }

    private void createColumns() {
        interestedFields.forEach(col -> {
            createRecord(null, col, null);
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

    public boolean isHorizontal() {
        return false;
    }

    public void updateModel(List<? extends Object> messages) {
        messages.forEach(msg -> {
            Map<String, Object> dataMap = (Map<String, Object>) msg;
            Map<String, Object> filteredMap = filterInterestedFields(dataMap);
            String rawId = "";
            if (recordIds.size() == 1) {
                rawId = filteredMap.get(recordIdField).toString();
            } else {
                String createdId = "";
                for (String recId : recordIds) {
                    createdId = createdId + filteredMap.get(recId) + StringUtils.KEY_SEPERATOR;
                }
                rawId = createdId.substring(0, createdId.length() - 1);
            }

            if (rawId != null) {
                final String id;
                if (showHistory) {
                    id = getHistoricID(rawId);
                } else {
                    id = rawId;
                }
                if (!recordKeys.contains(id)) {
                    recordKeys.add(id);
                    sendRecordCount(recordKeys.size());
                }
                createColumnsIfRequired(id);
                Set<DataBean> beansToRefresh = new HashSet<DataBean>();
                filteredMap.entrySet().forEach(o -> {
                    Map.Entry entry = (Map.Entry) o;
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();
                    DataBean oldBean = allDataKeyedByID.get(key);
                    if (oldBean != null) {
                        boolean added = oldBean.setPropertyValue(id, value);
                        if (added) {
                            beansToRefresh.add(oldBean);
                        }
                    } else {
                        createRecord(id, key, value);
                    }
                });
                beansToRefresh.forEach(data -> {
                    int index = tableData.indexOf(data);
                    if (index > -1) {
                        tableData.set(index, data);
                    }
                });
            } else {
                LOG.warn("Got a map with no ID, id key = " + recordIds + " data we got " + filteredMap);
            }
        });
    }

    private void createRecord(String id, String key, Object value) {
        DataBean newBean = new DataBean(key);
        newBean.setPropertyValue(recordKey, key);
        if (id != null) {
            newBean.setPropertyValue(id, value);
        }
        tableData.add(newBean);
        allDataKeyedByID.put(key, newBean);
    }

    public void clearData() {
        recordKeys.clear();
        super.clearData();
        removeAllColumns();
    }

    @Override
    public void createMainColumn() {
        createTableColumn("Field");
    }
}