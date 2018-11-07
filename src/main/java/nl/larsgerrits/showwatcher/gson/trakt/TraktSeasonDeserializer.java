package nl.larsgerrits.showwatcher.gson.trakt;

import com.google.gson.*;
import nl.larsgerrits.showwatcher.api_impl.trakt.TraktSeason;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TraktSeasonDeserializer implements JsonDeserializer<List<TraktSeason>>
{
    @SuppressWarnings("Duplicates")
    @Override
    public List<TraktSeason> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        List<TraktSeason> seasons = new ArrayList<>();
        
        JsonArray seasonArray = json.getAsJsonArray();
        // System.out.println(seasonArray);
        
        for (JsonElement e : seasonArray)
        {
            JsonObject seasonObj = e.getAsJsonObject();
            
            int seasonNumber = seasonObj.get("number").getAsInt();
            if (seasonNumber > 0)
            {
                int episodeCount = seasonObj.get("episode_count").getAsInt();
                
                JsonElement dateElement = seasonObj.get("first_aired");
                Date date = dateElement.isJsonNull() ? new Date(0) : Date.from(Instant.parse(dateElement.getAsString()));
                
                seasons.add(new TraktSeason(seasonNumber, episodeCount, date));
            }
        }
        
        return seasons;
    }
}
