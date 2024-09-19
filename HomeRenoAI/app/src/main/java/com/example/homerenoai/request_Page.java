package com.example.homerenoai;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class request_Page extends AppCompatActivity {

    //sey base url
    String baseUrl = "";
    String get = "";
    OkHttpClient client;

    //variables initialized
    boolean noServer = false;

    SharedPreferences paintPrefs;
    private String picturePass = null;
    private String maskPass = null;

    Intent passingIntent;
    Uri passing_image;

    ImageView mainPick;
    ImageView sketchView;
    Button back;
    Button generate;
    Button back2;
    Button save;
    Button retry;
    Button edit;

    EditText prompt;
    EditText negPrompt;
    Switch mode;
    Switch fill;
    SeekBar cfg;
    SeekBar denoise;
    RadioButton euler;
    RadioButton dpm;
    RadioButton ddim;
    RadioGroup samplers;

    String b64Pic;
    String b64Mask;

    Bitmap downscaled;
    Bitmap mask;


    // Vars for json

    int widthVal;
    int heightVal;

    String promptStr;
    String negPromptStr;
    int maskContent = 1;
    float cfgScale = 7.0f;
    float denosingStrength = 0.75f;
    int cfgScaleInt = 14;
    int denosingStrengthInt = 75;
    String sampler = "Euler a";

    promptProcess obj = new promptProcess();




    Bitmap generatedPicBitmap;
    String generatePicStr;


    //main method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //blocking async methods when generating image to avoid errors
        StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(gfgPolicy);


        client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(100, TimeUnit.SECONDS).build();

        //showing picture
        paintPrefs = getSharedPreferences("picPrefs", MODE_PRIVATE);
        picturePass = paintPrefs.getString("picStr", null);
        maskPass = paintPrefs.getString("maskStr", null);

        if(picturePass != null && maskPass !=null ){
            byte[] tmpPic = Base64.decode(picturePass, Base64.DEFAULT);
            downscaled = BitmapFactory.decodeByteArray(tmpPic, 0, tmpPic.length);
            widthVal = downscaled.getWidth();
            heightVal = downscaled.getHeight();
            byte[] tmpMask = Base64.decode(maskPass, Base64.DEFAULT);
            mask = BitmapFactory.decodeByteArray(tmpMask, 0, tmpMask.length);

        }



        passingIntent = getIntent();
        passing_image = passingIntent.getParcelableExtra("passPic");


        //xml elements
        mainPick = findViewById(R.id.mainPic);
        sketchView = findViewById(R.id.sketch_View);
        back = findViewById(R.id.button5);
        generate = findViewById(R.id.button6);
        back2 = findViewById(R.id.back);
        save = findViewById(R.id.save);
        retry = findViewById(R.id.retry);
        edit = findViewById(R.id.MoreEdits);

        prompt = findViewById(R.id.prompt);
        negPrompt = findViewById(R.id.negPrompt);
        mode = findViewById(R.id.switch1);
        fill = findViewById(R.id.switch2);
        cfg = findViewById(R.id.seekBar);
        denoise = findViewById(R.id.seekBar2);
        euler = findViewById(R.id.radioButton1);
        dpm = findViewById(R.id.radioButton2);
        ddim = findViewById(R.id.radioButton3);


        mainPick.setImageBitmap(downscaled);
        sketchView.setImageBitmap(mask);
        back2.setVisibility(View.GONE);
        save.setVisibility(View.GONE);
        retry.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);

        euler.toggle();
        cfg.setProgress(cfgScaleInt);
        cfg.setVisibility(View.VISIBLE);

        denoise.setProgress(denosingStrengthInt);
        denoise.setVisibility(View.VISIBLE);



        //simple/complex toggle switch sets values for certain profile
        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                euler.toggle();
                if(compoundButton.isChecked()){
                    denosingStrength = 0.95f;
                    cfgScale = 24.0f;

                    cfgScaleInt = (int) (cfgScale * 2);
                    denosingStrengthInt = (int) (denosingStrength * 100);

                    cfg.setProgress(cfgScaleInt);
                    denoise.setProgress(denosingStrengthInt);

                    fill.setChecked(true);
                    maskContent = 2;


                }
                else{
                    denosingStrength = 0.75f;
                    cfgScale = 7.0f;

                    cfgScaleInt = (int) (cfgScale * 2);
                    denosingStrengthInt = (int) (denosingStrength * 100);

                    cfg.setProgress(cfgScaleInt);
                    denoise.setProgress(denosingStrengthInt);

                    fill.setChecked(false);
                    maskContent = 1;


                }
            }
        });

        //mask content toggle
       fill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               if(compoundButton.isChecked()){
                   maskContent = 2;
               }
               else{
                   maskContent = 1;
               }
           }
       });



        //previous page
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(request_Page.this, inpaintScreen.class);
                intent.putExtra("passPic", passing_image);
                startActivity(intent);
            }
        });

        //attempt to start image generation
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noServer = false;

                //make sure user inputs negative prompt
                if (negPrompt.getText().toString().isEmpty()) {
                    Toast.makeText(view.getContext(), "Please enter a negative prompt", Toast.LENGTH_LONG).show();}

                //attempt to generate image
                else {
                    Toast.makeText(view.getContext(), "Please wait about 15 seconds for your image to be generated", Toast.LENGTH_LONG).show();
                    postInpaint();

                    //no server found message
                    if(noServer){
                        Toast.makeText(view.getContext(), "No server found", Toast.LENGTH_LONG).show();
                    }

                    // update picture with generated image and update screen with ne options
                    else{

                        back.setVisibility(View.GONE);
                        generate.setVisibility(View.GONE);
                        back2.setVisibility(View.VISIBLE);
                        save.setVisibility(View.VISIBLE);
                        retry.setVisibility(View.VISIBLE);
                        edit.setVisibility(View.VISIBLE);

                    }

                }


            }
        });

        //previous page
        back2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(request_Page.this, inpaintScreen.class);
                intent.putExtra("passPic", passing_image);
                startActivity(intent);
            }
        });

        //generate new image with same mask and original image
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Please wait about 15 seconds for your image to be generated", Toast.LENGTH_LONG).show();
                postInpaint();
            }
        });

        //save generated image to gallery
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage(generatedPicBitmap);
                Intent intent = new Intent(request_Page.this, MainActivity.class);
                intent.putExtra("passPic", passing_image);
                startActivity(intent);
            }
        });

        //return to inpaint screen with generated image
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = paintPrefs.edit();
                editor.putString("generatedPicStr",generatePicStr);
                editor.apply();
                Intent intent = new Intent(request_Page.this, inpaintScreen.class);
                intent.putExtra("passPic", passing_image);
                startActivity(intent);
            }
        });


    }

    //test request to check if server is visible (simpler than testing using post)
    public void get(){
        Request request = new Request.Builder().url(get).build();

        try {
            Response response = client.newCall(request).execute();
            String res = response.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //post function
    public void postInpaint(){
        if(euler.isChecked()){
            sampler = "Euler a";
        }
        else if (dpm.isChecked()) {
            sampler = "DPM++ 2M";
        }
        else if (ddim.isChecked()) {
            sampler = "DDIM";
        }


        promptStr = prompt.getText().toString();
        negPromptStr = negPrompt.getText().toString();

        JSONArray picList = new JSONArray();
        picList.put(picturePass);

        //prompt engineering user input
        String newPrompt = obj.processPos(promptStr);
        String newNegPrompt = obj.processNeg(negPromptStr);


        //craft request body using json obj and all necessary values
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt",newPrompt);
            jsonBody.put("negative_prompt",newNegPrompt);
            jsonBody.put("seed",-1);
            jsonBody.put("batch_size",1);
            jsonBody.put("n_iter",1);
            jsonBody.put("width",widthVal);
            jsonBody.put("height",heightVal);
            jsonBody.put("restore_faces",false);
            jsonBody.put("tiling",false);
            jsonBody.put("do_not_save_samples",true);
            jsonBody.put("do_not_save_grid",true);
            jsonBody.put("steps",20);
            jsonBody.put("sampler_name",sampler);
            jsonBody.put("scheduler","Automatic");
            jsonBody.put("save_images",false);
            jsonBody.put("denoising_strength",denosingStrength);
            jsonBody.put("cfg_scale",cfgScale);
            jsonBody.put("init_images",picList );
            jsonBody.put("resize_mode",1);
            jsonBody.put("mask",maskPass);
            jsonBody.put("mask_blur",0);
            //jsonBody.put("inpainting_fill",2);
            jsonBody.put("inpainting_fill",maskContent);
            jsonBody.put("inpaint_full_res",false);
            jsonBody.put("inpaint_full_res_padding",32);




        } catch (JSONException e) {
            e.printStackTrace();
        }


        //take generated image from response and displays it
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder().url(baseUrl).post(body).build();
        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string();

            JSONObject obj = new JSONObject(res);
            generatePicStr = obj.getString("images").substring(2,obj.getString("images").length() -2);

            byte[] newTmpPic = Base64.decode(generatePicStr, Base64.DEFAULT);
            generatedPicBitmap = BitmapFactory.decodeByteArray(newTmpPic, 0, newTmpPic.length);
            mainPick.setImageResource(0);
            sketchView.setImageResource(0);
            mainPick.setImageBitmap(generatedPicBitmap);
            sketchView.setImageBitmap(generatedPicBitmap);






        }
        catch (SocketTimeoutException e){
            noServer = true;

        }

        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }




    }

    //save image to gallery
    public void saveImage(Bitmap pic){
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                OutputStream out;
                ContentResolver res =getContentResolver();
                ContentValues vals = new ContentValues();
                vals.put(MediaStore.MediaColumns.DISPLAY_NAME,System.currentTimeMillis()+".png");
                vals.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
                Uri uri  = res.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), vals);
                out = res.openOutputStream(Objects.requireNonNull(uri));
                pic.compress(Bitmap.CompressFormat.PNG,100,out);
                Objects.requireNonNull(out);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }




    }


}