package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Iv on 11-5-2015.
 */
public class Tile{
    public int imgId;
    public Bitmap img;
    public Boolean empty;
    public Tile(int imgId, Bitmap img, Boolean empty){
        this.imgId= imgId;
        this.img = img;
        this.empty = empty;
    }
    public Bitmap getTileBitmap(){
        return img;
    }
    public int getTileId(){
        return imgId;
    }
    public void setTileBitmap(Bitmap img){
        this.img = img;
    }
    public Boolean isEmpty(){
        return empty;
    }
    public void setImgId(int imgId){
        this.imgId = imgId;
    }
}
