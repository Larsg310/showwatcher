package nl.larsgerrits.showwatcher.show;

import com.google.common.base.Objects;

import java.util.Date;

public class TVEpisode
{
    private String title;
    private int episodeNumber;
    private String fileName;
    private Date releaseDate;
    private boolean watched;
    private TVSeason season;
    
    public TVEpisode(String title, int episodeNumber, String fileName, Date releaseDate, TVSeason season, boolean watched)
    {
        this.title = title;
        this.episodeNumber = episodeNumber;
        this.fileName = fileName;
        this.releaseDate = releaseDate;
        this.season = season;
        this.watched = watched;
    }
    
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
        season.setDirty(true);
    }
    
    public int getEpisodeNumber()
    {
        return episodeNumber;
    }
    
    public String getFileName()
    {
        return fileName;
    }
    
    public Date getReleaseDate()
    {
        return releaseDate;
    }
    
    public TVSeason getSeason()
    {
        return season;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public boolean isWatched()
    {
        return watched;
    }
    
    public void setWatched(boolean watched)
    {
        this.watched = watched;
        season.setDirty(true);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(episodeNumber, season);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TVEpisode episode = (TVEpisode) o;
        return episodeNumber == episode.episodeNumber && Objects.equal(season, episode.season);
    }
}
