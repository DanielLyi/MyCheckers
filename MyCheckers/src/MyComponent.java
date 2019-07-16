import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class MyComponent extends JComponent {

    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 750;
    private static final int SIDE_LENGTH = 80;
    private static final int MARG_FROM_EDGE = 80;
    private Rectangle2D.Double[][] squares;
    private ArrayList<Rectangle2D.Double> availableSquares;
    private ArrayList<Piece> blacks;
    private ArrayList<Piece> reds;
    private boolean showAvailiableSquares;
    private boolean isEnded;
    /* private com.daniel.Piece focused;*/
    private int teamWithTurn;
    private int winner = -1;
    private boolean needsToKill;
    private Piece pieceThatNeedsToKill;
    private MyFrame frame;
    private JButton button;

    MyComponent(MyFrame frame) {
        this.frame = frame;
        squares = new Rectangle2D.Double[8][8];
        availableSquares = new ArrayList<>();
        pieceThatNeedsToKill = null;
        blacks = new ArrayList<>(12);
        blacks.trimToSize();
        reds = new ArrayList<>(12);
        reds.trimToSize();
        teamWithTurn = 1;
        isEnded = false;
        button = null;
        tableAdd();
        InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ctrl U"), "esc_pressed");
        AllUnfocusedEvent allUnfocusedEvent = new AllUnfocusedEvent();
        ActionMap aMap = this.getActionMap();
        aMap.put("esc_pressed", allUnfocusedEvent);

        addMouseListener(new MouseHandler());
    }

    private void tableAdd() {
        for (int h = 0; h < 8; h++) {

            for (int w = 0; w < 8; w++) {
                Rectangle2D.Double cur = new Rectangle2D.Double(MARG_FROM_EDGE + w * SIDE_LENGTH,
                        MARG_FROM_EDGE + h * SIDE_LENGTH,
                        SIDE_LENGTH, SIDE_LENGTH);
                squares[h][w] = cur;
                if (h >= 5 && (h + w) % 2 == 1) {
                    reds.add(new Piece(1, new ImageIcon("res/images/red_piece.png"), cur));
                }
                if (h <= 2 && (h + w) % 2 == 1) {
                    blacks.add(new Piece(0, new ImageIcon("res/images/black_piece1.png"), cur));
                }
            }
        }


        RefreshAction action = new RefreshAction();
        button = new JButton(action);
        this.add(button) ;
        button.setBounds(840,240,240,120);
        button.setFont(new Font("Times New Roman",Font.PLAIN,40));
        repaint();
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        for (int h = 0; h < 8; h++) {

            for (int w = 0; w < 8; w++) {
                Rectangle2D.Double cur = squares[h][w];
                if ((w + h) % 2 == 0) {
                    g2.setPaint(Color.PINK);
                } else {
                    g2.setPaint(Color.WHITE);
                    if (showAvailiableSquares) {

                        for (Rectangle2D.Double rect :
                                availableSquares) {
                            Rectangle2D.Double currentRect = getSquareWithIJCoordinates(h, w);
                            if (currentRect.equals(rect)) {
                                g2.setPaint(Color.ORANGE);
                            }
                        }
                    }
                    /*   g2.setPaint(Color.WHITE);*/
                }
                g2.fill(cur);
                for (Piece red : reds) {
                    if (red.getPosition().equals(cur)) {
                        if (!red.isInFocus()) {
                            g.drawImage(red.getImage(), MARG_FROM_EDGE + w * SIDE_LENGTH,
                                    MARG_FROM_EDGE + h * SIDE_LENGTH,
                                    SIDE_LENGTH, SIDE_LENGTH, g2.getColor(), null);
                        } else {
                            g.drawImage(red.getImage(), MARG_FROM_EDGE + w * SIDE_LENGTH + SIDE_LENGTH / 4,
                                    MARG_FROM_EDGE + h * SIDE_LENGTH + SIDE_LENGTH / 4,
                                    SIDE_LENGTH / 2, SIDE_LENGTH / 2, g2.getColor(), null);
                        }
                    }
                }
                for (Piece black : blacks) {
                    if (black.getPosition().equals(cur)) {
                        if (!black.isInFocus()) {
                            g.drawImage(black.getImage(), MARG_FROM_EDGE + w * SIDE_LENGTH,
                                    MARG_FROM_EDGE + h * SIDE_LENGTH,
                                    SIDE_LENGTH, SIDE_LENGTH, g2.getColor(), null);
                        } else {
                            g.drawImage(black.getImage(), MARG_FROM_EDGE + w * SIDE_LENGTH + SIDE_LENGTH / 4
                                    , MARG_FROM_EDGE + h * SIDE_LENGTH + SIDE_LENGTH / 4,
                                    SIDE_LENGTH / 2, SIDE_LENGTH / 2, g2.getColor(), null);
                        }
                    }
                }
            }
        }



        if (!isEnded) { //draw a circle of team that has a turn if game goes
            if (teamWithTurn == 0) {
                g2.setPaint(Color.BLACK);
            } else if (teamWithTurn == 1) {
                g2.setPaint(Color.RED);
            }
            g2.fill(new Ellipse2D.Double(MARG_FROM_EDGE + 10 * SIDE_LENGTH, MARG_FROM_EDGE + 5 * SIDE_LENGTH,
                    SIDE_LENGTH, SIDE_LENGTH));
        }
        if (winner != -1) {

            Font f = new Font("Times New Roman", Font.BOLD, 120);
            g2.setPaint(Color.WHITE);
            Rectangle2D bounds = f.getStringBounds("WINNER IS BLACK!", g2.getFontRenderContext());

            g2.fill(new Rectangle2D.Double(40, 360, bounds.getWidth(), bounds.getHeight()));
            g2.setPaint(Color.ORANGE);
            g2.setFont(f);
            if (winner == 0) {
                g2.drawString("WINNER IS BLACK!", 0, 460);


                g2.setPaint(Color.BLACK);

            } else if (winner == 1) {
                g2.drawString("WINNER IS RED!", 40, 460);
                g2.setPaint(Color.RED);
            }
            g2.fill(new Ellipse2D.Double(MARG_FROM_EDGE + 10 * SIDE_LENGTH, MARG_FROM_EDGE + 5 * SIDE_LENGTH,
                    SIDE_LENGTH * 2, SIDE_LENGTH * 2));
            repaint();

        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    private Rectangle2D.Double getSquareWithPoint(Point2D.Double point) {
        if (point == null) {
            return null;
        }
        for (int h = 0; h < 8; h++) {

            for (int w = 0; w < 8; w++) {
                if (squares[h][w].contains(point)) {
                    return squares[h][w];
                }

            }
        }
        return null;
    }

    private Rectangle2D.Double getSquareWithIJCoordinates(int i, int j) {
        return squares[i][j];
    }

    private int getRowWithSquare(Rectangle2D.Double rect) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (squares[i][j].equals(rect)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getColumnWithSquare(Rectangle2D.Double rect) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (squares[i][j].equals(rect)) {
                    return j;
                }
            }
        }
        return -1;
    }

    private Piece getPieceWithSquare(Rectangle2D.Double sq) {
        for (Piece p : reds) {
            if (p.getPosition().equals(sq)) {
                return p;
            }
        }
        for (Piece p : blacks) {
            if (p.getPosition().equals(sq)) {
                return p;
            }
        }
        return null;
    }

    private void setFocusedPiece(Piece p) {
        for (Piece r :
                reds) {
            if (r.isInFocus()) {
                r.setFocused(false);
            }
        }
        for (Piece b :
                blacks) {
            if (b.isInFocus()) {
                b.setFocused(false);
            }
        }
        p.setFocused(true);
        availableSquares.clear();
        if (!p.isKing()) {
            if (getPossibleKillSquaresRegular(p) != null) {
                showAvailiableSquares = true;
                availableSquares.addAll(getPossibleKillSquaresRegular(p));
            }
        } else {
            if (getPossibleKillSquaresKing(p) != null) {
                showAvailiableSquares = true;
                availableSquares.addAll(getPossibleKillSquaresKing(p));
                repaint();
            }
        }
    }

    private ArrayList<Piece> getPieceListWithEnemyTeam(int t) {
        if (t == 1) {
            return blacks;
        } else if (t == 0) {
            return reds;
        }
        return null;
    }

    private Piece getFocusedPiece() {
        for (Piece r :
                reds) {
            if (r.isInFocus()) {
                return r;
            }
        }
        for (Piece b :
                blacks) {
            if (b.isInFocus()) {
                return b;
            }
        }
        return null;
    }

    private void setAllUnfocused() {
        for (Piece r :
                reds) {
            if (r.isInFocus()) {
                r.setFocused(false);
            }
        }
        for (Piece b :
                blacks) {
            if (b.isInFocus()) {
                b.setFocused(false);
            }
        }
        showAvailiableSquares = false;
        availableSquares.clear();
    }

    private void changeTeamTurn() {
        if (teamWithTurn == 0) {
            teamWithTurn = 1;
        } else {
            teamWithTurn = 0;
        }
        repaint();
    }

    private void moveRegular(Piece p, Rectangle2D.Double rect) {
        /*ArrayList<Rectangle2D.Double> availables = new ArrayList<>();*/
        int row = getRowWithSquare(p.getPosition());
        int column = getColumnWithSquare(p.getPosition());


        if (p.getTeam() == 1) { //is red


            if (row >= 1) {
                if (column >= 1) {
                    if (getPieceWithSquare(squares[row - 1][column - 1]) == null) {
                        if (squares[row - 1][column - 1].equals(rect)) {
                            p.setPosition(squares[row - 1][column - 1]);
                            p.setMoving(false);
                            changeTeamTurn();
                            return;
                        }

                    } else if (getPieceWithSquare(squares[row - 1][column - 1]).getTeam() == 0) {
                        if (row - 2 >= 0 && column - 2 >= 0) {
                            if (getPieceWithSquare(squares[row - 2][column - 2]) == null) {
                                if (squares[row - 2][column - 2].equals(rect)) {
                                    p.setPosition(squares[row - 2][column - 2]);
                                    blacks.remove(getPieceWithSquare(squares[row - 1][column - 1]));
                                    p.setMoving(true);
                                    repaint();
                                    changeTeamTurn();
                                    return;
                                }
                            }
                        }
                    }
                }
                if (column <= 6) {
                    if (getPieceWithSquare(squares[row - 1][column + 1]) == null) {
                        if (squares[row - 1][column + 1].equals(rect)) {
                            p.setPosition(squares[row - 1][column + 1]);
                            p.setMoving(false);
                            changeTeamTurn();
                            return;
                        }

                    } else if (getPieceWithSquare(squares[row - 1][column + 1]).getTeam() == 0) {
                        if (row - 2 >= 0 && column + 2 <= 7) {
                            if (getPieceWithSquare(squares[row - 2][column + 2]) == null) {
                                if (rect.equals(squares[row - 2][column + 2])) {
                                    p.setPosition(squares[row - 2][column + 2]);
                                    blacks.remove(getPieceWithSquare(squares[row - 1][column + 1]));
                                    p.setMoving(true);
                                    repaint();
                                    changeTeamTurn();
                                    return;
                                }
                            }
                        }
                    }
                    //availablePos[row - 1][column + 1] = true;
                }

            }

        } else if (p.getTeam() == 0) {//black team


            if (row <= 6) {
                if (column >= 1) {
                    if (getPieceWithSquare(squares[row + 1][column - 1]) == null) {
                        if (squares[row + 1][column - 1].equals(rect)) {
                            p.setPosition(squares[row + 1][column - 1]);
                            p.setMoving(false);
                            changeTeamTurn();
                            return;
                        }

                    } else if (getPieceWithSquare(squares[row + 1][column - 1]).getTeam() == 1) {
                        if (row + 2 <= 7 && column - 2 >= 0) {
                            if (getPieceWithSquare(squares[row + 2][column - 2]) == null) {
                                if (rect.equals(squares[row + 2][column - 2])) {
                                    p.setPosition(squares[row + 2][column - 2]);
                                    reds.remove(getPieceWithSquare(squares[row + 1][column - 1]));
                                    p.setMoving(true);
                                    repaint();
                                    changeTeamTurn();
                                    return;
                                }
                            }
                        }
                    }
                }
                if (column <= 6) {
                    if (getPieceWithSquare(squares[row + 1][column + 1]) == null) {
                        if (squares[row + 1][column + 1].equals(rect)) {
                            p.setPosition(squares[row + 1][column + 1]);
                            p.setMoving(false);
                            changeTeamTurn();
                            return;
                        }

                    } else if (getPieceWithSquare(squares[row + 1][column + 1]).getTeam() == 1) {
                        if (row + 2 <= 7 && column + 2 <= 7) {
                            if (getPieceWithSquare(squares[row + 2][column + 2]) == null) {
                                if (rect.equals(squares[row + 2][column + 2])) {
                                    p.setPosition(squares[row + 2][column + 2]);
                                    reds.remove(getPieceWithSquare(squares[row + 1][column + 1]));
                                    p.setMoving(true);
                                    repaint();
                                    changeTeamTurn();
                                    return;
                                }
                            }
                        }
                    }
                    //availablePos[row - 1][column + 1] = true;
                }

            }
                    /*if(row<=6) {
                        if (column - 1 >= 0) {
                            availablePos[row + 1][column - 1] = true;
                        }
                        if (column + 1 <= 7) {
                            availablePos[row + 1][column + 1] = true;
                        }

            }*/


        }

        setAllUnfocused();
        repaint();

    }

    private ArrayList<Rectangle2D.Double> getPossibleKillSquaresRegular(Piece p)
            throws NullPointerException {
        ArrayList<Rectangle2D.Double> possibles = new ArrayList<>();
        if (p != null) {
            int row = getRowWithSquare(p.getPosition());
            int column = getColumnWithSquare(p.getPosition());
            if (p.getTeam() == 1) { //Red team
                if (row >= 1 && column >= 1) {
                    if (getPieceWithSquare(getSquareWithIJCoordinates(row - 1, column - 1)) != null) {
                        if (getPieceWithSquare(getSquareWithIJCoordinates(row - 1, column - 1)).getTeam() == 0) {
                            if (row >= 2 && column >= 2) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row - 2, column - 2)) == null) {
                                    possibles.add(getSquareWithIJCoordinates(row - 2, column - 2));
                                }
                            }
                        }
                    }
                }

                if (row >= 1 && column <= 6) {
                    if (getPieceWithSquare(getSquareWithIJCoordinates(row - 1, column + 1)) != null) {
                        if (getPieceWithSquare(getSquareWithIJCoordinates(row - 1, column + 1)).getTeam() == 0) {
                            if (row >= 2 && column <= 5) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row - 2, column + 2)) == null) {
                                    possibles.add(getSquareWithIJCoordinates(row - 2, column + 2));
                                }
                            }
                        }
                    }
                }
            } else if (p.getTeam() == 0) { //Black team
                if (row <= 6 && column >= 1) {
                    if (getPieceWithSquare(getSquareWithIJCoordinates(row + 1, column - 1)) != null) {
                        if (getPieceWithSquare(getSquareWithIJCoordinates(row + 1, column - 1)).getTeam() == 1) {
                            if (row <= 5 && column >= 2) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row + 2, column - 2)) == null) {
                                    possibles.add(getSquareWithIJCoordinates(row + 2, column - 2));
                                }
                            }
                        }
                    }
                }

                if (row <= 6 && column <= 6) {
                    if (getPieceWithSquare(getSquareWithIJCoordinates(row + 1, column + 1)) != null) {
                        if (getPieceWithSquare(getSquareWithIJCoordinates(row + 1, column + 1)).getTeam() == 1) {
                            if (row <= 5 && column <= 5) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row + 2, column + 2)) == null) {
                                    possibles.add(getSquareWithIJCoordinates(row + 2, column + 2));
                                }
                            }
                        }
                    }
                }
            }

            return possibles;

        } else {
            throw new NullPointerException();
        }

    }

    private ArrayList<Rectangle2D.Double> getPossibleKillSquaresKing(Piece p) {
        if (p.isKing()) {//necessary condition
            var possibles = new ArrayList<Rectangle2D.Double>();
            int row = getRowWithSquare(p.getPosition());
            int column = getColumnWithSquare(p.getPosition());
            /*for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if((i+j==row+column||i-j==row-column)&&(i!=row||j!=column)){
                        if(getPieceWithSquare(squares[i][j])==null){
                            availables.add(squares[i][j]);
                        }
                    }
                }
            }*/
            boolean killed = false;
            for (int i = 1; true; i++) {

                if (row - i >= 0 && column - i >= 0) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row - i, column - i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) { //not killed
                        if (currentPiece == null) {

                            /*availables.add(currentSquare);*/
                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row - i - 1 >= 0 && column - i - 1 >= 0 &&
                                    getPieceWithSquare(getSquareWithIJCoordinates(row - i - 1, column - i - 1)) ==
                                            null) {

                                killed = true;
                                /*possibles.add(getSquareWithIJCoordinates(row - i - 1, column - i - 1));*/


                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            possibles.add(currentSquare);
                            /*availables.add(currentSquare);*/
                        } else {
                            killed = false;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            killed = false;
//Second iteration
            for (int i = 1; true; i++) {
                if (row + i <= 7 && column - i >= 0) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row + i, column - i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) {
                        if (currentPiece == null) {

                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row + i + 1 <= 7 && column - i - 1 >= 0 &&
                                    getPieceWithSquare(getSquareWithIJCoordinates(row + i + 1, column - i - 1)) ==
                                            null) {
                                killed = true;

                            } else {
                                break;
                            }

                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            possibles.add(currentSquare);
                            /*availables.add(currentSquare);*/
                        } else {
                            killed = false;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            killed = false;
//Third iteration
            for (int i = 1; true; i++) {
                if (row + i <= 7 && column + i <= 7) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row + i, column + i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) {
                        if (currentPiece == null) {

                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row + i + 1 <= 7 && column + i + 1 <= 7) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row + i + 1, column + i + 1)) ==
                                        null) {
                                    killed = true;
                                }
                            } else {
                                break;
                            }

                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            possibles.add(currentSquare);
                            /*availables.add(currentSquare);*/
                        } else {
                            killed = false;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            killed = false;
//Fourth iteration
            for (int i = 1; true; i++) {
                if (row - i >= 0 && column + i <= 7) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row - i, column + i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) {
                        if (currentPiece == null) {

                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row - i - 1 >= 0 && column + i + 1 <= 7 &&
                                    getPieceWithSquare(getSquareWithIJCoordinates(row - i - 1, column + i + 1)) ==
                                            null) {
                                killed = true;

                            } else {
                                break;
                            }

                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            possibles.add(currentSquare);
                            /*availables.add(currentSquare);*/
                        } else {
                            killed = false;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }


            return possibles;
        }
        return null;
    }

    private void moveKing(Piece p, Rectangle2D.Double rect) {
        if (p.isKing()) {
            /*ArrayList<Rectangle2D.Double> availables = new ArrayList<>();*/
            int row = getRowWithSquare(p.getPosition());
            int column = getColumnWithSquare(p.getPosition());
            /*for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if((i+j==row+column||i-j==row-column)&&(i!=row||j!=column)){
                        if(getPieceWithSquare(squares[i][j])==null){
                            availables.add(squares[i][j]);
                        }
                    }
                }
            }*/
            Piece toBeKilled1 = null;
            Piece toBeKilled2 = null;
            Piece toBeKilled3 = null;
            Piece toBeKilled4 = null;
            boolean killed = false;
            for (int i = 1; true; i++) {

                if (row - i >= 0 && column - i >= 0) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row - i, column - i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) { //not killed
                        if (currentPiece == null) {
                            if (currentSquare.equals(rect)) {
                                p.setPosition(rect);
                                changeTeamTurn();
                                return;
                            }
                            /*availables.add(currentSquare);*/
                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row - i - 1 >= 0 && column - i - 1 >= 0 &&
                                    getPieceWithSquare(getSquareWithIJCoordinates(row - i - 1, column - i - 1)) ==
                                            null) {
                                toBeKilled1 = currentPiece;
                                killed = true;
                                if (rect.equals(getSquareWithIJCoordinates(row - i - 1, column - i - 1))) {
                                    p.setPosition(getSquareWithIJCoordinates(row - i - 1, column - i - 1));
                                    getPieceListWithEnemyTeam(p.getTeam()).remove(toBeKilled1);
                                    p.setMoving(true);
                                    changeTeamTurn();
                                    return;
                                }


                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            if (currentSquare.equals(rect)) {
                                p.setPosition(rect);
                                changeTeamTurn();
                                getPieceListWithEnemyTeam(p.getTeam()).remove(toBeKilled1);
                                p.setMoving(true);
                                return;
                            }
                            /*availables.add(currentSquare);*/
                        } else {
                            killed = false;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            killed = false;
//Second iteration
            for (int i = 1; true; i++) {
                if (row + i <= 7 && column - i >= 0) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row + i, column - i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) {
                        if (currentPiece == null) {
                            if (rect.equals(currentSquare)) {
                                p.setPosition(currentSquare);
                                changeTeamTurn();
                                return;
                            }
                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row + i + 1 <= 7 && column - i - 1 >= 0 &&
                                    getPieceWithSquare(getSquareWithIJCoordinates(row + i + 1, column - i - 1)) ==
                                            null) {
                                toBeKilled2 = currentPiece;
                                killed = true;
                                if (rect.equals(getSquareWithIJCoordinates(row + i + 1, column - i - 1))) {
                                    p.setPosition(getSquareWithIJCoordinates(row + i + 1, column - i - 1));
                                    getPieceListWithEnemyTeam(p.getTeam()).remove(toBeKilled2);
                                    p.setMoving(true);
                                    changeTeamTurn();
                                    return;
                                }
                            } else {
                                break;
                            }

                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            if (currentSquare.equals(rect)) {
                                p.setPosition(rect);
                                changeTeamTurn();
                                getPieceListWithEnemyTeam(p.getTeam()).remove(toBeKilled2);
                                p.setMoving(true);
                                return;
                            }
                            /*availables.add(currentSquare);*/
                        } else {
                            killed = false;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            killed = false;
//Third iteration
            for (int i = 1; true; i++) {
                if (row + i <= 7 && column + i <= 7) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row + i, column + i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) {
                        if (currentPiece == null) {
                            if (rect.equals(currentSquare)) {
                                p.setPosition(currentSquare);
                                changeTeamTurn();
                                return;
                            }
                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row + i + 1 <= 7 && column + i + 1 <= 7 &&
                                    getPieceWithSquare(getSquareWithIJCoordinates(row + i + 1, column + i + 1)) ==
                                            null) {
                                toBeKilled3 = currentPiece;
                                killed = true;
                                if (rect.equals(getSquareWithIJCoordinates(row + i + 1, column + i + 1))) {
                                    p.setPosition(getSquareWithIJCoordinates(row + i + 1, column + i + 1));
                                    getPieceListWithEnemyTeam(p.getTeam()).remove(toBeKilled3);
                                    p.setMoving(true);
                                    changeTeamTurn();
                                    return;
                                }
                            } else {
                                break;
                            }

                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            if (currentSquare.equals(rect)) {
                                p.setPosition(rect);
                                changeTeamTurn();
                                getPieceListWithEnemyTeam(p.getTeam()).remove(toBeKilled3);
                                p.setMoving(true);
                                return;
                            }
                            /*availables.add(currentSquare);*/
                        } else {
                            killed = false;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            killed = false;
//Fourth iteration
            for (int i = 1; true; i++) {
                if (row - i >= 0 && column + i <= 7) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row - i, column + i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) {
                        if (currentPiece == null) {
                            if (rect.equals(currentSquare)) {
                                p.setPosition(currentSquare);
                                changeTeamTurn();
                                return;
                            }
                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row - i - 1 >= 0 && column + i + 1 <= 7 &&
                                    getPieceWithSquare(getSquareWithIJCoordinates(row - i - 1, column + i + 1)) ==
                                            null) {
                                toBeKilled4 = currentPiece;
                                killed = true;
                                if (rect.equals(getSquareWithIJCoordinates(row - i - 1, column + i + 1))) {
                                    p.setPosition(getSquareWithIJCoordinates(row - i - 1, column + i + 1));
                                    getPieceListWithEnemyTeam(p.getTeam()).remove(toBeKilled4);
                                    p.setMoving(true);
                                    changeTeamTurn();
                                    return;
                                }
                            } else {
                                break;
                            }

                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            if (currentSquare.equals(rect)) {
                                p.setPosition(rect);
                                changeTeamTurn();
                                getPieceListWithEnemyTeam(p.getTeam()).remove(toBeKilled4);
                                p.setMoving(true);
                                return;
                            }
                            /*availables.add(currentSquare);*/
                        } else {
                            killed = false;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }


        } else {
            return;
        }
    }

    private class AllUnfocusedEvent extends AbstractAction {

        public AllUnfocusedEvent() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Performed");
            setAllUnfocused();
            repaint();
        }
    }


    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (!needsToKill) {
                int x = e.getX();
                int y = e.getY();
                Rectangle2D.Double currentRect;
                Point2D.Double currentPoint = new Point2D.Double(x, y);
                if (getSquareWithPoint(currentPoint) != null) {
                    currentRect = getSquareWithPoint(currentPoint);
                    Piece curPiece = getPieceWithSquare(currentRect);

                    if (curPiece != null) {
                        if (curPiece.getTeam() == teamWithTurn) {
                            if (!curPiece.isInFocus()) {
                                setFocusedPiece(curPiece);
                            } else {
                                setAllUnfocused();
                            }
                            repaint();
                        }
                    } else if (getFocusedPiece() != null) {
                        Piece focusedPiece = getFocusedPiece();

                        if (!focusedPiece.isKing()) { //isRegular

                            moveRegular(focusedPiece, currentRect);

                            if (focusedPiece.getTeam() == 1) {
                                if (getRowWithSquare(focusedPiece.getPosition()) == 0) {
                                    focusedPiece.setToKing();
                                }
                            } else if (focusedPiece.getTeam() == 0) {
                                if (getRowWithSquare(focusedPiece.getPosition()) == 7) {
                                    focusedPiece.setToKing();
                                }
                            }

                            if (!focusedPiece.isKing()) { //did not become king
                                if (focusedPiece.isMoving()) { //killed someone
                                    if (getPossibleKillSquaresRegular(focusedPiece).size() != 0) {
                                        needsToKill = true;
                                        pieceThatNeedsToKill = focusedPiece;
                                        changeTeamTurn();
                                        setFocusedPiece(focusedPiece);
                                        repaint();
                                    } else {
                                        focusedPiece.setMoving(false);
                                        setAllUnfocused();
                                    }
                                } else {
                                    setAllUnfocused();
                                }
                            } else { //became king
                                if (focusedPiece.isMoving()) { //killed someone
                                    if (getPossibleKillSquaresKing(focusedPiece).size() != 0) {
                                        needsToKill = true;
                                        pieceThatNeedsToKill = focusedPiece;
                                        changeTeamTurn();
                                        setFocusedPiece(focusedPiece);
                                        repaint();
                                    } else {
                                        focusedPiece.setMoving(false);
                                        setAllUnfocused();
                                    }
                                } else {
                                    setAllUnfocused();
                                }
                            }


                            repaint();

                        } else { //isKing
                            moveKing(focusedPiece, currentRect);
                            if (focusedPiece.isMoving()) {
                                if (getPossibleKillSquaresKing(focusedPiece).size() == 0) {
                                    focusedPiece.setMoving(false);
                                    setAllUnfocused();
                                    repaint();
                                } else {
                                    needsToKill = true;
                                    pieceThatNeedsToKill = focusedPiece;
                                    setFocusedPiece(focusedPiece);
                                    changeTeamTurn();
                                    repaint();
                                }
                            } else {
                                setAllUnfocused();
                                repaint();
                            }

                        }

                    }

                } else {

                    setAllUnfocused();
                    repaint();
                }
                repaint();
            } else {
                int x = e.getX();
                int y = e.getY();
                Point2D.Double currentPoint = new Point2D.Double(x, y);
                Rectangle2D.Double currentRect = getSquareWithPoint(currentPoint);
                Piece currentPiece = pieceThatNeedsToKill;
                if (currentRect != null) {
                    if (!currentPiece.isKing()) { //not king
                        if (getPossibleKillSquaresRegular(currentPiece).contains(currentRect)) {
                            moveRegular(currentPiece, currentRect);

                            if (currentPiece.getTeam() == 1) {
                                if (getRowWithSquare(currentPiece.getPosition()) == 0) {
                                    currentPiece.setToKing();
                                }
                            } else if (currentPiece.getTeam() == 0) {
                                if (getRowWithSquare(currentPiece.getPosition()) == 7) {
                                    currentPiece.setToKing();
                                }
                            }

                            if (!currentPiece.isKing()) { //not became king
                                if (currentPiece.isMoving()) { //ate somebody
                                    if (getPossibleKillSquaresRegular(currentPiece).size() == 0) { //can't kill anymore
                                        needsToKill = false;
                                        pieceThatNeedsToKill = null;
                                        currentPiece.setMoving(false);
                                        setAllUnfocused();
                                        repaint();
                                    } else { //can kill more

                                        changeTeamTurn();
                                        setFocusedPiece(currentPiece);
                                        repaint();
                                    }
                                } else { //did not eat anybody
                                    needsToKill = false;
                                    pieceThatNeedsToKill = null;
                                    currentPiece.setMoving(false);
                                    setAllUnfocused();
                                    repaint();
                                }
                            } else { //became king
                                if (getPossibleKillSquaresKing(currentPiece).size() == 0) { //can't kill anymore
                                    needsToKill = false;
                                    pieceThatNeedsToKill = null;
                                    currentPiece.setMoving(false);
                                    setAllUnfocused();
                                    repaint();
                                    System.out.println("BEACAME KING CAN'T KILL MORE");
                                } else {

                                    changeTeamTurn();
                                    setFocusedPiece(currentPiece);
                                    repaint();
                                    System.out.println("BEACAME KING CAN KILL MORE");
                                }
                            }

                        }
                    } else {//king
                        if (getPossibleKillSquaresKing(currentPiece).contains(currentRect)) {
                            moveKing(currentPiece, currentRect);
                            if (getPossibleKillSquaresKing(currentPiece).size() == 0) { //can't kill anymore
                                needsToKill = false;
                                pieceThatNeedsToKill = null;
                                currentPiece.setMoving(false);
                                setAllUnfocused();
                                repaint();
                            } else {

                                changeTeamTurn();
                                setFocusedPiece(currentPiece);
                                repaint();
                            }
                        }
                    }
                }
            }
            if (reds.size() <= 0) {
                winner = 0;
                isEnded = true;
                frame.setBlackWins(frame.getBlackWins()+1);
                frame.setScore();
            } else if (blacks.size() <= 0) {
                winner = 1;
                isEnded = true;
                frame.setRedWins(frame.getRedWins()+1);
                frame.setScore();
            }
            repaint();
        }

    }

    private class RefreshAction extends AbstractAction{

        RefreshAction(){
            putValue(Action.NAME,"New game");
            putValue(Action.SHORT_DESCRIPTION,"Start a new game");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            frame.refreshComponent();
        }
    }

    public int getWinner() {
        return winner;
    }

    public boolean isEnded() {
        return isEnded;
    }
}
