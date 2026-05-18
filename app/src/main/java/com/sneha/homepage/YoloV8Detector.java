package com.sneha.homepage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class YoloV8Detector {
    private static final String TAG = "YoloV8Detector";
    private static final String MODEL_FILE = "best_int8.tflite";
    private Interpreter interpreter;
    private int inputSize = 416;
    private boolean isInitialized = false;
    private float confidenceThreshold = 0.3f;

    // Class names in order
    private String[] classNames = {
            "Corn Gray leaf spot", "Corn leaf blight", "Corn rust leaf",
            "Potato leaf", "Potato leaf early blight", "Potato leaf late blight",
            "Tomato Early blight leaf", "Tomato Septoria leaf spot", "Tomato leaf",
            "Tomato leaf bacterial spot", "Tomato leaf late blight", "Tomato leaf mosaic virus",
            "Tomato leaf yellow virus", "Tomato mold leaf", "Tomato two spotted spider mites leaf",
            "grape leaf", "grape leaf black rot"
    };

    public YoloV8Detector(Context context) {
        try {
            Log.d(TAG, "=== INITIALIZING YOLO DETECTOR ===");

            // Load model
            MappedByteBuffer modelBuffer = loadModelFile(context);
            interpreter = new Interpreter(modelBuffer);

            int[] inputShape = interpreter.getInputTensor(0).shape();
            if (inputShape.length >= 2) {
                inputSize = inputShape[1];
            }
            Log.d(TAG, "Input size: " + inputSize + "x" + inputSize);

            int[] outputShape = interpreter.getOutputTensor(0).shape();
            Log.d(TAG, "Output shape: " + outputShape[0] + ", " + outputShape[1] + ", " + (outputShape.length > 2 ? outputShape[2] : ""));

            isInitialized = true;
            Log.d(TAG, "✅ Model loaded!");

        } catch (Exception e) {
            Log.e(TAG, "Init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        android.content.res.AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        FileChannel inputChannel = new java.io.FileInputStream(fileDescriptor.getFileDescriptor()).getChannel();
        MappedByteBuffer buffer = inputChannel.map(FileChannel.MapMode.READ_ONLY,
                fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public List<Detection> detect(Bitmap bitmap) {
        List<Detection> detections = new ArrayList<>();

        if (!isInitialized || bitmap == null) {
            return detections;
        }

        try {
            Log.d(TAG, "=== DETECTION START ===");

            // Prepare input
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3);
            inputBuffer.order(ByteOrder.nativeOrder());

            int[] pixels = new int[inputSize * inputSize];
            resizedBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);

            for (int pixel : pixels) {
                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8) & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;
                inputBuffer.putFloat(r);
                inputBuffer.putFloat(g);
                inputBuffer.putFloat(b);
            }
            inputBuffer.rewind();

            // Try different output formats

            // Format 1: [1, 300, 6] or [1, 6, 300]
            try {
                float[][][] output = new float[1][300][6];
                interpreter.run(inputBuffer, output);

                for (int i = 0; i < 300; i++) {
                    float x1 = output[0][i][0];
                    float y1 = output[0][i][1];
                    float x2 = output[0][i][2];
                    float y2 = output[0][i][3];
                    float confidence = output[0][i][4];
                    int classId = (int) output[0][i][5];

                    if (confidence > confidenceThreshold && classId >= 0 && classId < classNames.length) {
                        addDetection(detections, x1, y1, x2, y2, confidence, classId, bitmap);
                    }
                }
                Log.d(TAG, "Used format [1,300,6], found " + detections.size() + " detections");
            } catch (Exception e) {
                Log.d(TAG, "Format [1,300,6] failed, trying transposed");

                // Format 2: [1, 6, 300]
                try {
                    float[][][] output = new float[1][6][300];
                    interpreter.run(inputBuffer, output);

                    for (int i = 0; i < 300; i++) {
                        float x1 = output[0][0][i];
                        float y1 = output[0][1][i];
                        float x2 = output[0][2][i];
                        float y2 = output[0][3][i];
                        float confidence = output[0][4][i];
                        int classId = (int) output[0][5][i];

                        if (confidence > confidenceThreshold && classId >= 0 && classId < classNames.length) {
                            addDetection(detections, x1, y1, x2, y2, confidence, classId, bitmap);
                        }
                    }
                    Log.d(TAG, "Used format [1,6,300], found " + detections.size() + " detections");
                } catch (Exception e2) {
                    Log.d(TAG, "Format [1,6,300] failed, trying byte output");

                    // Format 3: Byte output (INT8 quantized)
                    try {
                        byte[][][] output = new byte[1][300][6];
                        interpreter.run(inputBuffer, output);

                        for (int i = 0; i < 300; i++) {
                            float x1 = (output[0][i][0] & 0xFF) / 255.0f;
                            float y1 = (output[0][i][1] & 0xFF) / 255.0f;
                            float x2 = (output[0][i][2] & 0xFF) / 255.0f;
                            float y2 = (output[0][i][3] & 0xFF) / 255.0f;
                            float confidence = (output[0][i][4] & 0xFF) / 255.0f;
                            int classId = output[0][i][5] & 0xFF;

                            if (confidence > confidenceThreshold && classId >= 0 && classId < classNames.length) {
                                addDetection(detections, x1, y1, x2, y2, confidence, classId, bitmap);
                            }
                        }
                        Log.d(TAG, "Used byte format [1,300,6], found " + detections.size() + " detections");
                    } catch (Exception e3) {
                        Log.e(TAG, "All output formats failed: " + e3.getMessage());
                    }
                }
            }

            Log.d(TAG, "Total detections: " + detections.size());

        } catch (Exception e) {
            Log.e(TAG, "Detection error: " + e.getMessage());
            e.printStackTrace();
        }

        return detections;
    }

    private void addDetection(List<Detection> detections, float x1, float y1, float x2, float y2,
                              float confidence, int classId, Bitmap bitmap) {
        float left = x1 * bitmap.getWidth();
        float top = y1 * bitmap.getHeight();
        float right = x2 * bitmap.getWidth();
        float bottom = y2 * bitmap.getHeight();

        left = Math.max(0, Math.min(bitmap.getWidth(), left));
        top = Math.max(0, Math.min(bitmap.getHeight(), top));
        right = Math.max(0, Math.min(bitmap.getWidth(), right));
        bottom = Math.max(0, Math.min(bitmap.getHeight(), bottom));

        if (left < right && top < bottom) {
            detections.add(new Detection(
                    new RectF(left, top, right, bottom),
                    confidence,
                    classId,
                    classNames[classId]
            ));
            Log.d(TAG, "✅ " + classNames[classId] + " (" + String.format("%.1f", confidence * 100) + "%)");
        }
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
        isInitialized = false;
    }

    public static class Detection {
        private RectF boundingBox;
        private float confidence;
        private int classId;
        private String className;

        public Detection(RectF boundingBox, float confidence, int classId, String className) {
            this.boundingBox = boundingBox;
            this.confidence = confidence;
            this.classId = classId;
            this.className = className;
        }

        public RectF getBoundingBox() { return boundingBox; }
        public float getConfidence() { return confidence; }
        public int getClassId() { return classId; }
        public String getClassName() { return className; }
    }
}