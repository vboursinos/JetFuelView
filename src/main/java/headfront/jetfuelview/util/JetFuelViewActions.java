package headfront.jetfuelview.util;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import headfront.dataexplorer.controlfx.ActionUtils;
import headfront.guiwidgets.AboutPopup;
import headfront.guiwidgets.PopUpDialog;
import headfront.jetfuelview.graph.FileUtil;
import headfront.jetfuelview.graph.JetFuelGraphModel;
import headfront.jetfuelview.panel.JetFuelViewStatusBar;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionMap;
import org.controlsfx.control.action.ActionProxy;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static headfront.jetfuelview.graph.Styles.AMPS_GROUP;
import static headfront.jetfuelview.graph.Styles.AMPS_SERVER_BAD;
import static headfront.jetfuelview.graph.Styles.AMPS_SERVER_GOOD;
import static org.controlsfx.control.action.ActionMap.action;
import static org.controlsfx.control.action.ActionMap.actions;


/**
 * Created by Deepak on 20/07/2016.
 */
public class JetFuelViewActions {

    private static final String update1Image = "images/icons/update1.png";
    private static final String update2Image = "images/icons/update2.png";
    private static final String savePathImage = "images/icons/Save.png";
    private static final String helpPathImage = "images/icons/doc.png";
    private static final String preferencesPathImage = "images/icons/preferences.png";
    private static final String aboutPathImage = "images/icons/about.png";

    private Collection<? extends Action> menuBarActions;
    private Collection<Action> toolBarActions;
    private HostServices hostServices;
    private String environment;
    private Runnable shutdownHandler;
    private mxGraphComponent graphComponent;
    private JetFuelGraphModel graphModel;
    private JetFuelViewStatusBar jetFuelStatusBar;
    private MaskerPane maskerPane;

    public JetFuelViewActions(String environment, Runnable shutdownHandler) {
        this.environment = environment;
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
        SwingUtilities.invokeLater(() -> {
            FileUtil.saveGraph(graphComponent, getConfigFileName());
            jetFuelStatusBar.updateMessage("Saved SystemView");
        });
    }

    @ActionProxy(text = "Load From Disk", graphic = update1Image)
    public void loadFromDisk() {
        maskerPane.setVisible(true);
        jetFuelStatusBar.clearCount();
        FileUtil.loadGraph(graphComponent, getConfigFileName());
        mxCell aCell = (mxCell) graphComponent.getGraph().getDefaultParent();
        int parentChildCount = aCell.getChildCount();
        for (int i = parentChildCount - 1; i >= 0; i--) {
            mxCell aChild = (mxCell) graphComponent.getGraph().getModel().getChildAt(aCell, i);
            String style = aChild.getStyle();
            processAmpsServer(style);
            if (style.equals(AMPS_GROUP)) {
                int childCount = aChild.getChildCount();
                for (int j = childCount - 1; j >= 0; j--) {
                    mxCell childCell = (mxCell) graphComponent.getGraph().getModel().getChildAt(aChild, j);
                    String childStyle = childCell.getStyle();
                    processAmpsServer(childStyle);
                }
            }
        }
        jetFuelStatusBar.updateMessage("Loaded SystemView from disk");
        maskerPane.setVisible(false);
    }

    private void processAmpsServer(String style) {
        if (style.equals(AMPS_SERVER_GOOD)) {
            jetFuelStatusBar.incrementActiveCount();
        } else if (style.equals(AMPS_SERVER_BAD)) {
            jetFuelStatusBar.incrementInactiveCount();
        }
    }

    @ActionProxy(text = "Load From Server", graphic = update2Image)
    private void loadFromServer() {
        SwingUtilities.invokeLater(() -> {
            graphModel.updateFromServer(true);
        });
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

    public void setGraphComponent(mxGraphComponent graphComponent) {
        this.graphComponent = graphComponent;
    }

    public void setGraphModel(JetFuelGraphModel graphModel) {
        this.graphModel = graphModel;
    }

    public void setJetFuelStatusBar(JetFuelViewStatusBar jetFuelStatusBar) {
        this.jetFuelStatusBar = jetFuelStatusBar;
    }

    public void setMaskerPane(MaskerPane maskerPane) {
        this.maskerPane = maskerPane;
    }

    private String getConfigFileName(){
        return "JetFuelView-" + environment + ".xml";
    }
}
