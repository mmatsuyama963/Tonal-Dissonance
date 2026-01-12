import java.util.ArrayList;

public class Composition {
    public String key;           // e.g., "C"
    public boolean isMajor;// true = Major, false = Minor
    public String instrument;
    public ArrayList<Chord> chords;

    public Composition() {
        chords = new ArrayList<>();
        this.key = null;
        this.isMajor = true; // default
        this.instrument = "Piano"; // default
        this.chords = new ArrayList<>();
    }
}