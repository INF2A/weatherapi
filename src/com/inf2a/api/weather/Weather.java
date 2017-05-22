package main.java.com.inf2a.api.weather;

import main.java.com.inf2a.api.weather.JsonParser;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

@Path("/")
public class Weather {
    //Temporary JSONArray
    private JSONArray tempJsonArray = null;

    //Temporary JSONObject
    private JSONObject tempJsonObj = null;

    //Store complete set of weather data.
    private JSONObject weatherCollection;

    /*
    Returns complete JSON string containing current and forecast weather data.
    unit -> Celsius = metric, Fahrenheit = imperial

    @param cityName
    @param units

    @return Response
    */
    @Path("/weather/{city-name}/{units}")
    @GET
    public Response weather(@PathParam("city-name") String cityName, @PathParam("units") String units) throws JSONException
    {
        //Check if cityname and units exist in URL.
        if(cityName == null || cityName.trim().length() == 0)
        {
            return Response.serverError().entity("City name cannot be blank").build();
        }
        else if(units == null || units.trim().length() == 0)
        {
            return Response.serverError().entity("Units cannot be blank, please choose metric (celsius) or imperial (fahrenheit)").build();
        }

        //Combine current and forecast weather data.
        JSONObject jsonComplete = new JSONObject();

        jsonComplete.put("current", getCurrentWeather(cityName, units));
        jsonComplete.put("forecast", getWeatherForecast(cityName,units));

        return Response.ok(jsonComplete.toJSONString()).build();
    }

    /*
    Get current weather data.
    Uses JSON getters to get specific data for current weather (city_name, country, sunrise, sunset) which is not available in the forecast API.
    General weather data is retrieved from the getWeatherData method.

    @param cityName
    @param units

    @return weatherCollection
    */
    private JSONObject getCurrentWeather(String cityName, String units)
    {
        weatherCollection = new JSONObject();

        //Root
        JSONObject jsonObj = JsonParser.parseURL("http://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=" + units + "&appid=a6eca7225bf1abe691e7357baeaf2c98");

        //------------------- Specific data for current weather only -------------------//
        //City name
        weatherCollection.put("city_name", jsonObj.get("name").toString());

        //General
        tempJsonObj = (JSONObject)jsonObj.get("sys");
        weatherCollection.put("country", tempJsonObj.get("country").toString());

        //Sunrise & Sunset
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date epoch = new Date(Long.parseLong(tempJsonObj.get("sunrise").toString()) * 1000);
        weatherCollection.put("sunrise", dateFormat.format(epoch));

        epoch = new Date(Long.parseLong(tempJsonObj.get("sunset").toString()) * 1000);
        weatherCollection.put("sunset", dateFormat.format(epoch));

        //------------------------------------------------------------------------------//

        getWeatherData(jsonObj);

        return weatherCollection;
    }

    /*
    Get weather forecast (5 days)
    Uses forecastCollection to store forecast for each day (weatherCollection = weather data for 1 day).

    @param cityName
    @param units

    @return forecastCollection
    */
    private JSONObject getWeatherForecast(String cityName, String units)
    {
        JSONObject forecastCollection = new JSONObject();

        //Root
        tempJsonObj = JsonParser.parseURL("http://api.openweathermap.org/data/2.5/forecast?q=" + cityName + "&units=" + units + "&appid=a6eca7225bf1abe691e7357baeaf2c98");

        JSONArray jsonArray = (JSONArray)tempJsonObj.get("list");

        int count = 0;

        Iterator<JSONObject> it = jsonArray.iterator();
        while(it.hasNext())
        {
            tempJsonObj = it.next();
            if(tempJsonObj.get("dt_txt").toString().contains("15:00:00"))
            {
                weatherCollection = new JSONObject();
                count++;
                getWeatherData(tempJsonObj);
                forecastCollection.put("day_" + count, weatherCollection);
            }
        }
        return forecastCollection;
    }

    /*
    Get general weather information, used for getting partly current weather data and complete forecast weather data.
    Uses tempJsonArray and tempJsonObj to store temporary JSON data for navigation purposes.
    Stores weather data in weatherCollections.

    @param root
    */
    private void getWeatherData(JSONObject root)
    {
        //Main
        tempJsonArray = (JSONArray)root.get("weather");
        tempJsonObj = (JSONObject)tempJsonArray.get(0);
        weatherCollection.put("main", tempJsonObj.get("main").toString());

        //Weather description
        weatherCollection.put("description", tempJsonObj.get("description").toString());

        //Weather icon http://openweathermap.org/img/w/$ICON.png
        weatherCollection.put("icon", "http://openweathermap.org/img/w/"+tempJsonObj.get("icon").toString()+".png");

        //Temperatures
        tempJsonObj = (JSONObject)root.get("main");
        weatherCollection.put("temp", tempJsonObj.get("temp").toString());
        weatherCollection.put("temp_min", tempJsonObj.get("temp_min").toString());
        weatherCollection.put("temp_max", tempJsonObj.get("temp_max").toString());

        //Humidity
        weatherCollection.put("humidity", tempJsonObj.get("humidity").toString());

        //Wind
        tempJsonObj = (JSONObject)root.get("wind");
        weatherCollection.put("wind_deg", tempJsonObj.get("deg").toString());
        weatherCollection.put("wind_speed", tempJsonObj.get("speed").toString());
    }
}