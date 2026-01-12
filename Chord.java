import java.util.ArrayList;

public class Chord {
    public ArrayList<Integer> midiNotes;  // MIDI notes
    public String instrument;
    public String dynamics;

    public Chord() {
        midiNotes = new ArrayList<>();
        instrument = "Piano";
        dynamics = "mf";
    }
}