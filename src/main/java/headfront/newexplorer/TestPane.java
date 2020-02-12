package headfront.newexplorer;

import goryachev.fx.CPopupMenu;
import goryachev.fxdock.FxDockPane;
import headfront.dataexplorer.tabs.PublisherTab;

/**
 * Created by Deepak on 08/03/2017.
 */
public class TestPane extends FxDockPane {
    public TestPane() {
        super(ExplorerGenerator.PUBLISHER);
        setTitle("Welcome");
        setCenter(new PublisherTab("", null).getContent());
        // set up context menu off the title field
        titleField.setContextMenu(new CPopupMenu() {
            protected void createPopupMenu() {
                add("Pop up in Window", popToWindowAction);
                add("Close", closeAction);
            }
        });
    }
}