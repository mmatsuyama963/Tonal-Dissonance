import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MyProgram {

    JFrame frame;
    JPanel container;
    CardLayout layout;

    Composition currentComposition;
    ArrayList<Integer> currentChordNotes;
    int currentChordColumn = 1;
    private double[][] gridData = new double[5][12];

    //Utility Methods

    private double dynamicsToAmplitude(String dyn) {
        return switch (dyn) {
            case "pp" -> 0.2;
            case "p"  -> 0.4;
            case "mp" -> 0.6;
            case "mf" -> 0.8;
            case "f"  -> 1.0;
            case "ff" -> 1.2;
            default   -> 0.8;
        };
    }

    private double midiToFrequency(int midi) {
        return 440.0 * Math.pow(2, (midi - 69) / 12.0);
    }

    private String extractBase(String noteName) {
        return noteName.replaceAll("\\d", "");
    }

    // Constructor

    public MyProgram() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Harmony Navigator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);

            layout = new CardLayout();
            container = new JPanel(layout);
            container.setBackground(Color.decode("#2C2F33"));

            container.add(createMainMenuPanel(), "MainMenu");
            container.add(createBuildMelodyPanel(), "BuildMelody");
            container.add(createListenerFeedbackPanel(), "ListenerFeedback");

            frame.add(container);
            frame.setVisible(true);
        });
    }

    // Main Menu

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#2C2F33"));

        JLabel title = new JLabel("Harmony Navigator", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 75));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 20, 20));
        buttons.setBackground(Color.decode("#2C2F33"));

        JButton buildBtn = createTitleButton("Build Your Melody");
        buildBtn.addActionListener(e -> layout.show(container, "BuildMelody"));

        JButton feedbackBtn = createTitleButton("Listener Feedback");
        feedbackBtn.addActionListener(e -> layout.show(container, "ListenerFeedback"));

        buttons.add(buildBtn);
        buttons.add(feedbackBtn);
        panel.add(buttons, BorderLayout.CENTER);

        PianoPanel piano = new PianoPanel(null);
        JScrollPane scroll = new JScrollPane(
                piano,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        scroll.setPreferredSize(new Dimension(1000, 220));
        panel.add(scroll, BorderLayout.SOUTH);

        return panel;
    }

    // Build Melody Panel

    private JPanel createBuildMelodyPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#2C2F33"));

        currentComposition = new Composition();
        currentChordNotes = new ArrayList<>();

        JButton backBtn = createStyledButton("Back to Main Menu");
        backBtn.addActionListener(e -> layout.show(container, "MainMenu"));

        JLabel instruction = new JLabel("Step 1: Select a Key (click any piano key)", SwingConstants.CENTER);
        instruction.setFont(new Font("Serif", Font.BOLD, 22));
        instruction.setForeground(Color.WHITE);

        StaffPanel staff = new StaffPanel();  
        staff.setPreferredSize(new Dimension(1000, 220));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.decode("#2C2F33"));
        topPanel.add(instruction, BorderLayout.NORTH);
        topPanel.add(staff, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#2C2F33"));
        JPanel backWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        backWrapper.setBackground(Color.decode("#2C2F33"));
        backWrapper.add(backBtn);
        
        headerPanel.add(backWrapper, BorderLayout.WEST);

        headerPanel.add(topPanel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);

        /* ----- Dissonance Grid ----- */

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.decode("#2C2F33"));

        JLabel gridTitle = new JLabel("Dissonance Scores", SwingConstants.CENTER);
        gridTitle.setFont(new Font("Serif", Font.BOLD, 20));
        gridTitle.setForeground(Color.WHITE);
        centerPanel.add(gridTitle, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(5, 12));
        gridPanel.setBackground(Color.WHITE);

        for (int c = 0; c < 12; c++) {
            JLabel label = (c == 0)
                    ? new JLabel("", SwingConstants.CENTER)
                    : new JLabel("Chord " + c, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setFont(new Font("Serif", Font.BOLD, 14));
            gridPanel.add(label);
        }

        String[] rows = {"Sethares", "TTP", "RQA", "Holistic"};
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 12; c++) {
                JLabel label = (c == 0)
                        ? new JLabel(rows[r], SwingConstants.CENTER)
                        : new JLabel("", SwingConstants.CENTER);
                label.setOpaque(true);
                label.setBackground(Color.WHITE);
                gridPanel.add(label);
            }
        }

        centerPanel.add(gridPanel, BorderLayout.CENTER);

        JPanel chordControls = new JPanel(new GridLayout(1, 3, 10, 10));
        chordControls.setBackground(Color.decode("#2C2F33"));

        JButton undoBtn = createStyledButton("Undo Note");
        JButton doneBtn = createStyledButton("Done Chord");
        JButton clearBtn = createStyledButton("Clear All");

        chordControls.add(undoBtn);
        chordControls.add(doneBtn);
        chordControls.add(clearBtn);

        centerPanel.add(chordControls, BorderLayout.SOUTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        /* ----- Piano Input ----- */

        PianoPanel piano = new PianoPanel((midi, name) -> {
            if (currentComposition.key == null) {
                currentComposition.key = extractBase(name);
                currentComposition.isMajor = JOptionPane.showConfirmDialog(
                        frame, "Major scale?", "Scale Type",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION;

                staff.setKey(currentComposition.key, currentComposition.isMajor);
                instruction.setText("Step 2: Build chords");
                return;
            }

            if (!currentChordNotes.contains(midi)) {
                currentChordNotes.add(midi);
                staff.setPreviewChord(currentChordNotes);
            }
        });

        JScrollPane pianoScroll = new JScrollPane(
                piano,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        pianoScroll.setPreferredSize(new Dimension(1000, 220));
        panel.add(pianoScroll, BorderLayout.SOUTH);

        undoBtn.addActionListener(e -> {
            if (!currentChordNotes.isEmpty()) {
                currentChordNotes.remove(currentChordNotes.size() - 1);
                staff.setPreviewChord(currentChordNotes);
            }
        });

        clearBtn.addActionListener(e -> {
            currentChordNotes.clear();
            currentComposition.chords.clear();
            staff.clearAll();
            currentChordColumn = 1;
        });

        return panel;
    }

    //Listener Feedback 
private JPanel createListenerFeedbackPanel() {
    JPanel panel = new JPanel(new CardLayout());
    panel.setBackground(Color.decode("#2C2F33"));
    CardLayout cl = (CardLayout) panel.getLayout();

    //Data
    String[] intervals = {"7:4", "9:5", "8:5", "5:3", "4:3", "3:2", "5:4", "2:1"};
    ArrayList<ListenerGraph.IntervalData> intervalData = new ArrayList<>();
    for (String s : intervals) {
        intervalData.add(new ListenerGraph.IntervalData(s, 5.0, 0.0)); 
    }

    // PAGE 1: Staff + Textboxes + Done + Piano
    JPanel page1 = new JPanel(null);
    page1.setBackground(Color.decode("#2C2F33"));

    // Back button
    JButton backBtn = createStyledButton("Back to Main Menu");
    backBtn.setBounds(10, 10, 180, 30);
    backBtn.addActionListener(e -> layout.show(container, "MainMenu"));
    page1.add(backBtn);

    // Staff
    StaffPanel staff = new StaffPanel();
    int staffHeight = 100;
    staff.setBounds(15, 50, 1100, staffHeight);
    page1.add(staff);

    // Add preset intervals as notes 
    ArrayList<ArrayList<Integer>> demoIntervals = new ArrayList<>();
    demoIntervals.add(new ArrayList<>(Arrays.asList(65, 76))); // 7:4
    demoIntervals.add(new ArrayList<>(Arrays.asList(62, 71))); // 9:5
    demoIntervals.add(new ArrayList<>(Arrays.asList(64, 72))); // 8:5
    demoIntervals.add(new ArrayList<>(Arrays.asList(65, 74))); // 5:3
    demoIntervals.add(new ArrayList<>(Arrays.asList(67, 72))); // 4:3
    demoIntervals.add(new ArrayList<>(Arrays.asList(69, 76))); // 3:2
    demoIntervals.add(new ArrayList<>(Arrays.asList(72, 76))); // 5:4
    demoIntervals.add(new ArrayList<>(Arrays.asList(62, 74))); // 2:1

    // Add each interval as a chord to the staff
    for (ArrayList<Integer> interval : demoIntervals) {
        staff.addFinalChord(interval, null);
    }

    // Interval labels + text fields
    ArrayList<JTextField> inputFields = new ArrayList<>();
    int n = intervals.length;
    int spacing = 645 / n;
    int labelY = 50 + staffHeight + 5;  
    int textY = labelY + 20;            

    for (int i = 0; i < n; i++) {
        // Interval label
        JLabel lbl = new JLabel(intervals[i], SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setBounds(170 + i * spacing + spacing / 4, labelY, 50, 20);
        page1.add(lbl);

        // Text box for user rating
        JTextField tf = new JTextField(String.valueOf(intervalData.get(i).rating));
        tf.setBounds(170 + i * spacing + spacing / 4, textY, 50, 25);
        page1.add(tf);
        inputFields.add(tf);
    }

    // Done button
    JButton doneBtn = createStyledButton("Done");
    doneBtn.setBounds(450, textY + 35, 100, 30);
    page1.add(doneBtn);

    // Piano (non-interactive)
    PianoPanel piano = new PianoPanel((midi, name) -> {
    });
    piano.setBounds(50, textY + 80, 900, 420); 
    page1.add(piano);

    // PAGE 2: Validation
    JPanel page2 = new JPanel(null);
    page2.setBackground(Color.decode("#2C2F33"));

    JLabel q = new JLabel("Are these output values accurate?", SwingConstants.CENTER);
    q.setFont(new Font("Serif", Font.BOLD, 22));
    q.setForeground(Color.WHITE);
    q.setBounds(0, 20, 1000, 30);
    page2.add(q);

    // Pick 4 intervals to validate
    ArrayList<ListenerGraph.IntervalData> testIntervals = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
        testIntervals.add(intervalData.get(i));
    }

    // Staff for page 2
    StaffPanel staff2 = new StaffPanel();
    staff2.setBounds(150, 500, 600, staffHeight);
    page2.add(staff2);

    // Add the same 4 intervals for validation
    for (int i = 0; i < testIntervals.size(); i++) {
        ArrayList<Integer> notes = new ArrayList<>();
        switch (i) {
            case 0 -> notes.addAll(Arrays.asList(67, 77));
            case 1 -> notes.addAll(Arrays.asList(64, 72));
            case 2 -> notes.addAll(Arrays.asList(65, 74));
            case 3 -> notes.addAll(Arrays.asList(67, 75));
        }
        staff2.addFinalChord(notes, null);
    }

    // Text boxes for corrections
    ArrayList<JTextField> correctionFields = new ArrayList<>();
    int page2Spacing = 310 / testIntervals.size();
    int label2Y = 70 + staffHeight + 5;
    int text2Y = label2Y + 20;

    for (int i = 0; i < testIntervals.size(); i++) {
        JLabel lbl = new JLabel(testIntervals.get(i).label, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setBounds(310 + i * page2Spacing + page2Spacing / 4, label2Y + 430, 50, 20);
        page2.add(lbl);

        JTextField tf = new JTextField(String.valueOf(testIntervals.get(i).rating));
        tf.setBounds(310 + i * page2Spacing + page2Spacing / 4, text2Y + 430, 50, 25);
        page2.add(tf);
        correctionFields.add(tf);
    }

    // ListenerGraph for page 2
    ListenerGraph graph = new ListenerGraph(intervalData);
    graph.setBounds(50, 50, 900, 400);
    page2.add(graph);

    // Yes/No buttons
    JButton yesBtn = createStyledButton("Yes – Score shown");
    yesBtn.setBounds(250, 450, 200, 30);
    page2.add(yesBtn);

    JButton noBtn = createStyledButton("No – Readjust below");
    noBtn.setBounds(500, 450, 200, 30);
    page2.add(noBtn);

    // NAVIGATION 
    doneBtn.addActionListener(e -> {
        for (int i = 0; i < intervalData.size(); i++) {
            try {
                double v = Double.parseDouble(inputFields.get(i).getText());
                if (v < 1.0 || v > 10.0) throw new NumberFormatException();
                intervalData.get(i).rating = v;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Ratings must be between 1.0 and 10.0",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                inputFields.get(i).setText("5.0");
                return;
            }
        }
        staff2.repaint();
        cl.show(panel, "validate");
    });

    yesBtn.addActionListener(e -> {
        JOptionPane.showMessageDialog(frame,
                "Score computed successfully.\nCalibration complete.",
                "Done",
                JOptionPane.INFORMATION_MESSAGE);
    });

    noBtn.addActionListener(e -> {
        for (int i = 0; i < correctionFields.size(); i++) {
            try {
                double v = Double.parseDouble(correctionFields.get(i).getText());
                if (v < 1.0 || v > 10.0) throw new NumberFormatException();
                testIntervals.get(i).rating = v;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Ratings must be between 1.0 and 10.0",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                correctionFields.get(i).setText("5.0");
                return;
            }
        }
        graph.repaint();
    });

    // ADD PAGES 
    panel.add(page1, "input");
    panel.add(page2, "validate");
    cl.show(panel, "input");

    return panel;
}

private class ListenerGraph extends JPanel {

    public static class IntervalData {
        public String label;      
        public double rating;     
        public double complexity;

        public IntervalData(String label, double rating, double complexity) {
            this.label = label;
            this.rating = rating;
            this.complexity = complexity;
        }
    }

    private ArrayList<IntervalData> data;

    public ListenerGraph(ArrayList<IntervalData> data) {
        this.data = data;
        setPreferredSize(new Dimension(900, 450));
        setBackground(Color.decode("#2C2F33"));
    }

    public void setData(ArrayList<IntervalData> newData) {
        this.data = newData;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int padding = 70;
        int labelPadding = 40;
        int width = getWidth();
        int height = getHeight();

        int graphWidth = width - 2 * padding - labelPadding;
        int graphHeight = height - 2 * padding;

        g2.setColor(Color.WHITE);

        // Axes
        int x0 = padding + labelPadding;
        int y0 = height - padding;
        g2.drawLine(x0, padding, x0, y0);               
        g2.drawLine(x0, y0, width - padding, y0);      

        // Y-axis ticks + labels
        for (int i = 0; i <= 10; i++) {
            int y = y0 - (i * graphHeight / 10);
            g2.drawLine(x0 - 5, y, x0 + 5, y);
            g2.drawString(String.valueOf(i), x0 - 30, y + 5);
        }

        // Sort by complexity (high → low)
        ArrayList<IntervalData> sorted = new ArrayList<>(data);
        sorted.sort((a, b) -> Double.compare(b.complexity, a.complexity));

        int n = sorted.size();
        if (n == 1) {
            drawSinglePoint(g2, sorted.get(0), x0, y0, graphHeight);
            return;
        }

        // X-axis ticks + labels
        for (int i = 0; i < n; i++) {
            int x = x0 + (i * graphWidth / (n - 1));
            g2.drawLine(x, y0 - 5, x, y0 + 5);
            g2.drawString(sorted.get(i).label, x - 15, y0 + 20);
        }

        // Plot line + points
        for (int i = 0; i < n - 1; i++) {
            Point p1 = mapPoint(sorted.get(i), i, n, x0, y0, graphWidth, graphHeight);
            Point p2 = mapPoint(sorted.get(i + 1), i + 1, n, x0, y0, graphWidth, graphHeight);

            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            drawPoint(g2, p1);
        }

        // Last point
        drawPoint(
                g2,
                mapPoint(sorted.get(n - 1), n - 1, n, x0, y0, graphWidth, graphHeight)
        );
    }

    // Helpers 

    private Point mapPoint(IntervalData d,
                           int index,
                           int total,
                           int x0,
                           int y0,
                           int graphWidth,
                           int graphHeight) {

        int x = x0 + (index * graphWidth / (total - 1));
        int y = y0 - (int) (clamp(d.rating) * graphHeight / 10.0);
        return new Point(x, y);
    }

    private void drawPoint(Graphics2D g2, Point p) {
        g2.fillOval(p.x - 4, p.y - 4, 8, 8);
    }

    private void drawSinglePoint(Graphics2D g2,
                                 IntervalData d,
                                 int x0,
                                 int y0,
                                 int graphHeight) {

        int x = x0 + 200;
        int y = y0 - (int) (clamp(d.rating) * graphHeight / 10.0);
        drawPoint(g2, new Point(x, y));
        g2.drawString(d.label, x - 15, y0 + 20);
    }

    private double clamp(double v) {
        return Math.max(0, Math.min(10, v));
    }
}



    // Buttons 

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));  
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode("#4A90E2"));
        button.setFocusPainted(false);
    
        button.setPreferredSize(new Dimension(160, 32));   
        button.setMaximumSize(new Dimension(160, 32));
        button.setMargin(new Insets(4, 10, 4, 10));        

    return button;
    }

    private JButton createTitleButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 40));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode("#4A90E2"));
        button.setFocusPainted(false);
        return button;
    }

    // Main

    public static void main(String[] args) {
        new MyProgram();
    }
}
