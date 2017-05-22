package main.java.com.inf2a.api.weather;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
Convert URL to JSONObject.

@param apiURL

@return jsonObject
*/
public class JsonParser {
    public static JSONObject parseURL(String apiURL)
    {
        JSONObject jsonObject = null;
        try
        {
            URL url = new URL(apiURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            JSONParser parser = new JSONParser();
            try
            {
                Object obj = parser.parse((new InputStreamReader((InputStream)request.getContent(), "UTF-8")));
                jsonObject = (JSONObject) obj;
            }
            catch (org.json.simple.parser.ParseException pe)
            {
                pe.printStackTrace();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return jsonObject;
    }
}
