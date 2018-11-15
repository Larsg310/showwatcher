package nl.larsgerrits.showwatcher.gson.tmdb;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class TMDBFindDeserializer implements JsonDeserializer<Integer>
{
    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        try{
            return json.getAsJsonObject().get("tv_results").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
        }catch (IndexOutOfBoundsException e){
            return -1;
        }
    }
}
