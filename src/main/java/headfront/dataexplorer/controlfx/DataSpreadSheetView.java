package headfront.dataexplorer.controlfx;

import headfront.dataexplorer.DataExplorer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

/**
 * Created by Deepak on 04/06/2017.
 */
public class DataSpreadSheetView extends SpreadsheetView {
    private final Runnable selected;
    private Runnable showNewRecords;

    public DataSpreadSheetView(Grid grid, Runnable selected, Runnable showNewRecords) {
        super(grid);
        this.selected = selected;
        this.showNewRecords = showNewRecords;
    }

    public ContextMenu getSpreadsheetViewContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem menuItem = new MenuItem("JetFuel Data Selector");
        menuItem.setOnAction(e -> {
            selected.run();
        });
        menuItem.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN));
        menuItem.setGraphic(new ImageView(DataExplorer.jetfuelButtonImage));

        MenuItem menuItemNew = new MenuItem("All New records from Topic");
        menuItemNew.setOnAction(e -> {
            showNewRecords.run();
        });
        menuItemNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));

        final MenuItem copyItem = new MenuItem(localize(asKey("spreadsheet.view.menu.copy"))); //$NON-NLS-1$
        copyItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("copySpreadsheetView.png")))); //$NON-NLS-1$
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copyItem.setOnAction(e -> copyClipboard());

        final MenuItem pasteItem = new MenuItem(localize(asKey("spreadsheet.view.menu.paste"))); //$NON-NLS-1$
        pasteItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("pasteSpreadsheetView.png")))); //$NON-NLS-1$
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
        pasteItem.setOnAction(e -> pasteClipboard());

        contextMenu.getItems().addAll(menuItem, menuItemNew, copyItem, pasteItem);
        return contextMenu;
    }
}

