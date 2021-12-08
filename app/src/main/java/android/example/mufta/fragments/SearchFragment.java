package android.example.mufta.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.example.mufta.CustomAdapter;
import android.example.mufta.MainActivity;
import android.example.mufta.QueryUtils;
import android.content.Context;
import android.example.mufta.R;
import android.example.mufta.SongsInfo;
import android.example.mufta.YoutubeUrlFetcher;
import android.example.mufta.observerpattern.Broadcaster;
import android.example.mufta.observerpattern.Observers;
import android.example.mufta.observerpattern.Subject;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.example.mufta.ImmutableConstants.SPOTIFY_ALBUM;
import static android.example.mufta.ImmutableConstants.SPOTIFY_PLAYLIST;
import static android.example.mufta.ImmutableConstants.SPOTIFY_TRACK;
import static android.example.mufta.ImmutableConstants.YOUTUBE;
import static android.example.mufta.ImmutableConstants.accessToken;
import static android.example.mufta.ImmutableConstants.actionYoutube;
import static android.example.mufta.ImmutableConstants.spotifyAuthenticationRequest;
import static android.example.mufta.ImmutableConstants.storagePath;
import static android.example.mufta.ImmutableConstants.storagePermissions;
import static android.example.mufta.internetconnection.ConnctionInternet.makeHTTPRequest;

public class SearchFragment extends Fragment implements Observers {

    public static Broadcaster broadcaster1;
    private CustomAdapter customAdapter;
    public static List<SongsInfo> songsList;
    private SongsInfo deletedSong;
    private boolean downloading = false;
    private boolean mp3Download = true;

    private ProgressBar pb;
    private TextView progressText;
    private TextInputEditText urlTV;
    private TextInputEditText fromTV;
    private TextInputEditText toTV;
    private RadioButton mp3Button;
    private RadioButton mp4Button;
    private RecyclerView songListView;
    private LottieAnimationView loading;
    private FloatingActionButton floatingActionButton;
    private LinearLayout progress_layout;
    private Button searchButton;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        initTV(root);
        addListeners();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notification", "Noty", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = this.getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        return root;
    }

    ItemTouchHelper.SimpleCallback simpleCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getBindingAdapterPosition();
                    if (direction == ItemTouchHelper.RIGHT) {
                        deletedSong = songsList.get(position);
                        songsList.remove(position);
                        customAdapter.notifyItemRemoved(position);
                        Snackbar.make(songListView, deletedSong.getSongName() + " is deleted ", Snackbar.LENGTH_LONG)
                                .setAction("undo",
                                        v -> {
                                            songsList.add(position, deletedSong);
                                            customAdapter.notifyItemInserted(position);
                                        }).show();
                    }
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addSwipeRightBackgroundColor(ContextCompat.getColor(SearchFragment.this.requireContext(), R.color.red))
                            .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
                            .create()
                            .decorate();
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };


    void initTV(ViewGroup root) {
        songListView = root.findViewById(R.id.list);
        loading = root.findViewById(R.id.load);
        floatingActionButton = root.findViewById(R.id.floating_action_button);
        urlTV = root.findViewById(R.id.url_edit_text);
        pb = root.findViewById(R.id.pb);
        progressText = root.findViewById(R.id.pr);
        progress_layout = root.findViewById(R.id.progress_layout);
        fromTV = root.findViewById(R.id.from_et);
        toTV = root.findViewById(R.id.to_et);
        mp3Button = root.findViewById(R.id.mp3_radio);
        mp4Button = root.findViewById(R.id.mp4_radio);
        searchButton = root.findViewById(R.id.button_search);

        songsList = new ArrayList<>();
        customAdapter = new CustomAdapter();

        songListView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        songListView.setAdapter(customAdapter);
    }

    private void addListeners() {
        urlTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                actionYoutube = -1;
            }
        });

        urlTV.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH && !downloading) {
                urlTV.clearFocus();
                hideKeyboard(this.requireActivity());
                musicFetcher();
                return true;
            }
            return false;
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(songListView);

        floatingActionButton.setOnClickListener(v -> {
            if (!downloading && songsList.size()!=0) {
                download();
            }else if(downloading && songsList.size() != 0){
                progress_layout.setVisibility(progress_layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }else if(!downloading && songsList.size()==0){
                musicFetcher();
            }
        });

        mp3Button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mp3Download = isChecked;
            if(isChecked)
                mp4Button.setChecked(false);
        });
        mp4Button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                mp3Button.setChecked(false);
        });

        searchButton.setOnClickListener(v -> {
            if(!downloading)
                musicFetcher();
        });
    }

    private void hideKeyboard(FragmentActivity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void download() {
        downloading = true;

        builder = new NotificationCompat.Builder(this.getActivity(),"Notification");
        builder.setContentTitle("Downloading");
        builder.setContentText("Started");
        builder.setSmallIcon(R.drawable.app_icon);
        builder.setAutoCancel(true);

        notificationManager=NotificationManagerCompat.from(this.getActivity());
        notificationManager.notify(1,builder.build());

        progress_layout.setVisibility(View.VISIBLE);
        int permission = ActivityCompat.checkSelfPermission(SearchFragment.this.requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (initDownloader() && permission == PackageManager.PERMISSION_GRANTED)
            searchThenDownload();
        else {
            Toast.makeText(SearchFragment.this.requireContext(),
                    "Please grant permission to write file in your directory",
                    Toast.LENGTH_LONG).show();
            if(storagePermissions(this.getActivity())==PackageManager.PERMISSION_GRANTED)
                musicFetcher();
            builder.setContentText("Failed!");
            progress_layout.setVisibility(View.INVISIBLE);
            notificationManager.notify(1, builder.build());
            downloading=false;
        }
    }

    private void searchThenDownload() {
        File mainPath = new File(storagePath);
        if (!mainPath.exists()) {
            boolean mkdirWork = mainPath.mkdir();
            Log.i(SearchFragment.this.getTag(), "Directory make = " +
                    mkdirWork + "\npath = " + mainPath.getPath());
        }
        Broadcaster broadcaster = new Broadcaster();
        broadcaster.addListner(this);
        broadcaster.setDownloading(true);
        startDownload(0, broadcaster, mp3Download);
    }

    public static void startDownload(int start, Subject broadcaster, boolean mp3Download) {
        if (start < songsList.size()) {
            new YoutubeUrlFetcher(start, broadcaster, mp3Download).execute(songsList.get(start));
        } else
            ((Broadcaster) broadcaster).setDownloading(false);
        ((Broadcaster) broadcaster).setTotalSongProgress(start);
        ((Broadcaster) broadcaster).setProgressOfOneSong(0);

    }

    public void musicFetcher(){
        String[] info = extractSongKeyAndDomain(Objects.requireNonNull(urlTV.getText()).toString().trim());
        String toS = Objects.requireNonNull(toTV.getText()).toString().trim();
        String fromS = Objects.requireNonNull(fromTV.getText()).toString().trim();
        int to = 100;
        int from = 0;
        if((!toS.equals(""))){
            to = Integer.parseInt(toS);
        }
        if((!fromS.equals(""))){
            from = Integer.parseInt(fromS);
        }
        int action;
        Uri baseUri;

        if(info == null){
            Toast.makeText(this.getContext(),
                    "Invalid Url of song",
                    Toast.LENGTH_LONG).show();
            return;
        } else if(info.length<3){
            Toast.makeText(this.getContext(),
                    "Invalid Url of song",
                    Toast.LENGTH_LONG).show();
            return;
        } else if (info[0].equals("open.spotify.com") && info[1].equals("playlist")) {
            baseUri = Uri.parse("https://api.spotify.com/v1/playlists/" + info[2] +
                    "/tracks?market=US&fields=items(track(name%2Cartists(name)))&limit="+to+"&offset="+from);
            action  = SPOTIFY_PLAYLIST;
        }else if (info[0].equals("open.spotify.com") && info[1].equals("track")){
            baseUri = Uri.parse("https://api.spotify.com/v1/tracks/"+info[2]+"");
            action = SPOTIFY_TRACK;
        } else if(info[0].equals("open.spotify.com") && info[1].equals("album")){
            baseUri = Uri.parse("https://api.spotify.com/v1/albums/"+info[2]+
                    "/tracks?market=US&&limit="+to+"&offset="+from);
            action = SPOTIFY_ALBUM;
        }else if(info[0].equals("youtube")){
            actionYoutube  = YOUTUBE;
            songsList.clear();
            songsList.add(new SongsInfo("Youtube",
                    "Video", info[1]));
            customAdapter.notifyDataSetChanged();
            new AlertDialog.Builder(this.getContext())
                    .setTitle("Download Confirm")
                    .setMessage("Do you want to download this video")

                    .setPositiveButton(android.R.string.yes, (dialog, which) ->{
                        if(!downloading)
                            download();
                    })

                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.stat_sys_download)
                    .show();
            return;
        }else{
            Toast.makeText(this.getContext(),
                    "Invalid Url of song",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (accessToken == null && info[0].equals("open.spotify.com")){
            broadcaster1 = new Broadcaster();
            broadcaster1.addListner(this);
            broadcaster1.setRedirect(false);

            spotifyAuthenticationRequest(this.getActivity());
            return;
        }
            Uri.Builder uriBuilder = baseUri.buildUpon();
            new Fetcher(this.getContext(),action).execute(uriBuilder.toString());
    }

    private String[] extractSongKeyAndDomain(String url) {
        String[] info = new String[3];
        String[] entries = url.split("/");

        if(entries.length>=3) {
            if (url.contains("spotify")) {
                info[0] = entries[entries.length - 3];
                info[1] = entries[entries.length - 2];
                info[2] = entries[entries.length - 1];

                if (info[2].indexOf('?') == -1) return info;
                else {
                    String[] finalEntries = info[2].split("[?]");
                    info[2] = finalEntries[0];
                }
            } else if (url.contains("youtu.be")) {
                info[0] = "youtube";
                String[] finalEntries = entries[entries.length - 1].split("[?]");
                info[1] = finalEntries[0];
            } else if (url.contains("youtube")) {
                info[0] = "youtube";
                String[] finalEntries = entries[entries.length - 1].split("&");
                info[1] = finalEntries[0];
                String[] finalEntries1 = info[2].split("=");
                info[1] = finalEntries1[1];
            }
            else
                return null;
            return info;
        }
        else
            return null;
    }

    private boolean initDownloader() {
        try {
            YoutubeDL.getInstance().init(this.requireActivity().getApplication());
            FFmpeg.getInstance().init(this.requireActivity().getApplication());
            return true;

        } catch (YoutubeDLException e) {
            Log.e(MainActivity.class.getName(),
                    "failed to initialize youtubedl-android or FFmpeg", e);
            return false;
        }
    }

    @Override
    public void update(float progressOfaSong, int totalSongProgress, boolean downloading) {
        this.downloading = downloading;
        if (!downloading) {
            progress_layout.setVisibility(View.INVISIBLE);
            return;
        }
        float progresss = (float) (((float) totalSongProgress / (float) songsList.size()) * (100.0));
        float progressOfOneSong = (float) ((1 / (float) songsList.size()) * 100.0);
        float progressOfDownloadingSong = (float) ((progressOfOneSong * (float) progressOfaSong) / 100.0);
        this.getActivity().runOnUiThread(() -> {
            progressText.setText(totalSongProgress + " of " + songsList.size() + " songs downloaded");
            pb.setProgress((int) (progresss + progressOfDownloadingSong), true);
            });

        builder.setContentText(totalSongProgress + " of " + songsList.size() + " songs downloaded")
                .setProgress(100, (int) (progresss + progressOfDownloadingSong), false);
        notificationManager.notify(1, builder.build());

        if((progresss + progressOfDownloadingSong)== 100) {
            builder.setContentText("Download complete")
                    .setProgress(0, 0, false);
            notificationManager.notify(1, builder.build());
        }
    }

    @Override
    public void update(boolean redirectToDownload) {
        if(redirectToDownload)
            musicFetcher();
    }

    @SuppressLint("StaticFieldLeak")
    class Fetcher extends AsyncTask<String, Integer, String> {
        Context context;
        int action;
        public Fetcher(Context context, int action) {
            this.context = context;
            this.action = action;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setAnimation(R.raw.loading);
            songsList.clear();
            customAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                QueryUtils qUtils = new QueryUtils();
                String jsonResponse;
                jsonResponse = makeHTTPRequest(url, true);

                if (jsonResponse.equals("")) {
                    return null;
                }
                songsList = new ArrayList<>();
                if(action == SPOTIFY_PLAYLIST) {
                    songsList = qUtils.extractPlaylistSongs(jsonResponse);
                }else if(action == SPOTIFY_TRACK){
                    songsList = qUtils.extractTrackSongs(jsonResponse);
                }else if(action == SPOTIFY_ALBUM) {
                    songsList = qUtils.extractAlbumSongs(jsonResponse);
                }

            } catch (MalformedURLException e) {
                Log.i(MainActivity.class.getName(), "Request url cannot proceed", e);

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            customAdapter.notifyDataSetChanged();
            if (0 != songsList.size()) {
                floatingActionButton.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
            } else {
                loading.setAnimation(R.raw.emptyy);
                Toast.makeText(context, "The feature to download album and single track will be available soon",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
