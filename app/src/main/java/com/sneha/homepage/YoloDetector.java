package com.sneha.homepage;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class YoloDetector {

    private static final String TAG = "YoloDetector";

    private Interpreter interpreter;
    private List<String> labels;

    private final int INPUT_SIZE = 640;
    private final int NUM_BOXES = 8400;
    private final int NUM_CLASSES = 30;

    private final float CONF_THRESHOLD = 0.25f;

    public YoloDetector(Context context) throws IOException {

        interpreter = new Interpreter(loadModelFile(context));
        labels = loadLabels(context);

        Log.d(TAG, "YOLO model loaded successfully");
    }

    // Load TFLite model
    private MappedByteBuffer loadModelFile(Context context) throws IOException {

        AssetFileDescriptor fileDescriptor =
                context.getAssets().openFd("best_int8.tflite");

        FileInputStream inputStream =
                new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
        );
    }

    // Load class labels
    private List<String> loadLabels(Context context) throws IOException {

        List<String> labelList = new ArrayList<>();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        context.getAssets().open("classes.txt")
                )
        );

        String line;

        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }

        reader.close();

        return labelList;
    }

    // Detection function
    public List<Detection> detect(Bitmap bitmap) {

        Bitmap resizedBitmap =
                Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        ByteBuffer inputBuffer =
                ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);

        inputBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];

        resizedBitmap.getPixels(
                pixels,
                0,
                INPUT_SIZE,
                0,
                0,
                INPUT_SIZE,
                INPUT_SIZE
        );

        // Convert pixels → normalized floats
        for (int pixel : pixels) {

            float r = ((pixel >> 16) & 0xFF) / 255.0f;
            float g = ((pixel >> 8) & 0xFF) / 255.0f;
            float b = (pixel & 0xFF) / 255.0f;

            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        float[][][] output =
                new float[1][NUM_BOXES][5 + NUM_CLASSES];

        interpreter.run(inputBuffer, output);

        List<Detection> detections = new ArrayList<>();

        for (int i = 0; i < NUM_BOXES; i++) {

            float confidence = output[0][i][4];

            if (confidence > CONF_THRESHOLD) {

                float x = output[0][i][0];
                float y = output[0][i][1];
                float w = output[0][i][2];
                float h = output[0][i][3];

                int classIndex = -1;
                float maxScore = 0;

                for (int c = 5; c < 5 + NUM_CLASSES; c++) {

                    if (output[0][i][c] > maxScore) {
                        maxScore = output[0][i][c];
                        classIndex = c - 5;
                    }
                }

                float finalScore = confidence * maxScore;

                if (finalScore > CONF_THRESHOLD) {

                    float left = (x - w / 2) * INPUT_SIZE;
                    float top = (y - h / 2) * INPUT_SIZE;
                    float right = (x + w / 2) * INPUT_SIZE;
                    float bottom = (y + h / 2) * INPUT_SIZE;

                    RectF rect =
                            new RectF(left, top, right, bottom);

                    detections.add(
                            new Detection(
                                    rect,
                                    labels.get(classIndex),
                                    finalScore
                            )
                    );
                }
            }
        }

        return detections;
    }

    // Detection result class
    public static class Detection {

        public RectF box;
        public String label;
        public float confidence;

        public Detection(RectF box,
                         String label,
                         float confidence) {

            this.box = box;
            this.label = label;
            this.confidence = confidence;
        }
    }
}