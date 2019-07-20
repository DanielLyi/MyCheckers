import javax.swing.*;
import java.awt.*;

public class Start {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            TheFrame frame = new TheFrame();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        });
    }

}

