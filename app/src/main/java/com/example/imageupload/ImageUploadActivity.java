package com.example.imageupload;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageUploadActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    String[] permission;
    Button btnTakePhoto;
    ImageView imgShowImage;
    private Bitmap bitmap;
    Button btnGalleryImage;
    private Uri uri;
    ImageView imgGetImage;
    String imageData;
    private RecyclerView recyclerView;

    ImageListAdapter imageListAdapter;

    ArrayList<String> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        imgShowImage = findViewById(R.id.imgShowImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnGalleryImage = findViewById(R.id.btnGalleryImage);
        imgGetImage = findViewById(R.id.imgGetImage);
        recyclerView = findViewById(R.id.recyclerView);
        arrayList = new ArrayList<>();


        File imageFile = getImageFile();
        if (imageFile != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            imgGetImage.setImageBitmap(bitmap);

        }

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPermission();
            }
        });

        btnGalleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryPermission();
            }
        });

    }

    private void galleryPermission() {
        //Permission storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            permission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        Dexter.withContext(this).withPermissions(permission).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                Intent takePicture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (takePicture.resolveActivity(getPackageManager()) != null) {
                    activityResultLauncherGalleryImage.launch(takePicture);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    private void cameraPermission() {
        //Permission storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            permission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        Dexter.withContext(this).withPermissions(permission).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePicture.resolveActivity(getPackageManager()) != null) {
                    activityResultLauncher.launch(takePicture);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() != RESULT_CANCELED) {
                Log.e("RESULT_CANCELED", "onActivityResult: ");
                if (result.getResultCode() == RESULT_OK) {
                    Bundle bundle = result.getData().getExtras();
                    bitmap = (Bitmap) bundle.get("data");
                    imgShowImage.setImageBitmap(bitmap);

                    File file = createImageFile();
                    if (file != null) {
                        FileOutputStream fout;
                        try {
                            fout = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                            fout.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Uri uri = Uri.fromFile(file);
                        Log.e("Uri", "onActivityResult: " + uri);

                        File imageFile = getImageFile();
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        imgGetImage.setImageBitmap(bitmap);
                    }

                }
            }
        }
    });

    ActivityResultLauncher<Intent> activityResultLauncherGalleryImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                Glide.with(ImageUploadActivity.this).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imgShowImage.setImageBitmap(resource);
                        File file = createImageFile();
                        if (file != null) {
                            FileOutputStream fout;
                            try {
                                fout = new FileOutputStream(file);
                                resource.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                                fout.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Uri uri2 = Uri.fromFile(file);
                            Log.e("Uri", "onActivityResult: " + uri2);


                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

            }
        }
    });

    public File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File mFileTemp = null;
        String root = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name);
        File myDir = new File(root + "/ImageData");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        try {
            mFileTemp = File.createTempFile(imageFileName, ".jpg", myDir.getAbsoluteFile());
            Log.e("TAG", "createImageFile: " + mFileTemp);
            MediaScannerConnection.scanFile(ImageUploadActivity.this, new String[]{myDir.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    // Image is saved and visible in the gallery
                }
            });
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return mFileTemp;
    }

    private File getImageFile() {
        String Path = Environment.getExternalStorageDirectory() + "/ImageData";
        String root = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name);
        File f = new File(root + "/ImageData");
        File[] imageFiles = f.listFiles();


        if (imageFiles != null) {
            ArrayList<File> imageFileList = new ArrayList<>(Arrays.asList(imageFiles));

            // Do something with the image files
            for (File imageFile : imageFileList) {
                // Handle each image file here
                // For example, you can get the file path using imageFile.getAbsolutePath()
                arrayList.add(imageFile.getPath());
                imageListAdapter = new ImageListAdapter(this,arrayList);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(imageListAdapter);
                Log.e("GetPath", "getImageFile: "+ imageFile.getPath());

            }
        }

        if (imageFiles == null || imageFiles.length == 0) {
            return null;
        }

        File lastModifiedFile = imageFiles[0];
        for (int i = 1; i < imageFiles.length; i++) {
            if (lastModifiedFile.lastModified() < imageFiles[i].lastModified()) {
                lastModifiedFile = imageFiles[i];

            }
            Log.e("ImageUpload", "getImageFile: " + imageFiles[i]);
        }
        return lastModifiedFile;
    }

}