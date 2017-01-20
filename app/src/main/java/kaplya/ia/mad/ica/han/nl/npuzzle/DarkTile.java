package kaplya.ia.mad.ica.han.nl.npuzzle;

/**
 * Created by Iv on 17-1-2017.
 */
public class DarkTile {
    public int newPosition;
    public int oldPosition;
    DarkTile(){

    }
    public void setNewPosition(int newPosition){
        this.newPosition = newPosition;
    }
    public void setOldPosition(int oldPosition){
        this.oldPosition = oldPosition;
    }

    public int getNewPosition(){
        return this.newPosition;
    }
    public int getOldPosition(){
        return this.oldPosition;
    }
}
