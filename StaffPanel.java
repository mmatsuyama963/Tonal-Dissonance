import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class StaffPanel extends JPanel {

    private ArrayList<ArrayList<Integer>> finalizedChords = new ArrayList<>();
    private ArrayList<Chord> chordMeta = new ArrayList<>();
    private ArrayList<Integer> previewChord = new ArrayList<>();

    private String key = "C";
    private boolean isMajor = true;

    private static final int LINE_SPACING = 12;
    private static final int STAFF_TOP = 30;
    private static final int NOTE_RADIUS = 8;
    private static final int START_X = 200;
    private static final int NOTE_X_SPACING = 80;

    private HashMap<Integer, Double> stepToY = new HashMap<>();
    private Image trebleClef;

    // CONSTRUCTOR
    public StaffPanel() {
        setPreferredSize(new Dimension(900, 180));
        generateStepToYMapping();

        try {
            trebleClef = new ImageIcon(
                getClass().getResource("/treble_clef.png")
            ).getImage();
        } catch (Exception e) {
            trebleClef = null;
        }
    }

    // PUBLIC API
    public void setKey(String key, boolean isMajor) {
        this.key = key;
        this.isMajor = isMajor;
        repaint();
    }

    public void setPreviewChord(ArrayList<Integer> chordNotes) {
        previewChord = new ArrayList<>(chordNotes);
        repaint();
    }

    public void clearPreview() {
        previewChord.clear();
        repaint();
    }

    public void addFinalChord(ArrayList<Integer> chordNotes, Chord meta) {
        finalizedChords.add(new ArrayList<>(chordNotes));
        chordMeta.add(meta);

        int neededWidth =
            START_X + (finalizedChords.size() + 2) * NOTE_X_SPACING + 200;
        setPreferredSize(
            new Dimension(
                Math.max(getPreferredSize().width, neededWidth),
                getPreferredSize().height
            )
        );
        revalidate();
        repaint();
    }

    public void clearAll() {
        finalizedChords.clear();
        chordMeta.clear();
        previewChord.clear();
        repaint();
    }

    // STAFF MAPPING

    /**
     * step 0 = F5 (top treble line)
     * each step = one diatonic move (line or space)
     */
    private void generateStepToYMapping() {
        for (int step = -30; step <= 30; step++) {
            stepToY.put(step,
                STAFF_TOP - step * (LINE_SPACING / 2.0));
        }
    }

  
    private int midiToStep(int midi) {
        // F5 = MIDI 77 = step 0
        int semitoneOffset = midi - 77;

        // Semitone → diatonic step (relative to F)
        int[] semitoneToStep = {
            0,  // F
            0,  // F#
            1,  // G
            1,  // G#
            2,  // A
            2,  // A#
            3,  // B
            4,  // C
            4,  // C#
            5,  // D
            5,  // D#
            6   // E
        };

        int octave = Math.floorDiv(semitoneOffset, 12);
        int semitone = Math.floorMod(semitoneOffset, 12);

        return octave * 7 + semitoneToStep[semitone];
    }

// NOTES
private void drawChord(Graphics2D g2,
                       ArrayList<Integer> chord,
                       int x) {

    for (int midi : chord) {
        int step = midiToStep(midi);
        Double yObj = stepToY.get(step);
        if (yObj == null) continue;

        double y = yObj;

        // notehead
        g2.setColor(Color.WHITE);
        g2.fillOval(
            x - NOTE_RADIUS,
            (int)(y - NOTE_RADIUS),
            NOTE_RADIUS * 2,
            NOTE_RADIUS * 2
        );

        g2.setColor(Color.BLACK);
        g2.drawOval(
            x - NOTE_RADIUS,
            (int)(y - NOTE_RADIUS),
            NOTE_RADIUS * 2,
            NOTE_RADIUS * 2
        );

    }
}

    // PAINT
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(44, 47, 51));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));

        // 5 staff lines
        for (int i = 0; i < 5; i++) {
            int y = STAFF_TOP + i * LINE_SPACING;
            g2.drawLine(50, y, getWidth() - 50, y);
        }

        // Treble clef
        if (trebleClef != null) {
            g2.drawImage(trebleClef, 5,
                STAFF_TOP - 30, 40, 80, this);
        }

        if (key != null) drawKeySignature(g2);

        // Draw chords
        int x = START_X;
        for (int i = 0; i < finalizedChords.size(); i++) {
            drawChord(g2, finalizedChords.get(i), x);
            x += NOTE_X_SPACING;
        }

        if (!previewChord.isEmpty()) {
            drawChord(g2, previewChord, x);
        }
    }

    


    //KEY SIGNATURE
    private void drawKeySignature(Graphics2D g2) {
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.setColor(Color.WHITE);

        int count = getKeySignatureCount(key, isMajor);
        int startX = 60;
        int dx = 12;

        int[] sharpSteps = {0, -3, 1, -2, -5, -1, -4}; // F C G D A E B
        int[] flatSteps  = {-4, -1, -5, -2, 1, -3, 0}; // Bb Eb Ab Db Gb C F

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                g2.drawString("#",
                    startX + i * dx,
                    (int)(stepToY.get(sharpSteps[i]) + 5));
            }
        } else if (count < 0) {
            for (int i = 0; i < -count; i++) {
                g2.drawString("b",
                    startX + i * dx,
                    (int)(stepToY.get(flatSteps[i]) + 5));
            }
        }
    }

    private int getKeySignatureCount(String keyName, boolean major) {
    if (major) {
        switch (keyName) {
            case "C":  return 0;
            case "G":  return 1;
            case "D":  return 2;
            case "A":  return 3;
            case "E":  return 4;
            case "B":  return 5;
            case "F#": return 6;
            case "C#": return 7;
            case "F":  return -1;
            case "Bb": return -2;
            case "Eb": return -3;
            case "Ab": return -4;
            case "Db": return -5;
            case "Gb": return -6;
            case "Cb": return -7;
        }
    } else {
        switch (keyName) {
            case "A":  return 0;
            case "E":  return 1;
            case "B":  return 2;
            case "F#": return 3;
            case "C#": return 4;
            case "G#": return 5;
            case "D#": return 6;
            case "A#": return 7;
            case "D":  return -1;
            case "G":  return -2;
            case "C":  return -3;
            case "F":  return -4;
            case "Bb": return -5;
            case "Eb": return -6;
            case "Ab": return -7;
        }
    }
    return 0;
}
}