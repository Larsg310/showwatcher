package nl.larsgerrits.showwatcher.show;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class TVShow implements Iterable<TVSeason>
{
    private final String title;
    private final String imdbId;
    private final List<TVSeason> seasons = new ArrayList<>();
    
    private Consumer<TVSeason> seasonAdded;
    
    public TVShow(String title, String imdbId)
    {
        this.title = title;
        this.imdbId = imdbId;
    }
    
    public void addSeason(TVSeason season, boolean notify)
    {
        seasons.add(season);
        if (notify) getSeasonAdded().accept(season);
    }
    
    public TVSeason getSeason(int seasonNumber)
    {
        return seasons.get(seasonNumber - 1);
    }
    
    public boolean isCompletelyWatched()
    {
        if(seasons.isEmpty()) return false;
        return seasons.stream().flatMap(s -> s.getEpisodes().stream()).allMatch(e -> e.getWatched().get() || !e.isReleased());
    }
    
    private Consumer<TVSeason> getSeasonAdded()
    {
        return seasonAdded == null ? t -> {} : seasonAdded;
    }
    
    public void setSeasonAdded(Consumer<TVSeason> seasonAdded)
    {
        this.seasonAdded = seasonAdded;
    }
    
    @Override
    @Nonnull
    public Iterator<TVSeason> iterator()
    {
        return seasons.iterator();
    }
    
    public boolean hasSeason(int number)
    {
        return seasons.stream()//
                      .anyMatch(season -> season.getSeasonNumber() == number);
    }
    
    public String getImdbId()
    {
        return imdbId;
    }
    
    public List<TVSeason> getSeasons()
    {
        return seasons;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(title, imdbId);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TVShow tvShow = (TVShow) o;
        return Objects.equal(title, tvShow.title) && Objects.equal(imdbId, tvShow.imdbId);
    }
    
    @Override
    public String toString()
    {
        return "TVShow{" + "title='" + title + '\'' + ", imdbId='" + imdbId + '\'' + ", seasons=" + seasons.size() + '}';
    }
}
