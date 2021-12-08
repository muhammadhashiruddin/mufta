package android.example.mufta;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QueryUtils {
    public List<SongsInfo> extractPlaylistSongs(String SAMPLE_JSON_RESPONSE ){
        Log.i(MainActivity.class.getName(), "extractSongs method got called");

        List<SongsInfo> songs = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(SAMPLE_JSON_RESPONSE);
            JSONArray items = root.getJSONArray("items");
            for(int i = 0 ; i < items.length() ; i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject track = item.getJSONObject("track");
                JSONArray artists = track.getJSONArray("artists");
                String artName="";
                for(int j = 0 ; j < artists.length() ; j++) {
                    JSONObject artist = artists.getJSONObject(j);
                    if(j==artists.length()-1){
                        artName = artName + artist.getString("name") + ". ";
                    }else
                        artName = artName + artist.getString("name") + ", ";
                }
                String name = track.getString("name");
                songs.add(new SongsInfo(name,artName,""));
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }
        return songs;
    }

    public List<SongsInfo> extractTrackSongs(String SAMPLE_JSON_RESPONSE ){
        Log.i(MainActivity.class.getName(), "extractSongs method got called");

        List<SongsInfo> songs = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(SAMPLE_JSON_RESPONSE);
            JSONArray items = root.getJSONArray("tracks");
            for(int i = 0 ; i < items.length() ; i++) {
                JSONObject item = items.getJSONObject(i);
                JSONArray artists = item.getJSONArray("artists");
                String artName="";
                for(int j = 0 ; j < artists.length() ; j++) {
                    JSONObject artist = artists.getJSONObject(j);
                    if(j==artists.length()-1){
                        artName = artName + artist.getString("name") + ". ";
                    }else
                        artName = artName + artist.getString("name") + ", ";
                }
                String name = item.getString("name");
                songs.add(new SongsInfo(name,artName,""));
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }
        return songs;
    }

    public List<SongsInfo> extractAlbumSongs(String SAMPLE_JSON_RESPONSE ){
        Log.i(MainActivity.class.getName(), "extractSongs method got called");

        List<SongsInfo> songs = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(SAMPLE_JSON_RESPONSE);
            JSONArray items = root.getJSONArray("items");
            for(int i = 0 ; i < items.length() ; i++) {
                JSONObject item = items.getJSONObject(i);
                JSONArray artists = item.getJSONArray("artists");
                String artName="";
                for(int j = 0 ; j < artists.length() ; j++) {
                    JSONObject artist = artists.getJSONObject(j);
                    if(j==artists.length()-1){
                        artName = artName + artist.getString("name") + ". ";
                    }else
                        artName = artName + artist.getString("name") + ", ";
                }
                String name = item.getString("name");
                songs.add(new SongsInfo(name,artName,""));
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }
        return songs;
    }

    public String extractYoutubeVideo(String SAMPLE_JSON_RESPONSE){
        try {
            JSONObject root = new JSONObject(SAMPLE_JSON_RESPONSE);
            JSONArray items = root.getJSONArray("items");
            JSONObject item = items.getJSONObject(0);
            JSONObject id = item.getJSONObject("id");
            return id.getString("videoId");

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }
        return null;
    }

}

