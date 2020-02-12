package headfront.newexplorer;

import goryachev.fx.CPopupMenu;
import goryachev.fxdock.FxDockPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Created by Deepak on 08/03/2017.
 */
public class GraphPane extends FxDockPane {
    public GraphPane() {
        super(ExplorerGenerator.GRAPHPLOT);
        setTitle("Price Table");
        TableView<TempPrice> table = new TableView<>();
        ObservableList<TempPrice> data =
                FXCollections.observableArrayList(new TempPrice("DE012312312", 10.1, 15.2, "German Bond", "True"),
                        new TempPrice("US451231232", 100.1, 105.2, "US Bond", "True"), new TempPrice("DG012312312", 150.1, 155.2, "German Bond", "True"),
                        new TempPrice("US045612312", 10.11, 150.2, "US Bond", "True"), new TempPrice("PR012312312", 150.1, 135.2, "German Bond", "True"),
                        new TempPrice("US012235412", 100.1, 150.2, "US Bond", "True"), new TempPrice("TE082312312", 160.1, 415.2, "German Bond", "True")
                );
        TableColumn firstNameCol = new TableColumn("Isin");
        firstNameCol.setMinWidth(120);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<>("inst"));

        TableColumn lastNameCol = new TableColumn("Bid");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<>("bid"));

        TableColumn lastNameOffer = new TableColumn("Offer");
        lastNameOffer.setMinWidth(100);
        lastNameOffer.setCellValueFactory(
                new PropertyValueFactory<>("ask"));

        TableColumn lastNameColdesc = new TableColumn("Description");
        lastNameColdesc.setMinWidth(150);
        lastNameColdesc.setCellValueFactory(
                new PropertyValueFactory<>("desc"));

        TableColumn lastNameColactive = new TableColumn("Active");
        lastNameColactive.setMinWidth(100);
        lastNameColactive.setCellValueFactory(
                new PropertyValueFactory<>("active"));

        table.setItems(data);
        table.getColumns().addAll(firstNameCol, lastNameCol, lastNameOffer, lastNameColdesc, lastNameColactive);
        setCenter(table);
        // set up context menu off the title field
        titleField.setContextMenu(new CPopupMenu() {
            protected void createPopupMenu() {
                add("Pop up in Window", popToWindowAction);
                add("Close", closeAction);
            }
        });
    }
}
