import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGeneratorAWT extends Frame implements ActionListener, ItemListener {
    private final TextField tfLength = new TextField("12", 5);
    private final Checkbox cbLower  = new Checkbox("a-z", true);
    private final Checkbox cbUpper  = new Checkbox("A-Z", true);
    private final Checkbox cbDigits = new Checkbox("0-9", true);
    private final Checkbox cbSymbols= new Checkbox("Symbols (!@#$…)", true);
    private final Button btnGenerate = new Button("Generate");
    private final Button btnCopy     = new Button("Copy");
    private final TextField tfOutput = new TextField("", 30);
    private final Label status = new Label(" ");
    private final Checkbox cbFullscreen = new Checkbox("Fullscreen", false); // ✅ added

    private final SecureRandom rng = new SecureRandom();

    public PasswordGeneratorAWT() {
        super("Password Generator (AWT)");
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Length
        c.gridx = 0; c.gridy = 0; add(new Label("Length:"), c);
        c.gridx = 1; c.gridy = 0; add(tfLength, c);

        // Row 1: Options
        Panel options = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        options.add(cbLower); options.add(cbUpper); options.add(cbDigits); options.add(cbSymbols);
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; add(options, c);

        // Row 2: Buttons
        Panel buttons = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(btnGenerate); buttons.add(btnCopy);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; add(buttons, c);

        // Row 3: Output
        tfOutput.setEditable(false);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; add(tfOutput, c);

        // Row 4: Status
        status.setForeground(Color.DARK_GRAY);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; add(status, c);

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; add(cbFullscreen, c);

        // Listeners
        btnGenerate.addActionListener(this);
        btnCopy.addActionListener(this);
        cbLower.addItemListener(this);
        cbUpper.addItemListener(this);
        cbDigits.addItemListener(this);
        cbSymbols.addItemListener(this);
        cbFullscreen.addItemListener(this); // ✅ listens fullscreen toggle

        // Window close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { dispose(); System.exit(0); }
        });

        setResizable(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        status.setText(" ");
        if (src == btnGenerate) {
            generateAndShow();
        } else if (src == btnCopy) {
            copyToClipboard();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        if (src == cbFullscreen) {
            if (cbFullscreen.getState()) {
                // Go fullscreen
                dispose(); // required when changing undecorated
                setUndecorated(true);
                setExtendedState(Frame.MAXIMIZED_BOTH);
                setVisible(true);
            } else {
                // Back to windowed
                dispose();
                setUndecorated(false);
                setExtendedState(Frame.NORMAL);
                setSize(600, 400);
                setLocationRelativeTo(null);
                setVisible(true);
            }
        }
    }

    private void generateAndShow() {
        try {
            int length = Integer.parseInt(tfLength.getText().trim());
            if (length < 4 || length > 256) {
                status.setText("Choose a length between 4 and 256.");
                return;
            }

            String lower = "abcdefghijklmnopqrstuvwxyz";
            String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String digits = "0123456789";
            String symbols = "!@#$%^&*()-_=+[]{};:,.?/";

            List<char[]> sets = new ArrayList<>();
            StringBuilder pool = new StringBuilder();

            if (cbLower.getState()) { sets.add(lower.toCharArray()); pool.append(lower); }
            if (cbUpper.getState()) { sets.add(upper.toCharArray()); pool.append(upper); }
            if (cbDigits.getState()) { sets.add(digits.toCharArray()); pool.append(digits); }
            if (cbSymbols.getState()) { sets.add(symbols.toCharArray()); pool.append(symbols); }

            if (sets.isEmpty()) {
                status.setText("Select at least one character set.");
                return;
            }

            // Build password ensuring at least one from each selected set
            List<Character> chars = new ArrayList<>(length);

            // 1) Ensure coverage
            for (char[] set : sets) {
                chars.add(randomChar(set));
            }

            // 2) Fill remaining from the whole pool
            char[] poolArr = pool.toString().toCharArray();
            while (chars.size() < length) {
                chars.add(randomChar(poolArr));
            }

            // 3) Shuffle for randomness
            Collections.shuffle(chars, rng);

            // 4) Emit
            StringBuilder out = new StringBuilder(length);
            for (char ch : chars) out.append(ch);
            tfOutput.setText(out.toString());
            status.setText("Generated.");
        } catch (NumberFormatException ex) {
            status.setText("Length must be a number.");
        }
    }

    private char randomChar(char[] set) {
        return set[rng.nextInt(set.length)];
    }

    private void copyToClipboard() {
        String text = tfOutput.getText();
        if (text == null || text.isEmpty()) {
            status.setText("Nothing to copy. Generate first.");
            return;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            status.setText("Copied to clipboard.");
        } catch (Exception ex) {
            status.setText("Copy failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // Optional: better text rendering on some platforms
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        new PasswordGeneratorAWT();
    }
}
