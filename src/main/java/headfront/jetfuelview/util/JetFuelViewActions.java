package headfront.jetfuelview.util;

import headfront.dataexplorer.controlfx.ActionUtils;
import headfront.guiwidgets.AboutPopup;
import headfront.guiwidgets.PopUpDialog;
import headfront.jetfuelview.graph.FileUtil;
import headfront.utils.FileUtils;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionMap;
import org.controlsfx.control.action.ActionProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.controlsfx.control.action.ActionMap.action;
import static org.controlsfx.control.action.ActionMap.actions;


/**
 * Created by Deepak on 20/07/2016.
 */
public class JetFuelViewActions {

    private static final String savePathImage = "images/icons/Save.png";
    private static final String helpPathImage = "images/icons/doc.png";
    private static final String preferencesPathImage = "images/icons/preferences.png";
    private static final String aboutPathImage = "images/icons/about.png";

    private Collection<? extends Action> menuBarActions;
    private Collection<Action> toolBarActions;
    private HostServices hostServices;
    private Runnable shutdownHandler;

    public JetFuelViewActions(boolean prod, Runnable shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
        ActionMap.register(this);

        menuBarActions = Arrays.asList(
                new ActionGroup("File", action("save"), action("exit")),
                new ActionGroup("Edit", actions("preference")),
                new ActionGroup("Help", action("help"), action("about"))
        );

        toolBarActions = new ArrayList();

        toolBarActions.add(action("save"));
        toolBarActions.add(action("loadFromDisk"));
        toolBarActions.add(action("loadFromServer"));
    }

    @ActionProxy(text = "Save", graphic = savePathImage, accelerator = "ctrl+S")
    private void save() {
        PopUpDialog.showWarningPopup("Save not available", "Save not implemented in this version");
    }

    @ActionProxy(text = "Load From Disk", graphic = savePathImage)
    private void loadFromDisk() {
        PopUpDialog.showWarningPopup("Save not available", "Save not implemented in this version");
    }

    @ActionProxy(text = "Load From Server", graphic = savePathImage)
    private void loadFromServer() {
        PopUpDialog.showWarningPopup("Save not available", "Save not implemented in this version");
    }

    @ActionProxy(text = "Preference", graphic = preferencesPathImage)
    private void preference() {
        PopUpDialog.showWarningPopup("Preference not available", "Preference not implemented in this version");
    }

    @ActionProxy(text = "Exit")
    private void exit() {
        shutdownHandler.run();
    }

    @ActionProxy(text = "About", graphic = aboutPathImage)
    private void about() {
        new AboutPopup(hostServices);
    }


    @ActionProxy(text = "Help", graphic = helpPathImage)
    private void help() {
        hostServices.showDocument("http://www.headfront.co.uk/JetFuel.html");
    }

    public MenuBar getMenuBar() {
        return ActionUtils.createMenuBar(menuBarActions);
    }

    public ToolBar getToolBar(Color backgroundColour) {
        ToolBar toolBar = ActionUtils.createToolBar(toolBarActions, ActionUtils.ActionTextBehavior.SHOW);
        toolBar.setBackground(new Background(new BackgroundFill(backgroundColour, CornerRadii.EMPTY, Insets.EMPTY)));
        return toolBar;
    }


    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
