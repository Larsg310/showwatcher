package nl.larsgerrits.showwatcher.gson;

import com.google.gson.*;

import java.lang.reflect.Type;

public class TMDBSeasonPosterDeserializer implements JsonDeserializer<String>
{
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject obj = json.getAsJsonObject();
        
        JsonElement imgObj = obj.get("images");
        if (!imgObj.isJsonNull())
        {
            JsonObject images = imgObj.getAsJsonObject();
            JsonArray posters = images.get("posters").getAsJsonArray();
            
            if (posters.size() > 0)
            {
                JsonObject poster = posters.get(0).getAsJsonObject();
                return poster.get("file_path").getAsString();
            }
        }
        return "";
    }
}
