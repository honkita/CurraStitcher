import javax.swing.*;
import java.util.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.*;

/**
 * 
 */
public class CurraStitcherUI extends JFrame {

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // Colours for the UI
    private final Color darkBlue = Color.decode("0x272443"); // Dark Blue
    private final Color gold = Color.decode("0xe9b074"); // Yellow/Gold
    private final Color red = Color.decode("0xa9253d"); // Red
    private final Color white = Color.decode("0xffffff"); // White
    private final Color brown = Color.decode("0x562D2B"); // Brown

    private final JLabel projectNameLabel = new JLabel("Project Folder");
    private final JLabel projectName = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JLabel pixelColourLabel = new JLabel("Pixel Colours (HEX)");
    private final JLabel DMCColourLabel = new JLabel("DMC Colours");
    private final JLabel DMCColourHexLabel = new JLabel();
    private final JLabel DMCCurrentColourLabel = new JLabel("Current Thread Colour");
    private final JLabel presetDMCColourLabel = new JLabel();
    private final JLabel presetDMCColourHexLabel = new JLabel();

    private final JPanel coloursPanel = new JPanel();
    private final JPanel buttonsPanel = new JPanel();
    private final JPanel pixelColourPanel = new JPanel();
    private final JPanel DMCColourPanel = new JPanel();
    private final JPanel presetDMCColourPanel = new JPanel();

    private final JButton directoryButton = new JButton(new ImageIcon("./Images/folders.png"));
    private final JButton openFolderButton = new JButton("Open Folder");
    private final JButton generateJSONButton = new JButton("Generate JSON");
    private final JButton generatePatternButton = new JButton("Generate Pattern");
    private final JButton changeDMCColourButton = new JButton("Change Colour");
    private final JComboBox<String> coloursMenu = new JComboBox<>();
    private final JComboBox<String> DMCColoursMenu = new JComboBox<>();

    private final Crossstitch crossstitch = new Crossstitch();

    private JSONObject presets = null;

    private final int BORDER = screenSize.width / 64;

    private final int width = screenSize.width / 8 * 5;
    private final int leftHalf = (width - 3 * BORDER) / 3 * 2;
    private final int rightHalf = (width - 3 * BORDER) / 3;
    private final int height = screenSize.width / 2;
    private final int fullHeight = (int) (height * 1.0 - BORDER * 3.0);
    private final int halfHeight = (int) (height - 5.0 * BORDER) / 2;

    HashMap<String, String> DMCColours = new HashMap<String, String>();

    String folderRegex = "/./";
    String replace = "/";

    public CurraStitcherUI() {
        // Set up the JFrame
        setTitle("CurraStitcher");
        setSize(width, height);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(darkBlue);

        setResizable(false);

        setLayout(null);
        setVisible(true);

        buttonActions();
        repaint();
    }

    /**
     * Paints the components
     */
    public void paint(Graphics g) {
        super.paint(g);

        setLabel(projectNameLabel, white, BORDER, BORDER, width / 16 * 7, BORDER * 2);
        setLabel(projectName, white, BORDER, BORDER * 3, leftHalf - BORDER * 2, BORDER * 2);
        projectName.setBackground(red);
        projectName.setOpaque(true);
        projectName.setBorder(BorderFactory.createLineBorder(brown, 2));

        setButton(directoryButton, white, gold, leftHalf - BORDER, BORDER * 3, BORDER * 2, BORDER * 2);

        // setPanelEnabled(buttonsPanel, enabled);
        setPanel(buttonsPanel, white, BORDER, BORDER * 6, leftHalf, halfHeight);

        setButton(openFolderButton, white, gold, BORDER, BORDER, 150, BORDER * 2);
        setButton(generateJSONButton, white, gold, BORDER, BORDER * 7 / 2, 150, BORDER * 2);
        setButton(generatePatternButton, white, gold, BORDER, BORDER * 6, 150, BORDER * 2);

        setLabel(statusLabel, white, BORDER, BORDER * 8 + halfHeight, leftHalf, BORDER * 2);
        statusLabel.setBackground(red);
        statusLabel.setOpaque(true);

        setPanel(coloursPanel, red, leftHalf + 2 * BORDER, BORDER, rightHalf, fullHeight);

        setLabel(pixelColourLabel, white, BORDER, BORDER, rightHalf - 2 * BORDER, BORDER);
        setComboBox(coloursMenu, white, gold, gold, BORDER, BORDER * 3, rightHalf - 2 * BORDER, BORDER);
        setPanel(pixelColourPanel, null, BORDER, BORDER * 5, rightHalf - 2 * BORDER, BORDER * 2);
        pixelColourPanel.setBorder(BorderFactory.createLineBorder(gold, 2));

        setLabel(DMCColourLabel, white, BORDER, BORDER * 8, rightHalf - 2 * BORDER, BORDER);
        setComboBox(DMCColoursMenu, white, gold, gold, BORDER, BORDER * 10, rightHalf - 2 * BORDER, BORDER);
        setPanel(DMCColourPanel, null, BORDER, BORDER * 12, rightHalf - 2 * BORDER, BORDER * 2);
        setLabel(DMCColourHexLabel, white, BORDER, BORDER * 15, rightHalf - 2 * BORDER, BORDER);
        DMCColourHexLabel.setBackground(gold);
        DMCColourHexLabel.setOpaque(true);
        DMCColourPanel.setBorder(BorderFactory.createLineBorder(gold, 2));
        if (DMCColoursMenu.getSelectedItem() != null) {
            String hexColour = DMCColours.get(DMCColoursMenu.getSelectedItem().toString());
            DMCColourHexLabel.setText(hexColour);
            DMCColourPanel.setBackground(hextoColor(hexColour));
        }

        setLabel(DMCCurrentColourLabel, white, BORDER, BORDER * 18, rightHalf - 2 * BORDER, BORDER);
        setLabel(presetDMCColourLabel, white, BORDER, BORDER * 20, rightHalf - 2 * BORDER, BORDER);
        presetDMCColourLabel.setBackground(gold);
        presetDMCColourLabel.setOpaque(true);
        setPanel(presetDMCColourPanel, null, BORDER, BORDER * 22, rightHalf - 2 * BORDER, BORDER * 2);
        setLabel(presetDMCColourHexLabel, white, BORDER, BORDER * 25, rightHalf - 2 * BORDER, BORDER);
        presetDMCColourHexLabel.setBackground(gold);
        presetDMCColourHexLabel.setOpaque(true);

        // Checks if the colour menu is altered and will update if there is a colour set
        if (coloursMenu.getSelectedItem() != null) {
            pixelColourPanel.setBackground(hextoColor(coloursMenu.getSelectedItem().toString()));
            try {
                String colour = presets.getString(coloursMenu.getSelectedItem().toString());
                String hexColour = DMCColours.get(colour);
                presetDMCColourLabel.setText(colour);
                presetDMCColourPanel.setOpaque(true);
                presetDMCColourPanel.setBackground(hextoColor(hexColour));
                presetDMCColourHexLabel.setText(hexColour);
                statusLabel.setText("Thread colour changed successfully");
            } catch (Exception e) {
                presetDMCColourLabel.setText("No Colour");
                presetDMCColourPanel.setOpaque(false);
                statusLabel.setText(e.getMessage());
            }
        }

        setButton(changeDMCColourButton, white, gold, BORDER, fullHeight - BORDER * 3, rightHalf - 2 * BORDER,
                BORDER * 2);

        presetDMCColourPanel.setBorder(BorderFactory.createLineBorder(gold, 2));

        // Add components to the JFrame or respective JPanels
        add(projectNameLabel);
        add(projectName);
        add(directoryButton);
        add(statusLabel);
        buttonsPanel.add(openFolderButton);
        buttonsPanel.add(generateJSONButton);
        buttonsPanel.add(generatePatternButton);
        add(buttonsPanel);
        coloursPanel.add(pixelColourLabel);
        coloursPanel.add(coloursMenu);
        coloursPanel.add(pixelColourPanel);
        coloursPanel.add(DMCColourLabel);
        coloursPanel.add(DMCColoursMenu);
        coloursPanel.add(DMCColourPanel);
        coloursPanel.add(DMCColourHexLabel);
        coloursPanel.add(DMCCurrentColourLabel);
        coloursPanel.add(presetDMCColourLabel);
        coloursPanel.add(presetDMCColourPanel);
        coloursPanel.add(changeDMCColourButton);
        coloursPanel.add(presetDMCColourHexLabel);
        add(coloursPanel);
    }

    /**
     * Sets JButton
     * 
     * @param button
     * @param fg
     * @param bg
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void setButton(JButton button, Color fg, Color bg, int x, int y, int w, int h) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFocusable(false);
        button.setForeground(white);
        button.setBackground(bg);
        button.setOpaque(true);
        button.setBounds(x, y, w, h);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bg.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });
    }

    /**
     * Sets JLabel
     * 
     * @param label
     * @param fg
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void setLabel(JLabel label, Color fg, int x, int y, int w, int h) {
        label.setForeground(white);
        label.setBounds(x, y, w, h);
    }

    /**
     * Sets JPanel
     * 
     * @param panel
     * @param fg
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void setPanel(JPanel panel, Color fg, int x, int y, int w, int h) {
        panel.setBackground(fg);
        panel.setLayout(null);
        panel.setBounds(x, y, w, h);
    }

    /**
     * Sets JComboBox<String>
     * 
     * @param comboBox
     * @param fg
     * @param bg
     * @param gold
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void setComboBox(JComboBox<String> comboBox, Color fg, Color bg, Color gold, int x, int y, int w, int h) {
        comboBox.setBackground(bg);
        comboBox.setForeground(fg);
        comboBox.setVisible(true);
        comboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXX");
        comboBox.setEditable(false);
        comboBox.setUI(ComboBoxUI.createUI(coloursMenu, gold));
        comboBox.setBounds(x, y, w, h);
        comboBox.getEditor().getEditorComponent().setBackground(gold);

        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                repaint();
            }
        });
    }

    // private void setPanelEnabled(JPanel panel, Boolean isEnabled) {
    // panel.setEnabled(isEnabled);

    // Component[] components = panel.getComponents();

    // for (Component component : components) {
    // System.out.println(component.getName());
    // // if (component instanceof JPanel) {
    // // setPanelEnabled((JPanel) component, isEnabled);
    // // }
    // component.setEnabled(isEnabled);
    // }
    // }

    /**
     * Converts hex code to a colour by removing the # and adding a "0x"
     * 
     * @param hex
     * @return
     */
    private Color hextoColor(String hex) {
        hex = hex.toLowerCase().trim().replace("#", "0x");
        return Color.decode(hex);
    }

    /**
     * 
     */
    private void getColours() {
        // Load colours from JSON
        File coloursFile = new File(projectName.getText() + "/colours.json");
        if (coloursFile.exists()) {
            try {
                JSONObject jsonObject = new JSONObject(new JSONTokener(new FileReader(coloursFile)));
                Iterator<?> keys = jsonObject.keys();
                coloursMenu.removeAllItems();
                while (keys.hasNext()) {
                    coloursMenu.addItem((String) keys.next());
                }
            } catch (Exception e1) {
                statusLabel.setText(e1.getMessage());
            }
        }
        if (DMCColoursMenu.getItemCount() == 0) {
            try (BufferedReader br = new BufferedReader(new FileReader("DMC.csv"))) {
                String line;
                br.readLine(); // skip the first line
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    DMCColours.put("DMC " + values[0], values[1].trim().toLowerCase());
                    DMCColoursMenu.addItem("DMC " + values[0]);
                }
            } catch (Exception e) {
                statusLabel.setText(e.getMessage());
            }
        }
    }

    private void checkJSON() throws JSONException, FileNotFoundException {
        presets = new JSONObject(new JSONTokener(new FileReader(crossstitch.returnJSON())));
    }

    private void buttonActions() {
        changeDMCColourButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (coloursMenu.getSelectedItem() != null && DMCColoursMenu.getSelectedItem() != null) {
                    try {
                        checkJSON();
                        presets.put(coloursMenu.getSelectedItem().toString().toLowerCase(),
                                DMCColoursMenu.getSelectedItem().toString());
                        FileWriter rewrite = new FileWriter(crossstitch.returnJSON());
                        rewrite.flush();
                        rewrite.write(presets.toString(3));
                        rewrite.close();
                        repaint();
                    } catch (JSONException | IOException e1) {
                        statusLabel.setText(e1.getMessage());
                    }
                } else {
                    statusLabel.setText("Colour and/or DMC colour not selected");
                }

            }

        });

        directoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new java.io.File("."));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    try {
                        setFolderName(selectedDirectory.getAbsolutePath().replace(folderRegex, replace));
                        checkJSON();
                    } catch (Exception e1) {
                        System.out.println(e1.getMessage());
                    }
                    getColours();
                    repaint();
                }
            }

        });

        generatePatternButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    crossstitch.generateImage();
                } catch (Exception e1) {
                    statusLabel.setText(e1.getMessage());
                }
            }
        });

        generateJSONButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = new File(projectName.getText() + "/colours.json");

                // generate a new JSON with all the pixel colours from the image
                if (!f.exists()) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        HashMap<String, Color> colours = crossstitch.returnColours();
                        for (String name : colours.keySet()) {
                            jsonObject.put(name.toString(), "");
                        }
                        FileWriter file = new FileWriter(projectName.getText() + "/colours.json");
                        file.write(jsonObject.toString(3));
                        file.close();
                        getColours(); // add the colours to the dropdown
                        checkJSON();

                    } catch (Exception e1) {
                        statusLabel.setText(e1.getMessage());
                    }
                }
            }
        });

        openFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!projectName.getText().isEmpty() && new File(projectName.getText()).exists()) {
                    setFolderName(projectName.getText());
                    JFileChooser fileChooser = new JFileChooser(projectName.getText());
                    fileChooser.showOpenDialog(null);
                } else {
                    System.out.println("NOTHING");
                }
            }
        });
    }

    /**
     * 
     * @param folderName
     */
    private void setFolderName(String folderName) {
        projectName.setText(folderName);
        try {
            crossstitch.setFolderName(folderName);
        } catch (IOException e) {
            statusLabel.setText(e.getMessage());

        }
    }
}