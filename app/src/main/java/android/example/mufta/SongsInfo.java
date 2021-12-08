package android.example.mufta;

public class SongsInfo {
    String songName;
    String artistName;
    String youtubeUrl;

    @Override
    public String toString() {
        return "SongsInfo{" +
                "songName='" + songName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", youtubeUrl='" + youtubeUrl + '\'' +
                '}';
    }

    public SongsInfo(String songName, String artistName, String youtubeUrl) {
        this.songName = songName;
        this.artistName = artistName;
        this.youtubeUrl = youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }


}
