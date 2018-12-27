package nl.larsgerrits.showwatcher.gson.show;

import com.google.gson.*;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVEpisodeCollection;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShowCollectionDeserializer implements JsonDeserializer<TVEpisodeCollection>
{
    @Override
    public TVEpisodeCollection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonObj = json.getAsJsonObject();
        
        String title = jsonObj.get("title").getAsString();
        
        List<TVEpisode> episodes = new ArrayList<>();
        
        if (jsonObj.has("shows"))
        {
            JsonArray array = jsonObj.get("shows").getAsJsonArray();
            for (JsonElement element : array)
            {
                TVShow show = ShowManager.getShow(element.getAsString());
                if (show != null)
                {
                    for (TVSeason season : show)
                    {
                        episodes.addAll(season.getEpisodes());
                    }
                }
            }
        }
        if (jsonObj.has("episodes"))
        {
            JsonArray array = jsonObj.get("episodes").getAsJsonArray();
            for (JsonElement element : array)
            {
                String id = element.getAsString();
                String[] parts = id.split("_");
                
                String imdbId = parts[0];
                int season = Integer.parseInt(parts[1]);
                int episode = Integer.parseInt(parts[2]);
                
                TVShow show = ShowManager.getShow(imdbId);
                if (show != null)
                {
                    episodes.add(show.getSeason(season).getEpisode(episode));
                }
            }
        }
        
        episodes.removeIf(episode -> episode.getReleaseDate().getTime() == 0);
        episodes.sort(Comparator.comparing(TVEpisode::getReleaseDate));
        
        return new TVEpisodeCollection(title, episodes);
    }
}
