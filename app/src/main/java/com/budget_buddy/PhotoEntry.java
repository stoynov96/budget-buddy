package com.budget_buddy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
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

    public void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewSize = map.getOutputSizes(SurfaceTexture.class)[0];
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
        previewBuilder.addTarget(readerSurface);

        try {
            cameraDevice.createCaptureSession(Arrays.asList(surface, readerSurface), new CameraCaptureSession.StateCallback() {
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

    void getChangedPreview() {
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

    private void analyzeImage(final FirebaseVisionImage image) {
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                String text = firebaseVisionText.getText();
                Log.i("Image text", text);
                mGraphicOverlay.clear();

                List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();

                for(int i = 0; i < blocks.size(); i++) {
                    List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                    for(int j = 0; j < lines.size(); j++) {
                        List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                        for (int k = 0; k < elements.size(); k++) {
                            GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                            mGraphicOverlay.add(textGraphic);
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
