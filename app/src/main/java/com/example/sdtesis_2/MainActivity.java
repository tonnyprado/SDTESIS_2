package com.example.sdtesis_2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sdtesis_2.ml.TfliteModel;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.aliasing.qual.MaybeAliased;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnCamara;
    Button btnReconocer;
    ImageView imageView;
    TextView textView;
    //PROBABLEMENTE ESTE BITMAP CAUSE PROBLEMAS, VIGILALO
    Bitmap imgBitmap;

    /*
    private static Application myApp = getApp();
    private static ArrayList<File> myTempFiles = new ArrayList<File>();*/

    //private Interpreter interpreter;

    //private String modelPATH = "tflite_model.tflite";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamara = findViewById(R.id.btnCamara);
        btnReconocer = findViewById(R.id.btnReconocer);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        String[] townList = new String[0]; //HICE CAMBIOS A ESTE TOWNLIST

        /*String filename = "labels.txt";
        String inputString = String(getApplication().getAssets().open(filename).bufferReader().use{it.readText()});
        String[] townList = inputString.split("\n");*/



        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                townList = mLine.split("\n"); //AQUI HICE CAMBIOS, WARNING, HACER EL TOWN
            }
        } catch (IOException e) {
            //log the exception
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                    e.printStackTrace();
                }
            }
        }

        //AQUI LE DOY CLICK AL BOTON DE LA CAMARA
        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //abrirCamara();
                openSomeActivityForResult();
            }
        });

        //AQUI ESTOY ACTIVANDO EL MODELO
        String[] finalTownList = townList;
        btnReconocer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap resizedBitm = Bitmap.createScaledBitmap(imgBitmap, 32, 32, true);

                try {
                    TfliteModel model = TfliteModel.newInstance(getApplicationContext());

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
                    TensorImage tbuffer = TensorImage.fromBitmap(resizedBitm);
                    ByteBuffer byteBuffer = tbuffer.getBuffer(); //CREO QUE ESTO ESTA MAL DECLARADO RECUERDA: byteBuffer = tbuffer.buffer;
                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    TfliteModel.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    int max = getMax(outputFeature0.getFloatArray()); //weird, en revision

                    //textVIEW CREO QUE ESTA MAL EL GETFLOATARRAY, VIGILALO
                    //textView.setText(outputFeature0.getFloatArray().toString()); SI NO FUNCIONA EL OTRO, PRUEBA ESTE
                    //textView.setText(new StringBuilder().append(outputFeature0.getFloatArray()[max]).append(toString()));
                    textView.setText(finalTownList[max]); //CAMBIOS, WARNING

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                    e.printStackTrace();
                }
            }
        });

        /*try {
            //interpreter = new Interpreter(loadModelFile(), null);
            initInterpreter();
            initCamara();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        //doSomeOperations();

                        Bundle extras = data.getExtras();
                        Bitmap imgBitmap = (Bitmap) extras.get("data");

                        Uri uri = data.getData();
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, MainActivity.class);
                        intent.putExtra("Bitmap",imgBitmap);
                        startActivity(intent);

                        imageView.setImageBitmap(imgBitmap); //POSIBLEMENTE CAMBIA IMGBITMAP TO BITMAP2
                    }
                }
            });

    public void openSomeActivityForResult(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        someActivityResultLauncher.launch(intent);
    }

    // FUNCIÓN DE LA CAMARA SE CONECTA CON EL MODELO EN ESTA PARTE
    /*private void initCamara() {
        findViewById(R.id.imageView).setOnClickListener((View.OnClickListener) this);
    }

    private void initInterpreter() throws IOException {
        interpreter = new Interpreter(modelPATH);

    }*/

    /* GETTEMPFILE FUNCTION*/
    /*
    public static File getTempFile(String tmpName) {
        // getExternalCacheDir() returns something like /mnt/sdcard/Android/data/[package_name]/cache/
        File file = new File(getApp().getApplicationContext()
                .getExternalCacheDir(), tmpName);
        myTempFiles.add(file);
        return file;
    }

    public static File getTempFile() {
        return getTempFile("temp", ".tmp");
    }

    public static File getTempFile(String prefix, String suffix) {
        File file = null;
        try {
            file = File.createTempFile(prefix, suffix, getApp()
                    .getApplicationContext().getExternalCacheDir());
            myTempFiles.add(file);
        } catch (Exception e) {
        }
        return file;
    }

    public static Application getApp() {
        if (myApp == null) {
            try {
                @SuppressLint("PrivateApi") final Class<?> activityThreadClass = Class
                        .forName("android.app.ActivityThread");
                final Method method = activityThreadClass
                        .getMethod("currentApplication");
                myApp = (Application) method.invoke(null, (Object[]) null);
            } catch (Exception e) {
                // handle exception
            }
        }
        // below gives me /mnt/sdcard/Android/data/com.hyperionics.pdfxTest/cache/tmpPdfx
        // File file = new File(app.getApplicationContext().getExternalCacheDir(), "tmpPdfx");
        return myApp;
    }


     */
    /* GETTEMPFILE FUNCTION END */


    // SOLO ACTIVAS LA ACCION DE LA CAMARA CON ESTAS DOS FUNCIONES Y TOMAS LA FOTO
    /* FUE REEMPLAZADO
    private void abrirCamara(){
        Intent fotointent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 2
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("image/*");
        Uri captureImage = Uri.fromFile(getTempFile());
        fotointent.putExtra(MediaStore.EXTRA_OUTPUT, captureImage);

        if(fotointent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(fotointent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imgBitmap = (Bitmap) extras.get("data");

            Uri uri = data.getData();
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MainActivity.class);
            intent.putExtra("Bitmap",imgBitmap);
            startActivity(intent);

            imageView.setImageBitmap(imgBitmap); //POSIBLEMENTE CAMBIA IMGBITMAP TO BITMAP2
        }

        
    } ESTO FUE REEMPLAZADO */

    private int getMax(@NonNull float[] arR){
        int ind = 0;
        float min = 0.0f;

        for(int i=0;i<1000;i++){
           if(arR[i]>min){
               ind = 1;
               min = arR[i];
           }
        }
        return ind; //algo esta off here, estate al pendiente
    }
}