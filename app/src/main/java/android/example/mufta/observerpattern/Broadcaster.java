package android.example.mufta.observerpattern;

import java.util.ArrayList;
import java.util.List;

public class Broadcaster implements Subject{
    private float progressOfOneSong;
    private int totalSongProgress;
    private boolean downloading;

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
        notifyRedirects();
    }

    private boolean redirect;
    List<Observers> obj;
    public Broadcaster(){
        obj = new ArrayList<>();
    }

    @Override
    public void addListner(Observers o) {
        obj.add(o);
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
        notifyObservers();
    }

    public void setProgressOfOneSong(float progressOfOneSong) {
        this.progressOfOneSong = progressOfOneSong;
        notifyObservers();
    }

    public void setTotalSongProgress(int totalSongProgress) {
        this.totalSongProgress = totalSongProgress;
        notifyObservers();
    }

    @Override
    public void notifyObservers() {
        for(Observers observers : obj){
            observers.update(progressOfOneSong, totalSongProgress, downloading);
        }
    }

    @Override
    public void notifyRedirects() {
        for(Observers observers : obj){
            observers.update(redirect);
        }
    }
}
