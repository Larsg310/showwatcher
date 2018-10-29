package nl.larsgerrits.showwatcher.components;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import nl.larsgerrits.showwatcher.manager.DownloadManager;

public class TabDownload extends Tab
{
    private final FontAwesomeIconView DOWNLOAD = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD, "20px");
    
    private ScrollPane scrollPane = new ScrollPane();
    private VBox downloads = new VBox();
    
    public TabDownload()
    {
        DownloadManager.setDownloadAddedCallback(downloads.getChildren()::add);
        DownloadManager.setDownloadFinishedCallback(downloads.getChildren()::remove);
        setGraphic(DOWNLOAD);
        
        AnchorPane pane = new AnchorPane();
        pane.setId("bordered");
        pane.getStyleClass().addAll("anchor-pane");
        
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        scrollPane.setPrefHeight(1000);
        scrollPane.setLayoutX(10);
        scrollPane.setLayoutY(40);
        scrollPane.setFitToWidth(true);
        
        // downloads.setPrefWidth(800);
        // downloads.setPrefHeight(1000);
        // AnchorPane.setTopAnchor(downloads, 10D);
        // AnchorPane.setLeftAnchor(downloads, 10D);
        
        // downloads.getChildren().add(new Download(ShowManager.getTVShows().get(0).getSeasons().get(2).getEpisodes().get(7), .70));
        // downloads.getChildren().add(new Download(ShowManager.getTVShows().get(0).getSeasons().get(5).getEpisodes().get(19), .70));
        
        downloads.setSpacing(10);
        
        scrollPane.setContent(downloads);
        pane.getChildren().addAll(scrollPane);
        setContent(pane);
    }
}
