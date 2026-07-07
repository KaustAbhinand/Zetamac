import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

public class Zetamac {

    JFrame frame;

    // Start screen components
    JPanel startPanel;
    JRadioButton d30, d60, d90, d120;
    JCheckBox add, sub, mul, div, neg;
    JTextField range1Field, range2Field;
    JButton startBtn;

    // Game components
    JLabel timerLabel, scoreLabel, questionLabel;
    JTextField answerField;
    JPanel gamePanel;

    Random rand = new Random();

    int duration, timeLeft, score = 0;
    int r1, r2;
    boolean allowNegative;
    char[] operations;

    int a, b, result;
    char op;

    Timer gameTimer;

    public Zetamac() {
        // Frame is created once and reused for the lifetime of the app.
        frame = new JFrame("Zetamac");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // only the 'X' button exits
        frame.setLayout(new BorderLayout());

        buildStartUI();

        frame.setVisible(true);
    }

    // ---------------- START SCREEN ----------------
    void buildStartUI() {

        if (frame == null) {
            frame = new JFrame("Zetamac");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        frame.getContentPane().removeAll();
        frame.setSize(500, 400);

        startPanel = new JPanel();
        startPanel.setLayout(new GridLayout(7, 1));

        // Duration
        JPanel durationPanel = new JPanel();
        d30 = new JRadioButton("30");
        d60 = new JRadioButton("60");
        d90 = new JRadioButton("90");
        d120 = new JRadioButton("120");

        ButtonGroup bg = new ButtonGroup();
        bg.add(d30);
        bg.add(d60);
        bg.add(d90);
        bg.add(d120);
        d30.setSelected(true);

        durationPanel.add(new JLabel("Duration: "));
        durationPanel.add(d30);
        durationPanel.add(d60);
        durationPanel.add(d90);
        durationPanel.add(d120);

        // Operations
        JPanel opPanel = new JPanel();
        add = new JCheckBox("+", true);
        sub = new JCheckBox("-", true);
        mul = new JCheckBox("x", true);
        div = new JCheckBox("/", false);

        opPanel.add(new JLabel("Operations: "));
        opPanel.add(add);
        opPanel.add(sub);
        opPanel.add(mul);
        opPanel.add(div);

        // Negative toggle
        JPanel negPanel = new JPanel();
        neg = new JCheckBox("Allow Negative Numbers");
        negPanel.add(neg);

        // Range
        JPanel rangePanel = new JPanel();
        range1Field = new JTextField("10", 5);
        range2Field = new JTextField("10", 5);

        rangePanel.add(new JLabel("Range 1:"));
        rangePanel.add(range1Field);
        rangePanel.add(new JLabel("Range 2:"));
        rangePanel.add(range2Field);

        // Start button
        startBtn = new JButton("Start Game");

        startBtn.addActionListener(e -> startGameFromUI());

        startPanel.add(durationPanel);
        startPanel.add(opPanel);
        startPanel.add(negPanel);
        startPanel.add(rangePanel);
        startPanel.add(new JLabel());
        startPanel.add(startBtn);

        frame.add(startPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // ---------------- READ INPUTS ----------------
    void startGameFromUI() {

        score = 0;
        // Duration
        if (d30.isSelected())
            duration = 30;
        else if (d60.isSelected())
            duration = 60;
        else if (d90.isSelected())
            duration = 90;
        else
            duration = 120;

        timeLeft = duration;

        // Operations
        ArrayList<Character> opsList = new ArrayList<>();
        if (add.isSelected())
            opsList.add('+');
        if (sub.isSelected())
            opsList.add('-');
        if (mul.isSelected())
            opsList.add('x');
        if (div.isSelected())
            opsList.add('/');

        if (opsList.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Select at least one operation!");
            return;
        }

        operations = new char[opsList.size()];
        for (int i = 0; i < opsList.size(); i++)
            operations[i] = opsList.get(i);

        // Negative
        allowNegative = neg.isSelected();

        // Range
        try {
            r1 = Integer.parseInt(range1Field.getText());
            r2 = Integer.parseInt(range2Field.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Invalid range input!");
            return;
        }

        // Switch to game UI
        score = 0;
        frame.getContentPane().removeAll();
        buildGameUI();
        frame.revalidate();
        frame.repaint();

        startGame();
    }

    // ---------------- GAME UI ----------------
    void buildGameUI() {
        frame.setSize(800, 400);

        JPanel topPanel = new JPanel(new BorderLayout());
        timerLabel = new JLabel("Seconds left: " + timeLeft);
        scoreLabel = new JLabel("Score: 0", SwingConstants.RIGHT);

        topPanel.add(timerLabel, BorderLayout.WEST);
        topPanel.add(scoreLabel, BorderLayout.EAST);

        gamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 100));
        gamePanel.setBackground(new Color(220, 220, 220));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.BOLD, 36));

        answerField = new JTextField(10);
        answerField.setFont(new Font("Arial", Font.PLAIN, 28));

        gamePanel.add(questionLabel);
        gamePanel.add(answerField);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(gamePanel, BorderLayout.CENTER);

        // Auto check
        answerField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    int ans = Integer.parseInt(answerField.getText());
                    if (ans == result) {
                        score++;
                        scoreLabel.setText("Score: " + score);
                        generateQuestion();
                        answerField.setText("");
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    // ---------------- GAME START ----------------
    void startGame() {
        generateQuestion();
        answerField.requestFocusInWindow();

        gameTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Seconds left: " + timeLeft);

            if (timeLeft <= 0) {
                gameTimer.stop();
                System.out.println("[Zetamac] Time up. Final score: " + score);
                // Modal dialog blocks here until the user clicks "OK",
                // then we loop back to the start screen instead of exiting.
                JOptionPane.showMessageDialog(frame, "Time up! Score: " + score);
                resetToHome();
            }
        });

        gameTimer.start();
    }
    // -----reset to home-------

    void resetToHome() {

        if (gameTimer != null) {
            gameTimer.stop();
        }

        score = 0;
        timeLeft = 0;

        buildStartUI();

        frame.revalidate();
        frame.repaint();
    }

    // ---------------- QUESTION LOGIC ----------------
    void generateQuestion() {

        while (true) {
            a = rand.nextInt(r1) + 1;
            b = rand.nextInt(r2) + 1;

            if (allowNegative) {
                if (rand.nextBoolean())
                    a = -a;
                if (rand.nextBoolean())
                    b = -b;
            }

            op = operations[rand.nextInt(operations.length)];

            switch (op) {
                case '+':
                    result = a + b;
                    break;

                case '-':
                    if (!allowNegative && a < b)
                        continue;
                    result = a - b;
                    break;

                case 'x':
                    result = a * b;
                    break;

                case '/':
                    if (b == 0 || a % b != 0)
                        continue;
                    result = a / b;
                    break;
            }

            break;
        }

        questionLabel.setText(a + " " + op + " " + b + " = ");
    }

    public static void main(String[] args) {
        new Zetamac();
    }
}