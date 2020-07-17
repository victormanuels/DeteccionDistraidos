package com.google.firebase.samples.apps.mlkit.java.facedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.FileWriter;
import java.util.UUID;


import com.github.mikephil.charting.charts.HorizontalBarChart;
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
import com.opencsv.CSVWriter;

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
    HorizontalBarChart barChart;
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
        barChart = (HorizontalBarChart) view.findViewById(R.id.barchart);
        final String uniqueId = UUID.randomUUID().toString();


        Button buttonSave=(Button)view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    System.out.println("PERMISO");
                }else{
                    System.out.println("NO ");

                }

                saveCSV(uniqueId);
                saveBar(uniqueId);
            }


        });

    }
    private void saveBar(String uniqueId) {
    }
    private void saveCSV(String uniqueId){
        String csv = ( android.os.Environment.getExternalStorageDirectory() + "/"+uniqueId+".csv"); // Here csv file name is MyCsvFile.csv
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(csv));

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"Seconds","Color"});

            for(int i =0;i<seconds.size();i++){
              data.add(new String[]{seconds.get(i)+"",colors.get(i)+""});
            }

            writer.writeAll(data); // data is adding to csv

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawBar() {

        try {
            BarDataSet barDataSet = new BarDataSet(entries, "Inducesmile");
            barDataSet.setColors(colors);
            BarData barData = new BarData(barDataSet);
            barData.setDrawValues(false);
            barChart.getAxisLeft().setDrawLabels(false);
            barChart.getAxisRight().setDrawLabels(false);
            barChart.getXAxis().setDrawLabels(false);

            barChart.getLegend().setEnabled(false);
            barChart.setFitBars(false);
            barChart.setData(barData);
            barChart.setFitBars(true);
            barChart.invalidate();
            barChart.setDrawValueAboveBar(true);

            barChart.getXAxis().setDrawGridLines(false);
            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getAxisRight().setDrawGridLines(false);


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

    boolean flag = false;

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
                long segundos = (faceDetectTime.get(1) - faceDetectTime.get(0)) / 1000;
                if (flag == false) {
                    flag = true;
                    updateChart((float) segundos, Color.BLUE);
                } else {
                    float valsF[] = entries.get(0).getYVals();
                    valsF[valsF.length - 1] = segundos;
                    seconds.set((seconds.size() - 1), (float) segundos);
                    entries.get(0).setVals(valsF);
                    drawBar();
                }

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

                    faceDetectTime.clear();
                    flag = false;
                } catch (Exception ex) {

                    System.out.println(ex);
                }
            }

            if (faceNoDetectTime.size() == 2) {
                faceNoDetectTime.set(1, System.currentTimeMillis());
                long segundos = (faceNoDetectTime.get(1) - faceNoDetectTime.get(0)) / 1000;

                if (flag == false) {
                    flag = true;
                    updateChart((float) segundos, Color.RED);
                } else {
                    float valsF[] = entries.get(0).getYVals();
                    valsF[valsF.length - 1] = segundos;
                    entries.get(0).setVals(valsF);
                    seconds.set((seconds.size() - 1), (float) segundos);
                    drawBar();
                }


            } else {
                faceNoDetectTime.add(System.currentTimeMillis());
            }


        } else {
            if (faceNoDetectTime.size() > 0) {
                try {

                    faceNoDetectTime.clear();
                    flag = false;
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

            float[] myFloat = new float[this.seconds.size()];
            for (int i = 0; i < this.seconds.size(); i++) {
                myFloat[i] = this.seconds.get(i);
            }
            entries.clear();
            entries.add(new BarEntry(0f, myFloat));
            drawBar();
        } catch (Exception es) {
            System.out.println(es);
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}
