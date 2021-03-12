package headfront.dataexplorer;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.amps.AmpsConnection;
import headfront.amps.services.AmpsConfigLoader;
import headfront.amps.services.AmpsTopicServiceImpl;
import headfront.amps.services.TopicService;
import headfront.dataexplorer.bean.DataBean;
import headfront.dataexplorer.tabs.*;
import headfront.dataexplorer.tabs.statistics.StatsTimeSeriesDataChooser;
import headfront.execute.impl.test.TestJetFuelExecuteService;
import headfront.guiwidgets.AmpsStatusBar;
import headfront.guiwidgets.NarrowableList;
import headfront.guiwidgets.PopUpDialog;
import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.impl.AmpsJetFuelExecute;
import headfront.utils.FileUtils;
import headfront.utils.GuiUtil;
import headfront.utils.StringUtils;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Created by Deepak on 28/06/2016.
 */
public class DataExplorer extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(DataExplorer.class);
    private TopicService topicService = null;
    private Stage chooserDialog;
    private DataExplorerChooserPanel dataExplorerChooserPanel;
    private TabPane tabPane = new TabPane();
    private AmpsConnection connection = null;
    private NotificationPane notificationPane = new NotificationPane();
    private AmpsStatusBar statusBar = new AmpsStatusBar(notificationPane);
    private PublisherTab publishTab = null;
    private DashBoardPanel dashBoardPanel = null;
    private DataSheetTab dataSheetTab = null;
    private FilePublisherTab filePublishTab = null;
    private AmpsSubscriberToLogFileTab subFromAmpstoFileTab = null;
    private AmpsStatsExtractor statsExtractor = null;
    private ExecuteTab executeTab = null;
    private SimpleStatusTableTab sowStatsTab = null;
    private SimpleStatusTableTab clientStatusTab = null;
    private DataExplorerActions dataExplorerActions = null;
    private String connectionsStr = "";
    private boolean useSecureHttp;
    private String adminPortStr = "";
    private String environmentStr = "";
    private String friendlyName = ""; // remove this @todo
    private String CONNECTION_SEPERATOR = "-";
    public final static Image jetfuelTitlebarImage = new Image("images/icons/JetFuelMediumNoBg2.png");
    public final static Image jetfuelButtonImage = new Image("images/icons/JetFuelSmallNoBg.png");
    private AmpsConfigLoader ampsConfigLoader;
    private Map<Tab, Stage> openWindows = new HashMap<>();
    private Stage mainStage = null;
    private boolean showSplash = true;
    private StatsTimeSeriesDataChooser statsTimeSeriesDataChooser;
    private String title = "";
    private String DATA_SHEET_PREFIX = "Data Sheet";
    private LocalDate freeLicenceWarningDate = LocalDate.of(2025, 03, 1);
    private int noOfFreeDaysAfterFreeTrial = 30;
    private boolean checkVersion = true;
    private Object version = "";
    private String noPasswordConnectionStr = "";

    private volatile boolean loadingConfig = false;


    static {
//        GlyphFontRegistry.register("icomoon", DataExplorer.class.getResourceAsStream("fonts/icomoon.ttf") , 12);
        ContentProperties.getInstance();
        StatsProperties.getInstance();
        JetFuelDataExplorerProperties.getInstance();
    }

    public DataExplorer() {
    }

    public DataExplorer(String connectionsStr, String adminPortStr, String environmentStr, boolean useSecureHttp) {
        this.connectionsStr = connectionsStr;
        this.useSecureHttp = useSecureHttp;
        if (connectionsStr.contains(CONNECTION_SEPERATOR)) {
            final String[] split = connectionsStr.split(CONNECTION_SEPERATOR);
            friendlyName = split[0];
            this.connectionsStr = split[1];
        }
        this.adminPortStr = adminPortStr;
        this.environmentStr = environmentStr;

    }

    public static void main(String[] args) {
//        System.setProperty("JetFuelLicence","er");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        checkVersion();
        version = JetFuelDataExplorerProperties.getInstance().getProperty("version");
        mainStage = stage;
        statsTimeSeriesDataChooser = new StatsTimeSeriesDataChooser(true, this::reloadMetaDataAfterConnection);
        statsTimeSeriesDataChooser.initOwner(mainStage);
        LOG.info("Starting Data Explorer version " + version);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOG.error("Caught Exception on Thread " + t, e);
        });
        try {
            initAmps();
            createChooserPanel();
            String adminUrl = StringUtils.getAdminUrl(connectionsStr, adminPortStr, useSecureHttp);
            adminUrl = StringUtils.removePasswordsFromUrl(adminUrl);
            String galvanometerUrl = StringUtils.getGalvanometerUrl(connectionsStr, adminPortStr, useSecureHttp);
            galvanometerUrl = StringUtils.removePasswordsFromUrl(galvanometerUrl);
            dataExplorerActions.setHostServices(getHostServices(), adminUrl, galvanometerUrl);
            Scene scene = new Scene(createMainPanel());
            scene.getStylesheets().add("fx.css");
            ServiceLoader<FXConfiguraion> configurationServiceLoader = ServiceLoader.load(FXConfiguraion.class);
            for (FXConfiguraion fxsamplerConfiguration : configurationServiceLoader) {
                String stylesheet = fxsamplerConfiguration.getSceneStylesheet();
                if (stylesheet != null) {
                    scene.getStylesheets().add(stylesheet);
                }
            }
            stage.setScene(scene);
            noPasswordConnectionStr = StringUtils.removePassword(connectionsStr);
            String friendlyNameStr = friendlyName.length() > 0 ? " FriendlyName[" + friendlyName + "]" : "";
            updateTitlebar();
            stage.setWidth(1200);
            stage.setHeight(900);
            stage.getIcons().add(jetfuelTitlebarImage);
            if (showSplash) {
                showSplash(stage);
            } else {
                stage.show();
            }
            stage.setOnCloseRequest(e -> {
                shutDownDataExplorer();
            });
        } catch (Exception e) {
            LOG.error("Unable to start DataExplorer", e);
            throw e;
        }
    }

    private void updateTitlebar() {
        title = "JetFuelExplorer - " + version + " [Server - "
                + noPasswordConnectionStr + "] [Environment - " + environmentStr + "]" +
                ampsConfigLoader.getInstanceName() + " [ConnectionName - " + connection.getConnectionNameToUse() + "]";
        mainStage.setTitle(title);
    }

    private void checkVersion() {
        if (checkVersion) {
            final String jetFuelCheck = System.getProperty("JetFuel");
            if (jetFuelCheck == null || jetFuelCheck.trim().isEmpty()) {
                final LocalDate expiryDate = freeLicenceWarningDate.plusDays(noOfFreeDaysAfterFreeTrial);
                if (LocalDate.now().isAfter(expiryDate)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "This version of JetFuel has expired. \nPlease email deepakcdo@gmail.com to get a new version");
                    alert.showAndWait();
                    LOG.info("Version check is expired.");
                    System.exit(1);
                }
                if (LocalDate.now().isAfter(freeLicenceWarningDate)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "This version of JetFuel will expire on " + expiryDate.toString() +
                            " and will NOT start after the date.\n\nPlease email deepakcdo@gmail.com to get a new version.");
                    alert.showAndWait();
                    LOG.info("Version warning showed to user as it expires on " + freeLicenceWarningDate);
                } else {
                    LOG.info("Version is in evaluation period as it expires on " + freeLicenceWarningDate);
                }
            } else {
                LOG.info("Version check is good and valid.");
            }
        } else {
            LOG.info("Version check not required.");
        }
    }

    public void setCheckVersion(boolean checkVersion) {
        this.checkVersion = checkVersion;
    }

    public void shutDownDataExplorer() {
        if (connection != null) {
            connection.cleanShutDown();
        }
        Platform.exit();
        System.exit(0);
    }

    private void initAmps() {
        if (connectionsStr.length() + environmentStr.length() == 0) {
            final Parameters params = getParameters();
            final List<String> parameters = params.getRaw();
            if (parameters.size() != 4) {
                throw new IllegalArgumentException("Expected 4 params ampsConnectionStr adminPort env securehttp");
            }
            connectionsStr = parameters.get(0);
            adminPortStr = parameters.get(1);
            environmentStr = parameters.get(2);
            useSecureHttp = Boolean.parseBoolean(parameters.get(3));
            if (connectionsStr.contains(CONNECTION_SEPERATOR)) {
                final String[] split = connectionsStr.split(CONNECTION_SEPERATOR);
                friendlyName = split[0];
                this.connectionsStr = split[1];
            }
        }

        dataExplorerActions = new DataExplorerActions(GuiUtil.isProd(environmentStr), this::shutDownDataExplorer);
        connection = new AmpsConnection("JetFuel", connectionsStr, () -> {
            PopUpDialog.showWarningPopup("Cant Connect to AMPS",
                    "JetFuel is being disconnected from amps every few seconds. Maybe there is duplicate connections. Please restart.",
                    5000);
            connection.cleanShutDown();
            connection.sendConnectionStatus();
            statusBar.restartAmps();
        });
        connection.addConnectionStatusListener((connected, message) -> {
            statusBar.setAmpsConnectionStatus(connected, message);
            if (connected) {
                reloadMetaDataAfterConnection();
            }
        });
        //        topicService = new TestTopicServiceImpl();
        topicService = new AmpsTopicServiceImpl(connection);
        ampsConfigLoader = new AmpsConfigLoader(connectionsStr, adminPortStr, topicService, useSecureHttp);
        connection.initialize();
        statsTimeSeriesDataChooser.setTopicService(topicService);

    }

    private void reloadMetaDataAfterConnection() {
        if (!loadingConfig) {
            loadingConfig = true;
            Runnable r = () -> {
                ampsConfigLoader.loadMetaData();
                Platform.runLater(() -> {
                    dataExplorerChooserPanel.reset(true);
                    updateTitlebar();
                    LOG.info("Connected to AMPS " + ampsConfigLoader.getInstanceName());
                });
                loadingConfig = false;
            };
            new Thread(r).start();
        }
    }

    private Parent createMainPanel() {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createTopButonPanel());
        borderPane.setCenter(createMiddleTabPanel());
        borderPane.setBottom(createStatusBar());
        return borderPane;
    }

    private Node createMiddleTabPanel() {
        notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        notificationPane.showFromTopProperty().setValue(false);
        notificationPane.hide();
        BorderPane middlePanel = new BorderPane();
        tabPane.getTabs().setAll(new WelcomeTab(statusBar));
        middlePanel.setCenter(tabPane);
        notificationPane.setContent(middlePanel);
        return notificationPane;
    }

    private void createChooserPanel() {
        chooserDialog = new Stage();
        dataExplorerChooserPanel = new DataExplorerChooserPanel(topicService, this::dataSelected, this::getSow, this::closeChooser, this::getCount);
        Scene chooserScene = new Scene(dataExplorerChooserPanel.createPanel());
        chooserScene.getStylesheets().add("fx.css");
        chooserDialog.setScene(chooserScene);
        chooserDialog.getIcons().add(jetfuelTitlebarImage);
        chooserDialog.setTitle("JetFuel - Data Explorer Record Chooser");
        chooserDialog.setX(50);
        chooserDialog.setY(50);
        chooserDialog.initModality(Modality.APPLICATION_MODAL);
        chooserDialog.resizableProperty().setValue(false);
        chooserDialog.setWidth(1260);
        chooserDialog.setHeight(700);
        chooserDialog.initOwner(mainStage);
    }

    private boolean checkAmpsIsRunning() {
        if (!connection.getConnected()) {
            PopUpDialog.showErrorPopup("JefFuel is not connected to Amps.",
                    "JetFuel is not connected to amps so we cant process this. Please wait to see if amps comes up.");
            return false;
        }
        return true;
    }

    private String getCount(DataExplorerSelection selection) {
        CommandId commandId = null;
        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong count = new AtomicLong(0);
        Consumer<Message> handler = message -> {
            if (message.getCommand() == Message.Command.Ack && message.getAckType() == Message.AckType.Stats) {
                count.set(message.getMatches());
                latch.countDown();
            }
        };
        try {
            if (selection.getUseFilter()) {
                commandId = topicService.subscribeToFilter(selection.getTopic(), selection.getFilter(), selection.getOrderBy(),
                        handler, false, false, false, selection.getJetFuelSelectorStart(), selection.getOptions());
            } else {
                commandId = topicService.subscribeToRecord(selection.getTopic(), selection.getRecords(),
                        handler, false, false, false, selection.getJetFuelSelectorStart(), selection.getOptions());
            }
            latch.await(10, TimeUnit.SECONDS);
            connection.unsubscribe(commandId);
        } catch (Exception e) {
            Platform.runLater(() -> {
                PopUpDialog.showErrorPopup("Could not subscribe to topic.",
                        "Could not subscribe to topic " + selection.getTopic() + " " + e.getMessage());
            });
        }
        return StringUtils.formatNumber(count.get());
    }

    private String getSow(DataExplorerSelection selection) {
        CommandId commandId = null;
        StringBuilder builder = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);
        Consumer<Message> handler = message -> {
            String data = message.getData().trim();
            if (data.length() > 1) {
                builder.append("PUBLISH=");
                builder.append(selection.getTopic());
                builder.append("-> ");
                builder.append(message.getData());
                builder.append("\n");
            }
            if (message.getCommand() == Message.Command.GroupEnd) {
                latch.countDown();
            }
        };
        try {
            if (selection.getUseFilter()) {
                commandId = topicService.subscribeToFilter(selection.getTopic(), selection.getFilter(), selection.getOrderBy(),
                        handler, false, false, false, selection.getJetFuelSelectorStart(), selection.getOptions());
            } else {
                commandId = topicService.subscribeToRecord(selection.getTopic(), selection.getRecords(),
                        handler, false, false, false, selection.getJetFuelSelectorStart(), selection.getOptions());
            }
            latch.await(10, TimeUnit.SECONDS);
            connection.unsubscribe(commandId);
        } catch (Exception e) {
            Platform.runLater(() -> {
                PopUpDialog.showErrorPopup("Could not subscribe to topic.",
                        "Could not subscribe to topic " + selection.getTopic() + " " + e.getMessage());
            });
        }
        return builder.toString();
    }

    public void openNewJetFuelSelector(String id) {
        String topicToUse = topicService.getJetFuelExecuteFunctionBus();
        if (topicToUse != null) {
            DataExplorerSelection dataExplorerSelection = new DataExplorerSelection(topicToUse);
            dataExplorerSelection.setRecordDisplayType(RecordDisplay.VERTICAL);
//        dataExplorerSelection.setJetFuelSelector(true);
            dataExplorerSelection.setShowHistory(true);
            dataExplorerSelection.setRecords(Arrays.asList(id));
            dataExplorerSelection.setSelectionType(NarrowableList.SelectionType.SHOW_SELECTED);
            dataExplorerSelection.setFieldSelectionType(NarrowableList.SelectionType.SHOW_SELECTED);
            dataExplorerSelection.setDeltaSubcribe(false);
            //@todo add journal starting point
            dataSelected(dataExplorerSelection);
        } else {
            PopUpDialog.showInfoPopup("JetFuelExecute Transaction history",
                    "JetFuelExecute Transaction history is not available in this test mode");
        }
    }

    private void dataSelected(DataExplorerSelection selection) {
        if (selection != null) {
            if (checkAmpsIsRunning()) {
                RecordDisplay recordDisplay = selection.getRecordDisplayType();
                AbstractDataTab dataTab = null;
                if (selection.isJetFuelSelector()) {
                    dataTab = new JetFuelSelectorDataTableTab(selection.getDisplayName(), connection,
                            selection.isShowHistory(), connection.getMessageConvertor(selection.getTopic()),
                            topicService.getSowKey(selection.getTopic()), selection);
                } else if (recordDisplay == RecordDisplay.HORIZONTAL) {
                    dataTab = new HorizontalDataTableTab(selection.getDisplayName(), connection,
                            selection.isShowHistory(), connection.getMessageConvertor(selection.getTopic()),
                            topicService.getSowKey(selection.getTopic()), selection);
                } else if (recordDisplay == RecordDisplay.VERTICAL) {
                    dataTab = new VerticalDataTableTab(selection.getDisplayName(), connection,
                            selection.isShowHistory(), connection.getMessageConvertor(selection.getTopic()),
                            topicService.getSowKey(selection.getTopic()), selection);
                } else if (recordDisplay == RecordDisplay.TREE) {
                    dataTab = new TreeTab(selection.getDisplayName(), connection,
                            connection.getMessageConvertor(selection.getTopic()), selection,
                            topicService.getSowKey(selection.getTopic()));
                } else {
                    dataTab = new TextAreaTab(selection.getDisplayName(), connection, false, selection);
                }
                initaliseTab(selection, dataTab);
            } else {
                // dont want to hide the chooser form below.
                return;
            }
        }
        closeChooser();
    }

    public void closeChooser() {
        chooserDialog.hide();
        mainStage.toFront();
    }

    private void initaliseTab(DataExplorerSelection selection, AbstractDataTab dataTab) {
        dataTab.onEditPressed(d -> explorerButtonPressed(d));
        dataTab.setMessageCountListener(statusBar::updateMessageCount);
        dataTab.setSubscriptionStatus(statusBar::updateSubscriptionStatus);
        dataTab.setRecordCountListener(statusBar::updateRowCount);
        boolean subscribed = false;
        CommandId commandId = null;
        try {
            NarrowableList.SelectionType selectionType = selection.getSelectionType();
            boolean subscribeOnly = false;
            if (selectionType == NarrowableList.SelectionType.SHOW_NEW) {
                subscribeOnly = true;
            }
            if (selection.getUseFilter()) {
                commandId = topicService.subscribeToFilter(selection.getTopic(), selection.getFilter(), selection.getOrderBy(),
                        dataTab::processAmpsMessage, selection.isShowHistory(), subscribeOnly, selection.isDeltaSubcribe(), selection.getJetFuelSelectorStart(), selection.getOptions());
            } else {
                commandId = topicService.subscribeToRecord(selection.getTopic(), selection.getRecords(),
                        dataTab::processAmpsMessage, selection.isShowHistory(), subscribeOnly, selection.isDeltaSubcribe(), selection.getJetFuelSelectorStart(), selection.getOptions());
            }
            subscribed = true;
        } catch (AMPSException e) {
            Platform.runLater(() -> {
                PopUpDialog.showErrorPopup("Could not subscribe to topic.",
                        "Could not subscribe to topic " + selection.getTopic() + " " + e.getMessage());
            });
        }
        dataTab.setAmpsSubCommandID(commandId);
        if (subscribed) {
            tabPane.getTabs().addAll(dataTab);
            tabPane.getSelectionModel().select(dataTab);
        }
    }

    private void explorerButtonPressed(DataExplorerSelection dataExplorerSelection) {
        if (dataExplorerSelection == null) {
//            dataExplorerChooserPanel.selectNoTopic();
            dataExplorerChooserPanel.forceLoadTopicIfRequired();
        } else {
            dataExplorerChooserPanel.setDataExplorerSelection(dataExplorerSelection);
        }
        dataExplorerChooserPanel.setDataSheetmode(false);
        chooserDialog.toFront();
        chooserDialog.show();
    }

    private Pane createTopButonPanel() {
        dataExplorerActions.onExploreButtonPressed(d -> explorerButtonPressed(d));

        dataExplorerActions.setOnReloadCacheButtonPressed(() -> topicService.clearTopicMetaData());

        dataExplorerActions.setOnShowSheetButtonPressed(() -> {
            if (dataSheetTab == null) {
                dataExplorerChooserPanel.setDataSheetmode(true);
                dataSheetTab = new DataSheetTab(DATA_SHEET_PREFIX, chooserDialog, dataExplorerChooserPanel, topicService, connection);
                tabPane.getTabs().add(dataSheetTab);
                dataExplorerChooserPanel.setDataSheetSelectionListener(dataSheetTab::selectionListener);
            }
            tabPane.getSelectionModel().select(dataSheetTab);
            dataSheetTab.setRecordCountListener(statusBar::updateRowCount);
            showWindowIfExits(dataSheetTab);
        });
        dataExplorerActions.onPublisherButtonPressed(() -> {
            if (publishTab == null) {
                publishTab = new PublisherTab("Publisher", connection);
                tabPane.getTabs().add(publishTab);
            }
            tabPane.getSelectionModel().select(publishTab);
            publishTab.setRecordCountListener(statusBar::updateRowCount);
            showWindowIfExits(publishTab);
        });
        dataExplorerActions.onFilePublisherButonPressed(() -> {
            if (filePublishTab == null) {
                filePublishTab = new FilePublisherTab("File Publisher", connection, topicService);
                tabPane.getTabs().add(filePublishTab);
            }
            tabPane.getSelectionModel().select(filePublishTab);
            filePublishTab.setRecordCountListener(statusBar::updateRowCount);
            showWindowIfExits(filePublishTab);
        });
        dataExplorerActions.onSubscribeAmpstoFileButonPressed(() -> {
            if (subFromAmpstoFileTab == null) {
                subFromAmpstoFileTab = new AmpsSubscriberToLogFileTab("Amps Sub to File", connection, topicService);
                tabPane.getTabs().add(subFromAmpstoFileTab);
            }
            tabPane.getSelectionModel().select(subFromAmpstoFileTab);
            subFromAmpstoFileTab.setRecordCountListener(statusBar::updateRowCount);
            showWindowIfExits(subFromAmpstoFileTab);
        });
        dataExplorerActions.onStopButtonPressed(() -> {
            Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
            if (selectedItem instanceof AbstractDataTab) {
                AbstractDataTab dataTab = (AbstractDataTab) selectedItem;
                dataTab.stopSubscription();
                dataTab.updateLastSubscriptionStatus("Subscription stopped by user");
            } else if (selectedItem.getText().contains(DATA_SHEET_PREFIX)) {
                if (dataSheetTab != null) {
                    dataSheetTab.stopSubscription();
                }
            }
        });
        dataExplorerActions.onClearButtonPressed(() -> {
            Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
            if (selectedItem instanceof AbstractDataTab) {
                AbstractDataTab dataTab = (AbstractDataTab) selectedItem;
                dataTab.clearData();
            } else if (selectedItem.getText().contains(DATA_SHEET_PREFIX)) {
                if (dataSheetTab != null) {
                    dataSheetTab.clear();
                }
            }
        });
        dataExplorerActions.onShowInNewWindowButtonPressed(() -> {
            Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (!selectedItem.getText().toLowerCase().contains("welcome")) {
                    showInNewWindow(selectedItem);
                } else {
                    PopUpDialog.showWarningPopup("Cannot undock", "Welcome page can not be undocked");
                }
            }
        });

        dataExplorerActions.onShowTimeSeriesStatsButtonPressed(() -> {
            if (checkAmpsIsRunning()) {
                statsTimeSeriesDataChooser.showAndWait();
                if (statsTimeSeriesDataChooser.isUserSelectedValidSettings()) {
                    LocalDateTime startDate = statsTimeSeriesDataChooser.getStartDate();
                    LocalDateTime toDate = statsTimeSeriesDataChooser.getToDate();
                    TreeItem<String> stats = statsTimeSeriesDataChooser.getStats();
                    showTimeSeriesStat(startDate, toDate, stats);
                }
            }
        });
        dataExplorerActions.onSubscriptionsButtonPressed(() -> {
            if (checkAmpsIsRunning()) {
                final List<String> activeUsers = topicService.getActiveUsers();
                if (activeUsers == null) {
                    PopUpDialog.showWarningPopup("No users found", "Unable to show users as we cant get data from Amps.");
                } else {
                    GuiUtil.showSelectionList(new HashSet<>(activeUsers),
                            "Choose a User", "Select a user to display the subscriptions.",
                            this::showSubscritpions);
                }
            }
        });

        dataExplorerActions.setOnShowAmpsStatsExtractorButtonPressed(() -> {
            if (statsExtractor == null) {
                statsExtractor = new AmpsStatsExtractor("Amps Stats Extractor",
                        connectionsStr, adminPortStr, useSecureHttp,
                        ampsConfigLoader.getInstanceName());
                tabPane.getTabs().add(statsExtractor);
            }
            tabPane.getSelectionModel().select(statsExtractor);
            statsExtractor.setRecordCountListener(statusBar::updateRowCount);
            showWindowIfExits(statsExtractor);
        });

        dataExplorerActions.onShowAmpsStatsButtonPressed(() -> {
            if (checkAmpsIsRunning()) {
                ObservableList<String> listToUse = FXCollections.observableArrayList();
                listToUse.addAll(StatsProperties.getInstance().getPropertyList("stats"));
                FXCollections.sort(listToUse);
                ChoiceDialog<String> dlg = new ChoiceDialog(listToUse.get(0), listToUse);
                dlg.setTitle("Choose a Stat");
                dlg.getDialogPane().setHeaderText("Select a stat you want view.");
                dlg.getDialogPane().setContentText("");
                dlg.initModality(Modality.APPLICATION_MODAL);
                dlg.initOwner(null);
                dlg.showAndWait().ifPresent(this::selectedStat);
            }
        });
        dataExplorerActions.onSaveTableToFileButtonPressed(() -> {
            Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
            if (selectedItem instanceof TableDataTab) {
                TableDataTab dataTab = (TableDataTab) selectedItem;
                TableView<DataBean> tableView = dataTab.getTableView();
                List<String> columnsName = new ArrayList<>();
                tableView.getColumns().forEach(c -> columnsName.add(c.getText()));
                ObservableList<DataBean> allItems = tableView.getItems();
                StringBuilder dataBuilder = new StringBuilder();
                columnsName.forEach(col -> {
                    dataBuilder.append(col);
                    dataBuilder.append("| ");
                });
                dataBuilder.append("\n");
                allItems.forEach(data -> {
                    columnsName.forEach(col -> {
                        String value = "";
                        Object propertyValue = data.getPropertyValue(col);
                        if (propertyValue != null) {
                            value = propertyValue.toString();
                        }
                        value = StringUtils.removeInvalidCharFromJson(value);
                        dataBuilder.append(value);
                        dataBuilder.append("| ");
                    });
                    dataBuilder.append("\n");
                });
                FileUtils.saveFile(dataBuilder.toString(), false);
            } else {
                PopUpDialog.showWarningPopup("Save to table not supported", "We can only save table views in this version");
            }
        });
        dataExplorerActions.onSowStatusButtonPressed(() -> {
            if (checkAmpsIsRunning()) {
                if (sowStatsTab == null) {
                    sowStatsTab = createSimpleStatusTableTab("SowStatus", AmpsConnection.SOW_STATS_TOPIC, "topic", "SOWStats", Collections.EMPTY_LIST, false);
                }
                tabPane.getSelectionModel().select(sowStatsTab);
                sowStatsTab.setOnClosed(e -> {
                    sowStatsTab.stopSubscription();
                    sowStatsTab = null;
                });
                showWindowIfExits(sowStatsTab);
            }
        });

        dataExplorerActions.onShowTimeSeriesGraphButtonPressed(() -> {
            if (checkAmpsIsRunning()) {
                GraphicalStatisticsChartTab graphicalStatisticsChartTab = new GraphicalStatisticsChartTab(connectionsStr,
                        adminPortStr, topicService, mainStage, this::reloadMetaDataAfterConnection, useSecureHttp);
                graphicalStatisticsChartTab.setRecordCountListener(statusBar::updateRowCount);
                tabPane.getTabs().add(graphicalStatisticsChartTab);
                tabPane.getSelectionModel().select(graphicalStatisticsChartTab);
                showWindowIfExits(graphicalStatisticsChartTab);
            }
        });

        dataExplorerActions.onClientStatusButtonPressed(() -> {
            if (checkAmpsIsRunning()) {
                if (clientStatusTab == null) {
                    List<String> columns = new ArrayList<String>();
                    columns.add("client_name");
                    columns.add("event");
                    columns.add("client_address");
                    columns.add("topic");
                    clientStatusTab = createSimpleStatusTableTab("ClientStatus", AmpsConnection.CLIENT_STATUS_TOPIC, "timestamp", "ClientStatus", columns, false);
                }
                tabPane.getSelectionModel().select(clientStatusTab);
                clientStatusTab.setOnClosed(e -> {
                    clientStatusTab.stopSubscription();
                    clientStatusTab = null;
                });
                showWindowIfExits(clientStatusTab);
            }
        });

        dataExplorerActions.onExecuteButtonPressed(() -> {
            if (executeTab == null) {
                JetFuelExecute executeService = null;
                if (topicService.doesTopicExists(topicService.getJetFuelExecuteFunction())) {
                    executeService = new AmpsJetFuelExecute(connection.getHaClient(), new ObjectMapper());
                    ((AmpsJetFuelExecute) executeService).setFunctionTopic(topicService.getJetFuelExecuteFunction());
                    ((AmpsJetFuelExecute) executeService).setFunctionBusTopic(topicService.getJetFuelExecuteFunctionBus());
                } else {
                    executeService = new TestJetFuelExecuteService();
                }
                executeService.initialise();
                executeTab = new ExecuteTab("Execute", executeService, environmentStr.toLowerCase().contains("prod"));
                executeTab.setTransactionViewer(this::openNewJetFuelSelector);
                executeTab.setSelectionListener(this::dataSelected);
                tabPane.getTabs().add(executeTab);
            }
            tabPane.getSelectionModel().select(executeTab);
            executeTab.setRecordCountListener(statusBar::updateRowCount);
            showWindowIfExits(executeTab);
        });

        dataExplorerActions.onShowDashBoardButtonPressed(() -> {
            if (dashBoardPanel == null) {
                dashBoardPanel = new DashBoardPanel();
                tabPane.getTabs().add(dashBoardPanel);
            }
            tabPane.getSelectionModel().select(dashBoardPanel);
            showWindowIfExits(filePublishTab);
        });
        BorderPane mainButtonPanel = new BorderPane();
        mainButtonPanel.setTop(dataExplorerActions.getMenuBar());
        mainButtonPanel.setCenter(dataExplorerActions.getToolBar(GuiUtil.getEnvColour(environmentStr)));
        return mainButtonPanel;
    }

    private void showWindowIfExits(Tab tab) {
        Stage stage = openWindows.get(tab);
        if (stage != null) {
            stage.toFront();
            stage.show();
        } else {
            mainStage.toFront();
        }
    }

    private void showInNewWindow(Tab selectedItem) {
        Stage newStatge = new Stage();
        String subTitle = title.replace("- Data Explorer ", "");
        subTitle = subTitle + " View - " + selectedItem.getText();
        newStatge.setTitle(subTitle);
        newStatge.getIcons().add(jetfuelTitlebarImage);
        newStatge.initOwner(null);
        newStatge.initModality(Modality.NONE);
        newStatge.setOnShown(t -> tabPane.getTabs().remove(selectedItem));
        newStatge.setOnCloseRequest(t -> {
            tabPane.getTabs().add(selectedItem);
            tabPane.getSelectionModel().select(selectedItem);
            openWindows.remove(selectedItem);
        });
        BorderPane grid = new BorderPane();
        grid.setCenter(selectedItem.getContent());
        Scene newScene = new Scene(grid);
        newStatge.setScene(newScene);
        newStatge.setWidth(900);
        newStatge.setHeight(900);
        newStatge.show();
        openWindows.put(selectedItem, newStatge);

    }

    private void showTimeSeriesStat(LocalDateTime startDate, LocalDateTime toDate, TreeItem<String> stat) {
        String tabName = "TimeSeries Stat " + stat.getValue();
        DataExplorerSelection selection = new DataExplorerSelection(tabName);
        selection.setSelectionType(NarrowableList.SelectionType.SHOW_ALL);
        selection.setFieldSelectionType(NarrowableList.SelectionType.SHOW_SELECTED);
        final StatsTableTab statsTableTab = new StatsTableTab(tabName, null,
                Arrays.asList("timestamp"), stat, startDate, toDate,
                Arrays.asList("value"), selection, connectionsStr, adminPortStr, 0, useSecureHttp);
        statsTableTab.setMessageCountListener(statusBar::updateMessageCount);
        statsTableTab.setSubscriptionStatus(statusBar::updateSubscriptionStatus);
        statsTableTab.setRecordCountListener(statusBar::updateRowCount);
        tabPane.getTabs().add(statsTableTab);
        statsTableTab.setRecordCountListener(statusBar::updateRowCount);
        tabPane.getSelectionModel().select(statsTableTab);
        mainStage.toFront();
    }

    private void selectedStat(String stat) {
        String tabName = "Stat " + stat;
        DataExplorerSelection selection = new DataExplorerSelection(tabName);
        selection.setSelectionType(NarrowableList.SelectionType.SHOW_ALL);
        selection.setFieldSelectionType(NarrowableList.SelectionType.SHOW_SELECTED);
        final StatsTableTab statsTableTab = new StatsTableTab(tabName, null,
                Arrays.asList(StatsProperties.getInstance().getProperty(stat + "ID").toString()), stat,
                StatsProperties.getInstance().getPropertyList(stat + "Fields"), selection,
                connectionsStr, adminPortStr, 10, useSecureHttp);
        statsTableTab.setMessageCountListener(statusBar::updateMessageCount);
        statsTableTab.setSubscriptionStatus(statusBar::updateSubscriptionStatus);
        statsTableTab.setRecordCountListener(statusBar::updateRowCount);
        tabPane.getTabs().add(statsTableTab);
        statsTableTab.setRecordCountListener(statusBar::updateRowCount);
        tabPane.getSelectionModel().select(statsTableTab);
        statsTableTab.setOnClosed(e -> {
            statsTableTab.stopSubscription();
        });
        mainStage.toFront();
    }

    private void showSubscritpions(final String user) {
        String tabName = "Sub " + user;
        DataExplorerSelection selection = new DataExplorerSelection(tabName);
        selection.setSelectionType(NarrowableList.SelectionType.SHOW_ALL);
        selection.setFieldSelectionType(NarrowableList.SelectionType.SHOW_SELECTED);
        SimpleStatusTableTab subscriptionPanel = new SimpleStatusTableTab(tabName, connection,
                connection.getMessageConvertor(selection.getTopic()), Arrays.asList("id"), null, Collections.EMPTY_LIST, selection, false);
        new Thread(() -> {
            while (true) {
                final List<Map<String, Object>> activeSubscriptions = topicService.getActiveSubscriptions(user);
                subscriptionPanel.updateModel(activeSubscriptions);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {

                }
            }
        }).start();
        subscriptionPanel.setMessageCountListener(statusBar::updateMessageCount);
        subscriptionPanel.setSubscriptionStatus(statusBar::updateSubscriptionStatus);
        subscriptionPanel.setRecordCountListener(statusBar::updateRowCount);
        tabPane.getTabs().add(subscriptionPanel);
        tabPane.getSelectionModel().select(subscriptionPanel);
        subscriptionPanel.setOnClosed(e -> {
            subscriptionPanel.stopSubscription();
        });
        mainStage.toFront();
    }

    private SimpleStatusTableTab createSimpleStatusTableTab(String tabPaneName, String topic, String id,
                                                            String dataFieldKey, List<String> columns,
                                                            boolean clearOnUpdate) {
        DataExplorerSelection selection = new DataExplorerSelection(topic);
        selection.setSelectionType(NarrowableList.SelectionType.SHOW_ALL);
        selection.setFieldSelectionType(NarrowableList.SelectionType.SHOW_SELECTED);
        SimpleStatusTableTab sowStatusPanel = new SimpleStatusTableTab(tabPaneName, connection,
                connection.getMessageConvertor(selection.getTopic()), Arrays.asList(id), dataFieldKey, columns, selection, clearOnUpdate);
        sowStatusPanel.setMessageCountListener(statusBar::updateMessageCount);
        sowStatusPanel.setSubscriptionStatus(statusBar::updateSubscriptionStatus);
        sowStatusPanel.setRecordCountListener(statusBar::updateRowCount);
        try {
            CommandId commandId = topicService.subscribeToRecord(topic, Collections.EMPTY_LIST,
                    sowStatusPanel::processAmpsMessage, false, false, false, selection.getJetFuelSelectorStart(), selection.getOptions());
            sowStatusPanel.setAmpsSubCommandID(commandId);
            tabPane.getTabs().add(sowStatusPanel);

        } catch (AMPSException e) {
            PopUpDialog.showWarningPopup("Could not subscribe to topic.", "Could not subscribe to Topic " + topic);
        }
        sowStatusPanel.setRecordCountListener(statusBar::updateRowCount);
        return sowStatusPanel;
    }


    private StatusBar createStatusBar() {
        return statusBar;
    }

    private void showSplash(Stage primaryStage) {
        try {
            Stage splashStage = new Stage();
            ImageView imageView = new ImageView(new Image("images/icons/JetFuelLarge.png"));
            VBox splashPanel = new VBox();
            splashPanel.getChildren().add(imageView);
            splashStage.setScene(new Scene(splashPanel));
            primaryStage.setX(100);
            primaryStage.setY(50);
            splashStage.initStyle(StageStyle.TRANSPARENT);
            splashStage.getScene().setFill(null);
            splashStage.toFront();
            splashStage.show();
            FadeTransition closeSplash = new FadeTransition(Duration.seconds(2), splashPanel);
            closeSplash.setFromValue(1);
            closeSplash.setToValue(0);
            closeSplash.setOnFinished(e -> {
                splashStage.hide();
                primaryStage.show();

            });
            closeSplash.play();

        } catch (Exception e) {
            LOG.error("Unable to show splash", e);
        }
    }
}
