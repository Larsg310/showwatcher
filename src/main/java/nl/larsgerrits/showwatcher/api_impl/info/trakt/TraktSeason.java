package nl.larsgerrits.showwatcher.api_impl.info.trakt;

import java.util.Date;

public class TraktSeason
{
    private int seasonNumber;
    private int episodeCount;
    private Date releaseDate;
    
    public TraktSeason(int seasonNumber, int episodeCount, Date releaseDate)
    {
        this.seasonNumber = seasonNumber;
        this.episodeCount = episodeCount;
        this.releaseDate = releaseDate;
    }
    
    public int getEpisodeCount()
    {
        return episodeCount;
    }
    
    public Date getReleaseDate()
    {
        return releaseDate;
    }
    
    public int getSeasonNumber()
    {
        return seasonNumber;
    }
}
