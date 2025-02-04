import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

/**
 *
 * @author elite
 */
public class ComboBoxUI extends BasicComboBoxUI {

    static Color co;

    public static ComboBoxUI createUI(JComponent c, Color color) {
        co = color;
        return new ComboBoxUI();
    }

    @Override
    protected JButton createArrowButton() {
        return new BasicArrowButton(
                BasicArrowButton.SOUTH,
                co, Color.WHITE,
                Color.WHITE, Color.WHITE);
    }
}
