package headfront.dataexplorer.tabs;

import headfront.amps.AmpsConnection;
import headfront.dataexplorer.DataExplorerSelection;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 08/07/2016.
 */
public class TextAreaTab extends AbstractDataTab {

    private TextArea textArea = new TextArea();
    private int messageCount = 0;

    public TextAreaTab(String tabName, AmpsConnection connection, boolean showHistory, DataExplorerSelection selection) {
        super(tabName, connection, showHistory, null, selection, Arrays.asList(""));
        setDecodeMessage(false);
        textArea.setEditable(false);
        createTab();
    }

    @Override
    protected Node createContent() {
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(textArea);
        return mainPane;
    }

    @Override
    public void clearData() {
        messageCount = 0;
        textArea.clear();
        textArea.appendText("Cleared data at " + LocalDateTime.now() + ".......\n");
        super.clearData();
    }

    @Override
    public void updateModel(List<? extends Object> objects) {
        List<String> messages = (List<String>) objects;
        messageCount = messageCount + messages.size();
        String message = messages.stream().collect(Collectors.joining("\n"));
        Platform.runLater(() -> {
            if (textArea.getText().length() > 1_000_000) {
                textArea.clear();
                textArea.appendText("Clearing data......\n");
            }
            textArea.appendText(message + "\n");
            sendRecordCount(messageCount);
        });
    }
}
