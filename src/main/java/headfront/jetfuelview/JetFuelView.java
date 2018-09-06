package headfront.jetfuelview;

import headfront.dataexplorer.DataExplorer;
import headfront.dataexplorer.FXConfiguraion;
import headfront.dataexplorer.JetFuelDataExplorerProperties;
import headfront.guiwidgets.PopUpDialog;
import headfront.jetfuelview.panel.AbstractLogonPanel;
import headfront.jetfuelview.panel.JetFuelExplorerLogonPanel;
import headfront.jetfuelview.panel.JetFuelViewLogonPanel;
import headfront.jetfuelview.panel.SystemViewPanel;
import headfront.jetfuelview.util.JetFuelViewActions;
import headfront.utils.GuiUtil;
import headfront.utils.StringUtils;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;

import static headfront.jetfuelview.util.ProcessLauncher.killAllChildrenProcess;

/**
 * Created by Deepak on 19/08/2018.
 */
public class JetFuelView extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(JetFuelView.class);
    public final static Image jetfuelTitlebarImage = new Image("images/icons/JetFuelMediumNoBg2.png");
    private Stage logonStage = null;
    private Stage mainStage = null;
    private Object version = 0;
    private List<String> logonDetails;
    private String environment = "";
    private String username = "";
    private String credential = "";
    private String propertiesFile = "";
    private JetFuelViewActions jetFuelViewActions;
    private String APP_TYPE_JETFUEL_VIEW = "JetFuelView";
    private String APP_TYPE_JETFUEL_EXPLORER = "JetFuelExplorer";
    private String appType = APP_TYPE_JETFUEL_EXPLORER;
    private boolean isAppJetFuelView = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final Parameters params = getParameters();
        final List<String> parameters = params.getRaw();
        if (parameters.size() != 1) {
            throw new IllegalArgumentException("Expected one parameter of value " +
                    APP_TYPE_JETFUEL_VIEW + " or " + APP_TYPE_JETFUEL_EXPLORER + " we found none.");
        }
        appType = parameters.get(0);
        if (appType.equalsIgnoreCase(APP_TYPE_JETFUEL_VIEW)) {
            isAppJetFuelView = true;
        } else if (appType.equalsIgnoreCase(APP_TYPE_JETFUEL_EXPLORER)) {
            isAppJetFuelView = false;
        } else {
            throw new IllegalArgumentException("Expected one parameter of value " + APP_TYPE_JETFUEL_VIEW + " or " + APP_TYPE_JETFUEL_EXPLORER +
                    " we found " + appType);
        }

        version = JetFuelDataExplorerProperties.getInstance().getProperty("version");
        LOG.info("Starting JetFuel version " + version + " appType " + appType);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOG.error("Caught Exception on Thread " + t, e);
        });

        try {
            showSplash(stage);
            Scene scene = new Scene(createLogonPanel());
            scene.getStylesheets().add("fx.css");
            ServiceLoader<FXConfiguraion> configurationServiceLoader = ServiceLoader.load(FXConfiguraion.class);
            for (FXConfiguraion fxsamplerConfiguration : configurationServiceLoader) {
                String stylesheet = fxsamplerConfiguration.getSceneStylesheet();
                if (stylesheet != null) {
                    scene.getStylesheets().add(stylesheet);
                }
            }
            stage.setScene(scene);
            String title = appType + " - " + version;
            stage.setTitle(title);
            stage.setWidth(350);
            if (isAppJetFuelView) {
                stage.setHeight(240);
            } else {
                stage.setHeight(270);
            }
            stage.setResizable(true);
            stage.getIcons().add(jetfuelTitlebarImage);
            logonStage = stage;
        } catch (Exception e) {
            LOG.error("Unable to start DataExplorer", e);
            throw e;
        }
    }

    private Parent createLogonPanel() {
        BorderPane borderPane = new BorderPane();
        AbstractLogonPanel logonPanel;
        if (isAppJetFuelView) {
            logonPanel = new JetFuelViewLogonPanel(this::shutDownJetFuelView, this::loggedOnJetFuelView);
        } else {
            logonPanel = new JetFuelExplorerLogonPanel(this::shutDownJetFuelView, this::loggedOnJetFuelExplorer);
        }
        borderPane.setCenter(logonPanel.getMainPane());
        return borderPane;
    }

    private void shutDownJetFuelView() {
        LOG.info("Shutting down " + appType);
        killAllChildrenProcess();
        Platform.exit();
        System.exit(0);
    }

    private void loggedOnJetFuelView(List<String> logonDetails) {
        username = logonDetails.get(0);
        credential = logonDetails.get(1);
        environment = logonDetails.get(2).replace(".properties", "");
        propertiesFile = logonDetails.get(2);
        LOG.info("Starting " + appType);
        jetFuelViewActions = new JetFuelViewActions(environment, this::shutDownJetFuelView);
        jetFuelViewActions.setHostServices(getHostServices());
        createMainStageJetFuelView();
        logonStage.close();
    }

    private void loggedOnJetFuelExplorer(List<String> logonDetails) {
        environment = logonDetails.get(0).replace(".properties", "");
        propertiesFile = logonDetails.get(0);
        username = logonDetails.get(0);
        credential = logonDetails.get(1);
        String connectionsStr = logonDetails.get(2);
        String adminPortStr = logonDetails.get(3);
        String environmentStr = logonDetails.get(4);
        LOG.info("Starting " + appType);
        DataExplorer dataExplorer = new DataExplorer(connectionsStr, adminPortStr, environmentStr);
        try {
            dataExplorer.start(logonStage);
        } catch (Exception e) {
            final String connStr = StringUtils.removePassword(connectionsStr);
            LOG.error("Unable to start JetFuelExplorer " + connStr, e);
            PopUpDialog.showWarningPopup("Unable to start JetFuelExplorer",
                    "Unable to start JetFuelExplorer to" + connStr + " " + e.getMessage());
        }
    }

    private void createMainStageJetFuelView() {
        SystemViewPanel systemViewPanel = new SystemViewPanel(environment, propertiesFile, username, credential, jetFuelViewActions);
        jetFuelViewActions.setGraphModel(systemViewPanel.getJetFuelGraphModel());
        BorderPane topPanels = new BorderPane();
        topPanels.setTop(jetFuelViewActions.getMenuBar());
        topPanels.setCenter(jetFuelViewActions.getToolBar(GuiUtil.getEnvColour(environment)));

        BorderPane mainPanel = new BorderPane();
        mainPanel.setTop(topPanels);
        mainPanel.setCenter(systemViewPanel.getMainPane());
        Scene mainScene = new Scene(mainPanel);
        mainScene.getStylesheets().add("fx.css");
        ServiceLoader<FXConfiguraion> configurationServiceLoader = ServiceLoader.load(FXConfiguraion.class);
        for (FXConfiguraion fxsamplerConfiguration : configurationServiceLoader) {
            String stylesheet = fxsamplerConfiguration.getSceneStylesheet();
            if (stylesheet != null) {
                mainScene.getStylesheets().add(stylesheet);
            }
        }
        mainStage = new Stage();
        mainStage.setScene(mainScene);

        String title = appType + " - " + version + " [Environment - " + environment + "]";
        mainStage.setTitle(title);
        mainStage.setWidth(1200);
        mainStage.setHeight(900);
        mainStage.getIcons().add(jetfuelTitlebarImage);
        mainStage.setOnCloseRequest(e -> {
            shutDownJetFuelView();
        });
        mainStage.show();
        mainStage.toFront();

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
                primaryStage.centerOnScreen();
                primaryStage.show();

            });
            closeSplash.play();

        } catch (Exception e) {
            LOG.error("Unable to show splash", e);
        }
    }
}
