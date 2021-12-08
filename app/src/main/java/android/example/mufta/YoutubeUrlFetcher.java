package android.example.mufta;

import android.example.mufta.fragments.SearchFragment;
import android.example.mufta.observerpattern.Broadcaster;
import android.example.mufta.observerpattern.Subject;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static android.example.mufta.ImmutableConstants.YOUTUBE;
import static android.example.mufta.ImmutableConstants.YOUTUBE_KEY;
import static android.example.mufta.ImmutableConstants.actionYoutube;
import static android.example.mufta.ImmutableConstants.storagePath;
import static android.example.mufta.internetconnection.ConnctionInternet.makeHTTPRequest;

public class YoutubeUrlFetcher extends AsyncTask<SongsInfo, Integer, String> {

    private final boolean mp3Download;
    private final int start;
    private final Subject broadcaster;

    public YoutubeUrlFetcher(int start, Subject broadcaster, boolean mp3Download) {
        this.start = start;
        this.broadcaster = broadcaster;
        this.mp3Download = mp3Download;
    }

    @Override
    protected String doInBackground(SongsInfo... songsInfos) {
        if(actionYoutube == YOUTUBE) {
            download(songsInfos[0].youtubeUrl);
            return "all done";
        }
        fetchAndDownload(songsInfos[0]);
        return "all done";
    }

    private void fetchAndDownload(SongsInfo songsInfo) {
        String videoKey = songUrl(songsInfo.getSongName() +" by "  //Fetching Youtube video key
                + songsInfo.getArtistName());

        if (videoKey == null)
            return;

        songsInfo.setYoutubeUrl(videoKey);
        download(videoKey);                     //starting download

    }

    void download(String key) {
        File mainPath = new File(storagePath);
        YoutubeDLRequest request = new YoutubeDLRequest("https://www.youtube.com/watch?v=" + key.trim());
        request.addOption("-o", mainPath + "/%(title)s.%(ext)s");

        if (mp3Download) {
            request.addOption("-ciw");
            request.addOption("--extract-audio");
            request.addOption("--audio-format", "mp3");
        }else{
            request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
        }
        final int[] i = {0};
        try {
            YoutubeDL.getInstance().execute(request, (progress, etaInSeconds) -> {
                ((Broadcaster)broadcaster).setProgressOfOneSong(progress);
                if (progress == 100 && i[0] == 0) {
                    SearchFragment.startDownload(start + 1,broadcaster,mp3Download);
                    i[0] = i[0] + 1;
                }
            });
        } catch (InterruptedException e) {
            Log.e(MainActivity.class.getName(), "Failed to download: An interrupt occurs", e);
        } catch (YoutubeDLException e) {
            Log.e(MainActivity.class.getName(), "Failed to download: Youtube-dl error", e);
        }
    }

    private String songUrl(String names) {
        Uri baseUri = Uri.parse("https://youtube.googleapis.com/youtube/v3/search?part=snippet&q=" +
                names + "&fields=items(id(videoId))&key="+YOUTUBE_KEY);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        URL url;
        try {
            url = new URL(uriBuilder.toString());
            String jsonResponse;
            QueryUtils qUtils = new QueryUtils();
            jsonResponse = makeHTTPRequest(url, false);

            if (jsonResponse == null)
                return null;
            return qUtils.extractYoutubeVideo(jsonResponse);
        } catch (MalformedURLException e) {
            Log.i(MainActivity.class.getName(), "Request url cannot proceed in youtube", e);
        }
        return null;
    }
}