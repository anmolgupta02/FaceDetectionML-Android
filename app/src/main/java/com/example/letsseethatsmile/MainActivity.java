package com.example.letsseethatsmile;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button cameraButton;
    private final  static int REQUEST_IMAGE_CAPTURE = 315;
    private FirebaseVisionImage img;
    private FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        cameraButton = findViewById(R.id.button);

        cameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null ){
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap) extra.get("data");
            detectFace(bitmap);
        }

    }

    private void detectFace(Bitmap bitmap) {
        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();
        try{
            img = FirebaseVisionImage.fromBitmap(bitmap);
            detector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);
        }catch (Exception e){
            e.printStackTrace();
        }

        detector.detectInImage(img).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                String resultText = "";
                int i = 1;
                for(FirebaseVisionFace face : firebaseVisionFaces){
                    resultText = resultText.concat("\nThere is/are "+i+" Face/s"+".").concat("\n You are Smiling " +face.getSmilingProbability()*100+"%")
                    .concat("\n Your Left Eye is opened "+face.getLeftEyeOpenProbability()*100+"%.").concat("\n Your Right Eye is opened "+face.getRightEyeOpenProbability()*100+"%.");
                    i++;
                }

                if(firebaseVisionFaces.size() == 0){
                    //Toast.makeText(MainActivity.this,"NO Faces", Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putString(FaceDetector.RESULT_TEXT,"\n I really cannot progress without a FACE!");
                    DialogFragment resultDialog = new ResultDialog();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(), FaceDetector.RESULT_DIALOG);

                }else {
                    Bundle bundle = new Bundle();
                    bundle.putString(FaceDetector.RESULT_TEXT,resultText);
                    DialogFragment resultDialog = new ResultDialog();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(), FaceDetector.RESULT_DIALOG);
                }
            }
        });
    }
}