package nl.larsgerrits.showwatcher.api.tmdb;

import java.util.Date;

public class TMDBEpisode
{
    private String title;
    private int episodeNumber;
    private Date releaseDate;
    // public String overview;
    
    public TMDBEpisode(String title, int episodeNumber, Date releaseDate)
    {
        this.title = title;
        this.episodeNumber = episodeNumber;
        this.releaseDate = releaseDate;
    }
    
    public int getEpisodeNumber()
    {
        return episodeNumber;
    }
    
    public Date getReleaseDate()
    {
        return releaseDate;
    }
    
    public String getTitle()
    {
        return title;
    }
}
