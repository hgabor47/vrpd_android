package com.hgplsoft.vrprelimutensdesktop;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
/*VR
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC4;
*/

//public class MainActivity extends GvrActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
public class MainActivity extends GvrActivity  {
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;
    public static final int BACKGROUND_REALTIMECAM = 1;
    SharedPreferences prefs;
    public double stereomode=0;
    public int background=0;
    //VR public Mat frame;
    //VR JavaCameraView cam=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST_CODE);
        }

        ///TESTRUN
        //new TestRun(this);
        ///

        if (false) {
/* VR
            try {stereomode = getStereoMode();} catch (Exception e){}
            try {background = getBackground();} catch (Exception e){}
            if (background==BACKGROUND_REALTIMECAM) {
                cam = new JavaCameraView(this, 0);
                cam.setVisibility(View.VISIBLE);
                cam.setCvCameraViewListener(this);
            }
            GvrView view = new GvrView(this);
            view.setStereoModeEnabled(stereomode==0);
            view.setRenderer(new MyRenderer(this));
            setContentView(view);
            if (background==BACKGROUND_REALTIMECAM)
                    view.addView(cam);
*/
        } else {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        try {stereomode = getStereoMode();} catch (Exception e){}
        try {background = getBackground();} catch (Exception e){}
        if (background==BACKGROUND_REALTIMECAM) {
/*VR
            cam = new JavaCameraView(this, 0);

            cam.setVisibility(View.VISIBLE);
            cam.setCvCameraViewListener(this);
            */
        }
        GvrView view = new GvrView(this);
        view.setStereoModeEnabled(stereomode==0);
        view.setRenderer(new MyRenderer(this));
        setContentView(view);
        //VR if (background==BACKGROUND_REALTIMECAM)
        //    view.addView(cam);
    }
    public double getStereoMode(){
        double s = Double.parseDouble(prefs.getString("stereomode_list", "0"));
        return s;
    }
    public int getBackground(){
        int s = Integer.parseInt(prefs.getString("scene_background", "0"));
        return s;
    }
/*VR
    @Override
    public void onCameraViewStopped() {
        frame.release();
    }

    public Bitmap Mat2Bitmap(Mat mRgba){
        if ((mRgba==null) || (background!=BACKGROUND_REALTIMECAM)) {
            return null;
        }
            //mRgba.convertTo(mRgba,-1,0.5,0.8);
        Bitmap bitmap=null;// = Bitmap.createBitmap(cam.getWidth()/4,cam.getHeight()/4, Bitmap.Config.ARGB_8888);
        try {
            bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_4444);
            Utils.matToBitmap(mRgba, bitmap);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        return bitmap;
    }
    public Bitmap cam2Bitmap(){
        return Mat2Bitmap(frame);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //frame=inputFrame.rgba();
        frame=inputFrame.gray();
        //Mat2Bitmap(frame);
        return null;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //frame=new Mat(height,width,CV_8UC4);
        frame=new Mat(height,width,CV_8UC1);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    cam.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void  onResume(){
        super.onResume();
        if ((background==BACKGROUND_REALTIMECAM) && (cam!=null)) {
            if (OpenCVLoader.initDebug()) {
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } else {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if ((background==BACKGROUND_REALTIMECAM) && (cam!=null)) {
            cam.disableView();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if ((background==BACKGROUND_REALTIMECAM) && (cam!=null)) {
            cam.disableView();
        }
    }

*/
}
