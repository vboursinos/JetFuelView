package headfront.jetfuelview.panel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Deepak on 31/08/2018.
 */
public class JetFuelViewLogonPanel extends AbstractLogonPanel {

    private ComboBox<String> systemsComboBox = new ComboBox<>();

    public JetFuelViewLogonPanel(Runnable shutdownProcess, Consumer<List<String>> validLogon) {
        super(shutdownProcess, validLogon, true);
    }

    private List<String> getOptionsList(List<Path> files) {
        return files.stream().map(f -> f.toFile().getName()).collect(Collectors.toList());
    }

    @Override
    public Pane getCenterPane(List<Path> files) {
        GridPane selectionPane = new GridPane();
        final List<String> names = getOptionsList(files);
        ObservableList<String> listToUse = FXCollections.observableArrayList();
        listToUse.addAll(names);
        FXCollections.sort(listToUse);
        selectionPane.setHgap(10);
        selectionPane.setVgap(10);
        selectionPane.setPadding(new Insets(5, 5, 0, 5));
        selectionPane.setMaxWidth(Double.MAX_VALUE);
        selectionPane.setAlignment(Pos.CENTER_LEFT);
        systemsComboBox.getItems().addAll(listToUse);
        systemsComboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(systemsComboBox, Priority.ALWAYS);
        GridPane.setFillWidth(systemsComboBox, true);

        selectionPane.add(new Label("Environment"), 0, 0);
        selectionPane.add(systemsComboBox, 1, 0);
        selectionPane.add(new Label("Username"), 0, 1);
        selectionPane.add(usernameTextField, 1, 1);
        selectionPane.add(new Label("Password"), 0, 2);
        selectionPane.add(passwordTextField, 1, 2);
        return selectionPane;
    }

    @Override
    public String getSelectedItem() {
        return systemsComboBox.getSelectionModel().getSelectedItem();
    }
}
