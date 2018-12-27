package nl.larsgerrits.showwatcher.components.tab;

import com.jfoenix.controls.JFXMasonryPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import nl.larsgerrits.showwatcher.components.poster.PosterShow;
import nl.larsgerrits.showwatcher.controller.MainController;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVShow;

public class TabAdd extends Tab
{
    public static final FontAwesomeIconView PLUS = new FontAwesomeIconView(FontAwesomeIcon.PLUS, "20px");
    
    private JFXMasonryPane showPane = new JFXMasonryPane();
    
    private MainController controller;
    
    public TabAdd(MainController controller)
    {
        this.controller = controller;
        
        setGraphic(PLUS);
        
        BorderPane pane = new BorderPane();
        
        AnchorPane searchPane = new AnchorPane();
    
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        showPane.setHSpacing(10);
        showPane.setVSpacing(10);
        showPane.setCellHeight(250);
        showPane.setCellWidth(150);
        showPane.setPadding(new Insets(16));
        
        scrollPane.setContent(showPane);
        
        pane.setTop(searchPane);
        pane.setCenter(scrollPane);
        
        setContent(pane);
        
        ShowManager.searchShows("", this::addShow);
    }
    
    private void addShow(TVShow show)
    {
        PosterShow poster = new PosterShow(show);
        showPane.getChildren().add(poster);
        poster.setOnMouseClicked(e -> {
            TabShow showTab = MainController.SHOW_TAB_MAP.get(show);
            
            if (showTab == null)
            {
                showTab = new TabShow(controller.tabPane, show);
                ShowManager.checkForNewUpdates(show);
                MainController.SHOW_TAB_MAP.put(show, showTab);
            }
            
            if (!controller.tabPane.getTabs().contains(showTab))
            {
                controller.tabPane.getTabs().add(showTab);
            }
    
            controller.tabPane.getSelectionModel().select(showTab);
        });
    }
}
