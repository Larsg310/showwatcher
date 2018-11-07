package nl.larsgerrits.showwatcher.api_impl.trakt;

import java.util.Date;

public class TraktEpisode
{
    private String title;
    private int episodeNumber;
    private Date releaseDate;
    public String overview;
    
    public TraktEpisode(String title, int episodeNumber, Date releaseDate, String overview)
    {
        this.title = title;
        this.episodeNumber = episodeNumber;
        this.releaseDate = releaseDate;
        this.overview = overview;
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
    
    public String getOverview()
    {
        return overview;
    }
}
