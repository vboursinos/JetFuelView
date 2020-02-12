package headfront.dataexplorer.tabs;

import headfront.guiwidgets.AmpsStatusBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Created by Deepak on 09/07/2016.
 */
public class WelcomeTab extends Tab {

    public WelcomeTab(AmpsStatusBar statusBar) {
        super("Welcome");
        closableProperty().setValue(false);
        if (statusBar != null) {
            statusBar.showWelcomeMessage();
        }
        BorderPane middlePanel = new BorderPane();
        middlePanel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        VBox box = new VBox();
        box.setSpacing(20);
        box.getChildren().addAll(createText("JetFuel - Data Explorer", "fancytextLarge"),
                createText("Click on the 'Explore Data' button to get started !", "fancytextSmall"));
        box.alignmentProperty().setValue(Pos.CENTER);
        middlePanel.setCenter(box);
        setContent(middlePanel);
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                statusBar.showWelcomeMessage();
            }
        });
    }

    private Text createText(String textToUse, String id) {
        Blend blend = new Blend();
        blend.setMode(BlendMode.MULTIPLY);

        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(254, 235, 66, 0.3));
        ds.setOffsetX(5);
        ds.setOffsetY(5);
        ds.setRadius(5);
        ds.setSpread(0.2);

        blend.setBottomInput(ds);

        DropShadow ds1 = new DropShadow();
        ds1.setColor(Color.web("#7b68ee"));
        ds1.setRadius(10);
        ds1.setSpread(0.2);

        Blend blend2 = new Blend();
        blend2.setMode(BlendMode.MULTIPLY);

        InnerShadow is = new InnerShadow();
        is.setColor(Color.web("#00AEDB"));
        is.setRadius(9);
        is.setChoke(0.8);
        blend2.setBottomInput(is);

        InnerShadow is1 = new InnerShadow();
        is1.setColor(Color.web("#f13a00"));
        is1.setRadius(5);
        is1.setChoke(0.4);
        blend2.setTopInput(is1);

        Blend blend1 = new Blend();
        blend1.setMode(BlendMode.MULTIPLY);
        blend1.setBottomInput(ds1);
        blend1.setTopInput(blend2);

        blend.setTopInput(blend1);
        Text text = new Text();
        text.setText(textToUse);
        text.setId(id);
        text.setEffect(blend);
        return text;
    }
}
