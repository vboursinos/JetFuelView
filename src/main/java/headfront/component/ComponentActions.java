package headfront.component;

import headfront.dataexplorer.controlfx.ActionUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.stage.StageStyle;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionMap;
import org.controlsfx.control.action.ActionProxy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.controlsfx.control.action.ActionMap.action;

/**
 * Created by Deepak on 31/07/2016.
 */
public class ComponentActions {

    private Collection<? extends Action> addActions;
    private ObservableList<String> listView;

    public ComponentActions() {
        ActionMap.register(this);
        addActions = Arrays.asList(
                new ActionGroup("Add Transformation",
                        action("transformField"),
                        action("addField"),
                        action("deleteField"),
                        action("changeTypeField"),
                        action("calculateNewField")));
    }


    public ToolBar createActionPane() {
        return ActionUtils.createToolBar(addActions, ActionUtils.ActionTextBehavior.SHOW);
    }

    @ActionProxy(text = "Transform Field")
    private void transformField() {
        Optional<String> s = showPopup();
        s.ifPresent(newData -> listView.add("Transform Field [" + newData + "]"));
    }

    @ActionProxy(text = "Add New Field")
    private void addField() {
        Optional<String> s = showPopup();
        s.ifPresent(newData -> listView.add("Add New Field [" + newData + "]"));
    }

    @ActionProxy(text = "Delete Field")
    private void deleteField() {
        Optional<String> s = showPopup();
        s.ifPresent(newData -> listView.add("Delete Field [" + newData + "]"));
    }

    @ActionProxy(text = "Change Type Field")
    private void changeTypeField() {
        Optional<String> s = showPopup();
        s.ifPresent(newData -> listView.add("Change Type Field [" + newData + "]"));
    }

    @ActionProxy(text = "Calculate New Field")
    private void calculateNewField() {
        Optional<String> s = showPopup();
        s.ifPresent(newData -> listView.add("Calculate New Field [" + newData + "]"));
    }

    private Optional<String> showPopup() {
        TextInputDialog dlg = new TextInputDialog("");
        dlg.setTitle("Enter Change");
//        String optionalMasthead = "Please Enter change";
        dlg.getDialogPane().setContentText("Enter Transformation");
//        configureSampleDialog(dlg, optionalMasthead);
        dlg.initOwner(null);
        dlg.initStyle(StageStyle.UNDECORATED);
        return dlg.showAndWait();
    }

    public void setListView(ObservableList<String> listView) {
        this.listView = listView;
    }
}
