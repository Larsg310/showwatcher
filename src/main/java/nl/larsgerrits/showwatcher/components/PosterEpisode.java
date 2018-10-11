package nl.larsgerrits.showwatcher.components;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import nl.larsgerrits.showwatcher.manager.ImageManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;

import java.util.function.Consumer;

public class PosterEpisode extends AnchorPane
{
    private TVEpisode episode;
    
    private ImageView imageView = new ImageView();
    
    private Text text = new Text();
    private double imageHeight = 0;
    
    private Consumer<TVEpisode> episodeChangeListener;
    
    public PosterEpisode(TVEpisode episode, Consumer<TVEpisode> episodeChangeListener)
    {
        this.episode = episode;
        this.episodeChangeListener = episodeChangeListener;
        
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
        setImage(new Image(ImageManager.getPosterURLForTVEpisode(episode, this::onDownloadImage)));
        
        if (episode.isWatched())
        {
            ColorAdjust effect = new ColorAdjust();
            effect.setSaturation(-1D);
            imageView.setEffect(effect);
        }
        
        setLeftAnchor(imageView, 10D);
        setRightAnchor(imageView, 10D);
        setTopAnchor(imageView, 10D);
        
        getChildren().addAll(imageView, text);
    }
    
    private void onClicked()
    {
        if (episodeChangeListener != null) episodeChangeListener.accept(episode);
    }
    
    private void setImage(Image image)
    {
        imageView.setImage(image);
        imageHeight = 188 * (image.getHeight() / image.getWidth());
        text.setLayoutY(10 + 16 + imageHeight);
        
        setPrefHeight(18 + imageHeight + text.getLayoutBounds().getHeight());
        
    }
    
    // public void initialize()
    // {
    // setOnMouseClicked(e -> {
    //     try
    //     {
    //         if (StringUtils.isNotEmpty(episode.getFileName()) && episode.getSeason().getPath() != null)
    //         {
    //             Desktop.getDesktop().open(episode.getSeason().getPath().resolve(episode.getFileName()).toFile());
    //         }
    //     }
    //     catch (IOException e1)
    //     {
    //         e1.printStackTrace();
    //     }
    // });
    // }
    
    public void onDownloadImage(String url)
    {
        Image image = new Image(url);
        setImage(image);
    }
    
    public TVEpisode getEpisode()
    {
        return episode;
    }
}
