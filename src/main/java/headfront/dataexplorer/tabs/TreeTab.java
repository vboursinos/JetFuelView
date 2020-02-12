package headfront.dataexplorer.tabs;

import headfront.amps.AmpsConnection;
import headfront.convertor.MessageConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.utils.StringUtils;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Deepak on 03/06/2017.
 */
public class TreeTab extends AbstractDataTab {

    private static final String rootView = "images/icons/tree.png";
    private static final String recordView = "images/icons/record.png";
    private static final String stringView = "images/icons/letters/S.png";
    private static final String booleanView = "images/icons/letters/B.png";
    private static final String intView = "images/icons/letters/I.png";
    private static final String longView = "images/icons/letters/L.png";
    private static final String doubleView = "images/icons/letters/D.png";
    private static final String objectView = "images/icons/letters/O.png";
    private static final String unknownView = "images/icons/letters/question.png";
    private static final String mapView = "images/icons/letters/M.png";
    private static final String listView = "images/icons/letters/L1.png";
    private static final String nullView = "images/icons/letters/question.png";
    private TreeView<String> treeView;
    private Map<String, TreeItem<String>> recordTreeItems = new ConcurrentHashMap<>();
    private String recordIdField;
    private List<String> recordIds;
    private final TreeItem<String> treeItemRoot = new TreeItem<>("Records", new ImageView(rootView));

    public TreeTab(String tabName, AmpsConnection connection, MessageConvertor messageConvertor,
                   DataExplorerSelection selection, List<String> recordIds) {
        super(tabName, connection, selection.isShowHistory(), messageConvertor, selection, recordIds);
        addInterestedFields();
        this.recordIdField = StringUtils.createRecordKey(recordIds);
        this.recordIds = recordIds;
        treeItemRoot.setExpanded(true);
        createTab();
    }

    @Override
    protected Node createContent() {
        treeView = new TreeView<>(treeItemRoot);
        BorderPane mainPane = new BorderPane();
//        Label label = new Label("This is still being developed and has update issues!!!");
//        label.setStyle("-fx-font-weight: bold;");
        BorderPane labelPane = new BorderPane();
//        labelPane.setCenter(label);
        mainPane.setTop(labelPane);
        mainPane.setCenter(treeView);
        return mainPane;
    }

    @Override
    public void updateModel(List<? extends Object> messages) {
        messages.forEach(msg -> {
            Map<String, Object> dataMap = (Map<String, Object>) msg;
            Map<String, Object> filteredMap = dataMap; //filteredMap(dataMap);
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
                final String id = rawId;
                TreeItem<String> oldRecordTreeItem = recordTreeItems.get(id);
                if (oldRecordTreeItem == null) {
                    final TreeItem<String> newRecordTreeItem = new TreeItem<>(getRootTreeNodeViewKey(recordIdField, id), new ImageView(recordView));
                    recordTreeItems.put(id, newRecordTreeItem);
                    filteredMap.forEach((k, v) -> {
                        TreeItem<String> orUpdateChild = createOrUpdateChild(new TreeItem<String>(k, getIcon(v)), v);
                        newRecordTreeItem.getChildren().add(orUpdateChild);
                    });
                    treeItemRoot.getChildren().add(newRecordTreeItem);
                    sendRecordCount(recordTreeItems.size());
                } else {
                    filteredMap.forEach((k, v) -> {
                        TreeItem<String> oldNode = getTreeItem(oldRecordTreeItem, k);
                        if (oldNode == null) {
                            TreeItem<String> newNode = new TreeItem<>(k, getIcon(v));
                            TreeItem<String> orUpdateChild = createOrUpdateChild(newNode, v);
                            oldRecordTreeItem.getChildren().add(orUpdateChild);
                        } else {
                            createOrUpdateChild(oldNode, v);
                        }
                    });
                }

            } else {
                LOG.info("Ignoring message as it did not have and ID  with field " + recordIds + " data was " + filteredMap);
                updateRecordsWithNoMessageCount();
            }
        });
    }

    private TreeItem<String> getTreeItem(TreeItem<String> oldNode, String key) {
        Optional<TreeItem<String>> any = oldNode.getChildren().stream().filter(t -> t.getValue().equals(key)).findAny();
        if (any.isPresent()) {
//            any.get().getChildren().clear();
            return any.get();
        } else {
            return null;
        }
    }

    private TreeItem<String> createOrUpdateChild(TreeItem<String> useTreeNode, Object v) {
        if (v instanceof HashMap) {
            Map<String, Object> subMap = (Map) v;
            subMap.forEach((newKey, newValue) -> {
                TreeItem<String> oldNode = getTreeItem(useTreeNode, newKey);
                if (oldNode == null) {
                    TreeItem<String> newNode = new TreeItem<>(newKey, getIcon(newValue));
                    TreeItem<String> orUpdateChild = createOrUpdateChild(newNode, newValue);
                    useTreeNode.getChildren().add(orUpdateChild);
                } else {
                    createOrUpdateChild(oldNode, newValue);
                }
            });
        } else if (v instanceof List) {
            List list = (List) v;
            list.forEach(item -> {
                if (item instanceof Map) {
                    TreeItem<String> orUpdateChild = createOrUpdateChild(useTreeNode, item);
//                    useTreeNode.getChildren().add(orUpdateChild);
                } else {
                    createOrUpdateChild(useTreeNode, item);
                }
            });
        } else {
            useTreeNode.getChildren().clear();
            TreeItem<String> valueTreeNode = new TreeItem<>("    " + ((v != null) ? v.toString() : "Null"));
            useTreeNode.getChildren().add(valueTreeNode);
        }
        return useTreeNode;
    }

    private String getRootTreeNodeViewKey(String k, Object v) {
        return " " + k + " = " + v;
    }

    private ImageView getIcon(Object v) {
        if (v == null) {
            return new ImageView(nullView);
        } else if (v instanceof String) {
            return new ImageView(stringView);
        } else if (v instanceof Integer) {
            return new ImageView(intView);
        } else if (v instanceof Long) {
            return new ImageView(longView);
        } else if (v instanceof Double) {
            return new ImageView(doubleView);
        } else if (v instanceof Boolean) {
            return new ImageView(booleanView);
        } else if (v instanceof Map) {
            return new ImageView(mapView);
        } else if (v instanceof List) {
            return new ImageView(listView);
        } else {
            return new ImageView(objectView);
        }
    }
}
