package headfront.dataexplorer.tabs;

import headfront.dataexplorer.bean.DataBean;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Created by Deepak on 06/07/2016.
 */
public class TableCellFactory<T> implements Callback<TableColumn.CellDataFeatures<DataBean, T>, ObservableValue<T>> {
    private final String propertyName;

    public TableCellFactory(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public ObservableValue<T> call(TableColumn.CellDataFeatures<DataBean, T> cellDataFeatures) {
        DataBean data = cellDataFeatures.getValue();
        return data == null ? null : data.getProperty(propertyName);
    }
}