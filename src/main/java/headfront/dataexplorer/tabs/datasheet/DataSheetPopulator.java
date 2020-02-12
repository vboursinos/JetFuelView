package headfront.dataexplorer.tabs.datasheet;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.exception.AMPSException;
import headfront.amps.AmpsConnection;
import headfront.amps.services.TopicService;
import headfront.convertor.MessageConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.tabs.AbstractDataTab;
import headfront.dataexplorer.tabs.DataSheetTab;
import headfront.guiwidgets.NarrowableList;
import headfront.utils.StringUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 04/06/2017.
 */
public class DataSheetPopulator {
    private static final Logger LOG = LoggerFactory.getLogger(DataSheetTab.class);

    private AmpsConnection connection;
    private DataSheetTab dataSheetTab;
    private final int rowId;
    private final int columnID;
    private int allocatedColumnID = 0;
    private DataExplorerSelection selection;
    private int allocatedRowID = 0;
    private TopicService topicService;
    private Map<String, Integer> rowIDsMap = new HashMap<>();
    private Map<String, Integer> recordLocations = new HashMap<>();
    private InternalAmpsListener ampsListener;

    public DataSheetPopulator(AmpsConnection connection, DataSheetTab dataSheetTab, int rowId, int columnID,
                              DataExplorerSelection selection, TopicService topicService) {
        this.connection = connection;
        this.dataSheetTab = dataSheetTab;
        this.rowId = rowId;
        this.columnID = columnID;
        this.allocatedColumnID = columnID + 1;
        this.selection = selection;
        this.topicService = topicService;
        LOG.info("Creating DataSheetPopulator at rowid = " + rowId + " and columnId = " + columnID);
        boolean showAllFields = false;
        if (selection.getSelectionType() == NarrowableList.SelectionType.SHOW_NEW ||
                selection.getSelectionType() == NarrowableList.SelectionType.SHOW_ALL) {
            showAllFields = true;
        }
        if (selection.getFieldSelectionType() == NarrowableList.SelectionType.SHOW_ALL) {
            showAllFields = true;
        }
        if (selection.getFields().size() > 0) {
            showAllFields = false;
        }
        setup();
        String topic = selection.getTopic();
        ampsListener = new InternalAmpsListener("", connection, false, connection.getMessageConvertor(topic), selection,
                topicService.getSowKey(topic), showAllFields);
        try {
            CommandId commandId;
            boolean subscribeOnly = false;
            if (selection.getSelectionType() == NarrowableList.SelectionType.SHOW_NEW) {
                subscribeOnly = true;
            }
            if (selection.getUseFilter()) {
                commandId = topicService.subscribeToFilter(selection.getTopic(), selection.getFilter(), selection.getOrderBy(),
                        ampsListener::processAmpsMessage, false, subscribeOnly, false, selection.getJetFuelSelectorStart(), selection.getOptions());
            } else {
                commandId = topicService.subscribeToRecord(selection.getTopic(), selection.getRecords(),
                        ampsListener::processAmpsMessage, false, subscribeOnly, false, selection.getJetFuelSelectorStart(), selection.getOptions());
            }
            ampsListener.setAmpsSubCommandID(commandId);
        } catch (AMPSException e) {
            LOG.error("Unable to subscribe to " + selection);
        }
    }

    public void stop() {
        ampsListener.stopSubscription();
    }

    private void setup() {
        List<String> fields = selection.getFields();
        fields.add(AmpsConnection.TIMESTAMP);
        fields.add(AmpsConnection.OOF_KEY);
        int counter = 1;
        for (String f : fields) {
            String fieldName = f.split("\\[")[0].trim();
            int xcoordinate = rowId + (counter++);
            dataSheetTab.setData(xcoordinate, columnID, fieldName);
            rowIDsMap.put(fieldName, xcoordinate);
            allocatedRowID = xcoordinate;
        }
    }

    // Dont like this as this is not a tab but I cant refactor this now.
    class InternalAmpsListener extends AbstractDataTab {

        private List<String> recordIds;
        private String recordIdField;
        private boolean showAllFields;

        public InternalAmpsListener(String tabName, AmpsConnection connection,
                                    boolean showHistory, MessageConvertor messageConvertor,
                                    DataExplorerSelection selection, List<String> recordIds, boolean showAllFields) {
            super(tabName, connection, showHistory, messageConvertor, selection, recordIds);
            this.recordIds = recordIds;
            this.recordIdField = StringUtils.createRecordKey(recordIds);
            this.showAllFields = showAllFields;
        }

        @Override
        protected Node createContent() {
            return null;
        }

        @Override
        public void updateModel(List<? extends Object> messages) {
            messages.forEach(msg -> {
                Platform.runLater(() -> {
                    Map<String, Object> dataMap = (Map<String, Object>) msg;
                    Object idObject;
                    if (recordIds.size() == 1) {
                        idObject = dataMap.get(recordIdField);
                    } else {
                        String createdId = "";
                        for (String recId : recordIds) {
                            createdId = createdId + dataMap.get(recId) + StringUtils.KEY_SEPERATOR;
                        }
                        idObject = createdId.substring(0, createdId.length() - 1);
                    }
                    if (idObject != null) {
                        String rawId = idObject.toString();
                        final String id = rawId;
                        Integer oldId = recordLocations.get(id);
                        if (oldId == null) {
                            oldId = allocatedColumnID++;
                            recordLocations.put(id, oldId);
                        }
                        dataSheetTab.setData(rowId, oldId, id);
                        final int columnIdToUse = oldId;
                        dataMap.forEach((k, v) -> {
                            Integer rowIdToUse = rowIDsMap.get(k);
                            if (rowIdToUse == null && showAllFields) {
                                rowIdToUse = allocatedRowID + 1;
                                dataSheetTab.setData(rowIdToUse, columnID, k);
                                rowIDsMap.put(k, rowIdToUse);
                                allocatedRowID = rowIdToUse;
                            }
                            if (rowIdToUse != null) {
                                dataSheetTab.setData(rowIdToUse, columnIdToUse, v);
                            }
                        });
                    } else {
                        LOG.info("Ignoring message as it did not have and ID  with field " + recordIds + " data was " + dataMap);
                    }
                });

            });
        }
    }
}
