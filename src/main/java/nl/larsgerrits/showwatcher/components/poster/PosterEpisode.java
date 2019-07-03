package nl.larsgerrits.showwatcher.components.poster;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import nl.larsgerrits.showwatcher.manager.DownloadManager;
import nl.larsgerrits.showwatcher.manager.ImageManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;

import java.util.function.Consumer;

public class PosterEpisode extends AnchorPane
{
    public static final ColorAdjust GRAYSCALE_EFFECT = new ColorAdjust(0, -1, -0.75, 0);
    
    private TVEpisode episode;
    
    private ImageView imageView = new ImageView();
    
    private Text text = new Text();
    
    private Consumer<TVEpisode> episodeSelectedListener;
    
    private long lastClicked;
    
    public PosterEpisode(TVEpisode episode, Consumer<TVEpisode> episodeSelectedListener)
    {
        this.episode = episode;
        this.episodeSelectedListener = episodeSelectedListener == null ? e -> {} : episodeSelectedListener;
        episode.getWatched().addChangeListener(this::updateWatched);
        
        setPrefHeight(155);
        setPrefWidth(210);
        setId("poster");
        
        text.setId("text");
        text.setLayoutX(10);
        text.setText("Episode " + episode.getEpisodeNumber() + ": " + episode.getTitle());
        text.setStrokeType(StrokeType.OUTSIDE);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(188);
        
        imageView.setFitWidth(188);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        
        setOnMouseClicked(e -> onClicked());
        ImageManager.getPosterURLForTVEpisode(episode, this::setImage);
        
        updateWatched(false, episode.getWatched().get());
        
        AnchorPane.setLeftAnchor(imageView, 10D);
        AnchorPane.setRightAnchor(imageView, 10D);
        AnchorPane.setTopAnchor(imageView, 10D);
        
        getChildren().addAll(imageView, text);
    }
    
    private void onClicked()
    {
        if (episodeSelectedListener != null) episodeSelectedListener.accept(episode);
        
        long clicked = System.currentTimeMillis();
        
        if (clicked - lastClicked < 500)
        {
            if (episode.getVideoFilePath() != null)
            {
                DownloadManager.downloadEpisode(episode);
            }
            else episode.play();
        }
        lastClicked = clicked;
    }
    
    private void setImage(Image image)
    {
        imageView.setImage(image);
        double imageHeight = 188 * (image.getHeight() / image.getWidth());
        text.setLayoutY(26 + imageHeight);
        
        setPrefHeight(18 + imageHeight + text.getLayoutBounds().getHeight());
        
    }
    
    public TVEpisode getEpisode()
    {
        return episode;
    }
    
    public void updateWatched(boolean old, boolean watched)
    {
        imageView.setEffect(watched ? GRAYSCALE_EFFECT : null);
    }
}
