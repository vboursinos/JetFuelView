package headfront.newexplorer;

import goryachev.common.util.GlobalSettings;
import goryachev.common.util.Log;
import goryachev.fxdock.FxDockFramework;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

/**
 * Created by Deepak on 08/03/2017.
 */
public class NewExplorer
        extends Application {
    public static void main(String[] args) {
        // init logger
        Log.initConsole();
        Log.conf("DebugSettingsProvider", true);

        // init non-ui subsystems
        GlobalSettings.setFileProvider(new File("settings.conf"));

        // launch ui
        Application.launch(NewExplorer.class, args);
    }


    public void start(Stage s) throws Exception {
        // plug in custom windows and dockable panes.
        FxDockFramework.setGenerator(new ExplorerGenerator());

        // load saved layout
        int ct = FxDockFramework.loadLayout();
        if (ct == 0) {
            // when no saved layout exists, open the first window
            ExplorerWindow.showWelcome();
        }
    }
}

