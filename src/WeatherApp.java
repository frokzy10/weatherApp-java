import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;

public class WeatherApp {
    public static JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=GMT";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn.getResponseCode() != 200) {
                System.out.println("Не найдены данные в API Weather");
                return null;
            }
            StringBuilder resultJson = new StringBuilder();
            Scanner scan = new Scanner(conn.getInputStream());
            while (scan.hasNext()) {
                resultJson.append(scan.nextLine());
            }
            scan.close();
            conn.disconnect();
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObject = (JSONObject) parser.parse(String.valueOf(resultJson));

            JSONObject hourly = (JSONObject) resultJsonObject.get("hourly");
            JSONArray time = (JSONArray) hourly.get("time");

            int index = findIndexOfCurrentTime(time);

            JSONArray tempDate = (JSONArray) hourly.get("temperature_2m");
            double temp = (double) tempDate.get(index);

            JSONArray weatherCode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

            JSONArray realtiveHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) realtiveHumidity.get(index);

            JSONArray windSpeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windSpeed = (double) windSpeedData.get(index);

            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature",temp);
            weatherData.put("weather_condition",weatherCondition);
            weatherData.put("humidity",humidity);
            weatherData.put("windspeed",windSpeed);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn == null || conn.getResponseCode() != 200) {
                System.out.println("Не найдены данные в API Location Data");
                return null;
            } else {
                StringBuilder resJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                while (scanner.hasNext()) {
                    resJson.append(scanner.nextLine());
                }

                scanner.close();
                conn.disconnect();
                JSONParser parser = new JSONParser();
                JSONObject resJsonObj = (JSONObject) parser.parse(String.valueOf(resJson));

                JSONArray locationData = (JSONArray) resJsonObj.get("results");
                return locationData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equals(currentTime)) {
                return i;
            }
        }

        return 0;
    }

    public static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        String formattedDate = currentDateTime.format(formatter);
        return formattedDate;
    }

    private static String convertWeatherCode(long WeatherCode) {
        String weatherCondition = "";
        if (WeatherCode == 0L) {
            weatherCondition = "Clear";
        } else if (WeatherCode <= 3L && WeatherCode > 0L) {
            weatherCondition = "Cloudy";
        } else if ((WeatherCode >= 51L && WeatherCode > 67L) ||
                (WeatherCode >= 80L && WeatherCode <= 99L)) {
            weatherCondition = "Rain";
        } else if (WeatherCode >= 71L && WeatherCode <= 77L){
            weatherCondition = "Snow";
        }
        return weatherCondition;
    }
}
