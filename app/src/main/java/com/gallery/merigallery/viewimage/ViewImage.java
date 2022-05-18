package com.gallery.merigallery.viewimage;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.gallery.merigallery.MainActivity;
import com.gallery.merigallery.R;
import com.gallery.merigallery.database.DatabaseHandler;
import com.gallery.merigallery.database.ImageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Base64;

public class ViewImage extends AppCompatActivity {

    ImageView user_clicked_image;
    String fetched_image = null;
    FloatingActionButton delete_btn;
    boolean isDeleted = false;
    ScaleGestureDetector scaleGestureDetector;
    float mScaleFactor = 1.0f;

    ImageAdapter imageAdapter;

    DatabaseHandler databaseHandler = new DatabaseHandler(this);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        user_clicked_image = findViewById(R.id.clicked_image);
        delete_btn = findViewById(R.id.delete_image);
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        SharedPreferences sharedPreferences = getSharedPreferences("imagePref", Context.MODE_PRIVATE);
        fetched_image = sharedPreferences.getString("encoded_image", "");
        sharedPreferences.edit().clear();

        byte [] result = Base64.getMimeDecoder().decode(fetched_image);
        Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
        user_clicked_image.setImageBitmap(bitmap);

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDeleted = databaseHandler.deleteImage(fetched_image);
                if (isDeleted) {
                    Intent intent = new Intent(ViewImage.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Data is not deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 10.0f));
            user_clicked_image.setScaleX(mScaleFactor);
            user_clicked_image.setScaleY(mScaleFactor);
            return true;
        }
    }
}