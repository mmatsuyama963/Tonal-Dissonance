//Some interactive features are being refined like note placement in the StaffPanel; full functionality will be available at the symposium.
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class MyProgram {

    JFrame frame;
    JPanel container;
    CardLayout layout;

    Composition currentComposition;
    ArrayList<Integer> currentChordNotes;
    int currentChordColumn = 1; // start at 1 since 0 is row labels
    private double[][] gridData = new double[5][12]; // 5 rows, 12 columns

    private double dynamicsToAmplitude(String dyn) 
    {
        return switch (dyn) 
            {
                case "pp" -> 0.2;
                case "p"  -> 0.4;
                case "mp" -> 0.6;
                case "mf" -> 0.8;
                case "f"  -> 1.0;
                case "ff" -> 1.2;
                default   -> 0.8;
            };
    }

    private double midiToFrequency(int midi) 
    {
        return 440.0 * Math.pow(2, (midi - 69) / 12.0);
    }


    public MyProgram() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Harmony Navigator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);

            layout = new CardLayout();
            container = new JPanel(layout);
            container.setBackground(Color.decode("#2C2F33"));

            JPanel mainMenuPanel = createMainMenuPanel();
            JPanel buildMelodyPanel = createBuildMelodyPanel();

            container.add(mainMenuPanel, "MainMenu");
            container.add(buildMelodyPanel, "BuildMelody");

            frame.add(container);
            frame.setVisible(true);
        });
    }

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#2C2F33"));

        JLabel title = new JLabel("Harmony Navigator", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setForeground(Color.decode("#F0F0F0"));
        panel.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 15, 15));
        buttons.setBackground(Color.decode("#2C2F33"));
        buttons.setOpaque(false);

        JButton build = createStyledButton("Build Your Melody");
        build.addActionListener(e -> layout.show(container, "BuildMelody"));
        buttons.add(build);

        panel.add(buttons, BorderLayout.CENTER);

        // Scrollable piano preview at bottom
        PianoPanel piano = new PianoPanel(null);
        JScrollPane scrollPane = new JScrollPane(
                piano,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        scrollPane.setPreferredSize(new Dimension(1000, 220));
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

   private JPanel createBuildMelodyPanel() 
   {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#2C2F33"));
    
        currentComposition = new Composition();
        currentChordNotes = new ArrayList<>();
    
        // Top: Instructions + Staff
        JLabel instruction = new JLabel("Step 1: Select a Key (click any piano key)", SwingConstants.CENTER);
        instruction.setFont(new Font("Serif", Font.BOLD, 22));
        instruction.setForeground(Color.WHITE);
    
        StaffPanel staff = new StaffPanel();
        staff.setPreferredSize(new Dimension(1000, 220));
    
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.decode("#2C2F33"));
        topPanel.add(instruction, BorderLayout.NORTH);
        topPanel.add(staff, BorderLayout.CENTER);
    
        panel.add(topPanel, BorderLayout.NORTH);
    
        // Center: Dissonance / Chord Table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.decode("#2C2F33"));

        // Label above the grid
        JLabel dissonanceLabel = new JLabel("Dissonance Scores", SwingConstants.CENTER);
        dissonanceLabel.setFont(new Font("Serif", Font.BOLD, 20));
        dissonanceLabel.setForeground(Color.WHITE);
        centerPanel.add(dissonanceLabel, BorderLayout.NORTH);
        
        // Grid panel: 5 rows x 12 columns (1 header column + 11 chords)
        JPanel gridPanel = new JPanel(new GridLayout(5, 12));
        gridPanel.setBackground(Color.WHITE);
        
        // First row: blank cell + chord headers
        for (int c = 0; c < 12; c++) {
            JLabel label;
            if (c == 0) {
                label = new JLabel("", SwingConstants.CENTER); // top-left blank
            } else {
                label = new JLabel("Chord " + c, SwingConstants.CENTER);
            }
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
            label.setFont(new Font("Serif", Font.BOLD, 14));
            gridPanel.add(label);
        }
        
        // Remaining rows: "Sethares", "TTP", "RQA", "Holistic" in first column
        String[] rowLabels = {"Sethares", "TTP", "RQA", "Holistic"};
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 12; c++) {
                JLabel label;
                if (c == 0) {
                    label = new JLabel(rowLabels[r], SwingConstants.CENTER);
                    label.setFont(new Font("Serif", Font.BOLD, 14));
                } else {
                    label = new JLabel("", SwingConstants.CENTER);
                    label.setFont(new Font("Serif", Font.PLAIN, 14));
                }
                label.setOpaque(true);
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);
                gridPanel.add(label);
            }
        }



    gridPanel.setPreferredSize(new Dimension(1000, 200));
    centerPanel.add(gridPanel, BorderLayout.CENTER);

    // Chord controls
    JPanel chordPanel = new JPanel(new GridLayout(1,3,5,5));
    chordPanel.setBackground(Color.decode("#2C2F33"));
    JButton undoBtn = createStyledButton("Undo Note");
    JButton doneBtn = createStyledButton("Done Chord");
    JButton clearAll = createStyledButton("Clear All");
    chordPanel.add(undoBtn);
    chordPanel.add(doneBtn);
    chordPanel.add(clearAll);

    centerPanel.add(chordPanel, BorderLayout.SOUTH);
    panel.add(centerPanel, BorderLayout.CENTER);

    //Bottom: Scrollable Piano 
    PianoPanel piano = new PianoPanel((midiNote, noteName) -> {
        // First time: select key
        if (currentComposition.key == null) {
            String base = extractBase(noteName);

            int choice = JOptionPane.showOptionDialog(frame,
                    "Choose scale type for " + base,
                    "Select Scale Type",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Major", "Minor"},
                    "Major");
            currentComposition.key = base;
            currentComposition.isMajor = (choice == 0);

            // Instrument selection
            String[] instruments = {
                    "Bassoon", "Cello", "Clarinet", "Cymbals", "Double Bass", "Flute", "French Horn",
                    "Gongs", "Harpsichord", "Harp", "Oboe", "Organ (Principal Stops)",
                    "Organ (Reed Stops)", "Piano", "Snare", "Saxophone", "Timpani",
                    "Trumpet", "Viola", "Violin", "Voice"
            };
            String selectedInstrument = (String) JOptionPane.showInputDialog(
                    frame,
                    "Select Instrument:",
                    "Choose Instrument",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    instruments,
                    instruments[0]
            );
            if (selectedInstrument == null) selectedInstrument = "Piano";
            currentComposition.instrument = selectedInstrument;

            // Time signature selection
            String[] times = {"4/4", "3/4", "2/4", "6/8"};
            String ts = (String) JOptionPane.showInputDialog(frame,
                    "Choose Time Signature:",
                    "Time Signature",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    times,
                    times[0]);

            staff.setKey(currentComposition.key, currentComposition.isMajor);

            // Update instruction text
            String text = "Step 2: Choose notes for the chord — Key: "
                    + currentComposition.key
                    + (currentComposition.isMajor ? " Major" : " Minor")
                    + ", Instrument: " + selectedInstrument;
            if (ts != null) text += ", Time: " + ts;
            instruction.setText(text);
            return;
        }

        // Step 2: add notes to current chord
        if (!currentChordNotes.contains(midiNote)) {
            currentChordNotes.add(midiNote);
            staff.setPreviewChord(currentChordNotes);
        }
    });

    JScrollPane pianoScroll = new JScrollPane(
            piano,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    );
    pianoScroll.setPreferredSize(new Dimension(1000, 220));
    panel.add(pianoScroll, BorderLayout.SOUTH);

    // Chord button listeners
    undoBtn.addActionListener(e -> {
        if(!currentChordNotes.isEmpty()) {
            currentChordNotes.remove(currentChordNotes.size()-1);
            staff.setPreviewChord(currentChordNotes);
        }
    });

    doneBtn.addActionListener(e -> {
    if (!currentChordNotes.isEmpty()) {
        Chord chord = new Chord();
        chord.midiNotes.addAll(currentChordNotes);

        // Ask for dynamics
        String[] dynamicsOptions = {"pp", "p", "mp", "mf", "f", "ff"};
        String selectedDynamics = (String) JOptionPane.showInputDialog(
                frame,
                "Select dynamics for this chord:",
                "Dynamics",
                JOptionPane.PLAIN_MESSAGE,
                null,
                dynamicsOptions,
                "mf"
        );
        if (selectedDynamics == null) selectedDynamics = "mf"; // default
        chord.dynamics = selectedDynamics;

        currentComposition.chords.add(chord);
        staff.addFinalChord(new ArrayList<>(currentChordNotes), chord);

        // Compute amplitudes and frequencies
        double amp = dynamicsToAmplitude(selectedDynamics);
        double[] freqs = currentChordNotes.stream()
                .sorted()
                .mapToInt(Integer::intValue)
                .mapToDouble(this::midiToFrequency)
                .toArray();
        double[] amps = new double[freqs.length];
        for (int i = 0; i < amps.length; i++) amps[i] = amp;

        // Previous chord for TTP
        int[] prevChord = currentChordColumn > 1 ? currentComposition.chords.get(currentChordColumn-2).midiNotes.stream().mapToInt(Integer::intValue).toArray() : null;

        // Key chroma
        int[] keyChroma = DissonanceCalculator.buildKeyChroma(currentComposition.key, currentComposition.isMajor);

        // Computer scores
        double setharesScore = DissonanceCalculator.sethares(freqs, amps);
        double ttpScore = DissonanceCalculator.ttpChord(currentChordNotes.stream().mapToInt(Integer::intValue).toArray(), prevChord, keyChroma);
        double rqaScore = DissonanceCalculator.rqa(freqs, amps);
        
        
        // Holistic score example
        double O = InstrumentOvertones.getOvertone(currentComposition.instrument);
        double setharesNScore = DissonanceCalculator.normalSethares(freqs, amps, O);
        double holisticScore = setharesNScore
                + (1 - rqaScore) * (1 - O) * 0.425
                + ttpScore * 0.4;

        // Update grid
        gridData[0][currentChordColumn] = setharesScore;
        gridData[1][currentChordColumn] = ttpScore;
        gridData[2][currentChordColumn] = rqaScore;
        gridData[3][currentChordColumn] = holisticScore;

        for (int r = 0; r < 4; r++) {
            JLabel label = (JLabel) gridPanel.getComponent((r+1) * 12 + currentChordColumn);
            label.setText(String.format("%.2f", gridData[r][currentChordColumn]));
        }

        currentChordNotes.clear();
        staff.clearPreview();

        // Move to next column
        currentChordColumn++;
        if (currentChordColumn > 11) {
            JOptionPane.showMessageDialog(frame, "All 11 chords already entered.");
        }
    }
});


    clearAll.addActionListener(e -> 
    {
        currentChordNotes.clear();
        currentComposition.chords.clear();
        staff.clearAll();
        
        for (int r=0; r<4; r++)
        {
            for(int c=1; c<12;c++)
            {
                JLabel label = (JLabel) gridPanel.getComponent((r + 1) * 12 + c);
                label.setText("");              
            
            }
        }

        // Reset gridData as well
        for (int r = 0; r < 4; r++) 
        {
            for (int c = 0; c < 12; c++) 
            {
                gridData[r][c] = 0;
            }
        }

        // Reset chord column to start again
        currentChordColumn = 1;
    });


    return panel;
}

    private JButton createStyledButton(String text){
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode("#4A90E2"));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.decode("#3A70C2"), 2));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt){ button.setBackground(Color.decode("#6AA5F2")); }
            public void mouseExited(MouseEvent evt){ button.setBackground(Color.decode("#4A90E2")); }
        });
        return button;
    }

    private JLabel createSideLabel(String text){
        JLabel label = new JLabel(text);
        label.setForeground(Color.decode("#F0F0F0"));
        label.setFont(new Font("Serif", Font.BOLD,14));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private String extractBase(String noteName) {
        return noteName.replaceAll("\\d", "");
    }

    public static void main(String[] args){
        new MyProgram();
    }
}