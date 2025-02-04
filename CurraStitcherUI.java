import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Iterator;
import org.json.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CurraStitcherUI extends JFrame {

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private Color bg0 = Color.decode("0x272443");
    private Color bg1 = Color.decode("0xE9B074");
    private Color bg2 = Color.decode("0xA9253D");
    private Color fg0 = Color.decode("0xFFFFFF");
    private Color borderColor = Color.decode("0x562D2B");

    private JLabel projectNameLabel = new JLabel("Project Folder");
    private JLabel projectName = new JLabel();

    private JPanel coloursPanel = new JPanel();

    private JButton directoryButton = new JButton(new ImageIcon("./Images/folders.png"));
    private JButton openFolderButton = new JButton("Open Folder");
    private JButton generateJSONButton = new JButton("Generate JSON");
    private JButton generatePatternButton = new JButton("Generate Pattern");
    private JComboBox<String> coloursMenu = new JComboBox<>();

    private final Crossstitch crossstitch = new Crossstitch();

    private final int BORDER = screenSize.width / 64;

    final int width = screenSize.width / 8 * 5;
    final int halfWidth = (width - 3 * BORDER) / 2;
    final int height = screenSize.width / 8 * 3;
    final int fullHeight = (int) (height * 1.0 - BORDER * 3.0);

    final int unit = width / 8;

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

        projectNameLabel.setForeground(fg0);
        projectNameLabel.setBounds(BORDER, BORDER, width / 16 * 7, BORDER * 2);

        projectName.setForeground(fg0);
        projectName.setBackground(bg2);
        projectName.setOpaque(true);
        projectName.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        projectName.setBounds(BORDER, BORDER * 3, halfWidth, BORDER * 2);

        setButton(directoryButton, fg0, bg1, BORDER, BORDER * 5, BORDER * 2, BORDER * 2);
        setButton(openFolderButton, fg0, bg1, BORDER, 250, 150, 32);
        setButton(generateJSONButton, fg0, bg1, BORDER, 300, 150, 32);
        setButton(generatePatternButton, fg0, bg1, BORDER, 350, 150, 32);

        coloursPanel.setBounds((int) (width / 2.0 + BORDER / 2.0), BORDER, halfWidth,
                fullHeight);

        coloursPanel.setBackground(bg2);

        coloursMenu.setBackground(bg1);
        coloursMenu.setForeground(fg0);
        coloursMenu.setVisible(true);

        coloursMenu.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXX");
        coloursMenu.setEditable(false);
        coloursMenu.setUI(ComboBoxUI.createUI(coloursMenu, bg2));

        coloursMenu.setBounds(0, 50, 128, 32);

        // Add components to the JFrame
        add(projectNameLabel);
        add(projectName);
        add(directoryButton);
        add(openFolderButton);
        add(generateJSONButton);
        add(generatePatternButton);

        coloursPanel.add(coloursMenu);
        add(coloursPanel);
    }

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
                        // Path p =
                        // Paths.get(fileChooser.getCurrentDirectory().getCanonicalPath()).getParent()
                        // .relativize(Paths.get(selectedDirectory.getCanonicalPath()));
                        // projectName.setText("./" + p);
                        // setFolderName("./" + p);
                        setFolderName(selectedDirectory.getAbsolutePath().replace(folderRegex, replace));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    // Load colours from JSON
                    File coloursFile = new File(selectedDirectory.getAbsolutePath() + "/colours.json");
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
                System.out.println("JSONS Function");
                System.out.println(projectName.getText());
                File f = new File(projectName.getText() + "/colours.json");
                JSONObject jsonObject = new JSONObject();
                if (f.exists()) {
                    System.out.println("UWU");
                } else {
                    try {
                        crossstitch.generateImage(false);
                        HashMap<String, Color> colours = crossstitch.returnColours();
                        for (String name : colours.keySet()) {
                            String key = name.toString();
                            System.out.println(key);
                            jsonObject.put(key, "");
                        }
                        FileWriter file = new FileWriter(projectName.getText() + "/colours.json");
                        file.write(jsonObject.toString(3));
                        file.close();
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
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

    public static void main(String[] args) {
        new CurraStitcher();
    }
}