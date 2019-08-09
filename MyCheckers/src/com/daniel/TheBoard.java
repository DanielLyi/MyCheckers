package com.daniel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Objects;

public class TheBoard extends JComponent {

    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 750;
    private static final int SIDE_LENGTH = 80;
    private static final int MARG_FROM_EDGE = 80;
    private Rectangle2D.Double[][] squares;
    private ArrayList<Rectangle2D.Double> availableSquaresToGo;
    private ArrayList<Rectangle2D.Double> availableSquaresToKill;
    private ArrayList<Piece> blacks;
    private ArrayList<Piece> reds;
    private boolean showAvailableSquares;
    private boolean isEnded;
    private boolean needsToKill;
    private boolean optionNeedToKill;
    private boolean choosing;
    private boolean showOnlyKillSquares;
    private int teamWithTurn;
    private int winner = -1;
    private Piece pieceThatNeedsToKill;
    private TheFrame frame;
    private JButton button;

    TheBoard(TheFrame frame) {
        this.frame = frame;
        squares = new Rectangle2D.Double[8][8];
        availableSquaresToGo = new ArrayList<>();
        availableSquaresToKill = new ArrayList<>();
        pieceThatNeedsToKill = null;
        blacks = new ArrayList<>(12);
        blacks.trimToSize();
        reds = new ArrayList<>(12);
        reds.trimToSize();
        teamWithTurn = 1;
        isEnded = false;
        button = null;
        needsToKill = false;
        optionNeedToKill = true;
        choosing = true;
        showAvailableSquares = false;
        initBoard();
        InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ctrl U"), "esc_pressed");
        AllUnfocusedEvent allUnfocusedEvent = new AllUnfocusedEvent();
        ActionMap aMap = this.getActionMap();
        aMap.put("esc_pressed", allUnfocusedEvent);

        addMouseListener(new MouseHandler());
    }

    private void initBoard() {
        for (int h = 0; h < 8; h++) {

            for (int w = 0; w < 8; w++) {
                Rectangle2D.Double cur = new Rectangle2D.Double(MARG_FROM_EDGE + w * SIDE_LENGTH,
                        MARG_FROM_EDGE + h * SIDE_LENGTH,
                        SIDE_LENGTH, SIDE_LENGTH);
                squares[h][w] = cur;
                if (h >= 5 && (h + w) % 2 == 1) {
                    reds.add(new Piece(1, new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(
                            "images/red_piece.png"))), cur));
                }
                if (h <= 2 && (h + w) % 2 == 1) {
                    blacks.add(new Piece(0, new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(
                            "images/black_piece.png"))), cur));
                }
            }
        }


        RefreshAction action = new RefreshAction();
        button = new JButton(action);
        this.add(button);
        button.setBounds(840, 240, 264, 120);
        button.setFont(new Font("Times New Roman", Font.PLAIN, 36));
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
                    if (showAvailableSquares) {
                        if (!showOnlyKillSquares) {
                            for (Rectangle2D.Double rect :
                                    availableSquaresToGo) {
                                Rectangle2D.Double currentRect = getSquareWithIJCoordinates(h, w);
                                if (currentRect.equals(rect)) {

                                    g2.setPaint(Color.ORANGE);
                                }
                            }
                        }
                        for (Rectangle2D.Double rect :
                                availableSquaresToKill) {
                            Rectangle2D.Double currentRect = getSquareWithIJCoordinates(h, w);
                            if (currentRect.equals(rect)) {
                                g2.setPaint(Color.RED);
                            }
                        }
                    }
                    /*   g2.setPaint(Color.WHITE);*/
                }
                g2.fill(cur);
                for (Piece red : reds) {
                    if (red.getPosition().equals(cur)) {
                        if (!red.isInFocus()) {
                            if (!red.isNeedToKill()) {
                                g.drawImage(red.getImage(), MARG_FROM_EDGE + w * SIDE_LENGTH,
                                        MARG_FROM_EDGE + h * SIDE_LENGTH,
                                        SIDE_LENGTH, SIDE_LENGTH, g2.getColor(), null);
                            } else {
                                System.out.println("Red has to kill another img");
                                g.drawImage(red.getImage()/*.getScaledInstance((int)(SIDE_LENGTH*1.2),
                                        (int)(SIDE_LENGTH*1.2),Image.SCALE_DEFAULT)*/, MARG_FROM_EDGE + w * SIDE_LENGTH,
                                        MARG_FROM_EDGE + h * SIDE_LENGTH,
                                        SIDE_LENGTH, SIDE_LENGTH, Color.RED.darker().darker(), null);
                            }
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
                            if (!black.isNeedToKill()) {
                                g.drawImage(black.getImage(), MARG_FROM_EDGE + w * SIDE_LENGTH,
                                        MARG_FROM_EDGE + h * SIDE_LENGTH,
                                        SIDE_LENGTH, SIDE_LENGTH, g2.getColor(), null);
                            } else {
                                g.drawImage(black.getImage(), MARG_FROM_EDGE + w * SIDE_LENGTH,
                                        MARG_FROM_EDGE + h * SIDE_LENGTH,
                                        SIDE_LENGTH, SIDE_LENGTH, Color.RED.darker(), null);
                            }
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
        if (winner != -1) { //someone won
            button.setText("Start new game");
            Font f = new Font("Times New Roman", Font.BOLD, 120);
            g2.setPaint(Color.LIGHT_GRAY);
            Rectangle2D bounds = f.getStringBounds("WINNER IS BLACK!", g2.getFontRenderContext());

            g2.fill(new Rectangle2D.Double(40, 360, bounds.getWidth() - 40, bounds.getHeight()));

            g2.setFont(f);
            if (winner == 0) {
                g2.setPaint(Color.BLACK);
                g2.drawString("WINNER IS BLACK!", 0, 460);


            } else if (winner == 1) {
                g2.setPaint(Color.RED);
                g2.drawString("WINNER IS RED!", 0, 460);

            }
            repaint();

        } else {//no winners yet
            button.setText("Restart game");
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

    private void setFocusedPiece(Piece p) {
        if (optionNeedToKill) {
            if (!availableSquaresToKill.isEmpty()) {
                if (!teamCanKill().contains(p)) {
                    return;
                }
            }
        }
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
        availableSquaresToGo.clear();
        availableSquaresToKill.clear();
        if (!p.isKing()) {
            if (getPossibleToGoSquaresRegular(p, true) != null) {
                showAvailableSquares = true;
                availableSquaresToGo.addAll(getPossibleToGoSquaresRegular(p, false));
                availableSquaresToKill.addAll(getPossibleToGoSquaresRegular(p, true));
            }
        } else {
            if (getPossibleToGoSquaresKing(p, true) != null) {
                showAvailableSquares = true;
                availableSquaresToGo.addAll(Objects.requireNonNull(getPossibleToGoSquaresKing(p, false)));
                availableSquaresToKill.addAll(getPossibleToGoSquaresKing(p, true));
            }
        }
        repaint();
    }

    private void setAllUnfocused() {
        for (Piece r :
                reds) {
            if (r.isInFocus()) {
                r.setFocused(false);
            }
            if (optionNeedToKill) {
                r.setNeedsToKill(false);
            }
        }
        for (Piece b :
                blacks) {
            if (b.isInFocus()) {
                b.setFocused(false);
            }
            if (optionNeedToKill) {
                b.setNeedsToKill(false);
            }
        }
        showAvailableSquares = false;
        availableSquaresToGo.clear();
        availableSquaresToKill.clear();
        repaint();
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
                            choosing = true;
                            p.setNeedsToKill(false);
                            p.setMoving(false);
                            changeTeamTurn();
                            return;
                        }

                    } else if (getPieceWithSquare(squares[row - 1][column - 1]).getTeam() == 0) {
                        if (row - 2 >= 0 && column - 2 >= 0) {
                            if (getPieceWithSquare(squares[row - 2][column - 2]) == null) {
                                if (squares[row - 2][column - 2].equals(rect)) {
                                    p.setPosition(squares[row - 2][column - 2]);
                                    choosing = true;
                                    p.setNeedsToKill(false);
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
                            choosing = true;
                            p.setNeedsToKill(false);
                            p.setMoving(false);
                            changeTeamTurn();
                            return;
                        }

                    } else if (getPieceWithSquare(squares[row - 1][column + 1]).getTeam() == 0) {
                        if (row - 2 >= 0 && column + 2 <= 7) {
                            if (getPieceWithSquare(squares[row - 2][column + 2]) == null) {
                                if (rect.equals(squares[row - 2][column + 2])) {
                                    p.setPosition(squares[row - 2][column + 2]);
                                    choosing = true;
                                    p.setNeedsToKill(false);
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
                            choosing = true;
                            p.setNeedsToKill(false);
                            p.setMoving(false);
                            changeTeamTurn();
                            return;
                        }

                    } else if (getPieceWithSquare(squares[row + 1][column - 1]).getTeam() == 1) {
                        if (row + 2 <= 7 && column - 2 >= 0) {
                            if (getPieceWithSquare(squares[row + 2][column - 2]) == null) {
                                if (rect.equals(squares[row + 2][column - 2])) {
                                    p.setPosition(squares[row + 2][column - 2]);
                                    choosing = true;
                                    p.setNeedsToKill(false);
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
                            choosing = true;
                            p.setNeedsToKill(false);
                            p.setMoving(false);
                            changeTeamTurn();
                            return;
                        }

                    } else if (getPieceWithSquare(squares[row + 1][column + 1]).getTeam() == 1) {
                        if (row + 2 <= 7 && column + 2 <= 7) {
                            if (getPieceWithSquare(squares[row + 2][column + 2]) == null) {
                                if (rect.equals(squares[row + 2][column + 2])) {
                                    p.setPosition(squares[row + 2][column + 2]);
                                    p.setNeedsToKill(false);
                                    choosing = true;
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

    /*private ArrayList<Rectangle2D.Double> getPossibleKillSquaresRegular(Piece p)
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

    }*/

    private ArrayList<Rectangle2D.Double> getPossibleToGoSquaresRegular(Piece p, boolean onlyKill)
            throws NullPointerException {
        ArrayList<Rectangle2D.Double> possibles = new ArrayList<>();
        if (p != null) {
            int row = getRowWithSquare(p.getPosition());
            int column = getColumnWithSquare(p.getPosition());
            if (p.getTeam() == 1) { //Red team
                if (row >= 1 && column >= 1) { //if someone is there
                    if (getPieceWithSquare(getSquareWithIJCoordinates(row - 1, column - 1)) != null) {
                        if (getPieceWithSquare(getSquareWithIJCoordinates(row - 1, column - 1)).getTeam() == 0) {
                            if (row >= 2 && column >= 2) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row - 2, column - 2)) == null) {
                                    possibles.add(getSquareWithIJCoordinates(row - 2, column - 2));
                                }
                            }
                        }
                    } else {
                        if (!onlyKill) {//if square is empty
                            possibles.add(getSquareWithIJCoordinates(row - 1, column - 1));
                        }
                    }
                }

                if (row >= 1 && column <= 6) {
                    if (getPieceWithSquare(getSquareWithIJCoordinates(row - 1, column + 1)) != null) {
                        //if someone is there
                        if (getPieceWithSquare(getSquareWithIJCoordinates(row - 1, column + 1)).getTeam() == 0) {
                            if (row >= 2 && column <= 5) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row - 2, column + 2)) == null) {
                                    possibles.add(getSquareWithIJCoordinates(row - 2, column + 2));
                                }
                            }
                        }
                    } else {
                        if (!onlyKill) { //if square is empty
                            possibles.add(getSquareWithIJCoordinates(row - 1, column + 1));
                        }
                    }
                }
            } else if (p.getTeam() == 0) { //Black team
                if (row <= 6 && column >= 1) {
                    if (getPieceWithSquare(getSquareWithIJCoordinates(row + 1, column - 1)) != null) {
                        //if square is empty
                        if (getPieceWithSquare(getSquareWithIJCoordinates(row + 1, column - 1)).getTeam() == 1) {
                            if (row <= 5 && column >= 2) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row + 2, column - 2)) == null) {
                                    possibles.add(getSquareWithIJCoordinates(row + 2, column - 2));
                                }
                            }
                        }
                    } else {
                        if (!onlyKill) { //if square is empty
                            possibles.add(getSquareWithIJCoordinates(row + 1, column - 1));
                        }
                    }
                }

                if (row <= 6 && column <= 6) {
                    if (getPieceWithSquare(getSquareWithIJCoordinates(row + 1, column + 1)) != null) {
                        //if square is empty
                        if (getPieceWithSquare(getSquareWithIJCoordinates(row + 1, column + 1)).getTeam() == 1) {
                            if (row <= 5 && column <= 5) {
                                if (getPieceWithSquare(getSquareWithIJCoordinates(row + 2, column + 2)) == null) {
                                    possibles.add(getSquareWithIJCoordinates(row + 2, column + 2));
                                }
                            }
                        }
                    } else { //if square is empty
                        if (!onlyKill) {
                            possibles.add(getSquareWithIJCoordinates(row + 1, column + 1));
                        }
                    }
                }
            }

            return possibles;

        } else {
            throw new NullPointerException();
        }

    }

   /* private ArrayList<Rectangle2D.Double> getPossibleKillSquaresKing(Piece p) {
        if (p.isKing()) {//necessary condition
            var possibles = new ArrayList<Rectangle2D.Double>();
            int row = getRowWithSquare(p.getPosition());
            int column = getColumnWithSquare(p.getPosition());
            *//*for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if((i+j==row+column||i-j==row-column)&&(i!=row||j!=column)){
                        if(getPieceWithSquare(squares[i][j])==null){
                            availables.add(squares[i][j]);
                        }
                    }
                }
            }*//*
            boolean killed = false;
            for (int i = 1; true; i++) {

                if (row - i >= 0 && column - i >= 0) {
                    Rectangle2D.Double currentSquare = getSquareWithIJCoordinates(row - i, column - i);
                    Piece currentPiece = getPieceWithSquare(currentSquare);
                    if (!killed) { //not killed
                        if (currentPiece == null) {

                            *//*availables.add(currentSquare);*//*
                        } else if (currentPiece.getTeam() != p.getTeam()) {
                            if (row - i - 1 >= 0 && column - i - 1 >= 0 &&
                                    getPieceWithSquare(getSquareWithIJCoordinates(row - i - 1, column - i - 1)) ==
                                            null) {

                                killed = true;
                                *//*possibles.add(getSquareWithIJCoordinates(row - i - 1, column - i - 1));*//*


                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else { //killed
                        if (currentPiece == null) {
                            possibles.add(currentSquare);
                            *//*availables.add(currentSquare);*//*
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
                            *//*availables.add(currentSquare);*//*
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
                            *//*availables.add(currentSquare);*//*
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
                            *//*availables.add(currentSquare);*//*
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
    }*/

    private ArrayList<Rectangle2D.Double> getPossibleToGoSquaresKing(Piece p, boolean onlyKill) {
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
                            if (!onlyKill) {
                                possibles.add(currentSquare);
                            }
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
                            if (!onlyKill) {
                                possibles.add(currentSquare);
                            }
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
                            if (!onlyKill) {
                                possibles.add(currentSquare);
                            }
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
                            if (!onlyKill) {
                                possibles.add(currentSquare);
                            }
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
                                p.setNeedsToKill(false);
                                choosing = true;
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
                                    choosing = true;
                                    p.setNeedsToKill(false);
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
                                choosing = true;
                                p.setNeedsToKill(false);
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
                                p.setNeedsToKill(false);
                                choosing = true;
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
                                    choosing = true;
                                    p.setNeedsToKill(false);
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
                                choosing = true;
                                p.setNeedsToKill(false);
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
                                choosing = true;
                                p.setNeedsToKill(false);
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
                                    choosing = true;
                                    p.setNeedsToKill(false);
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
                                choosing = true;
                                p.setNeedsToKill(false);
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
                                choosing = true;
                                p.setNeedsToKill(false);
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
                                    choosing = true;
                                    p.setNeedsToKill(false);
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
                                choosing = true;
                                p.setNeedsToKill(false);
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

    private boolean canNotMove() {
        int team = teamWithTurn;
        ArrayList<Piece> pieces;
        boolean hasNoMoves = true;
        if (team == 1) {
            pieces = reds;
        } else {
            pieces = blacks;
        }
        for (Piece p :
                pieces) {
            if (p != null) {
                if (!p.isKing()) {
                    if (!getPossibleToGoSquaresRegular(p, false).isEmpty()) { //if there is a square to go
                        hasNoMoves = false;
                        break;
                    }
                } else {
                    if (!getPossibleToGoSquaresKing(p, false).isEmpty()) { //if there is a square to go
                        hasNoMoves = false;
                        break;
                    }
                }
            }
        }
        return hasNoMoves;

    }

    private ArrayList<Piece> teamCanKill() {
        var piecesThatCanKill = new ArrayList<Piece>(12);
        var team = new ArrayList<Piece>();
        if (teamWithTurn == 0) {
            team = blacks;
        } else if (teamWithTurn == 1) {
            team = reds;
        }
        for (Piece p :
                team) {
            if (!p.isKing()) {
                if (!getPossibleToGoSquaresRegular(p, true).isEmpty()) {
                    piecesThatCanKill.add(p);
                    p.setNeedsToKill(true);
                }
            } else {
                if (!getPossibleToGoSquaresKing(p, true).isEmpty()) {
                    piecesThatCanKill.add(p);
                    p.setNeedsToKill(true);
                }
            }
        }
        return piecesThatCanKill;

    }

    public int getWinner() {
        return winner;
    }

    public boolean isEnded() {
        return isEnded;
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
            if (!canNotMove()) {
                if (!needsToKill) {
                    int x = e.getX();
                    int y = e.getY();
                    Rectangle2D.Double currentRect;
                    Point2D.Double currentPoint = new Point2D.Double(x, y);
                    if (getSquareWithPoint(currentPoint) != null) {

                        currentRect = getSquareWithPoint(currentPoint);
                        Piece curPiece = getPieceWithSquare(currentRect);
                        Piece focPiece = getFocusedPiece();
                        if (optionNeedToKill) {
                            if (choosing) {
                                if (!teamCanKill().isEmpty()) {
                                    showOnlyKillSquares = true;
                                    for (Piece p :
                                            teamCanKill()) {

                                        p.setNeedsToKill(true);
                                        repaint();
                                    }
                                    if (!teamCanKill().contains(curPiece)) {

                                        return;
                                    }
                                    choosing = false;

                                }
                            }
                        }

                        if (curPiece != null) {

                            if (curPiece.getTeam() == teamWithTurn) {
                                if (!curPiece.isInFocus()) {
                                    if (availableSquaresToKill.isEmpty()) {
                                        setFocusedPiece(curPiece);
                                    } else {
                                        if (teamCanKill().contains(curPiece)) {
                                            setFocusedPiece(curPiece);
                                        }
                                    }
                                } else {
                                    if (teamCanKill().isEmpty()) {
                                        setAllUnfocused();
                                    }
                                }
                                repaint();
                            }
                        } else if (getFocusedPiece() != null) {
                            Piece focusedPiece = getFocusedPiece();


                            if (optionNeedToKill) {

                                if (!teamCanKill().isEmpty()) {

                                    if (!focusedPiece.isKing()) {
                                        if (!getPossibleToGoSquaresRegular(focusedPiece, true).contains(currentRect)) {

                                            return;
                                        }
                                    } else {
                                        if (!getPossibleToGoSquaresKing(focusedPiece, true).contains(currentRect)) {

                                            return;
                                        }
                                    }

                                }
                                showOnlyKillSquares = false;
                            }


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
                                        if (getPossibleToGoSquaresRegular(focusedPiece, true).size() != 0) {
                                            needsToKill = true;
                                            pieceThatNeedsToKill = focusedPiece;
                                            showOnlyKillSquares = true;
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
                                        if (getPossibleToGoSquaresKing(focusedPiece, true).size() != 0) {
                                            needsToKill = true;
                                            pieceThatNeedsToKill = focusedPiece;
                                            showOnlyKillSquares = true;
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
                                    if (getPossibleToGoSquaresKing(focusedPiece, true).size() == 0) {
                                        focusedPiece.setMoving(false);
                                        setAllUnfocused();
                                        repaint();
                                    } else {
                                        needsToKill = true;
                                        pieceThatNeedsToKill = focusedPiece;
                                        showOnlyKillSquares = true;
                                        changeTeamTurn();
                                        setFocusedPiece(focusedPiece);
                                        repaint();
                                    }
                                } else {
                                    setAllUnfocused();
                                    repaint();
                                }

                            }

                            if (optionNeedToKill) {
                                if (!teamCanKill().isEmpty()) {
                                    for (Piece p :
                                            teamCanKill()) {

                                        p.setNeedsToKill(true);
                                        repaint();
                                    }

                                }
                            }

                        }

                    } else {
                        if (optionNeedToKill) {
                            if (availableSquaresToKill.isEmpty()) {
                                setAllUnfocused();
                            }
                        } else {
                            setAllUnfocused();
                        }
                        repaint();
                    }
                    repaint();
                } else {
                    showOnlyKillSquares = true;
                    int x = e.getX();
                    int y = e.getY();
                    Point2D.Double currentPoint = new Point2D.Double(x, y);
                    Rectangle2D.Double currentRect = getSquareWithPoint(currentPoint);
                    Piece currentPiece = pieceThatNeedsToKill;
                    if (currentRect != null) {
                        if (!currentPiece.isKing()) { //not king
                            if (getPossibleToGoSquaresRegular(currentPiece, true).contains(currentRect)) {
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
                                        if (getPossibleToGoSquaresRegular(currentPiece, true).size() == 0) {
                                            //can't kill anymore
                                            needsToKill = false;
                                            pieceThatNeedsToKill = null;
                                            showOnlyKillSquares = false;
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
                                        showOnlyKillSquares = false;
                                        currentPiece.setMoving(false);
                                        setAllUnfocused();
                                        repaint();
                                    }
                                } else { //became king
                                    if (getPossibleToGoSquaresKing(currentPiece, true).size() == 0) { //can't kill anymore
                                        needsToKill = false;
                                        pieceThatNeedsToKill = null;
                                        showOnlyKillSquares = false;
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
                            if (getPossibleToGoSquaresKing(currentPiece, true).contains(currentRect)) {
                                moveKing(currentPiece, currentRect);
                                if (getPossibleToGoSquaresKing(currentPiece, true).size() == 0) { //can't kill anymore
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
            } else {
                if (reds.size() > blacks.size()) {
                    winner = 1;
                    isEnded = true;
                    frame.setRedWins(frame.getRedWins() + 1);
                    frame.setScore();
                } else if (reds.size() == blacks.size()) {
                    System.out.println("draw");
                } else {
                    winner = 0;
                    isEnded = true;
                    frame.setBlackWins(frame.getBlackWins() + 1);
                    frame.setScore();
                }

            }
            if (reds.size() <= 0) {
                winner = 0;
                isEnded = true;
                frame.setBlackWins(frame.getBlackWins() + 1);
                frame.setScore();
            } else if (blacks.size() <= 0) {
                winner = 1;
                isEnded = true;
                frame.setRedWins(frame.getRedWins() + 1);
                frame.setScore();
            }
            repaint();
        }

    }

    private class RefreshAction extends AbstractAction {

        RefreshAction() {
            putValue(Action.NAME, "New game");
            putValue(Action.SHORT_DESCRIPTION, "Start a new game");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            frame.refreshComponent();
        }
    }
}
