package com.example.homerenoai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    //variable initialization
    String path;
    Uri getUri;

    private int CODE = 100;
    private static final int REQUEST_CAMERA_PERM = 0;
    Button takePick;
    Button gallery;
    Button next;
    ImageView displayPic;
    Uri passing_image;
    Intent passingIntent;
    private SharedPreferences sharedPreferences;

    //main method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //data shared between activities
        sharedPreferences = getSharedPreferences("picPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();



        takePick = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        next = findViewById(R.id.button3);
        displayPic = findViewById(R.id.takePic);

        passingIntent = getIntent();
        Uri pic = passingIntent.getParcelableExtra("passPic");

        //display pic
        if(pic != null){
            displayPic.setImageURI(pic);
            passing_image = pic;
        }

        // camera perms
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }



        //camera activity button
        takePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try {
                    File img = File.createTempFile("tmp",".jpg",dir);
                    getUri= FileProvider.getUriForFile(MainActivity.this, "com.example.homerenoai.provider",img);
                    path= img.getAbsolutePath();
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getUri);
                    startActivityForResult(cameraIntent,100);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });

        //gallery button
        gallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 200);
            }
        });

        //next button
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(passing_image != null){
                    Intent intent = new Intent(MainActivity.this, inpaintScreen.class);
                    intent.putExtra("passPic", passing_image);
                    startActivity(intent);
                }
                else {

                    Toast.makeText(view.getContext(), "Please select an image for editing", Toast.LENGTH_SHORT).show();


                }
            }
        });






    }

    //Functions

    //Image creation handler
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //camera
        if(requestCode == 100 ){
            Bitmap mapPic= BitmapFactory.decodeFile(path);

            displayPic.setImageURI(null);
            displayPic.setImageURI(getUri);
            passing_image = getUri;
        }
        //gallery
        else if(requestCode == 200 && data != null){
            Uri pic = data.getData();
            displayPic.setImageURI(pic);
            passing_image = pic;
        }
        //handles camera crash
        else {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        }


    }




}