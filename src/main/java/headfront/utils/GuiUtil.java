package headfront.utils;

import headfront.dataexplorer.controlfx.ChoiceDialog;
import headfront.guiwidgets.PopUpDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Deepak on 27/06/2017.
 */
public class GuiUtil {

    private static Color envColorNonProd = Color.GREEN;
    private static Color envColorProd = Color.INDIANRED;

    public static boolean isProd(String environmentStr) {
        boolean isProd = true;
        if (environmentStr.toLowerCase().contains("prod")) {
            isProd = true;
        } else {
            isProd = false;
        }
        return isProd;
    }

    public static Color getEnvColour(String environmentStr) {
        Color currentEnvColor = envColorProd;
        if (!environmentStr.toLowerCase().contains("prod")) {
            currentEnvColor = envColorNonProd;
        }
        return currentEnvColor;
    }

    public static void showSelectionList(Set<String> allTopic, String title, String msg, Consumer<String> reply) {
        if (allTopic.size() == 0) {
            PopUpDialog.showInfoPopup("No Items", "No items to display at the moment");
            return;
        }
        ObservableList<String> listToUse = FXCollections.observableArrayList();
        listToUse.addAll(allTopic);
        FXCollections.sort(listToUse);
        ChoiceDialog<String> dlg = new ChoiceDialog(listToUse.get(0), listToUse);
        ComboBox<String> comboBox = dlg.getComboBox();
        comboBox.setEditable(true);
        final AutoCompletionBinding<String> autoCompleteBinding = TextFields.bindAutoCompletion(comboBox.getEditor(), comboBox.getItems());
        comboBox.getEditor().widthProperty().addListener(((observable, oldValue, newValue) -> {
            autoCompleteBinding.setMinWidth(newValue.doubleValue());
        }));
        dlg.setTitle(title);
        dlg.getDialogPane().setHeaderText(msg);
        dlg.getDialogPane().setContentText("");
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(null);
        dlg.showAndWait().ifPresent(reply);
    }
}
