package com.example.notedell.vrcamera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;



class CameraView {


    private Camera mCamera;
    private Context context;
    private CameraPreview mPreview;
    private FrameLayout preview;
    private int deviceHeight;

    CameraView(Context c, FrameLayout preview, int deviceHeight) {
        context = c;
        this.preview = preview;
        this.deviceHeight = deviceHeight;
    }


    void createCamera() {
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Setting the right parameters in the camera
        Camera.Parameters params = mCamera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(params);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(context, mCamera);


        // Calculating the width of the preview so it is proportional.
        float widthFloat = (float) (deviceHeight) * 4 / 3;
        int width = Math.round(widthFloat);

        // Resizing the LinearLayout so we can make a proportional preview. This
        // approach is not 100% perfect because on devices with a really small
        // screen the the image will still be distorted - there is place for
        // improvment.
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, deviceHeight);
        preview.setLayoutParams(layoutParams);

        // Adding the camera preview after the FrameLayout and before the button
        // as a separated element.
        preview.addView(mPreview, 0);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            // attempt to get a Camera instance
            c = android.hardware.Camera.open();
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }

        // returns null if camera is unavailable
        return c;
    }

    void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
// no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
