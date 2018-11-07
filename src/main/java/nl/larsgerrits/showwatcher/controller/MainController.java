package nl.larsgerrits.showwatcher.controller;

import com.google.common.collect.Maps;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXTabPane;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import nl.larsgerrits.showwatcher.components.poster.PosterShow;
import nl.larsgerrits.showwatcher.components.tab.TabShow;
import nl.larsgerrits.showwatcher.manager.DescriptionManager;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.util.Map;

public class MainController
{
    private Map<TVShow, TabShow> showTabMap = Maps.newHashMap();
    
    @FXML
    private JFXTabPane tabPane;
    
    @FXML
    private JFXMasonryPane masonryPane;
    
    public void initialize()
    {
        // tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabPane.getSelectionModel().select(2);
        
        ShowManager.getTVShows().forEach(s -> {
            PosterShow poster = new PosterShow(s);
            Tooltip tooltip = new Tooltip();
            DescriptionManager.getShowDescription(s, tooltip::setText);
            tooltip.setFont(new Font(16D));
            tooltip.setWrapText(true);
            tooltip.setPrefWidth(600);
            bindTooltip(poster, tooltip);
            masonryPane.getChildren().add(poster);
            poster.setOnMouseClicked(e -> {
                TabShow showTab = showTabMap.get(s);
                
                if (showTab == null){
                    showTab = new TabShow(tabPane, s);
                    showTab.setId("show-tab");
                    showTabMap.put(s, showTab);
                }
                
                if(!tabPane.getTabs().contains(showTab)){
                    tabPane.getTabs().add(showTab);
                }
                
                tabPane.getSelectionModel().select(showTab);
            });
        });
        ShowManager.getTVShows().forEach(ShowManager::checkForNewUpdates);
    }
    
    public static void bindTooltip(final Node node, final Tooltip tooltip)
    {
        node.setOnMouseEntered(event -> tooltip.show(node, event.getScreenX() + 10, event.getScreenY() + 25));
        node.setOnMouseMoved(event -> {
            tooltip.setAnchorX(event.getScreenX() + 10);
            tooltip.setAnchorY(event.getScreenY() + 25);
        });
        node.setOnMouseExited(event -> tooltip.hide());
    }
    
    // private void onResizeWidth(ObservableValue<? extends Number> obs, Number oldVal, Number newVal)
    // {
    //     flow.setPrefWrapLength(newVal.intValue());
    // }
    
    // @Override
    // public void setStage(Stage stage)
    // {
    //     super.setStage(stage);
    //     // stage.widthProperty().addListener(this::onResizeWidth);
    //
    //     masonryPane.getChildren().stream().map(n -> (PosterShow)n).forEach(n -> n.setStage(stage));
    // }
}
