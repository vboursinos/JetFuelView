package headfront;

import demo.dock.DemoGenerator;
import demo.dock.DemoWindow;
import goryachev.common.util.GlobalSettings;
import goryachev.fxdock.FxDockFramework;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;

/**
 * Created by Deepak on 08/03/2016..
 */
public class JetFuelView extends Application {
    public static final ImageView jetfuelImage = new ImageView(new Image("images/JetFuelLarge.png"));

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception
    {
        try{
            GlobalSettings.setFileProvider(new File("settings.conf"));
//        showSplash(stage);
        // plug in custom windows and dockable panes.
        FxDockFramework.setGenerator(new DemoGenerator());

        // load saved layout
        int ct = FxDockFramework.loadLayout();
        if(ct == 0)
        {
            // when no saved layout exists, open the first window
            DemoWindow.openBrowser("https://github.com/andy-goryachev/FxDock");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        } catch (Exception e) {
            e.printStackTrace();
//            LOG.error("Unable to show splash", e);
        }
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
            e.printStackTrace();
//            LOG.error("Unable to show splash", e);
        }
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
//            LOG.info("JetFuelView Stopped cleanly");
        } catch (Exception e) {
//            LOG.info("Unable to Stopped cleanly ", e);
        }
    }
}