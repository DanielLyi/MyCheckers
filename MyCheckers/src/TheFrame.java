import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class TheFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 1000;
    private TheBoard board;
    private int redWins;
    private int blackWins;

    TheFrame() {
        this(DEFAULT_WIDTH,DEFAULT_HEIGHT);
    }

    private TheFrame(int width, int height) {
        super.setTitle("Checkers");
        super.setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(
                "images/red_piece_king.png"))).getImage());
        setSize(width,height);
        board = new TheBoard(this);
        add(board);
        board.repaint();
        blackWins=0;
        redWins=0;
        setScore();

    }

    void refreshComponent(){
        board.setVisible(false);
        board = new TheBoard(this);
        add(board);
        board.setVisible(true);
        board.repaint();
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
