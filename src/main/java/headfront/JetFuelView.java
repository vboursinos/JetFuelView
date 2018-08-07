package headfront;

import headfront.handlers.AlertHandler;
import headfront.handlers.ConfirmHandler;
import headfront.services.ProcessService;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by Deepak on 08/03/2016..
 */
@SpringBootApplication
public class JetFuelView extends Application {

    private static ConfigurableApplicationContext applicationContext;
    private static final Logger LOG = LoggerFactory.getLogger(JetFuelView.class);
    public static final ImageView jetfuelImage = new ImageView(new Image("images/JetFuelLarge.png"));

    public static void main(String[] args) {
        LOG.info("Starting JetFuelView");
        applicationContext = SpringApplication.run(JetFuelView.class, args);
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        String url = "http://localhost:8080";

        WebView browser = createWebBrowser(stage, url);
        stage.setScene(new Scene(browser));
        stage.setTitle("HeadFront JetFuelView");
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        showSplash(stage);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private void showSplash(Stage primaryStage) {
        try {
            Stage splashStage = new Stage();

            VBox splashPanel = new VBox();
            splashPanel.getChildren().add(jetfuelImage);
            splashStage.setScene(new Scene(splashPanel));
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(bounds.getWidth() - 600 / 2);
            primaryStage.setY(bounds.getHeight() - 500 / 2);
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

    private WebView createWebBrowser(Stage stage, String url) {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        webEngine.setConfirmHandler(new ConfirmHandler(stage));
        webEngine.setOnAlert(new AlertHandler(stage));
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
        {
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("java", new JavaBridge());
//            webEngine.executeScript("console.log = function(message)\n" +
//                    "{\n" +
//                    "    java.log(message);\n" +
//                    "};");
        });
        return browser;
    }

    @Override
    public void stop() {
        try {
            super.stop();
//            ProcessService processService = (ProcessService) applicationContext.getBean("processService");
//            SpringApplication.exit(applicationContext);
//            processService.shutdownAllProcess();
            Platform.exit();
            Thread.sleep(1000);
            System.exit(0);
            LOG.info("JetFuelView Stopped cleanly");
        } catch (Exception e) {
            LOG.info("Unable to Stopped cleanly ", e);
        }
    }


    public class JavaBridge {
        public void log(String text) {
            LOG.info(text);
        }
    }
}