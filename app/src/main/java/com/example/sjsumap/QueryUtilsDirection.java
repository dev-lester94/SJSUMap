package com.example.sjsumap;

import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.text.HtmlCompat;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtilsDirection {

    public static final String LOG_TAG = QueryUtilsDirection.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtilsDirection} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsDirection() {
    }

    public static Direction fetchDirectionData(String requestUrl) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        Direction directions = extractDirections(jsonResponse);

        // Return the {@link Event}
        return directions;
    }

    /**
     * Returns new URL object from the given string URL.
     */

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(40000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the Building JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    /**
     * Return a list of {@link Building} objects that has been built up from
     * parsing a JSON response.
     */
    public static Direction extractDirections(String directionJSON) {

        if (TextUtils.isEmpty(directionJSON)) {
            return null;
        }

        Direction directions = null;

        //Log.i("in here", "in hrere");


        // Try to parse the DIRECTION JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

             JSONObject jsonRootObject = new JSONObject(directionJSON);

             JSONArray routes = jsonRootObject.getJSONArray("routes");
             Log.i("routes", String.valueOf(routes.length()));
             if(routes.length()>0){
                 JSONObject boundsJson = routes.getJSONObject(0).getJSONObject("bounds");
                 JSONObject northeastJson = boundsJson.getJSONObject("northeast");
                 LatLng northEast = new LatLng(
                         northeastJson.getDouble("lat"),northeastJson.getDouble("lng"));

                 Log.i("northEast", northEast.toString());
                 JSONObject southwestJson = boundsJson.getJSONObject("southwest");
                 LatLng southWest =  new LatLng(
                         southwestJson.getDouble("lat"),southwestJson.getDouble("lng"));

                 Log.i("southWest", southWest.toString());

                 LatLngBounds bounds = new LatLngBounds(southWest, northEast);


                 JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                 //Log.i("legs", String.valueOf(legs));
                 JSONObject legsObject = legs.getJSONObject(0);
                 String endAddress = legsObject.getString("end_address");
                 Log.i("endAddress", endAddress);
                 LatLng endLocation = new LatLng(
                         legsObject.getJSONObject("end_location").getDouble("lat"),
                         legsObject.getJSONObject("end_location").getDouble("lng"));
                 Log.i("endLocation", String.valueOf(endLocation));

                 String startAddress = legsObject.getString("start_address");
                 Log.i("startAddress", startAddress);
                 LatLng startLocation = new LatLng(
                         legsObject.getJSONObject("start_location").getDouble("lat"),
                         legsObject.getJSONObject("start_location").getDouble("lng"));
                 Log.i("startLocation", String.valueOf(startLocation));


                 JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

                 ArrayList<LatLng> polyLineList = new ArrayList<LatLng>();
                 ArrayList<String> instructions = new ArrayList<String>();
                 for(int i =0 ; i<steps.length(); i++){
                     String polyLinePoints = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                     List<LatLng> decodePoints = PolyUtil.decode(polyLinePoints);
                     polyLineList.addAll(decodePoints);

                     String instruction= String.valueOf(HtmlCompat.fromHtml(
                             steps.getJSONObject(i).getString("html_instructions"),
                             HtmlCompat.FROM_HTML_MODE_COMPACT));

                     //Log.i("instruction", instruction);

                     instructions.add(instruction);

                 }

                 directions = new Direction(bounds,startLocation,endLocation,startAddress,endAddress, polyLineList,instructions );

             }



        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the Direction JSON results", e);
        }



        // Return the list of direction
        return directions;
    }

}