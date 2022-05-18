package com.gallery.merigallery.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 8;
    private static final String DB_NAME = "imageData";
    private static final String IMAGE_DATA = "userImage";
    private static final String ID = "ID";
    private static final String IMAGE = "IMAGE";

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public DatabaseHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String Create_Table = "CREATE TABLE " + IMAGE_DATA + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + IMAGE + " BLOB" + ")";
        sqLiteDatabase.execSQL(Create_Table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IMAGE_DATA);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(ImageData imageData) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(IMAGE ,imageData.getImage());
        sqLiteDatabase.insert(IMAGE_DATA, null, contentValues);
        sqLiteDatabase.close();
        return true;
    }

    public Bitmap getImage(String id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM IMAGE_DATA WHERE ID = ?", new String[]{id});
        cursor.moveToFirst();
        byte[] result = cursor.getBlob(1);
        Bitmap result_image = BitmapFactory.decodeByteArray(result, 0 , result.length);
        return result_image;
    }

    public List<ImageData> getAllImages() {
        List<ImageData> imageDataList = new ArrayList<>();

        String query = "SELECT * FROM " + IMAGE_DATA;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        Log.e("CURSOR", String.valueOf(cursor));

        if (cursor.moveToFirst()) {
            do {

                Log.e("TAG", "getAllImages: " + cursor.getString(1));
                ImageData imageData = new ImageData();
                imageData.setImage(cursor.getString(1));
                imageDataList.add(imageData);

            }
            while (cursor.moveToNext());
        }
        sqLiteDatabase.close();
        cursor.close();
        return imageDataList;
    }

    public boolean deleteImage(String image) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(IMAGE_DATA, IMAGE + "=?", new String[]{image});
        sqLiteDatabase.close();
        return true;
    }
}
