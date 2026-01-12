import java.util.Map;
import java.util.HashMap;

public class InstrumentOvertones {

    private static final Map<String, Double> instrumentOvertones = new HashMap<>();

    static {
        instrumentOvertones.put("Bassoon", 0.7);
        instrumentOvertones.put("Cello", 0.7);
        instrumentOvertones.put("Clarinet", 0.6);
        instrumentOvertones.put("Cymbals", 1.0);
        instrumentOvertones.put("Double Bass", 0.6);
        instrumentOvertones.put("Flute", 0.3);
        instrumentOvertones.put("French Horn", 0.8);
        instrumentOvertones.put("Gongs", 1.0);
        instrumentOvertones.put("Harpsichord", 0.9);
        instrumentOvertones.put("Harp", 0.7);
        instrumentOvertones.put("Oboe", 0.8);
        instrumentOvertones.put("Organ (Principal Stops)", 0.5);
        instrumentOvertones.put("Organ (Reed Stops)", 0.9);
        instrumentOvertones.put("Piano", 0.8);
        instrumentOvertones.put("Snare", 0.7);
        instrumentOvertones.put("Saxophone", 0.8);
        instrumentOvertones.put("Timpani", 0.6);
        instrumentOvertones.put("Trumpet", 0.8);
        instrumentOvertones.put("Viola", 0.7);
        instrumentOvertones.put("Violin", 0.8);
        instrumentOvertones.put("Voice", 0.7);
    }

    // Get overtone rating for a given instrument
    public static double getOvertone(String instrument) {
        return instrumentOvertones.getOrDefault(instrument, 0.8); // default to 0.8 if not found
    }
}