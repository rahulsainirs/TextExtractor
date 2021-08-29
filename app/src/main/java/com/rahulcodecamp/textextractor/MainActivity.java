package com.rahulcodecamp.textextractor;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    TextView dataTextView;
    Button captureButton, copyTextButton, getPanNumber;
    private static final int REQUEST_CAMERA_CODE = 100;
    Bitmap bitmap;
    StringBuilder stringBuilder;

    String panNumberRegex="\\w\\w\\w\\w\\w\\d\\d\\d\\d.*";
    boolean callPanNumber = false;
    long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataTextView = findViewById(R.id.dataTextView);
        captureButton = findViewById(R.id.captureButton);
        copyTextButton = findViewById(R.id.copyTextButton);
        getPanNumber = findViewById(R.id.getPanNumber);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            },REQUEST_CAMERA_CODE);
        }

        copyTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String scanned_text = dataTextView.getText().toString();
                copyToClipBoard(scanned_text);
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);

            }
        });

        getPanNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
                callPanNumber = true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    getTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void getTextFromImage(Bitmap bitmap){

        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if (!recognizer.isOperational()){
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        }
        else{
            dataTextView.setText("");
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            stringBuilder = new StringBuilder();
            if (!callPanNumber) {
                for (int i = 0; i < textBlockSparseArray.size(); i++) {
                    TextBlock textBlock = textBlockSparseArray.valueAt(i);
                    stringBuilder.append(textBlock.getValue());
                    stringBuilder.append("\n");
                    dataTextView.setText(stringBuilder.toString());
                }
            }
                else {
                for (int i = 0; i < textBlockSparseArray.size(); i++) {
                    TextBlock textBlock = textBlockSparseArray.valueAt(i);

                    if (textBlock.getValue().matches(panNumberRegex)){
                        stringBuilder.append(textBlock.getValue());
                        Toast.makeText(this, "pan number found", Toast.LENGTH_SHORT).show();
                        dataTextView.setText("Pan Number: "+stringBuilder.toString());
                        callPanNumber = false;
                        break;
                    }
                }
                    }

            if (dataTextView.getText().toString().isEmpty()){
                Toast.makeText(this, "can't recognize please adjust it properly", Toast.LENGTH_SHORT).show();
            }
            }
            copyTextButton.setVisibility(View.VISIBLE);

        }

    private void copyToClipBoard(String text){
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied data", text);
        clipBoard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied to ClipBoard", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 1000 > System.currentTimeMillis()){
            this.moveTaskToBack(true);   // this line is used to minimize app not to exit
        }
        else{
            Toast.makeText(getBaseContext(),
                    "Double tap to minimize!", Toast.LENGTH_SHORT)
                    .show();
        }
        back_pressed = System.currentTimeMillis();
    }
}