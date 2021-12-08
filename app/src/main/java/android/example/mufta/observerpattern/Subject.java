package android.example.mufta.observerpattern;

public interface Subject {
    void addListner(Observers o);
    void notifyObservers();

    void notifyRedirects();
}
