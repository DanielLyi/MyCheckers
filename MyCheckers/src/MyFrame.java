import javax.swing.*;
import java.awt.*;

public class MyFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 1000;
    private MyComponent component;
    private int redWins;
    private int blackWins;
    MyFrame(){
        this(DEFAULT_WIDTH,DEFAULT_HEIGHT);
    }

    private MyFrame(int width, int height){
        super.setTitle("Checkers");
        super.setIconImage(new ImageIcon("res/images/red_piece.png").getImage());
        setSize(width,height);
        component = new MyComponent(this);
        add(component);
        component.repaint();
        blackWins=0;
        redWins=0;
        setScore();

    }

    void refreshComponent(){
        component.setVisible(false);
        component = new MyComponent(this);
        add(component);
        component.setVisible(true);
        component.repaint();
        setScore();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT);
    }

    void setBlackWins(int blackWins) {
        this.blackWins = blackWins;
    }

    void setRedWins(int redWins) {
        this.redWins = redWins;
    }

    int getBlackWins() {
        return blackWins;
    }

    int getRedWins() {
        return redWins;
    }

    void setScore(){
        this.setTitle("Red - Black  " + getRedWins() + " : " + getBlackWins());
    }
}
