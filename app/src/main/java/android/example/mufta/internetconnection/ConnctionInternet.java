package android.example.mufta.internetconnection;

import android.example.mufta.MainActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.example.mufta.ImmutableConstants.accessToken;

public class ConnctionInternet {

    public static String makeHTTPRequest(URL url, boolean fetchSong){
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            if(fetchSong) {
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
            }

            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.connect();

            if(urlConnection.getResponseCode()==200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromInputStream(inputStream);
            }

            else {
                Log.e(MainActivity.class.getName(), "Response code is = " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.i(MainActivity.class.getName(), "HTTP connection failed", e);
        }finally {
            if(urlConnection != null)
                urlConnection.disconnect();
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.i(MainActivity.class.getName(), "Input Output Error", e);
                }
            }
        }
        return jsonResponse;
    }

    public static String readFromInputStream(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try {
            String line = bufferedReader.readLine();
            while (line != null){
                output.append(line);
                line = bufferedReader.readLine();
            }
            inputStream.close();
            bufferedReader.close();
            inputStreamReader.close();
        } catch (IOException e) {
            Log.i(MainActivity.class.getName(), "Error in parsing data from server", e);
        }
        return output.toString();
    }
}
