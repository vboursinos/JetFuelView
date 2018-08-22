package headfront.jetfuelview;

import headfront.dataexplorer.FXConfiguraion;
import headfront.dataexplorer.JetFuelDataExplorerProperties;
import headfront.jetfuelview.panel.LogonPanel;
import headfront.jetfuelview.panel.SystemViewPanel;
import headfront.jetfuelview.util.JetFuelViewActions;
import headfront.utils.GuiUtil;
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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        version = JetFuelDataExplorerProperties.getInstance().getProperty("version");
        LOG.info("Starting Data Explorer version " + version);
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
            String title = "JetFuelView - " + version;
            stage.setTitle(title);
            stage.setWidth(350);
            stage.setHeight(230);
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
        borderPane.setCenter(new LogonPanel(this::shutDownJetFuelView, this::loggedOn).getMainPane());
        return borderPane;
    }

    private void shutDownJetFuelView() {
        LOG.error("Shutting down JetFuelView");
        killAllChildrenProcess();
        Platform.exit();
        System.exit(0);
    }

    private void loggedOn(List<String> logonDetails) {
        environment = logonDetails.get(0).replace(".properties", "");
        propertiesFile = logonDetails.get(0);
        username = logonDetails.get(1);
        credential = logonDetails.get(2);
        LOG.info("Starting JetFuelView");
        jetFuelViewActions = new JetFuelViewActions(environment, this::shutDownJetFuelView);
        jetFuelViewActions.setHostServices(getHostServices());
        createMainStage();
        logonStage.close();
    }

    private void createMainStage() {
        SystemViewPanel systemViewPanel = new SystemViewPanel(environment, propertiesFile, username, credential);
        jetFuelViewActions.setGraphComponent(systemViewPanel.getGraphComponent());
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

        String title = "JetFuelView - " + version + " [Environment - " + environment + "]";
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
