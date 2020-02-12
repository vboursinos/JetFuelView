package headfront.newexplorer;

import goryachev.fxdock.FxDockFramework;
import goryachev.fxdock.FxDockPane;
import goryachev.fxdock.FxDockWindow;

/**
 * Created by Deepak on 08/03/2017.
 */
public class ExplorerGenerator implements FxDockFramework.Generator {
    /**
     * type id for a browser pane
     */
    public static final String WELCOME = "WELCOME";
    /**
     * type id for a CPane demo
     */
    public static final String PUBLISHER = "PUBLISHER";
    /**
     * type id for a HPane demo
     */
    public static final String GRAPHPLOT = "GRAPHPLOT";


    /**
     * creates custom window
     */
    public FxDockWindow createWindow() {
        return new ExplorerWindow();
    }


    /**
     * creates custom pane using the type id
     */
    public FxDockPane createPane(String type) {
        switch (type) {
            case WELCOME:
                return new WelcomePane();
            case PUBLISHER:
                return new PublisherPane();
            case GRAPHPLOT:
                return new GraphPane();
            default:
                // type here codes for background color
                return new TestPane();
        }
    }
}

