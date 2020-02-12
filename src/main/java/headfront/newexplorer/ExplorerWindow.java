package headfront.newexplorer;

import goryachev.common.util.D;
import goryachev.common.util.GlobalSettings;
import goryachev.fx.*;
import goryachev.fxdock.FxDockFramework;
import goryachev.fxdock.FxDockWindow;
import goryachev.fxdock.WindowListMenuItem;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Created by Deepak on 08/03/2017.
 */
public class ExplorerWindow extends FxDockWindow {

    public static final CAction newWelcomeAction = new CAction(ExplorerWindow::showWelcome);
    public static final CAction newGraphAction = new CAction(ExplorerWindow::actionGraph);
    public static final CAction newPublisherAction = new CAction(ExplorerWindow::actionPublsiher);
    public static final CAction newTestAction = new CAction(ExplorerWindow::actionTest);
    public static final CAction quitApplicationAction = new CAction(FxDockFramework::exit);
    public static final CAction saveSettingsAction = new CAction(ExplorerWindow::actionSaveSettings);
    public final CAction windowCheckAction = new CAction();
    public final Label statusField = new Label();
    private static GlobalBooleanProperty showCloseDialogProperty = new GlobalBooleanProperty("show.close.dialog", true);


    public ExplorerWindow() {
        setTop(createMenu());
        setBottom(createStatusBar());
        setTitle("JetFuel Future");

        bind("CHECKBOX_MENU", windowCheckAction.selectedProperty());
    }


    protected Node createMenu() {
        CMenuBar mb = new CMenuBar();
        CMenu m;
        CMenu m2;
        // file
        mb.add(m = new CMenu("File"));
        m.add("Save Settings", saveSettingsAction);
        m.separator();
        m.add("Close Window", closeWindowAction);
        m.separator();
        m.add("Quit Application", quitApplicationAction);
        // window
        mb.add(m = new CMenu("Window"));
        m.add("New Welcome", newWelcomeAction);
        m.add("New Price Window", newGraphAction);
        m.add("New publisher Window", newPublisherAction);
        m.add("New Test Window", newTestAction);
        m.separator();
        m.separator();
        m.add(new CCheckMenuItem("Confirm Window Closing", showCloseDialogProperty));
        m.add(new WindowListMenuItem(this, m));
        // help
        mb.add(m = new CMenu("Help"));
        m.add(new CCheckMenuItem("Check Box Menu", windowCheckAction));
        m.add(m2 = new CMenu("Test", new CAction() {
            public void action() {
                D.print("test");
            }
        }));
        m2.add("T2", new CAction() {
            public void action() {
                D.print("t2");
            }
        });
        m.add("T3", new CAction() {
            public void action() {
                D.print("t3");
            }
        });
        return mb;
    }


    protected Node createStatusBar() {
        BorderPane p = new BorderPane();
        p.setLeft(statusField);
        p.setRight(FX.label("copyright © 2016 andy goryachev", new Insets(1, 20, 1, 10)));
        return p;
    }


    public static ExplorerWindow actionPublsiher() {
        ExplorerWindow w = new ExplorerWindow();
        w.setTitle("Publisher");
        w.setContent(new PublisherPane());
        w.setWidth(1000);
        w.setHeight(750);
        w.open();
        return w;
    }

    public static ExplorerWindow actionGraph() {
        ExplorerWindow w = new ExplorerWindow();
        w.setTitle("Graph Demo");
        w.setContent(new GraphPane());
        w.setWidth(1000);
        w.setHeight(750);
        w.open();
        return w;
    }

    public static ExplorerWindow actionTest() {
        ExplorerWindow w = new ExplorerWindow();
        w.setTitle("Test Demo");
        w.setContent(new TestPane());
        w.setWidth(1000);
        w.setHeight(750);
        w.open();
        return w;
    }

    protected static void actionSaveSettings() {
        FxDockFramework.saveLayout();
        GlobalSettings.save();
    }

    public static ExplorerWindow showWelcome() {
        WelcomePane b = new WelcomePane();

        ExplorerWindow w = new ExplorerWindow();
        w.setContent(b);
        w.setWidth(900);
        w.setHeight(700);
        w.open();
        return w;
    }
}
