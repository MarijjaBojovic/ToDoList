
import javax.swing.*;

public class Main {
    public static  void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TidyTaskFrame frame = new TidyTaskFrame();
            frame.setVisible(true);
        });
    }

}