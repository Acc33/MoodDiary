package com.example.mooddiary;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ViewActivity extends AppCompatActivity {
    private EditText date;
    private EditText time;
    private Spinner mood;
    private ImageView moodIcon;
    private Spinner socialSituation;
    private EditText location;
    private EditText reason;
    private ImageView photo;
    private Switch edit;
    private Button save;
    private Button camera;
    private Button album;
    private Uri imageUri;

    private static final int TAKE_PHOTO = 1;
    private static final  int CHOOSE_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        mood = findViewById(R.id.mood);
        moodIcon = findViewById(R.id.moodicon);
        socialSituation = findViewById(R.id.social);
        location = findViewById(R.id.location);
        reason = findViewById(R.id.reason);
        photo = findViewById(R.id.photo);
        edit = findViewById(R.id.Edit);
        save = findViewById(R.id.save);
        camera = findViewById(R.id.camera);
        album = findViewById(R.id.album);

        nonFocusable();

        Intent i = getIntent();
        final MoodEvent m = (MoodEvent)i.getSerializableExtra("moodEvent");
        date.setText(m.getDate());
        time.setText(m.getTime());
        setSpinner(mood, m.getMood().getMood());
        moodIcon.setImageResource(m.getMood().getMoodImage());
        setSpinner(socialSituation, m.getSocialSituation());
        location.setText(m.getLocation());
        reason.setText(m.getReason());
        photo.setImageBitmap(BitmapFactory.decodeByteArray(m.getPhoto(),0,m.getPhoto().length));

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File outputImage = new File(getExternalCacheDir(),"out_image.jpg");

                try{
                    if (outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e){
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24){
                    imageUri = FileProvider.getUriForFile(ViewActivity.this,"com.example.mooddiary.fileprovider",outputImage);
                }else{
                    imageUri = Uri.fromFile(outputImage);
                }


                // start camera

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });

        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ViewActivity.this, "222", Toast.LENGTH_LONG).show();
                if(ContextCompat.checkSelfPermission(ViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ViewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });

        edit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    allFocusable();
                } else {
                    nonFocusable();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editDate = date.getText().toString();
                String editTime = time.getText().toString();
                String editMood = mood.getSelectedItem().toString();
                String editSocialSituation = socialSituation.getSelectedItem().toString();
                String editLocation = location.getText().toString();
                String editReason = reason.getText().toString();

                Bitmap bitmap = ((BitmapDrawable)photo.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] editPhoto = baos.toByteArray();
                MoodEvent editMoodEvent = new MoodEvent(editMood,editDate, editTime, editSocialSituation,editLocation, editReason, editPhoto);
                Intent i = new Intent();
                i.putExtra("editMoodEvent", editMoodEvent);
                i.putExtra("originMoodEvent", m);
                setResult(RESULT_OK, i);
                finish();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("lty", " acquire intent result code");
        switch (requestCode){
            case TAKE_PHOTO:
                Log.d("lty", " acquire intent result code from camera");
                if(resultCode == RESULT_OK){
                    try{
                        // show photo from camera
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        photo.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    Log.d("lty", " acquire intent result code from choose photo intent");
                    if(Build.VERSION.SDK_INT >= 19){
                        Log.d("lty", " version > 19");
                        handleImageOnKitKat(data);

                    }else{
                        Log.d("lty", " version < 19");
                        handleImageBeforeKitKat(data);

                    }
                }
                break;
            default:
                break;
        }
    }

    private void displayImage(String imagePath){
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            photo.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this,"failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("lty", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {

            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {

            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    /**
     * handleImageOnKitKat for version lower than 4.0
     * @param data
     *
     */
    private void handleImageBeforeKitKat(Intent data){
        Log.d("lty", " at beforekat");
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);

    }

    private String getImagePath(Uri uri, String selection){
        String path = null;
        // get image path throuth Uri and selection

        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor !=null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public void setSpinner(Spinner s, String text) {
        SpinnerAdapter adapter = s.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
            String tempString = (String)adapter.getItem(i);
            if(tempString.compareTo(text) == 0) {
                s.setSelection(i);
            }
        }
    }

    private void openAlbum(){
        Log.d("lty", " open album");
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    public void allFocusable() {
        date.setFocusableInTouchMode(true);
        time.setFocusableInTouchMode(true);
        mood.setFocusableInTouchMode(true);
        socialSituation.setFocusableInTouchMode(true);
        location.setFocusableInTouchMode(true);
        reason.setFocusableInTouchMode(true);
        photo.setFocusableInTouchMode(true);
    }

    public void nonFocusable() {
        date.setFocusable(false);
        time.setFocusable(false);
        mood.setFocusable(false);
        socialSituation.setFocusable(false);
        location.setFocusable(false);
        reason.setFocusable(false);
        photo.setFocusable(false);
    }
}
