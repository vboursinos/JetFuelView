package headfront.newexplorer;

import goryachev.fx.CPopupMenu;
import goryachev.fxdock.FxDockPane;
import headfront.dataexplorer.tabs.WelcomeTab;

/**
 * Created by Deepak on 08/03/2017.
 */
public class WelcomePane extends FxDockPane {

    public WelcomePane() {
        super(ExplorerGenerator.WELCOME);
        setTitle("Welcome");
        setCenter(new WelcomeTab(null).getContent());
        // set up context menu off the title field
        titleField.setContextMenu(new CPopupMenu() {
            protected void createPopupMenu() {
                add("Pop up in Window", popToWindowAction);
                add("Close", closeAction);
            }
        });
    }
}
