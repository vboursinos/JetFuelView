package headfront.guiwidgets;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TextAreaDialog extends Dialog<String> {

    /**************************************************************************
     * Fields
     **************************************************************************/

    private final GridPane grid;
    private final Label label;
    private final TextArea textArea;
    private final String defaultValue;


    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Creates a new TextInputDialog without a default value entered into the
     * dialog {@link TextField}.
     */
    public TextAreaDialog() {
        this("");
    }

    /**
     * Creates a new TextInputDialog with the default value entered into the
     * dialog {@link TextField}.
     */
    public TextAreaDialog(@NamedArg("defaultValue") String defaultValue) {
        final DialogPane dialogPane = getDialogPane();

        // -- textfield
        this.textArea = new TextArea(defaultValue);
        this.textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setEditable(false);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setFillWidth(textArea, true);

        // -- label
        label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        this.defaultValue = defaultValue;

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK);

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? textArea.getText() : null;
        });
    }

    private Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }


    /**************************************************************************
     *
     * Public API
     *
     **************************************************************************/

    /**
     * Returns the {@link TextField} used within this dialog.
     */
    public final TextArea getEditor() {
        return textArea;
    }

    /**
     * Returns the default value that was specified in the constructor.
     */
    public final String getDefaultValue() {
        return defaultValue;
    }


    /**************************************************************************
     * Private Implementation
     **************************************************************************/

    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(label, 0, 0);
        grid.add(textArea, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> textArea.requestFocus());
    }
}
