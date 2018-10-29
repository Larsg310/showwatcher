package nl.larsgerrits.showwatcher.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ShowCollectionDeserializer implements JsonDeserializer<List<TVShow>>
{
    @Override
    public List<TVShow> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        List<TVShow> shows = new ArrayList<>();
        json.getAsJsonArray().forEach(jsonElement -> shows.add(ShowManager.getShow(jsonElement.getAsString())));
        return shows;
    }
}
