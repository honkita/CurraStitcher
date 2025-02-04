import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.imageio.ImageIO;
import org.json.*;

public class Crossstitch {

    private final HashMap<String, Color> colours = new HashMap<String, Color>();
    private final Color FRAME_COLOR = new Color(100, 100, 100, 255);
    private final int RATIO = 15;
    private String folderName = "";
    private final HashMap<String, String> files = new HashMap<>();

    public void setFolderName(String name) {
        folderName = name;
        files.put("Original", folderName + "/Original.png");
        files.put("Colours", folderName + "/colours.json");
    }

    private final Font symbol = new Font("Roboto", Font.BOLD, 15);
    private final Font symbolLarge = new Font("Roboto", Font.BOLD, 20);

    protected HashMap<String, Color> returnColours() {
        return colours;
    }

    private int[][] rowScroller(int[][] data, BufferedImage im, int y, int y2, boolean borderLayer, int[] pix,
            int newPatternX) {
        for (int x = 0; x < im.getWidth(); x++) {
            for (int x2 = 0; x2 < RATIO + 1; x2++) {
                int currentPixelY = y * (RATIO + 1) + y2;
                int currentPixelX = x * (RATIO + 1) + x2;
                if (borderLayer) {
                    data[currentPixelY][currentPixelX] = FRAME_COLOR.getRGB();
                } else {
                    if (x2 == 0) {
                        Color value = new Color(pix[y * im.getWidth() + x]);
                        String hex = String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue());
                        if (!colours.containsKey(hex)) {
                            colours.put(hex, value);
                        }
                        data[currentPixelY][currentPixelX] = FRAME_COLOR.getRGB();
                    } else {
                        data[currentPixelY][currentPixelX] = pix[y * im.getWidth() + x];
                    }
                }
            }
            data[y * (RATIO + 1) + y2][newPatternX - 1] = FRAME_COLOR.getRGB();
        }
        return data;
    }

    private int[][] bottomBorder(int[][] data, int highest, int y, int startX) {
        for (int x = 0; x < highest; x++) {
            data[y][x + startX] = FRAME_COLOR.getRGB();
        }
        return data;
    }

    private String toExcel(int num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            num--;
            sb.insert(0, (char) ('A' + num % 26));
            num /= 26;
        }
        return sb.toString();
    }

    private int[][] scroller(BufferedImage im, int[] pix, int newPatternY, int newPatternX) {
        int[][] data = new int[newPatternY][newPatternX * 3 / 2];
        for (int y = 0; y < im.getHeight(); y++) {
            for (int y2 = 0; y2 < RATIO + 1; y2++) {
                if (y2 == 0) {
                    data = rowScroller(data, im, y, y2, true, pix, newPatternX);
                } else {
                    data = rowScroller(data, im, y, y2, false, pix, newPatternX);
                }
            }
        }
        data = bottomBorder(data, newPatternX, newPatternY - 1, 0);
        return data;
    }

    public void generateImage(boolean image) throws IOException {
        if (!folderName.isEmpty()) {
            colours.clear();
            BufferedImage im = ImageIO.read(Paths.get(files.get("Original")).toFile());
            int[] pix = im.getRGB(0, 0, im.getWidth(), im.getHeight(), null, 0, im.getWidth());
            int newPatternY = im.getHeight() * (RATIO + 1) + 1;
            int newPatternX = im.getWidth() * (RATIO + 1) + 1;

            int[][] data = scroller(im, pix, newPatternY, newPatternX);
            String[] coloursKeys = colours.keySet().toArray(new String[colours.size()]);

            if (image) {
                int legendDim = RATIO * 2 + 2;
                for (int i = 0; i < colours.size(); i++) {
                    for (int y = 0; y < legendDim; y++) {
                        for (int x = 0; x < legendDim * 8; x++) {
                            if (x == 0 || y == 0 || x == legendDim * 4 - 1 || x == legendDim * 8 - 1) {
                                data[i * legendDim + y][newPatternX + x + legendDim] = FRAME_COLOR.getRGB();
                            } else {
                                data[i * legendDim + y][newPatternX + x + legendDim] = colours.get(coloursKeys[i])
                                        .getRGB();
                            }
                        }
                        data[i * legendDim + y][newPatternX + 2 * legendDim - 1] = FRAME_COLOR.getRGB();
                    }
                }
                data = bottomBorder(data, legendDim * 8, legendDim * colours.size(), newPatternX + legendDim);

                BufferedImage img = new BufferedImage(data[0].length, data.length, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < data.length; y++) {
                    for (int x = 0; x < data[y].length; x++) {
                        img.setRGB(x, y, data[y][x]);
                    }
                }

                // Draws the legend
                Graphics2D draw = img.createGraphics();
                for (int i = 0; i < colours.size(); i++) {
                    String label = toExcel(i + 1);
                    Color color = colours.get(coloursKeys[i]);
                    int r = color.getRed();
                    int g = color.getGreen();
                    int b = color.getBlue();
                    Color textColor = (r + g + b) / 3 >= 125 ? Color.BLACK : Color.WHITE;
                    draw.setFont(symbolLarge);
                    draw.setColor(textColor);
                    draw.drawString(label, newPatternX + legendDim, i * legendDim + legendDim / 2);
                    draw.drawString(coloursKeys[i], newPatternX + legendDim * 2, i * legendDim + legendDim / 2);

                    File colourFile = Paths.get(files.get("Colours")).toFile();
                    if (colourFile.exists()) {
                        try {

                            JSONObject json = new JSONObject(new JSONTokener(new FileReader(colourFile)));
                            if (json.has(coloursKeys[i])) {
                                draw.drawString(json.getString(coloursKeys[i]), newPatternX + legendDim * 5,
                                        i * legendDim + legendDim / 2);
                            }
                        } catch (FileNotFoundException | JSONException e) {
                            System.out.println("UWU");
                        }

                    }
                }

                for (int y = 0; y < im.getHeight(); y++) {
                    for (int x = 0; x < im.getWidth(); x++) {
                        Color pixelColor = new Color(pix[y * im.getWidth() + x]);
                        String hex = String.format("#%02x%02x%02x", pixelColor.getRed(), pixelColor.getGreen(),
                                pixelColor.getBlue());
                        String label = toExcel(java.util.Arrays.asList(coloursKeys).indexOf(hex) + 1);
                        draw.setFont(symbol);
                        draw.setColor((pixelColor.getRed() + pixelColor.getGreen() + pixelColor.getBlue()) / 3 >= 125
                                ? Color.BLACK
                                : Color.WHITE);
                        draw.drawString(label, (int) ((x + 0.25) * (RATIO + 1)), (y + 1) * (RATIO + 1));
                    }
                }
                ImageIO.write(img, "png", new File(folderName + "/pattern.png"));
            }
        }
    }

}
