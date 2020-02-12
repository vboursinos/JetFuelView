package headfront.dataexplorer;

import com.crankuptheamps.client.Client;
import headfront.guiwidgets.NarrowableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deepak on 28/06/2016.
 */
public class DataExplorerSelection {

    private String topic = null;
    private List<String> records = new ArrayList<>();
    private List<String> fields = new ArrayList<>();
    private String filter = "";
    private boolean jetFuelSelector = false;
    private String jetFuelSelectorStart = Client.Bookmarks.EPOCH;
    private String jetFuelSelectorEnd = null;
    private boolean useFilter = false;
    private static final int displayFieldSize = 10;
    private NarrowableList.SelectionType selectionType = NarrowableList.SelectionType.SHOW_SELECTED;
    private NarrowableList.SelectionType fieldSelectionType = NarrowableList.SelectionType.SHOW_SELECTED;
    private boolean deltaSubcribe;
    private boolean sowToFileOnly = false;
    private String orderBy = "";
    private String options = "";

    public boolean getUseFilter() {
        return useFilter;
    }

    private boolean showHistory = false;
    private RecordDisplay recordDisplayType = RecordDisplay.VERTICAL;

    public DataExplorerSelection(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getRecords() {
        return records;
    }

    public void setRecords(List<String> newRecords) {
        records.clear();
        records.addAll(newRecords);
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> newFields) {
        fields.clear();

        fields.addAll(newFields);
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        if (filter.trim().length() > 0) {
            useFilter = true;
        }
    }

    public void setSowToFileOnly(boolean sowToFileOnly) {
        this.sowToFileOnly = sowToFileOnly;
    }

    public boolean isSowToFileOnly() {
        return sowToFileOnly;
    }

    public boolean isShowHistory() {
        return showHistory;
    }

    public void setShowHistory(boolean showHistory) {
        this.showHistory = showHistory;
    }

    public RecordDisplay getRecordDisplayType() {
        return recordDisplayType;
    }

    public void setRecordDisplayType(RecordDisplay recordDisplayType) {
        this.recordDisplayType = recordDisplayType;
    }

    public boolean isJetFuelSelector() {
        return jetFuelSelector;
    }

    public void setJetFuelSelector(boolean jetFuelSelector) {
        this.jetFuelSelector = jetFuelSelector;
    }

    @Override
    public String toString() {
        return "DataExplorerSelection{" +
                "topic='" + topic + '\'' +
                ", records=" + records +
                ", fields=" + fields +
                ", filter='" + filter + '\'' +
                ", jetFuelSelector=" + jetFuelSelector +
                ", jetFuelSelectorStart='" + jetFuelSelectorStart + '\'' +
                ", jetFuelSelectorEnd='" + jetFuelSelectorEnd + '\'' +
                ", useFilter=" + useFilter +
                ", selectionType=" + selectionType +
                ", fieldSelectionType=" + fieldSelectionType +
                ", deltaSubcribe=" + deltaSubcribe +
                ", sowToFileOnly=" + sowToFileOnly +
                ", orderBy='" + orderBy + '\'' +
                ", showHistory=" + showHistory +
                ", options=" + options +
                ", recordDisplayType=" + recordDisplayType +
                '}';
    }

    public String toBriefString() {
        String recordsStr = "";
        if (records.size() > 0) {
            recordsStr = records.toString();
        } else {
            recordsStr = "[" + selectionType.toString() + "]";
        }
        String fieldStr = "";
        if (fields.size() > 0) {
            fieldStr = fields.toString();
        } else {
            fieldStr = "[" + fieldSelectionType.toString() + "]";
        }
        return "Topic='" + topic + '\'' +
                ", records=" + recordsStr +
                ", fields=" + fieldStr +
                (filter.length() > 0 ? ", filter='" + filter : "") +
                '}';
    }

    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("Tab Description \n");
        builder.append("Topic = ").append(topic).append("\n");
        builder.append("Records = ").append(getRecordDesc()).append("\n");
        builder.append("Fields = ").append(getFieldDesc()).append("\n");
        if (filter.trim().length() > 0) {
            builder.append("Filter = ").append(filter).append("\n");
        }
        if (options.trim().length() > 0) {
            builder.append("Options = ").append(options).append("\n");
        }
        if (orderBy.trim().length() > 0) {
            builder.append("Order By = ").append(orderBy).append("\n");
        }
        if (showHistory) {
            builder.append("Showing History").append("\n");
        }
        if (jetFuelSelector) {
            builder.append("JetFuel Selector").append("\n");
        }
        if (showHistory) {
            if (jetFuelSelectorStart != null) {
                builder.append("Bookmark Start = ").append(jetFuelSelectorStart).append("\n");
            }
            if (jetFuelSelectorEnd != null) {
                builder.append("Bookmark End = ").append(jetFuelSelectorEnd).append("\n");
            }
        }
        if (deltaSubcribe) {
            builder.append("Delta Subscribe").append("\n");
        }
        if (sowToFileOnly) {
            builder.append("Sow To File Only").append("\n");
        }

        builder.append("Record shown = ").append(recordDisplayType);
        return builder.toString();
    }

    private String getFieldDesc() {
        if (fieldSelectionType == NarrowableList.SelectionType.SHOW_SELECTED) {
            List<String> displayList = fields;
            if (fields.size() > 0) {
                if (fields.size() > displayFieldSize) {
                    displayList = fields.subList(0, displayFieldSize);
                    displayList.add("<MORE>...");
                }
            }
            return displayList.toString();
        } else {

            return fieldSelectionType.name();
        }
    }

    private String getRecordDesc() {
        if (selectionType == NarrowableList.SelectionType.SHOW_SELECTED) {
            List<String> displayList = records;
            if (records.size() > displayFieldSize) {
                displayList = records.subList(0, displayFieldSize);
                displayList.add("<MORE>...");
            }
            return displayList.toString();
        } else {

            return selectionType.name();
        }
    }

    public String getDisplayName() {
        if (jetFuelSelector) {
            return "JetFuelSelector " + topic;
        }
        return topic;
    }

    public String getJetFuelSelectorStart() {
        return jetFuelSelectorStart;
    }

    public void setJetFuelSelectorStart(String jetFuelSelectorStart) {
        this.jetFuelSelectorStart = jetFuelSelectorStart;
    }

    public String getJetFuelSelectorEnd() {
        return jetFuelSelectorEnd;
    }

    public void setJetFuelSelectorEnd(String jetFuelSelectorEnd) {
        this.jetFuelSelectorEnd = jetFuelSelectorEnd;
    }

    public void setSelectionType(NarrowableList.SelectionType selectionType) {
        this.selectionType = selectionType;
    }

    public NarrowableList.SelectionType getSelectionType() {
        return selectionType;
    }

    public NarrowableList.SelectionType getFieldSelectionType() {
        return fieldSelectionType;
    }

    public void setFieldSelectionType(NarrowableList.SelectionType fieldSelectionType) {
        this.fieldSelectionType = fieldSelectionType;
    }

    public void setDeltaSubcribe(boolean deltaSubcribe) {
        this.deltaSubcribe = deltaSubcribe;
    }

    public boolean isDeltaSubcribe() {
        return deltaSubcribe;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
        if (options.trim().length() > 0) {
            useFilter = true;
        }
    }
}
