import javax.swing.*;
import java.awt.*;

public class Factory {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            MyFrame frame = new MyFrame();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        });
    }

}

