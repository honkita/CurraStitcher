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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.*;

public class CurraStitcherUI extends JFrame {

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // Colours for the UI
    private final Color bg0 = Color.decode("0x272443"); // Dark Blue
    private final Color bg1 = Color.decode("0xe9b074"); // Yellow/Gold
    private final Color bg2 = Color.decode("0xa9253d"); // Red
    private Color fg0 = Color.decode("0xffffff"); // White
    private Color borderColor = Color.decode("0x562D2B"); // Brown

    private final JLabel projectNameLabel = new JLabel("Project Folder");
    private final JLabel projectName = new JLabel();
    private final JLabel pixelColourLabel = new JLabel("Pixel Colours (HEX)");
    private final JLabel DMCColourLabel = new JLabel("DMC Colours");
    private final JLabel DMCColourHexLabel = new JLabel();
    private final JLabel DMCCurrentColourLabel = new JLabel("Current Thread Colour");

    private final JPanel coloursPanel = new JPanel();
    private final JPanel buttonsPanel = new JPanel();
    private final JPanel pixelColourPanel = new JPanel();
    private final JPanel DMCColourPanel = new JPanel();

    private final JButton directoryButton = new JButton(new ImageIcon("./Images/folders.png"));
    private final JButton openFolderButton = new JButton("Open Folder");
    private final JButton generateJSONButton = new JButton("Generate JSON");
    private final JButton generatePatternButton = new JButton("Generate Pattern");
    private final JComboBox<String> coloursMenu = new JComboBox<>();
    private final JComboBox<String> DMCColoursMenu = new JComboBox<>();

    private final Crossstitch crossstitch = new Crossstitch();

    private final int BORDER = screenSize.width / 64;

    final int width = screenSize.width / 8 * 5;
    final int leftHalf = (width - 3 * BORDER) / 3 * 2;
    final int rightHalf = (width - 3 * BORDER) / 3;
    final int height = screenSize.width / 2;
    final int fullHeight = (int) (height * 1.0 - BORDER * 3.0);
    final int halfHeight = (int) (height - 5.0 * BORDER) / 2;
    final int unit = width / 8;

    boolean enabled = false;

    HashMap<String, String> DMCColours = new HashMap<String, String>();

    String folderRegex = "/./";
    String replace = "/";

    public CurraStitcherUI() {
        // Set up the JFrame
        setTitle("CurraStitcher");
        setSize(width, height);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(bg0);

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

        setLabel(projectNameLabel, fg0, BORDER, BORDER, width / 16 * 7, BORDER * 2);
        setLabel(projectName, fg0, BORDER, BORDER * 3, leftHalf - BORDER * 2, BORDER * 2);
        projectName.setBackground(bg2);
        projectName.setOpaque(true);
        projectName.setBorder(BorderFactory.createLineBorder(borderColor, 2));

        setButton(directoryButton, fg0, bg1, leftHalf - BORDER, BORDER * 3, BORDER * 2, BORDER * 2);

        // setPanelEnabled(buttonsPanel, enabled);
        setPanel(buttonsPanel, fg0, BORDER, (height + BORDER) / 2, leftHalf, halfHeight);

        setButton(openFolderButton, fg0, bg1, BORDER, BORDER, 150, BORDER * 2);
        setButton(generateJSONButton, fg0, bg1, BORDER, BORDER * 7 / 2, 150, BORDER * 2);
        setButton(generatePatternButton, fg0, bg1, BORDER, BORDER * 6, 150, BORDER * 2);

        setPanel(coloursPanel, bg2, leftHalf + 2 * BORDER, BORDER, rightHalf, fullHeight);

        setLabel(pixelColourLabel, fg0, BORDER, BORDER, rightHalf - 2 * BORDER, BORDER);
        setComboBox(coloursMenu, fg0, bg1, bg1, BORDER, BORDER * 3, rightHalf - 2 * BORDER, BORDER);
        setPanel(pixelColourPanel, null, BORDER, BORDER * 5, rightHalf - 2 * BORDER, BORDER * 2);
        pixelColourPanel.setBorder(BorderFactory.createLineBorder(bg1, 2));
        if (coloursMenu.getSelectedItem() != null)
            pixelColourPanel.setBackground(hextoColor(coloursMenu.getSelectedItem().toString()));

        setLabel(DMCColourLabel, fg0, BORDER, BORDER * 8, rightHalf - 2 * BORDER, BORDER);
        setComboBox(DMCColoursMenu, fg0, bg1, bg1, BORDER, BORDER * 10, rightHalf - 2 * BORDER, BORDER);
        setPanel(DMCColourPanel, null, BORDER, BORDER * 12, rightHalf - 2 * BORDER, BORDER * 2);
        setLabel(DMCColourHexLabel, fg0, BORDER, BORDER * 14, rightHalf - 2 * BORDER, BORDER);
        DMCColourPanel.setBorder(BorderFactory.createLineBorder(bg1, 2));
        if (DMCColoursMenu.getSelectedItem() != null) {
            String hexColour = DMCColours.get(DMCColoursMenu.getSelectedItem().toString());
            DMCColourHexLabel.setText(hexColour);
            DMCColourPanel.setBackground(hextoColor(hexColour));
        }

        setLabel(DMCCurrentColourLabel, fg0, BORDER, BORDER * 16, rightHalf - 2 * BORDER, BORDER);

        // Add components to the JFrame or respective JPanels
        add(projectNameLabel);
        add(projectName);
        add(directoryButton);
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
        button.setForeground(fg0);
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
        label.setForeground(fg0);
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
     * @param bg1
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void setComboBox(JComboBox<String> comboBox, Color fg, Color bg, Color bg1, int x, int y, int w, int h) {
        comboBox.setBackground(bg);
        comboBox.setForeground(fg);
        comboBox.setVisible(true);
        comboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXX");
        comboBox.setEditable(false);
        comboBox.setUI(ComboBoxUI.createUI(coloursMenu, bg1));
        comboBox.setBounds(x, y, w, h);

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

    private Color hextoColor(String hex) {
        hex = hex.toLowerCase().trim().replace("#", "0x");
        return Color.decode(hex);
    }

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
                e1.printStackTrace();
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

            }
        }
    }

    private void buttonActions() {
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
                    crossstitch.generateImage(true);
                } catch (IOException e1) {
                    e1.printStackTrace();
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
                        crossstitch.generateImage(false);
                        HashMap<String, Color> colours = crossstitch.returnColours();
                        for (String name : colours.keySet()) {
                            jsonObject.put(name.toString(), "");
                        }
                        FileWriter file = new FileWriter(projectName.getText() + "/colours.json");
                        file.write(jsonObject.toString(3));
                        file.close();
                        getColours(); // add the colours to the dropdown

                    } catch (Exception e1) {
                        e1.printStackTrace();
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

    private void setFolderName(String folderName) {
        // Implement set_folder_name logic
        projectName.setText(folderName);
        crossstitch.setFolderName(folderName);
    }
}