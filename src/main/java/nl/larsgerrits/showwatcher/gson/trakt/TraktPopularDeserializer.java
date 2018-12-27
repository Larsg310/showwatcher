package nl.larsgerrits.showwatcher.gson.trakt;

import com.google.gson.*;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TraktPopularDeserializer implements JsonDeserializer<List<TVShow>>
{
    @Override
    public List<TVShow> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        List<TVShow> popular = new ArrayList<>();
        
        JsonArray shows = json.getAsJsonArray();
        
        for (JsonElement showElement : shows)
        {
            JsonObject showObject = showElement.getAsJsonObject().get("show").getAsJsonObject();
            
            String imdbId = showObject.get("ids").getAsJsonObject().get("imdb").getAsString();
            String title = showObject.get("title").getAsString();
            
            popular.add(new TVShow(title, imdbId));
        }
        return popular;
    }
}
