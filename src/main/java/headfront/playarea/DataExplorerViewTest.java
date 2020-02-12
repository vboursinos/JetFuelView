package headfront.playarea;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.controlsfx.control.spreadsheet.*;

import java.util.*;

/**
 * Created by Deepak on 08/03/2016..
 */
public class DataExplorerViewTest extends Application {

    private GridBase grid;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
//        List<String> stringList = getParameters().getUnnamed();
        stage.setScene(new Scene(getNode()));
        stage.setTitle("HeadFront JetFuel - ");
//        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(200);
        stage.setY(50);
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.show();
        updateCell();
    }

    private Parent getNode() {
        int rowCount = 31; //Will be re-calculated after if incorrect.
        int columnCount = 100;

        grid = new GridBase(rowCount, columnCount);
        buildGrid(grid);

        SpreadsheetView spreadSheetView = new SpreadsheetView(grid);
        spreadSheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadSheetView.getColumns().forEach(c -> c.setPrefWidth(100));

        GridPane centerPane = new GridPane();
        centerPane.add(spreadSheetView, 0, 0);
        GridPane.setHgrow(spreadSheetView, Priority.ALWAYS);
        GridPane.setVgrow(spreadSheetView, Priority.ALWAYS);
        return centerPane;
    }

    private void buildGrid(GridBase grid) {
        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<>(grid.getRowCount());

        int requiredRows = 100;
        for (int row = 0; row < requiredRows; row++) {
            rows.add(getSeparator(grid, row));
        }
        grid.setRows(rows);

    }

    private void setData(int x, int y, Object value) {
        grid.setCellValue(x, y, value);

    }


    private ObservableList<SpreadsheetCell> getSeparator(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> separator = FXCollections.observableArrayList();

        for (int column = 0; column < grid.getColumnCount(); ++column) {
            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
            cell.setEditable(true);
            cell.getStyleClass().add("separator");
            separator.add(cell);
        }
        return separator;
    }


    private ObservableList<SpreadsheetCell> getCompanies(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> companies = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Company : ");
        ((SpreadsheetCellBase) cell).setTooltip("This cell displays a custom toolTip.");
        cell.setEditable(false);
        companies.add(cell);

        for (int column = 1; column < grid.getColumnCount(); ++column) {
            cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1,
                    "dd" + column);
            cell.setEditable(false);
            cell.getStyleClass().add("compagny");
            companies.add(cell);
        }

        return companies;
    }

    boolean first = true;


    private void updateCell() {
        new Thread(() -> {
            final List<String> companiesList = Arrays.asList("Startup", "ControlsFX", "Aperture Science",
                    "Rapture", "Ammu-Nation", "Nuka-Cola", "Pay'N'Spray", "Umbrella Corporation");
            Random random = new Random();

            while (true) {
                try {

                    Thread.sleep(10);
                    final String companyIndex = companiesList.get(random.nextInt(companiesList.size()));
                    final int x = random.nextInt(100);
                    final int y = random.nextInt(100);

                    Platform.runLater(() -> {
                        setData(x, y, companyIndex);
                        setData(0, 0, new Date().toString() + " " + x + " " + y);
                        if (first) {
                            setData(4, 3, "Deepak");
                            setData(4, 4, "Sarah");
                            setData(5, 3, "Deepak");
                            setData(5, 4, "Vanessa");
                        } else {
                            setData(4, 3, "James");
                            setData(4, 4, "Salma");
                            setData(4, 3, "James");
                            setData(4, 4, "Hayak");
                        }
                        first = !first;

                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
