package com.hgplsoft.vrprelimutensdesktop;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.google.vr.sdk.base.HeadTransform;


import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.scene.Scene;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static com.hgplsoft.vrprelimutensdesktop.MainActivity.BACKGROUND_REALTIMECAM;

public class MyRenderer extends VRRenderer implements SensorEventListener {

    public static String APPID = "VRPD";
    public static Resources res;
    public static MainActivity ctx;
    public static Scene scene;

    // Keep reference to update it later
    public Object3D origo;
    public PDWindow arrow;
    public Object3D arroworigo;
    public Object3D cameraFollower;
    public Cube realWorld;
    SharedPreferences prefs;
    int AppSize = 8;
    int AppRow=2;
    double panelDistance = 18;
    double panelDistanceNear = 1;
    long AnimTime = 300; //ms
    double panelDepthScale = 0.01f;
    boolean zoomWhenLook = true;
    int BackgroundColor = Color.rgb(0xf0,0xf0,0xf0);
    //int BackgroundColor = Color.rgb(0x80,0x80,0x90);
    //int BackgroundColor = Color.rgb(0xe0,0xe0,0xf0);
    //Color.argb(255, 100, 30, 50));

    String ip = "192.168.42.100";
    ArrayList<PDWindow> buffered3DObjects;
    ArrayList<String> deleted3DObjects; //List from delete objects

    public PDWindow selectedobj; //amit kiválasztottunk a xbutton1 + ránézéssel
    public PDWindow lookedobj;  //amire nézünk éppen

    ConnectTask2 connect;
    //mouseConnectTask2 mouse=null;
    public MyRenderer(Context context) {
        super(context);
        ctx = (MainActivity) context;
        res = getContext().getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        try {AppSize = getAppSize();} catch (Exception e){}
        try {AppRow = getAppRow();} catch (Exception e){}
        try {panelDistance = getPanelDistance();} catch (Exception e){}
        try {panelDepthScale = getPanelDepthScale();} catch (Exception e){}
        try {ip = getIP();} catch (Exception e){}
        try {zoomWhenLook =  getZoomWhenLook();} catch (Exception e){}

        buffered3DObjects = new ArrayList<PDWindow>();
        deleted3DObjects = new ArrayList<String>();
    }

    /**
     * This will be called at start time.
     */
    private Texture cameratex;
    //public static PDWindow win;
    @Override
    protected void initScene() {
        scene = getCurrentScene();
        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setNearPlane(0.1);
        getCurrentCamera().setFarPlane(200);
        getCurrentCamera().setFieldOfView(10);
        //fog
        //FogMaterialPlugin.FogParams fog = new FogMaterialPlugin.FogParams(FogMaterialPlugin.FogType.LINEAR,0x999999,5,10);
        //scene.setFog(fog);
        scene.setBackgroundColor(BackgroundColor);

        origo  = new Object3D();
        scene.addChild(origo);
        arroworigo  = new Object3D();
        origo.addChild(arroworigo);




        try {
            DirectionalLight mDirectionalLight = new DirectionalLight(0.4f, 0.7f, 1.0f);
            mDirectionalLight.setColor(1.0f, 1.0f, 1.0f);
            mDirectionalLight.setPower(1);
            mDirectionalLight.setPosition(5,-5,-5);
            scene.addLight(mDirectionalLight);

            PointLight mLight = new PointLight();
            mLight.setPosition(0, 0, 0);
            mLight.setPower(10);
            scene.addLight(mLight);

            //arrow = new PDWindow(PDObjectType.Arrow,arroworigo,1,"arrow",0.1f,0.1f);
            LoaderOBJ arrowParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.arrow_obj);
            arrowParser.parse();
            Object3D objarrow = arrowParser.getParsedObject();
            if (true) {
                Material material = new Material();
                material.setColor(0xff888888);
                material.enableLighting(true);
                material.setDiffuseMethod(new DiffuseMethod.Lambert());
                material.setSpecularMethod(new SpecularMethod.Phong());
                material.setAmbientColor(Color.GRAY);
                material.setAmbientIntensity(0.9, 0.9, 0.9);
                ArrayList<ALight> light = new ArrayList<>(1);
                light.add(mLight);
                material.setLights(light);

                //material.setColorInfluence(0);

                objarrow.setMaterial(material);
            }
            arrow = new PDWindow(PDObjectType.Arrow,arroworigo,1,"arrow",0.1f,0.1f, objarrow,false);

            //Material mat = new Material();
            //mat.setColor(Color.BLACK);
            if (ctx.background==2) {//Prelimutenstheme1
                LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.floor_obj);
                objParser.parse();
                Object3D Umbrella = objParser.getParsedObject();
                if (true) {
                    Material material = new Material();
                    material.setColor(0xffdddddd);
                    material.enableLighting(true);
                    material.setDiffuseMethod(new DiffuseMethod.Lambert());
                    material.setSpecularMethod(new SpecularMethod.Phong());
                    material.setAmbientColor(Color.GRAY);
                    material.setAmbientIntensity(0.5, 0.5, 0.5);
                    //Umbrella.setColor(Color.RED);
                    Umbrella.setMaterial(material);
                }
                Umbrella.setRotation(0, 20, 0);
                Umbrella.setPosition(0, -0, -5);
                scene.addChild(Umbrella);
            }

        }catch (Exception e){
            Log.e(APPID, "initScene: ");

        }


        /*Sphere back = new Sphere(30,30,30);
        Material backmat = new Material();
        backmat.setColor(Color.argb(255, 100, 30, 50));
        back.setMaterial(backmat);
        back.setDoubleSided(true);
        scene.addChild(back);
        /* from Camera */

        /*VR
        if (ctx.background==BACKGROUND_REALTIMECAM) {
            cameraFollower = new Object3D();  //this copy the camera position and rotation in all frame for attaching
            scene.addChild(cameraFollower);
            Material mat = new Material();
            mat.setColor(Color.argb(255, 10, 30, 50));
            //mat.setColorInfluence(0);
            try {
                Bitmap bmp = null;
                while (bmp == null) {
                    bmp = ctx.cam2Bitmap();
                }
                cameratex = new Texture("realworld", bmp);
                mat.addTexture(cameratex);
            } catch (Exception e4) {
            }
            realWorld = new Cube(27);
            realWorld.setScale(1.777, 1, 0.001);
            realWorld.setRotY(180);
            realWorld.setPosition(0, 0, -20);
            realWorld.setMaterial(mat);
            cameraFollower.addChild(realWorld);
        }*/
        connect = new ConnectTask2(this);
    }

    /**
     * This will be called on every frame update.
     */

    Semaphore updatelock = new Semaphore(1);
    @Override
    protected void update() {
/*VR
        if (ctx.background==BACKGROUND_REALTIMECAM){
            cameraFollower.setPosition(getCurrentCamera().getPosition());
            cameraFollower.setOrientation(getCurrentCamera().getOrientation());
            cameratex.setBitmap(ctx.cam2Bitmap());
            TextureManager.getInstance().replaceTexture(cameratex);
        }
*/
        boolean isLookingAt;
        boolean ok = false;
        try {
            //mivel átfedésben is lehetnek ezért ha még ránézek egy objektumra akkor ne érdekeljen, hogy esetleg másik is az útban van.

            //ha m'r n;zek egy objektumra akkor addig amig el;rhet[ addig ne v'ltson m'sikra mert villogni fog
            if (lookedobj != null){
                isLookingAt = isLookingAtObject3(lookedobj.cu);
                if (isLookingAt){
                    ok=true;
                    objectsModulator(false);
                }
            }
            if (!ok) {
                updatelock.acquire();
                objectsModulator(true);
/*
                for (int i = 0; i < buffered3DObjects.size(); i++) {
                    PDWindow obj = buffered3DObjects.get(i);
                    if ((lookedobj==null) || (obj!=lookedobj)) {
                        isLookingAt = isLookingAtObject3(obj.cu);
                        if (isLookingAt) {
                            if (connect != null) {
                                lookedobj = obj;
                                break;
                            }
                        } else {
                            obj.cu.setColor(Color.argb(66, 44, 44, 55));
                        }
                    }
                }*/
                updatelock.release();
            }

        } catch (Exception e){
            updatelock.release();

        }
    }
    int BLOWAMPLITUDE = 4;
    // If true then lookingat this object
    public boolean objectModulator(PDWindow obj){
        float origodistance = 4;
        float v[] = ObjectPicthYaw(obj);  //0=pitch , 1= yaw
        double diffangle = 3*v[1];
        double diff_angle = BLOWAMPLITUDE + origodistance + (Math.cos( ((Math.PI)+diffangle)% (2 * Math.PI) ) * BLOWAMPLITUDE);
        obj.LengthFromOrigo(3+diff_angle);
        return (Math.abs(v[0]) < PITCH_LIMIT) && (Math.abs(v[1]) < YAW_LIMIT);
    }

    public void objectsModulator(boolean abletoNewLookedObject){
        PDWindow obj;
        int cnt = buffered3DObjects.size();
        if (cnt > 0) { //van objektum
            for(int i=0; i<cnt; i++){
                obj = buffered3DObjects.get(i);
                if (objectModulator(obj)){
                    if (abletoNewLookedObject) {
                        Log.e(APPID,"Changed focus");
                        lookedobj = obj;
                    }
                }
            }
        }
    }

    private double cameradriver(PDWindow ob2, double origodistance, double blowamplitude) {
        float v[] = ObjectPicthYaw(ob2);  //0=pitch , 1= yaw
        double diffangle = 3*v[1];
        double a = blowamplitude + origodistance + (Math.cos( ((Math.PI)+diffangle)% (2 * Math.PI) ) * blowamplitude);

        return a;    //#diff angle
    }

    public void deleteBuffered3DObject(String code){
        try {
            updatelock.acquire(); // az update ciklus megakasztására, ne vágjunk fát magunk alatt
            for (PDWindow d : buffered3DObjects) {
                if (d.Code.compareTo(code)==0) {
                    deleted3DObjects.add(code);
                    buffered3DObjects.remove(d);
                    origo.removeChild(d);
                    d.destroy();
                    break;
                }
            }

        }catch (Exception e ){

        }
        finally {
            updatelock.release();
        }
    }

    public boolean isDeletedObject(String code){
        if (deleted3DObjects==null) return false;
        for(String s : deleted3DObjects){
            if (s.compareTo(code)==0){
                return true;
            }
        }
        return false;
    }


    public float[] ObjectPicthYaw(Object3D object) {
        Vector3 v = new Vector3();
        v.multiply(object.getModelViewMatrix()); //adott nézőponthoz képest az objektum
        float pitch = 1, yaw = 1;
        try {
            pitch = (float) Math.atan2(v.y, -v.z);
            yaw = (float) Math.atan2(v.x, -v.z);
        } catch (Exception e){}
        float res[] = new float[2];
        res[0] = Math.abs(pitch);
        res[1] = Math.abs(yaw);
        return res;
    }

    private static final float YAW_LIMIT = 0.25f;
    private static final float PITCH_LIMIT = 0.25f;
    public boolean isLookingAtObject3(Object3D object) {
        float v[] = ObjectPicthYaw(object);
        return (Math.abs(v[0]) < PITCH_LIMIT) && (Math.abs(v[1]) < YAW_LIMIT);
    }

    public boolean checkDelete=false;
    //result> true = újra kell próbálni
    private boolean deleteNotFoundBuffered3DObjectCore(){

        PDWindow obj;
        for (int i=0; i<buffered3DObjects.size(); i++)
        {
            obj = buffered3DObjects.get(i);
            if ( obj._found==false){
//                Log.e(APPID, "DEB5:2" );
                obj.destroy();
                origo.removeChild(obj);
                buffered3DObjects.remove(i);
//                Log.e(APPID, "DEB5:3" );
                return true;
            } else {
//                Log.e(APPID, "DEB5:4" );
            }
        }
        return false;
    }
    private int deleteNotFoundBuffered3DObject() {
        int i=0;
//        Log.e(APPID, "DEB5:0" );
        while (deleteNotFoundBuffered3DObjectCore()){
            i++;
        }
//        Log.e(APPID, "DEB5:1" );
        checkDelete=false;
        return i;
    }

    public String getIP(){
        String s = prefs.getString("ip_address", "");
        return s;
    }
    public String getPort(){
        String s = prefs.getString("ip_port", "");
        return s;
    }
    public int getAppRow(){
        int s = Integer.parseInt(prefs.getString("scene_row_count", ""));
        return s;
    }
    public int getAppSize(){
        int s = Integer.parseInt(prefs.getString("scene_panelsize", ""));
        return s;
    }
    public double getPanelDistance(){
        double s = Double.parseDouble(prefs.getString("scene_paneldistance", ""));
        return s;
    }
    public double getPanelDepthScale(){
        double s = Double.parseDouble(prefs.getString("scene_paneldepthscale", ""));
        return s;
    }
    public boolean getZoomWhenLook(){

        boolean s = prefs.getBoolean("scene_zoomwhenlook", true);
        return s;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}

//region 3D Modell Load Test
        /*
        try {
            Material mat = new Material();
            mat.setColor(Color.BLACK);
            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.umbrella_obj);
            objParser.parse();
            Object3D Umbrella = objParser.getParsedObject();
            Umbrella.setColor(Color.RED);
            Umbrella.setMaterial(mat);
            Umbrella.setPosition(0, 0, -6);
            Umbrella.setRotation(0, 0, 0);
            //origo.addChild(Umbrella);
        }catch (Exception e){}




                // Rotate cube on Y-axis
        // 90 degrees per second
        //arrow.AddRotY(getDeltaTime() * 90);
        //w1.AddRotY(getDeltaTime() * 90);
        //w1.AddRotX(getDeltaTime() * 30);

        */



//endregion

        /*
        try {
            Material matcb = new Material();
            Cube cb = new Cube(2);
            cb.setRotation(180,0,0);
            matcb.setColor(Color.BLACK);
            Texture texcb = new Texture("bitmap111111", BitmapFactory.decodeResource(MyRenderer.res, R.raw.blank));
            matcb.addTexture(texcb);
            cb.setMaterial(matcb);
            cb.setPosition(0, 0, -10);
            origo.addChild(cb);
            //texcb = new Texture("bitmap111111", BitmapFactory.decodeResource(MyRenderer.res, R.raw.arrow));
            texcb.setBitmap(BitmapFactory.decodeResource(MyRenderer.res, R.raw.arrow));
            TextureManager.getInstance().replaceTexture(texcb);
            texcb.setBitmap(BitmapFactory.decodeResource(MyRenderer.res, R.raw.blank));
            TextureManager.getInstance().replaceTexture(texcb);
        }catch (Exception aer){};
        */
//win = new PDWindow(PDObjectType.Window,origo,3f,"test",0.1f,2f);
//win.setX(4);




    //boolean threadmain = Looper.myLooper() == Looper.getMainLooper();

        /*
        while (true){
            try {
                Thread.sleep(100);
            }catch (Exception e){}
        }
        */
//connect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //elvileg vegleg kiszedtem
//mouse = new mouseConnectTask2(this);
//mouse.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /*
        if ( checkDelete){// (checkDelete) {
            try{
                deleteN8tFoundBuffered3DObject();
            }catch (Exception e6){
                Log.e(APPID, "DEB3:" );
            }
        }
        */
//TextureManager.getInstance().replaceTexture(win.tex);

/*
    private double cameradriver(PDWindow ob2, double origodistance, double blowamplitude) {

        Camera ob = getCurrentCamera();
        double rotorigo = ob.getRotY();
        double rotpanel = -ob2.getRotY();
        double diffangle = Math.abs(rotpanel - rotorigo) % (2*Math.PI);

        //original double a = blowamplitude + origodistance + (Math.cos(Math.PI + Math.abs(rotpanel - rotorigo) % (2 * Math.PI)) * blowamplitude);
        double a = blowamplitude + origodistance + (Math.cos( ((Math.PI/2)+diffangle)% (2 * Math.PI) ) * blowamplitude);

        return a;    //#diff angle
    }
    */

