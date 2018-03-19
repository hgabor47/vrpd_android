package com.hgplsoft.vrprelimutensdesktop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.animation.Interpolator;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.CubicBezierCurve3D;
import org.rajawali3d.curves.LinearBezierCurve3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;

import static com.hgplsoft.vrprelimutensdesktop.MyRenderer.APPID;
import static com.hgplsoft.vrprelimutensdesktop.MyRenderer.scene;

/**
 * Created by horvath3ga on 2017.10.06..
 */

public class PDObject3D extends Object3D {

    public Object3D dummyx;
    public Object3D dummyy;
    public Object3D dummyz;
    Texture tex;
    Material mat;
    public double Rx,Ry,Rz;
    double NearZ; // Az objektumra vonatkozó közelítés mértéke egyedileg állítható

    public PDObject3D(double NearZ){
        dummyx = new Object3D();
        dummyy = new Object3D();    // Z - Y - X - Object
        dummyz = new Object3D();
        dummyz.addChild(dummyy);  //o1 add o2
        dummyy.addChild(dummyx);  //o2 add o3
        this.NearZ = NearZ;
    }

    public void RotZ(double value){ //dummyY   PercentRotX
        Rz = value;
        dummyx.setRotZ(value);
    }
    public void RotY(double value){ //dummy
        Ry = value;
        dummyy.setRotY(value);
    }
    public void RotX(double value){
        Rx = value;
        dummyz.setRotX(value);
    }
    public void absoluteRotZ(double value){ //dummyY   PercentRotX
        //dummyx.setRotation(0,0,value);
        //dummyx.setRotation(0,0,1,value);
        dummyx.setRotZ(value-Rz);
        Rz = value;
    }
    public void absoluteRotY(double value){ //dummy
        //dummyy.setRotation(0,value,0);
        //dummyy.setRotation(0,1,0,value);
        dummyy.setRotY(value-Ry);
        Ry = value;
    }
    public void absoluteRotX(double value){
        //dummyz.setRotation(value,0,0);
        //dummyz.setRotation(1,0,0,value);
        dummyz.setRotX(value-Rx);
        Rx = value;
    }

    @Override
    public double getRotX(){  //percent
        return Rx;//dummyz.getRotX();
    }
    @Override
    public double getRotY(){

        return Ry; //dummyy.getRotY();
    }
    @Override
    public double getRotZ(){
        return Rz; //dummyx.getRotZ();
    }

    /*
    public void AddRotX(double value){
        dummyx.rotate(Vector3.Axis.X,value);
    }
    public void AddRotY(double value){
        dummyy.rotate(Vector3.Axis.Y,value);
    }
    public void AddRotZ(double value){
        dummyz.rotate(Vector3.Axis.Z,value);
    }
*/
    public Object3D Base(){
        return dummyz;
    }

    public void destroy(){

        try {dummyx.destroy();}catch (Exception e){}
        try {dummyy.destroy();}catch (Exception e){}
        try {dummyz.destroy();}catch (Exception e){}
        dummyx=null;
        dummyy=null;
        dummyz=null;
        tex=null;
        mat = null;
    }

    // A window pixszélessége alapján az aktuáls objektum és annak távolsága figyelembe vételével
    // visszad egy pixelhez tartozó szögelfordulás értéket.
    // ennek winwidth szerese az adott objektum jobboldalához való illesztést
    // ennek 0 szorosa a baloldalhoz illesztést jelenti

    public static double ContentWidthPixelToAngle(double objectwidth, double distance, double winwidth){
        return Math.abs(2.0*Math.atan((objectwidth/2.0)/distance)/winwidth);
    }

    public Animation3D sheepAnim;
    CustomInterpolator sheepip;
    double destinationFromOrigo;
    public void goTo(double lengthFromOrigo,long duration){
        //if ((sheepAnim==null)||(sheepAnim.isEnded()))
        if (!isAnim()) {
            //double z = this.getZ();
            if (destinationFromOrigo != lengthFromOrigo) {
                destinationFromOrigo = lengthFromOrigo;
                sheepAnim = new TranslateAnimation3D(new Vector3(this.getX(), this.getY(), lengthFromOrigo));
                sheepAnim.setDurationMilliseconds(duration);
                sheepip = new CustomInterpolator(this, lengthFromOrigo);
                sheepip.enterSlowMode();
                sheepAnim.setInterpolator(sheepip);
                sheepAnim.setRepeatMode(Animation.RepeatMode.NONE);
                sheepAnim.setTransformable3D(this);
                scene.registerAnimation(sheepAnim);
                sheepAnim.play();
            }
        }
    }
    public boolean isAnim(){
        boolean ret = false;
        if (sheepAnim!=null) {
            ret = sheepAnim.isPlaying();
            /*if (!ret) {
                if (Math.abs(getZ() - destinationFromOrigo) > 0.05) {
                    ret = true; //meg nem ert a vegere
                }
            }*/
        }
        return ret;
    }


    public class CustomInterpolator implements Interpolator {

        public boolean slowMode;
        float lastInput;
        float lastInputBeforeSlowed;
        double endDistance;
        Object3D obj;

        public CustomInterpolator(Object3D obj,double endDistance){
            this.endDistance = endDistance;
            this.obj = obj;
        }

        @Override
        public float getInterpolation(float input) {
            if (!slowMode) {
                //Should be edited
                lastInput = input;
                return input;
            } else {
                //double z = obj.getZ();
                //if (Math.abs(z-endDistance)<0.05) {
                //    endSlowMode();
                //}
                float ret = (input - lastInputBeforeSlowed) * .5f + lastInputBeforeSlowed;
                return ret;
            }
        }

        public void enterSlowMode() {
            slowMode = true;
            lastInputBeforeSlowed = lastInput;
        }

        public void endSlowMode() {
            slowMode = false;
            //Should be edited
        }
    }

}


/*

     public void goTo(double lengthFromOrigo){
//        RotY(angX);
//        RotZ(angY);
        CompoundCurve3D redBezierPath = new CompoundCurve3D();
        redBezierPath.addCurve(new LinearBezierCurve3D(
                this.getPosition(), new Vector3(this.getX(), this.getY(), lengthFromOrigo)));

    }
    public void goTo(double x,double y, double z){
        CompoundCurve3D redBezierPath = new CompoundCurve3D();
        redBezierPath.addCurve(new LinearBezierCurve3D(
                this.getPosition(), new Vector3(x, y, z)));
    }




* */