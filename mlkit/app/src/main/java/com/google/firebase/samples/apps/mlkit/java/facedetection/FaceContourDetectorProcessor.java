package com.google.firebase.samples.apps.mlkit.java.facedetection;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.samples.apps.mlkit.R;
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic;
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.java.LivePreviewActivity;
import com.google.firebase.samples.apps.mlkit.java.VisionProcessorBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Face Contour Demo.
 */
public class FaceContourDetectorProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceContourDetectorProc";

    private final FirebaseVisionFaceDetector detector;
    private ArrayList<Long> faceDetectTime = new ArrayList<>();
    private ArrayList<Long> faceNoDetectTime = new ArrayList<>();
    LivePreviewActivity view;
    BarChart barChart;
    ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
    ;
    ArrayList<Integer> colors = new ArrayList<>();
    ArrayList<Float> seconds = new ArrayList<>();

    public FaceContourDetectorProcessor(LivePreviewActivity view) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        this.view = view;


        barChart = (BarChart) view.findViewById(R.id.barchart);

        entries.add(new BarEntry(0, new float[]{5, 5, 4, 5}));
        BarDataSet barDataSet = new BarDataSet(entries, "Inducesmile");
        //  barDataSet.setColors(Color.BLUE, Color.RED, Color.BLUE);
        barDataSet.setBarBorderWidth(0.9f);
        BarData barData = new BarData(barDataSet);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        barChart.setData(barData);
        barChart.setFitBars(true);
        // barChart.animateXY(5000, 5000);
        barChart.invalidate();

        barChart.setHighlightFullBarEnabled(false);

        entries.clear();
        barDataSet.notifyDataSetChanged();
        BarEntry bar = new BarEntry(3, new float[]{5,5,5,5, 5});

        try{
            entries.add(bar);

        }catch (Exception ssss){
            System.out.println(ssss);
        }

        barChart.notifyDataSetChanged();
        barChart.invalidate();

        makeBar();

    }


    private void makeBar() {

        try {


        } catch (Exception ss) {

            System.out.println(ss);
        }
        //  barChart.notifyDataSetChanged();
        //barChart.invalidate();
    }


    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Contour Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        Task<List<FirebaseVisionFace>> det = detector.detectInImage(image);
        return det;
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }

        boolean faceD = false;
        for (int i = 0; i < faces.size(); ++i) {
            faceD = true;
            if (faceDetectTime.size() == 2) {
                faceDetectTime.set(1, System.currentTimeMillis());

            } else {
                faceDetectTime.add(System.currentTimeMillis());
            }
            FirebaseVisionFace face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face);
            graphicOverlay.add(faceGraphic);
        }

        if (!faceD) {
            if (faceDetectTime.size() > 0) {
                try {
                    if (faceDetectTime.size() > 1) {
                        long segundos = (faceDetectTime.get(1) - faceDetectTime.get(0)) / 1000;


                        updateChart((float) segundos, Color.BLUE);
                    }
                    faceDetectTime.clear();
                } catch (Exception ex) {

                    System.out.println(ex);
                }
            }

            if (faceNoDetectTime.size() == 2) {
                faceNoDetectTime.set(1, System.currentTimeMillis());
            } else {
                faceNoDetectTime.add(System.currentTimeMillis());
            }


        } else {
            if (faceNoDetectTime.size() > 0) {
                try {
                    if (faceNoDetectTime.size() > 1) {
                        long segundos = (faceNoDetectTime.get(1) - faceNoDetectTime.get(0)) / 1000;
                        updateChart((float) segundos, Color.RED);

                    }
                    faceNoDetectTime.clear();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }

        }


        graphicOverlay.postInvalidate();


    }

    private void updateChart(float segundos, int color) {
        try {
            this.colors.add(color);
            this.seconds.add(segundos);

            //   entries.clear();
            float[] myFloat = new float[this.seconds.size()];

            for (int i = 0; i < this.seconds.size(); i++) {
                myFloat[i] = this.seconds.get(i);
            }
            this.seconds.toArray();

            entries.add(new BarEntry(0, new float[]{10, 10, 10, 10}));
            barChart.notifyDataSetChanged();
            barChart.invalidate();
        } catch (Exception es) {
            System.out.println(es);
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}
