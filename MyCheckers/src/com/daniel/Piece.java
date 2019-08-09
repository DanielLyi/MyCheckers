package com.daniel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

public class Piece extends ImageIcon {

    private int team;
    private Rectangle2D.Double pos;
    private boolean inFocus;
    private boolean isKing;
    private boolean isMoving;
    private boolean needsToKill;

    /**
     * Construct a new com.daniel.com.daniel.Piece object
     * @param team int value 0 is black and 1 is red. Any other value sets to be -1
     * @param icon icon of the piece
     * @param rect the rectangle where you want the piece to be in
     */
    public Piece(int team, ImageIcon icon, Rectangle2D.Double rect){
        if(team==0||team==1) {
            this.team = team;
        }
        else{
            team = -1;
        }
        super.setImage(icon.getImage());
        this.pos = rect;
        needsToKill = false;
    }

    public void setPosition(Rectangle2D.Double newPosition){
        this.pos = newPosition;
    }

    public Rectangle2D.Double getPosition(){
        return this.pos;
    }

    public boolean isHere(Rectangle2D.Double rect){
        if(rect.equals(this.pos)){
            return true;
        }
        return false;
    }

    public Image getImage(){
        return super.getImage();
    }

    public void setNeedsToKill(boolean needsToKill) {
        this.needsToKill = needsToKill;
    }

    public boolean isNeedToKill() {
        return needsToKill;
    }

    public void setToKing(){
        this.isKing = true;
        if(team==0){
            super.setImage(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(
                    "images/black_piece_king.png"))).getImage());
        } else if(team==1){
            super.setImage(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(
                    "images/red_piece_king.png"))).getImage());
        }
    }

    public boolean isKing(){
        return isKing;
    }

    public int getTeam() {
        return team;
    }

    public void setFocused(boolean b){
        inFocus =b;
    }

    public boolean isInFocus(){
        return inFocus;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public String toString(){
        return "[com.daniel.com.daniel.Piece:team=" + team+",Position="+pos+",inFocus="+inFocus+",isKing="+isKing+",isMoving="+
                isMoving + "]";
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public static ImageIcon getDefIconFromTeam(int t){
        if(t==0){
            return new ImageIcon(Objects.requireNonNull(Start.class.getClassLoader().getResource(
                    "images/black_piece.png")));
        }
        else if(t==1){
            return new ImageIcon(Objects.requireNonNull(Start.class.getClassLoader().getResource(
                    "images/red_piece.png")));
        }
        else{
            return null;
        }
    }


}
