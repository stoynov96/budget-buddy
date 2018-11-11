package com.budget_buddy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.hardware.camera2.CameraCaptureSession;
import android.util.Log;
import android.util.Size;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.budget_buddy.TextGraphic;
import com.budget_buddy.GraphicOverlay;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;


import android.util.SparseIntArray;
import android.view.TextureView;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// http://coderzpassion.com/android-working-camera2-api/

public class PhotoEntry extends AppCompatActivity {

    private Size previewSize;
    private TextureView cameraTextureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private ImageReader mImageReader;
    private GraphicOverlay mGraphicOverlay;
    // "This" makes it so we can access this activity across internal callbacks
    private AppCompatActivity This = this;
    private static final int CAMERAREQUEST = 1;
    private static final int MAX_PREVIEW_WIDTH = 480;
    private static final int MAX_PREVIEW_HEIGHT = 640;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_entry);

        mGraphicOverlay = findViewById(R.id.graphic_overlay);

        cameraTextureView =(TextureView) findViewById(R.id.textureView);
        cameraTextureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    /**
     * Camera2 boilerplate
     */
    public void openCamera(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // The 0th element of the output sizes is supposedly the largest resolution available
            //previewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            setUpCameraOutput(width, height);
            // swapping the height and the width is the only way I got accurate boxes
            mGraphicOverlay.setCameraInfo(previewSize.getHeight(), previewSize.getWidth(), characteristics.get(CameraCharacteristics.LENS_FACING));
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERAREQUEST);
            }
            else {
                // permission already granted
                manager.openCamera(cameraId, stateCallback, null);
            }
            Point displaySize = new Point();
            this.getWindowManager().getDefaultDisplay().getSize(displaySize);
            Log.i("Screen", "" + displaySize.x);
        } catch (Exception e) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERAREQUEST: {
                try {
                    CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    String cameraId = manager.getCameraIdList()[0];
                    // permission granted by user, open the camera
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        manager.openCamera(cameraId, stateCallback, null);
                    }
                    // permission not granted by user, do not open the camera
                    else {

                    }
                    return;
                }
                catch (Exception e) {

                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraDevice!=null) {
            cameraDevice.close();
        }
    }

    /**
     * This function configures the two surfaces that receive frame buffers and passes them to the previewBuilder.
     * The camera session is then kicked off and the related threads are initialized in the onConfigured callback.
     */
    void startCamera() {
        if(cameraDevice == null || !cameraTextureView.isAvailable() || previewSize == null) {
            return;
        }

        SurfaceTexture texture = cameraTextureView.getSurfaceTexture();
        if(texture == null) {
            return;
        }

        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface surface = new Surface(texture);

        mImageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 1);
        Surface readerSurface = mImageReader.getSurface();

        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (Exception e) {

        }

        previewBuilder.addTarget(surface);
        // set the image reader surface as the target of the preview as well so we can analyze the frames
        previewBuilder.addTarget(readerSurface);

        try {
            cameraDevice.createCaptureSession(Arrays.asList(surface, readerSurface), new CameraCaptureSession.StateCallback() {

                /**
                 * This kicks of the threads used to generate the preview and analyze the frames.
                 * @param session
                 */
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    previewSession = session;
                    setupImageReader();
                    getChangedPreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, null);
        } catch (Exception e) {

        }
    }

    /**
     * This uses preview builder to continuosly update the cameraTextureView with the next frame.
     */
    private void getChangedPreview() {
        if( cameraDevice == null) {
            return;
        }

        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("changed Preview");
        thread.start();
        Handler handler = new Handler(thread.getLooper());

        try {
            previewSession.setRepeatingRequest(previewBuilder.build(), null, handler);
        } catch (Exception e) {

        }
    }

    /**
     * This method initializes the image reader, which is responsible for reading in frame buffers and
     * analyzing them. In its analysis, every frameModulo'th frame it passes the frame buffer to firebase
     * for text analysis.
     */
    private void setupImageReader() {
        try {

            final int rotation = getRotationCompensation(cameraDevice.getId(), this, this);
            ImageReader.OnImageAvailableListener mImageAvailable = new ImageReader.OnImageAvailableListener() {

                final int frameModulo = 5;
                int frameCounter = 0;
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireNextImage();

                        if(frameCounter % frameModulo == 0) {
                            FirebaseVisionImage fbImage = FirebaseVisionImage.fromMediaImage(image, rotation);
                            analyzeImage(fbImage);
                        }
                        frameCounter = (frameCounter + 1) % frameModulo;
                        image.close();
                    } catch (Exception e) {

                    }
                }
            };

            HandlerThread handlerThread=new HandlerThread("analyzepicture");
            handlerThread.start();
            final Handler handler = new Handler(handlerThread.getLooper());

            mImageReader.setOnImageAvailableListener(mImageAvailable, handler);
        } catch (Exception e) {

        }

    }

    /**
     * This takes the height/width of the textureView and passes those sizes to an output size optimizer.
     * It finds the max necessary size by determining the max resolution of the device but this resolution is
     * capped to MAX_PREVIEW_WIDTH x MAX_PREVIEW_HEIGHT (probably 720p or 480p).
     * Right now, this function just sets the previewSize but it could do other things.
     * @param width
     * @param height
     */
    private void setUpCameraOutput(int width, int height) {
        Activity activity = this;
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            //CameraCharacteristics characteristics = manager.getCameraIdList()[0];
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if(maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if(maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            StreamConfigurationMap map = manager.getCameraCharacteristics(manager.getCameraIdList()[0]).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, maxPreviewHeight, maxPreviewWidth);
            Log.i("Sizing", "size  " +previewSize);
        } catch (Exception e) {

        }
    }

    /**
     * This function iterates through an array of possible output resolutions for the camera.
     * It looks for all the resolutions (smaller than maxRes) that are at least as large of the texture view
     * and adds those to bigEnough. All other resolutions are added to the notBigEnough array.
     * It returns the smallest of the bigEnough resolutions but if none, it returns the largest notBigEnough.
     * @param choices
     * @param textureViewWidth
     * @param textureViewHeight
     * @param maxHeight
     * @param maxWidth
     * @return
     */
    private Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxHeight, int maxWidth) {
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();

        for (Size option: choices) {
            Log.i("Sizing", "option: height: " + option.getHeight() + " width: " + option.getWidth());
            if(option.getHeight() <= maxWidth && option.getWidth() <= maxHeight) {
                // compare the height of the option to the width of the texture view and vice versa
                // since the height is supposedly the smaller dimension
                if(option.getHeight() >= textureViewWidth && option.getWidth() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        Comparator<Size> compareBySize = new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                return Long.signum((long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
            }
        };

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, compareBySize);
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, compareBySize);
        }

        Log.e("Size", "Couldn't find any suitable preview size");
        return choices[0];
    }

    /**
     * This function takes in an image and begins analysis on it. See onSuccess() in the OnSuccessListener
     * @param image
     */
    private void analyzeImage(final FirebaseVisionImage image) {
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {

            // this needs to be globally accessible across the threads so they can see
            // when one was successful
            Double price;

            /**
             * This function reads through all of the blocks of text in result of the image analysis.
             * Blocks are composed of lines, which are composed of elements which are individual words.
             * Once total is found, it looks to see if it can find a box that's adjacent to it, which
             * contains the price.
             * This function is synchronized to prevent race conditions. This is less efficient than
             * the other way but this function is hella fast compared to actually doing computer vision.
             * @param firebaseVisionText
             */
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                String text = firebaseVisionText.getText();
                Log.i("Image text", text);

                // synchronizing mGraphicOverlay ensures that this section cannot run concurrently, this prevents a race condition
                // where the boxes are cleared prematurely after the total is found because the next frame is also processed
                synchronized (mGraphicOverlay) {

                    if (price != null) { return; }

                    mGraphicOverlay.clear();

                    List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();

                    Rect totalRect = null;

                    for (int i = 0; i < blocks.size(); i++) {
                        List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                        for (int j = 0; j < lines.size(); j++) {
                            List<FirebaseVisionText.Element> elements = lines.get(j).getElements();

                            for (int k = 0; k < elements.size(); k++) {
                                // draw box around the word
                                GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                                mGraphicOverlay.add(textGraphic);

                                if (totalRect == null) {
                                    if (elements.get(k).getText().equals("TOTAL")) {
                                        totalRect = elements.get(k).getBoundingBox();
                                    }
                                } else {

                                    Rect elBox = elements.get(k).getBoundingBox();
                                    // check if this box is on the level as the total box, if so assume this box contains the price
                                    if (elBox.top <= totalRect.bottom && totalRect.top <= elBox.bottom) {

                                        try {
                                            // when the price is found, stop the camera
                                            price = Double.parseDouble(elements.get(k).getText());
                                            previewSession.stopRepeating();
                                            mImageReader.close();
                                            Intent manualEntryIntent = new Intent(This, ManualEntry.class);
                                            manualEntryIntent.putExtra("price", elements.get(k).getText());
                                            startActivity(manualEntryIntent);
                                            return;
                                        } catch (Exception e) {

                                        }

                                    }
                                }
                            }
                        }
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Image text", "failure");
            }
        });
    }

    /**
     * This is the method of accounting for rotation when passing images into firebase.
     * @param cameraId
     * @param activity
     * @param context
     * @return
     * @throws CameraAccessException
     */
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                //Log.e(TAG, "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            startCamera();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

}