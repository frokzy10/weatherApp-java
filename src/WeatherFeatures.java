import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherFeatures {
    public static JSONObject getWeatherData(String locationName){
        JSONArray locationData = getLocationData(locationName);
        return null;
    }
    public static JSONArray getLocationData(String locationName){
        locationName = locationName.replaceAll("","+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name= " +
                locationName + "&count=10&language=en&format=json";

        try{
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn.getResponseCode() != 200){
                System.out.println("Не читает вашу API");
                return null;
            }else{
                StringBuilder resJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while(scanner.hasNext()){
                    resJson.append(scanner.nextLine());
                }

                scanner.close();
                conn.disconnect();
                JSONParser parser = new JSONParser();
                JSONObject resjsonObj = (JSONObject) parser.parse(String.valueOf(resJson));

                JSONArray locationData = (JSONArray) resjsonObj.get("results");
                return locationData;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            URL url = new URL(urlString);
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            connect.setRequestMethod("GET");
            connect.connect();
            return connect;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
