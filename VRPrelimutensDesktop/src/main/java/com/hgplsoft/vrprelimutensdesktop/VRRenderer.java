package com.hgplsoft.vrprelimutensdesktop;

import android.content.Context;
import android.view.MotionEvent;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Base class for implementing VR renderer. This handles rendering for Google VR.
 * Based on https://github.com/Rajawali/Rajawali/blob/master/vr/src/main/java/org/rajawali3d/vr/renderer/VRRenderer.java
 */
public abstract class VRRenderer extends Renderer implements GvrView.StereoRenderer {

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 200.0f;

    private final Matrix4 eyeMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();

    /*
     * These are for update()
     */
    private HeadTransform headTransform;
    private long ellapsedRealtime;
    private double deltaTime;

    public VRRenderer(Context context) {
        super(context);
    }

    /**
     * Performs every frame update in subclass.
     */
    protected abstract void update();

    @Override
    public final void onNewFrame(HeadTransform headTransform) {
        this.headTransform = headTransform;
        onRenderFrame(null);
    }

    @Override
    protected final void onRender(long ellapsedRealtime, double deltaTime) {
        this.ellapsedRealtime = ellapsedRealtime;
        this.deltaTime = deltaTime;

        update();
    }

    @Override
    public void onDrawEye(Eye eye) {

        Camera camera = getCurrentCamera();

        // Update projection matrix
        projectionMatrix.setAll(eye.getPerspective(Z_NEAR, Z_FAR));
        camera.setProjectionMatrix(projectionMatrix);

        // Update camera position and rotation
        eyeMatrix.setAll(eye.getEyeView()).inverse();
        //eyeMatrix.setAll(eye.getEyeView());//.inverse()
        camera.setRotation(eyeMatrix);
        camera.setPosition(eyeMatrix.getTranslation());

        render(ellapsedRealtime, deltaTime);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        // Unused
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        onRenderSurfaceSizeChanged(null, width, height);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        onRenderSurfaceCreated(eglConfig, null, -1, -1);
    }

    @Override
    public void onRendererShutdown() {
        onRenderSurfaceDestroyed(null);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        // Unused
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        // Unused
    }

    /*
     * Getters for update()
     */

    public HeadTransform getHeadTransform() {
        return headTransform;
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public long getEllapsedRealtime() {
        return ellapsedRealtime;
    }
}
