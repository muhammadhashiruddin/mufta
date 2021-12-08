package android.example.mufta.observerpattern;

public interface Observers {
    void update(float progressOfOneSong, int totalSongProgress, boolean downloading);
    void update(boolean reprocess);
}
