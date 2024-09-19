package com.example.homerenoai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.prefs.Preferences;

public class inpaintScreen extends AppCompatActivity {

    //variables initialized
    SharedPreferences paintPrefs;
    private String generatePass = null;




    Button back;
    Button draw;
    Button erase;
    Button finalPage;

    ImageView mainPick;
    ImageView sketchView;
    Intent passingIntent;
    Uri original;
    int width;
    int height;
    Bitmap downscale;

    Bitmap mask;
    String trueMaskStr = "";
    Canvas canvas;
    Paint paint = new Paint();

    float downX = -1;
    float downY = -1;
    float upX = -1;
    float upY = -1;

    Boolean paintMode = true;
    Integer counter = 0;



    //main method
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inpaint_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //shared image
        paintPrefs = getSharedPreferences("picPrefs", MODE_PRIVATE);
        generatePass = paintPrefs.getString("generatedPicStr", null);



        //xml elements
        back = findViewById(R.id.button4);
        draw = findViewById(R.id.button7);
        erase = findViewById(R.id.button8);
        finalPage = findViewById(R.id.finalPageButton);


        mainPick = findViewById(R.id.mainPic);
        sketchView = findViewById(R.id.sketch_View);



        int left = getLeftOffset(mainPick);
        int top = getTopOffset(mainPick);



        passingIntent = getIntent();
        Uri backgroundImg = passingIntent.getParcelableExtra("passPic");
        original = backgroundImg;

        //getting dims
        int[] dims = null;
        try {
            dims = getDims(original);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // rescaling image
        try {
            downscale = MediaStore.Images.Media.getBitmap(this.getContentResolver(),original);

            if (dims[0] < 768 && dims[1] < 768 ){
                downscale = downscale;
                width = downscale.getWidth();
                height = downscale.getHeight();
            }
            else {
                if(dims[0] > dims[1]){
                    width = 768;
                    height = (768*dims[1])/dims[0];
                    downscale = Bitmap.createScaledBitmap(downscale, width, height, false);
                }
                else {
                    height = 768;
                    width = (768*dims[0])/dims[1];
                    downscale = Bitmap.createScaledBitmap(downscale, width, height, false);
                }

            }


            mainPick.setImageBitmap(downscale);


            if(generatePass != null ){
                byte[] tmpPic = Base64.decode(generatePass, Base64.DEFAULT);
                Bitmap genBitmap = BitmapFactory.decodeByteArray(tmpPic, 0, tmpPic.length);
                mainPick.setImageBitmap(genBitmap);

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }




        //return to main
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(inpaintScreen.this, MainActivity.class);
                intent.putExtra("passPic", original);
                startActivity(intent);



            }
        });

        //last page
        finalPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mask != null){

                    SharedPreferences paintPrefs = getSharedPreferences("picPrefs", MODE_PRIVATE);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    downscale.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byte[] downscaleByteArray = outputStream.toByteArray();
                    String strDownscaled = Base64.encodeToString(downscaleByteArray, Base64.DEFAULT);

                    if(generatePass != null ){strDownscaled = generatePass; }

                    SharedPreferences.Editor editor = paintPrefs.edit();
                    editor.putString("picStr",strDownscaled);
                    editor.putString("maskStr", trueMaskStr);
                    editor.apply();

                    Intent intent = new Intent(inpaintScreen.this, request_Page.class);

                    intent.putExtra("passPic", original);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(view.getContext(), "Highlight the aspect of the image you wish to change", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //draw mode
        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintMode = true;
            }
        });

        //erase mode
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintMode = false;
            }
        });




    }

    //find offset from left of screen
    private int getLeftOffset(View myView) {
        if (myView.getParent() == myView.getRootView()) return myView.getLeft();
        else return myView.getLeft() + getLeftOffset((View) myView.getParent());
    }

    //find offset from top of screen
    private int getTopOffset(View myView) {
        if (myView.getParent() == myView.getRootView()) return myView.getTop();
        else return myView.getTop() + getTopOffset((View) myView.getParent());
    }

    //painting activity
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float yOffset = 200;
        float xOffset = 0;
        float xScale = 1.0f;
        float yScale = 1.0f;

        //find aspect ratio and set scales and offsets
        float ratio = (float) width/height;

        //16:9
        if (ratio > 1.4){
            xScale = 0.9f;
            yScale = 1.2f;
            yOffset = 400;
        }

        //4:3
        else if (ratio > 1.2){
            xScale = 0.9f;
            yScale = 0.95f;
            yOffset = 400;

        }
        //1:1
        else if(ratio > 0.98){
            xScale = 1.3f;
            yScale = 1.3f;
            yOffset = 350;
        }
        else if (ratio > 0.74) {
            xScale = 1.2f;
            yScale = 1.1f;
            yOffset = 250;

        }

        //9:16
        else  {
            xScale = 1.4f;
            yScale = 1.3f;
            yOffset = 200;
            xOffset = 50;

        }


        //catch all finger movements and paint them on image
        if(event.getAction() ==MotionEvent.ACTION_DOWN){
            downX = (event.getX()-xOffset)/xScale;
            downY = (event.getY()-yOffset)/yScale;

        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            upX = (event.getX()-xOffset)/xScale;
            upY = (event.getY()-yOffset)/yScale;
            sketch(paintMode);
            downX = (event.getX()-xOffset)/xScale;
            downY = (event.getY()-yOffset)/yScale;
        }

        if (event.getAction() == MotionEvent.ACTION_UP){
            upX = (event.getX()-xOffset)/xScale;
            upY = (event.getY()-yOffset)/yScale;
            sketch(paintMode);
        }


        return super.onTouchEvent(event);
    }

    // apply paint to canvas
    private void sketch(Boolean paintMode){

        //handling null pointer exceptions and setting paint params
        if(mask == null ){
            mask = Bitmap.createBitmap(downscale.getWidth(),downscale.getHeight(),Bitmap.Config.ARGB_8888);
            canvas = new Canvas(mask);
            paint.setStrokeWidth(10);
            paint.setAntiAlias(true);

            paint.setColor(Color.WHITE);
            paint.setAlpha(50);


            if(!paintMode){
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            }
        }
        else if(mask != null && paintMode){

            paint.setStrokeWidth(10);
            paint.setAntiAlias(true);

            paint.setColor(Color.WHITE);
            paint.setAlpha(50);
            paint.setXfermode(null);


        }
        else if(mask != null && !paintMode){

            paint.setStrokeWidth(10);
            paint.setAntiAlias(true);

            paint.setColor(Color.WHITE);
            paint.setAlpha(50);



            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        }



        canvas.drawCircle(downX,downY,40,paint);
        sketchView.setImageBitmap(mask);

        //update buffer to avoid crashes
        counter+=1;
        if (counter == 20){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mask.compress(Bitmap.CompressFormat.PNG, 1, outputStream);

            byte[] arr = outputStream .toByteArray();
            trueMaskStr = Base64.encodeToString(arr, Base64.DEFAULT);
            counter = 0;


        }

    }


    //getting dimensions of uri
    private int[] getDims(Uri picture) throws IOException {

        Bitmap tmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(),picture);

        int width = tmp.getWidth();
        int height = tmp.getHeight();




        return (new int[]{width, height});
    }




}