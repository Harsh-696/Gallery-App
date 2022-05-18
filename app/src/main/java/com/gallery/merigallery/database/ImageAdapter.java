package com.gallery.merigallery.database;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.gallery.merigallery.MainActivity;
import com.gallery.merigallery.R;
import com.gallery.merigallery.viewimage.ViewImage;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;

public class ImageAdapter extends BaseAdapter implements Serializable {

    List<ImageData> imageDataList;
    Context context;
    String encode_image_result;
    String delete_image;
    int IMAGE_ID = 123;

    ConstraintLayout constraintLayout;
    DatabaseHandler databaseHandler;
    Boolean isDeleted = false;

    public ImageAdapter(List<ImageData> imageDataList, Context context) {
        this.imageDataList = imageDataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return imageDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ImageView image;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.grid_content, null);
        }

        image = view.findViewById(R.id.images);
        ImageData imageData = imageDataList.get(i);

        byte [] result = Base64.getMimeDecoder().decode(imageData.getImage());
        Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
        image.setImageBitmap(bitmap);

        constraintLayout = view.findViewById(R.id.user_images);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ViewImage.class);

                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                byte [] image = byteArray.toByteArray();
                encode_image_result = android.util.Base64.encodeToString(image, android.util.Base64.DEFAULT);

                SharedPreferences sharedPreferences = context.getSharedPreferences("imagePref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("encoded_image", encode_image_result);
                editor.commit();

                context.startActivity(intent);
                ((Activity) context).finish();

            }
        });

        constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                databaseHandler = new DatabaseHandler(context);
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                byte [] image = byteArray.toByteArray();
                delete_image = android.util.Base64.encodeToString(image, android.util.Base64.DEFAULT);

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Want to delete this image ?");

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                isDeleted = databaseHandler.deleteImage(delete_image);
                                if (isDeleted) {
                                    Intent intent = new Intent(context, MainActivity.class);
                                    context.startActivity(intent);
                                    ((Activity) context).finish();
                                }
                                else {
                                    Toast.makeText(context, "Image is not deleted", Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return false;
            }
        });

        return view;
    }
}
