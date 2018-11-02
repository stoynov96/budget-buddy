package com.budget_buddy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
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

import java.util.Arrays;
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
    public void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // The 0th element of the output sizes is supposedly the largest resolution available
            previewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            // swapping the height and the width is the only way I got accurate boxes
            mGraphicOverlay.setCameraInfo(previewSize.getHeight(), previewSize.getWidth(), characteristics.get(CameraCharacteristics.LENS_FACING));
            manager.openCamera(cameraId, stateCallback, null);
        } catch (Exception e) {

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
            openCamera();
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
