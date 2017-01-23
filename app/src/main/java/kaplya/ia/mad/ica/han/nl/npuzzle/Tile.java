package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Iv on 11-5-2015.
 */
public class Tile{
    public int imgId;
    public Bitmap img;
    public Bitmap bitmapOriginalImage;
    public Boolean empty;
    public Boolean isChanged = false;
    public Tile(int imgId, Bitmap img, Boolean empty){
        Log.d("PuzzleAdapter", "Constructor of the Title was called");
        this.imgId= imgId;
        this.img = img;
        this.empty = empty;
        backupOriginalImage();
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
    public void backupOriginalImage(){
        Log.d("PuzzleAdapter", "Backuping...");
        bitmapOriginalImage = img.copy(Bitmap.Config.ARGB_8888, true);
        Log.d("PuzzleAdapter", "Backuping done");
        //bitmapOriginalImage = this.img;
    }
    public void setIsChanged(){
        isChanged = true;
    }
    public Boolean wasChanged(){
      return isChanged;
    }
    public void restoreOriginalImage(){
        Log.d("PuzzleAdapter", "Restoring image " + this.imgId);
        this.img = bitmapOriginalImage.copy(Bitmap.Config.ARGB_8888, true);
        isChanged = false;
    }
    public void setTemporaryImage(Bitmap bitmap){
        //backupOriginalImage();
        //Log.d("PuzzleAdapter", "Image was backuped " + imgId);
        this.img = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Log.d("PuzzleAdapter", "New image has been set " + imgId);
    }
}
