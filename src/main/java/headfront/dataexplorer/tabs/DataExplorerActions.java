package headfront.dataexplorer.tabs;

import headfront.dataexplorer.DataExplorerSelection;
import headfront.dataexplorer.controlfx.ActionUtils;
import headfront.guiwidgets.AboutPopup;
import headfront.guiwidgets.PopUpDialog;
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
import java.util.function.Consumer;

import static org.controlsfx.control.action.ActionMap.action;
import static org.controlsfx.control.action.ActionMap.actions;

/**
 * Created by Deepak on 20/07/2016.
 */
public class DataExplorerActions {

    private static final String timeSeriesStatsPathImage = "images/icons/TimeSeriesStats.png";
    private static final String timeSeriesGraphStatsPathImage = "images/icons/TimeSeriesGraph.png";
    private static final String jetFuelImage = "images/icons/JetFuelSmallNoBg.png";
    private static final String undockPathImage = "images/icons/Undock.png";
    private static final String statPathImage = "images/icons/Stat.png";
    private static final String savePathImage = "images/icons/Save.png";
    private static final String helpPathImage = "images/icons/doc.png";
    private static final String adminIconPathImage = "images/icons/Admin.png";
    private static final String galvnoIconPathImage = "images/icons/G-icon.png";
    private static final String preferencesPathImage = "images/icons/preferences.png";
    private static final String aboutPathImage = "images/icons/about.png";
    private static final String stopPathImage = "images/icons/Stop.png";
    private static final String usersPathImage = "images/icons/users.png";
    private static final String topicsPathImage = "images/icons/topics.png";
    private static final String explorePathImage = "images/icons/Explore.png";
    private static final String publishPathImage = "images/icons/publish.png";
    private static final String publishFromFilePathImage = "images/icons/FilePublisher.png";
    private static final String clearDataPathImage = "images/icons/Clear.png";
    private static final String executePathImage = "images/icons/lightning.png";
    private static final String sheetPathImage = "images/icons/JetFuelSheet.png";
    private static final String dashBoardPathImage = "images/icons/Dashboard.png";
    private static final String subscriptionsPathImage = "images/icons/Subscribe.png";

    private Collection<? extends Action> menuBarActions;
    private Collection<Action> toolBarActions;
    private Consumer<DataExplorerSelection> onExplorerButtonPressed;
    private Runnable onPublisherButonPressed;
    private Runnable onFilePublisherButonPressed;
    private Runnable onSubscribeAmpstoFileButonPressed;
    private Runnable onStopButtonPressed;
    private Runnable onSowStatusbuttonPressed;
    private Runnable onClientStatusButtonPressed;
    private Runnable onClearButtonPressed;
    private Runnable onExecuteButtonPressed;
    private Runnable onSubscriptionsButtonPressed;
    private Runnable onSaveTableToFileButtonPressed;
    private Runnable onShowAmpsStatsButtonPressed;
    private Runnable onShowTimeSeriesGraphButtonPressed;
    private Runnable onShowTimeSeriesStatsButtonPressed;
    private Runnable onShowInNewWindowButtonPressed;
    private Runnable onShowSheetButtonPressed;
    private Runnable onShowDashBoardButtonPressed;
    private Runnable onReloadCacheButtonPressed;
    private HostServices hostServices;
    private String adminUrl;
    private String galvanometerUrl;
    private Runnable shutdownHandler;

    public DataExplorerActions(boolean prod, Runnable shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
        ActionMap.register(this);

        ActionGroup viewsActionGroup = new ActionGroup("Views",
                new ActionGroup("Stats",
                        action("showAmpsStats"),
                        action("showTimeSeriesStats"),
                        action("showTimeSeriesGraph")));

        menuBarActions = Arrays.asList(
                new ActionGroup("File", action("save"), action("exit")),
                new ActionGroup("Edit", actions("preference")),
                viewsActionGroup,
                new ActionGroup("Help",  action("reloadCache"), action("help"), action("about"), action("jetFuel"))
        );

        toolBarActions = new ArrayList();

        toolBarActions.add(action("exploreData"));
        toolBarActions.add(action("showSheet"));
        toolBarActions.add(action("execute"));
        if (!prod) {
            toolBarActions.add(action("publish"));
            ActionGroup publishActionGroup = new ActionGroup("Publishers",
                    action("publish"),
                    action("publishFromFile"));
            viewsActionGroup.getActions().add(publishActionGroup);
        }
        toolBarActions.add(action("saveTableToFile"));
        toolBarActions.add(action("showInNewWindowStat"));
        toolBarActions.add(action("stop"));
        toolBarActions.add(action("clear"));
        toolBarActions.add(action("showSowStatus"));
        toolBarActions.add(action("showUserStatus"));
        toolBarActions.add(action("showSubscriptions"));
        toolBarActions.add(action("openAdminPage"));
        toolBarActions.add(action("openGalvanometerPage"));
        toolBarActions.add(action("dashBoard"));
        ActionGroup subscriberActionGroup = new ActionGroup("Subscribers",
                action("subscribeFromAmpsToFile"));
        viewsActionGroup.getActions().add(subscriberActionGroup);
    }

    @ActionProxy(text = "Save", graphic = savePathImage, accelerator = "ctrl+S")
    private void save() {
        PopUpDialog.showWarningPopup("Save not available", "Save not implemented in this version");
    }

    @ActionProxy(text = "Export", graphic = savePathImage)
    private void saveTableToFile() {
        onSaveTableToFileButtonPressed.run();
    }

    @ActionProxy(text = "Admin", graphic = adminIconPathImage)
    private void openAdminPage() {
        if (hostServices != null) {
            hostServices.showDocument(adminUrl);
        }
    }

    @ActionProxy(text = "Galvanometer", graphic = galvnoIconPathImage)
    private void openGalvanometerPage() {
        if (hostServices != null) {
            hostServices.showDocument(galvanometerUrl);
        }
    }

    @ActionProxy(text = "JetFuel Sheet", graphic = sheetPathImage)
    private void showSheet() {
        onShowSheetButtonPressed.run();
    }

    @ActionProxy(text = "Subscriptions", graphic = subscriptionsPathImage)
    private void showSubscriptions() {
        onSubscriptionsButtonPressed.run();
    }

    @ActionProxy(text = "Amps Stats", graphic = statPathImage)
    private void showAmpsStats() {
        onShowAmpsStatsButtonPressed.run();
    }

    @ActionProxy(text = "TimeSeries Graphs", graphic = timeSeriesGraphStatsPathImage, accelerator = "ctrl+G")
    private void showTimeSeriesGraph() {
        onShowTimeSeriesGraphButtonPressed.run();
    }

    @ActionProxy(text = "TimeSeries Stats", graphic = timeSeriesStatsPathImage)
    private void showTimeSeriesStats() {
        onShowTimeSeriesStatsButtonPressed.run();
    }

    @ActionProxy(text = "Undock", graphic = undockPathImage)
    private void showInNewWindowStat() {
        onShowInNewWindowButtonPressed.run();
    }

    @ActionProxy(text = "Reload Cache", graphic = undockPathImage)
    private void reloadCache() {
        onReloadCacheButtonPressed.run();
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

    @ActionProxy(text = "Dashboard", graphic = dashBoardPathImage)
    private void dashBoard() {
        onShowDashBoardButtonPressed.run();
    }


    @ActionProxy(text = "Help", graphic = helpPathImage)
    private void help() {
        hostServices.showDocument("http://www.headfront.co.uk/JetFuel.html");
    }

    @ActionProxy(text = "JetFuel", graphic = jetFuelImage)
    private void jetFuel() {
        hostServices.showDocument("https://jetfuel.solutions/");
    }

    @ActionProxy(text = "Explore Data", graphic = explorePathImage)
    private void exploreData() {
        onExplorerButtonPressed.accept(null);
    }

    @ActionProxy(text = "Publish Data", graphic = publishPathImage)
    private void publish() {
        onPublisherButonPressed.run();
    }

    @ActionProxy(text = "Publish Data From AMPS log File", graphic = publishFromFilePathImage)
    private void publishFromFile() {
        onFilePublisherButonPressed.run();
    }

    @ActionProxy(text = "Stop", graphic = stopPathImage)
    private void stop() {
        onStopButtonPressed.run();
    }

    @ActionProxy(text = "Execute", graphic = executePathImage)
    private void execute() {
        onExecuteButtonPressed.run();
    }

    @ActionProxy(text = "Clear", graphic = clearDataPathImage)
    private void clear() {
        onClearButtonPressed.run();
    }

    @ActionProxy(text = "SOW Stats", graphic = topicsPathImage)
    private void showSowStatus() {
        onSowStatusbuttonPressed.run();
    }

    @ActionProxy(text = "Client Status", graphic = usersPathImage)
    private void showUserStatus() {
        onClientStatusButtonPressed.run();
    }

    @ActionProxy(text = "Subscribe from Amps and write to log file")
    private void subscribeFromAmpsToFile() {
        onSubscribeAmpstoFileButonPressed.run();
    }

    public MenuBar getMenuBar() {
        return ActionUtils.createMenuBar(menuBarActions);
    }

    public ToolBar getToolBar(Color backgroundColour) {
        ToolBar toolBar = ActionUtils.createToolBar(toolBarActions, ActionUtils.ActionTextBehavior.SHOW);
        toolBar.setBackground(new Background(new BackgroundFill(backgroundColour, CornerRadii.EMPTY, Insets.EMPTY)));
        return toolBar;
    }

    public void onExploreButtonPressed(Consumer<DataExplorerSelection> r) {
        onExplorerButtonPressed = r;
    }

    public void onSaveTableToFileButtonPressed(Runnable runnable) {
        onSaveTableToFileButtonPressed = runnable;
    }

    public void onPublisherButtonPressed(Runnable runnable) {
        onPublisherButonPressed = runnable;
    }

    public void onStopButtonPressed(Runnable runnable) {
        onStopButtonPressed = runnable;
    }

    public void onSowStatusButtonPressed(Runnable runnable) {
        onSowStatusbuttonPressed = runnable;
    }

    public void onClientStatusButtonPressed(Runnable runnable) {
        onClientStatusButtonPressed = runnable;
    }

    public void onClearButtonPressed(Runnable runnable) {
        onClearButtonPressed = runnable;
    }

    public void onShowTimeSeriesGraphButtonPressed(Runnable runnable) {
        onShowTimeSeriesGraphButtonPressed = runnable;
    }

    public void onShowTimeSeriesStatsButtonPressed(Runnable runnable) {
        onShowTimeSeriesStatsButtonPressed = runnable;
    }

    public void onShowAmpsStatsButtonPressed(Runnable runnable) {
        onShowAmpsStatsButtonPressed = runnable;
    }

    public void onShowInNewWindowButtonPressed(Runnable runnable) {
        onShowInNewWindowButtonPressed = runnable;
    }

    public void onExecuteButtonPressed(Runnable runnable) {
        onExecuteButtonPressed = runnable;
    }

    public void onFilePublisherButonPressed(Runnable runnable) {
        onFilePublisherButonPressed = runnable;
    }

    public void onSubscribeAmpstoFileButonPressed(Runnable runnable) {
        onSubscribeAmpstoFileButonPressed = runnable;
    }

    public void onShowDashBoardButtonPressed(Runnable runnable) {
        onShowDashBoardButtonPressed = runnable;
    }

    public void onSubscriptionsButtonPressed(Runnable runnable) {
        onSubscriptionsButtonPressed = runnable;
    }

    public void setOnShowSheetButtonPressed(Runnable onShowSheetButtonPressed) {
        this.onShowSheetButtonPressed = onShowSheetButtonPressed;
    }

    public void setOnReloadCacheButtonPressed(Runnable onReloadCacheButtonPressed) {
        this.onReloadCacheButtonPressed = onReloadCacheButtonPressed;
    }

    public void setHostServices(HostServices hostServices, String adminUrl, String galvanometerUrl) {
        this.hostServices = hostServices;
        this.adminUrl = adminUrl;
        this.galvanometerUrl = galvanometerUrl;
    }
}
