import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PianoPanel extends JPanel {

    // KeyPress Listener
    public interface KeyPressListener {
        void onKeyPress(int midiNote, String noteName);
    }

    private KeyPressListener listener;

    private final int WHITE_WIDTH = 40;
    private final int WHITE_HEIGHT = 200;
    private final int BLACK_WIDTH = 26;
    private final int BLACK_HEIGHT = 120;

    private ArrayList<PianoKey> whiteKeys = new ArrayList<>();
    private ArrayList<PianoKey> blackKeys = new ArrayList<>();

    // Start from D3 (MIDI 50) → End at C8 (MIDI 108)
    private static final int MIDI_START = 50; // D3
    private static final int MIDI_END = 108;  // C8

    public PianoPanel(KeyPressListener listener) {
        this.listener = listener;

        setBackground(new Color(44, 47, 51));

        buildKeyboard();

        // set preferred width based on number of white keys
        int totalWhite = whiteKeys.size();
        setPreferredSize(new Dimension(Math.max(800, totalWhite * WHITE_WIDTH), WHITE_HEIGHT));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Black keys are drawn above white keys
                for (PianoKey k : blackKeys) {
                    if (k.contains(e.getPoint())) {
                        handleKey(k);
                        return;
                    }
                }
                for (PianoKey k : whiteKeys) {
                    if (k.contains(e.getPoint())) {
                        handleKey(k);
                        return;
                    }
                }
            }
        });
    }

    // Build keyboard with MIDI 50..108
    private void buildKeyboard() {
        whiteKeys.clear();
        blackKeys.clear();

        // semitone names (sharps)
        String[] namesSharp = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        HashMap<String, String> enharmonicMap = new HashMap<>();
        enharmonicMap.put("C#", "Db");
        enharmonicMap.put("D#", "Eb");
        enharmonicMap.put("F#", "Gb");
        enharmonicMap.put("G#", "Ab");
        enharmonicMap.put("A#", "Bb");

        int x = 0;

        // White keys
        for (int midi = MIDI_START; midi <= MIDI_END; midi++) {
            String nameSharp = namesSharp[midi % 12];
            int octave = (midi / 12) - 1;
            boolean isBlack = nameSharp.contains("#");

            if (!isBlack) {
                PianoKey white = new PianoKey(
                        x, 0, WHITE_WIDTH, WHITE_HEIGHT,
                        false, midi, nameSharp + octave, isMiddleC(midi)
                );
                whiteKeys.add(white);
                x += WHITE_WIDTH;
            }
        }

        // Black keys
        for (int i = 0; i < whiteKeys.size(); i++) {
            PianoKey w = whiteKeys.get(i);
            int possibleBlackMidi = w.midi + 1;
            if (possibleBlackMidi > MIDI_END) continue;

            int pc = possibleBlackMidi % 12;
            boolean isBlack = (pc == 1 || pc == 3 || pc == 6 || pc == 8 || pc == 10);
            if (isBlack) {
                String sharpName = getNameForMidi(possibleBlackMidi);
                String flatName = getEnharmonic(sharpName);
                int blackX = w.x + WHITE_WIDTH - (BLACK_WIDTH / 2);

                PianoKey black = new PianoKey(
                        blackX, 0, BLACK_WIDTH, BLACK_HEIGHT,
                        true, possibleBlackMidi, sharpName, false,
                        flatName != null ? new String[]{sharpName, flatName} : new String[]{sharpName}
                );
                blackKeys.add(black);
            }
        }
    }

    private boolean isMiddleC(int midi) {
        return midi == 60;
    }

    private String getNameForMidi(int midi) {
        String[] namesSharp = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int pc = midi % 12;
        int octave = (midi / 12) - 1;
        return namesSharp[pc] + octave;
    }

    private String getEnharmonic(String sharpLabelWithOctave) {
        String root = sharpLabelWithOctave.substring(0, sharpLabelWithOctave.length() - 1);
        String octave = sharpLabelWithOctave.substring(sharpLabelWithOctave.length() - 1);
        switch (root) {
            case "C#": return "Db" + octave;
            case "D#": return "Eb" + octave;
            case "F#": return "Gb" + octave;
            case "G#": return "Ab" + octave;
            case "A#": return "Bb" + octave;
            default: return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(44, 47, 51));
        g.fillRect(0, 0, getWidth(), getHeight());

        for (PianoKey k : whiteKeys) k.draw(g);
        for (PianoKey k : blackKeys) k.draw(g);
    }

    private void handleKey(PianoKey key) {
        String note = key.label;

        if (key.enharmonics != null && key.enharmonics.length > 1) {
            String choice = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose note name:",
                    "Black Key Enharmonic",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    key.enharmonics,
                    key.enharmonics[0]
            );
            if (choice != null) note = choice;
            else return;
        }

        if (listener != null)
            listener.onKeyPress(key.midi, note);
    }

    // KEY CLASS
    private static class PianoKey {
        int x, y, w, h;
        boolean isBlack;
        int midi;
        String label;
        boolean isMiddleC;
        String[] enharmonics;

        PianoKey(int x, int y, int w, int h,
                 boolean isBlack, int midi, String label, boolean isMiddleC) {
            this(x, y, w, h, isBlack, midi, label, isMiddleC, new String[]{label});
        }

        PianoKey(int x, int y, int w, int h,
                 boolean isBlack, int midi, String label, boolean isMiddleC, String[] enharmonics) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.isBlack = isBlack;
            this.midi = midi;
            this.label = label;
            this.isMiddleC = isMiddleC;
            this.enharmonics = enharmonics;
        }

        void draw(Graphics g) {
            if (isBlack) {
                g.setColor(Color.BLACK);
                g.fillRect(x, y, w, h);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                g.drawString(label, x + 3, y + h - 5);
            } else {
                g.setColor(isMiddleC ? Color.RED : Color.WHITE);
                g.fillRect(x, y, w, h);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, w, h);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.drawString(label, x + 5, y + h - 10);
            }
        }

        boolean contains(Point p) {
            return (p.x >= x && p.x <= x + w && p.y >= y && p.y <= y + h);
        }
    }
}