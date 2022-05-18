package com.gallery.merigallery;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.GridView;
import android.widget.TextView;

import com.gallery.merigallery.database.DatabaseHandler;
import com.gallery.merigallery.database.ImageAdapter;
import com.gallery.merigallery.database.ImageData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    String currentPhotoPath;

    FloatingActionButton camera;
    FloatingActionButton gallery;

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAM = "camera_pref";

    View actionBar;
    Toolbar toolbar;
    TextView toolbarTitle;

    ImageData imageData;
    DatabaseHandler databaseHandler = new DatabaseHandler(this);
    boolean isInserted = false;
    List<ImageData> imageDataList = new ArrayList<>();
    GridView gridView;
    ImageAdapter imageAdapter;

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbarTitle = findViewById(R.id.app_title);
        camera = findViewById(R.id.camera);
        gallery = findViewById(R.id.add_photo);
        actionBar = findViewById(R.id.actionBAR);
        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);
        setTitle("");

        Typeface typeface = Typeface.createFromAsset(getAssets(), "ubuntu_bold.ttf");
        toolbarTitle.setTypeface(typeface);

        callAdapter();


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (getFromPref(MainActivity.this, ALLOW_KEY)) {
                        showSettingsAlert();
                    } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CAMERA)

                            != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.CAMERA)) {
                            showAlert();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    }
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
//                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == RESULT_OK ) {
                            File newFile = new File(currentPhotoPath);
                            Uri photoUri = FileProvider.getUriForFile(MainActivity.this, "com.gallery.android.fileprovider",
                                    newFile);

                            Bitmap camera_image = null;
                            try {
                                camera_image = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),photoUri);
                            }
                            catch (IOException ioException) {
                                ioException.printStackTrace();
                            }

                            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                            camera_image.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                            byte [] image = byteArray.toByteArray();

                            String encoded_image = Base64.encodeToString(image, Base64.DEFAULT);

                            imageData = new ImageData(encoded_image);
                            imageData.setImage(encoded_image);
                            isInserted = databaseHandler.insertData(imageData);

                            if (isInserted) {
                                callAdapter();
                            }
                        }

                        if (result.getResultCode() == RESULT_OK) {
                            Intent resultUri = result.getData();
                            String image_result = resultUri.getDataString();
                            Log.e("IMAGEASDF", image_result);

//                            Bitmap galleryImage = null;
//
//                            try {
//                                galleryImage = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),resultUri);
//                            }
//                            catch (IOException ioException) {
//                                ioException.printStackTrace();
//                            }
//
//                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
////                String imageFileName = "PNG_" + timeStamp + "." + getFileExtension(resultUri);
//
//
//                            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//                            galleryImage.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
//                            byte [] image = byteArray.toByteArray();
//
//                            String encoded_image = Base64.encodeToString(image, Base64.DEFAULT);
//
//                            imageData = new ImageData(encoded_image);
//                            imageData.setImage(encoded_image);
//                            isInserted = databaseHandler.insertData(imageData);
//
//                            if (isInserted) {
//                                callAdapter();
//                            }
                        }
                    }
                });
    }

    private boolean getFromPref(Context context, String allowKey) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CAM, Context.MODE_PRIVATE);
        return (sharedPreferences.getBoolean(allowKey, false));
    }



    private String getFileExtension(Uri resultUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(resultUri));
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(MainActivity.this);
                    }
                });

        alertDialog.show();
    }

    private void startInstalledAppDetailsActivity(final Activity activity) {
        if (activity == null) {
            return;
        }
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activity.startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            saveToPreferences(MainActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }
        }
    }

    private void saveToPreferences(Context context, String allowKey, boolean b) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAM,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(allowKey, b);
        prefsEditor.commit();
    }

    private void callAdapter() {
        imageDataList = databaseHandler.getAllImages();
        gridView = findViewById(R.id.gallery_grid);
        imageAdapter = new ImageAdapter(imageDataList, this);
        Log.e("SIZE", String.valueOf(imageDataList.size()));
        gridView.setAdapter(imageAdapter);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile( imageFileName, ".png", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.gallery.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                activityResultLauncher.launch(takePictureIntent);
            }
        }
    }

}

/* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_IMAGE_CAPTURE) {

                File newFile = new File(currentPhotoPath);

                Uri photoUri = FileProvider.getUriForFile(this, "com.gallery.android.fileprovider",
                        newFile);

                Bitmap camera_image = null;
                try {
                    camera_image = MediaStore.Images.Media.getBitmap(this.getContentResolver(),photoUri);
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                camera_image.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                byte [] image = byteArray.toByteArray();

                String encoded_image = Base64.encodeToString(image, Base64.DEFAULT);

                imageData = new ImageData(encoded_image);
                imageData.setImage(encoded_image);
                isInserted = databaseHandler.insertData(imageData);

                if (isInserted) {
                    callAdapter();
                }
            }

            if (requestCode == GALLERY_REQUEST_CODE) {
                Uri resultUri = data.getData();
                Bitmap galleryImage = null;

                try {
                    galleryImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),resultUri);
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                String imageFileName = "PNG_" + timeStamp + "." + getFileExtension(resultUri);


                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                galleryImage.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                byte [] image = byteArray.toByteArray();

                String encoded_image = Base64.encodeToString(image, Base64.DEFAULT);

                imageData = new ImageData(encoded_image);
                imageData.setImage(encoded_image);
                isInserted = databaseHandler.insertData(imageData);

                if (isInserted) {
                    callAdapter();
                }
            }
        }
    }

 */