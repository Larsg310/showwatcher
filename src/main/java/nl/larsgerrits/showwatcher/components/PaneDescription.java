package nl.larsgerrits.showwatcher.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nl.larsgerrits.showwatcher.manager.DescriptionManager;
import nl.larsgerrits.showwatcher.manager.ImageManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;

import java.util.Date;

public class PaneDescription extends AnchorPane
{
    private ImageView imageView = new ImageView();
    private Text title = new Text("Default");
    private Text description = new Text("Default");
    private Text releaseDate = new Text("Default");
    
    public PaneDescription(int width, int height)
    {
        setPrefWidth(width);
        setPrefHeight(height);
        
        getStyleClass().addAll("anchor-pane");
        
        // imageView.set
        imageView.setFitHeight(height - 32);
        imageView.setPreserveRatio(true);
        AnchorPane.setLeftAnchor(imageView, 16D);
        AnchorPane.setTopAnchor(imageView, 16D);
        AnchorPane.setBottomAnchor(imageView, 16D);
        
        title.setId("text");
        title.setFont(new Font(24));
        title.setWrappingWidth(width - 190);
        AnchorPane.setTopAnchor(title, 20D);
        AnchorPane.setLeftAnchor(title, 170D);
        
        description.setId("text");
        description.setFont(new Font(18));
        description.setWrappingWidth(width - 190);
        AnchorPane.setTopAnchor(description, 56D);
        AnchorPane.setLeftAnchor(description, 170D);
        
        releaseDate.setId("text");
        releaseDate.setFont(new Font(14));
        AnchorPane.setTopAnchor(releaseDate, 4D);
        AnchorPane.setRightAnchor(releaseDate, 20D);
        
        getChildren().addAll(imageView, title, description, releaseDate);
    }
    
    public void onSeasonSelected(TVSeason season)
    {
        setTitle(String.format("Season %d", season.getSeasonNumber()));
        setReleaseDate(season.getReleaseDate());
        
        ImageManager.getPosterURLForTVSeason(season, this::setImage);
        DescriptionManager.getShowDescription(season.getShow(), this::setDescription);
    }
    
    public void onEpisodeSelected(TVEpisode episode)
    {
        setTitle(String.format("Episode %dx%02d: %s", episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber(), episode.getTitle()));
        setReleaseDate(episode.getReleaseDate());
        
        DescriptionManager.getEpisodeDescription(episode, this::setDescription);
    }
    
    private void setImage(Image image)
    {
        imageView.setImage(image);
    }
    
    private void setTitle(String title)
    {
        this.title.setText(title);
    }
    
    private void setDescription(String description)
    {
        this.description.setText(description);
    }
    
    private void setReleaseDate(Date date)
    {
        releaseDate.setText(String.format("Release Date: %02d/%02d/%04d %02d:%02d", date.getDate(), date.getMonth() + 1, date.getYear() + 1900, date.getHours(), date.getMinutes()));
    }
}
