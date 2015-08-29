package edu.nyu.scps.photo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PHOTO = 0;
    public static final int RESULT_GALLERY = 0;
    private File mCurrentPhoto;


    private ImageView imageView;
    private ImageView imageView2;
    private ImageButton cameraButton;
    private Button fullsizeButton;
    private Button deleteButton;
    private Button galleryButton;
    private Button openImageButton;

    private File file;
    private Uri uri, uri2;
    private int photoNumber = 0; //Each saved photo has a different name.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imageView);
        imageView2 = (ImageView)findViewById(R.id.imageView2);
        final TextView textView1 = (TextView)findViewById(R.id.textView1);

        PackageManager packageManager = getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            textView1.append("This device has no camera, front or rear.");
            return;
        }

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ComponentName componentName = intent.resolveActivity(packageManager);
        if (componentName == null) {
            textView1.append("This device has no component that can capture an image.");
            return;
        }
        textView1.append("componentName =\n" + componentName);

        final File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        textView1.append("\n\nexternalStoragePublicDirectory =\n" + directory);
        if (!directory.exists() && !directory.mkdirs()) {
                textView1.append("\ncould not create externalStoragePublicDirectory");
                return;
        }

        //Setting a listener enables the button.
        cameraButton = (ImageButton)findViewById(R.id.camera);
        fullsizeButton = (Button)findViewById(R.id.fullsize);
        deleteButton = (Button)findViewById(R.id.delete);
        galleryButton = (Button)findViewById(R.id.gallery);

//        Custom Open Image Button
        openImageButton = (Button)findViewById(R.id.open_Image);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                file = new File(directory, "image_" + photoNumber + ".jpg");
                ++photoNumber;
                uri = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });

        fullsizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "image/*"); //Second parameter is a MIME type.
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete image");
                builder.setMessage("Are you sure you want to delete this image?");
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteButton.setEnabled(false);
                        fullsizeButton.setEnabled(false);
                        galleryButton.setEnabled(false);
                        if (!file.delete()) {
                            Toast toast = Toast.makeText(MainActivity.this, "\nCould not delete file " + file, Toast.LENGTH_LONG);
                            toast.show();
                        }
                        imageView.setImageBitmap(null);
                    }
                });
                builder.show();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteButton.setEnabled(false);
                galleryButton.setEnabled(false);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(uri);
                sendBroadcast(intent);
            }
        });

        openImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setEnabled(false);
                galleryButton.setEnabled(false);
                fullsizeButton.setEnabled(false);
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri2);

                startActivityForResult(intent, RESULT_GALLERY);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        final TextView textView2 = (TextView)findViewById(R.id.textView2);
        textView2.setText("");
        if (requestCode != REQUEST_PHOTO) {
            textView2.append("\n\nunexpected requestCode " + requestCode);
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            textView2.append("\n\nbad resultCode " + resultCode);
            return;
        }


        textView2.append("\nfile =\n" + file);
        if (!file.exists()) {
            textView2.append("\n\nfile does not exist");
            return;
        }
        textView2.append("\n\nuri =\n" + uri);

        //Get the dimensions of the image.
        String path = file.getPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        textView2.append("\n\nimage dimensions = (" + imageWidth + ", " + imageHeight + ") pixels");

        //Get the dimensions of the ImageView.
        int imageViewWidth = imageView.getWidth();
        int imageViewHeight = imageView.getHeight();
        textView2.append("\nImageView dimensions = (" + imageViewWidth + ", " + imageViewHeight + ") pixels");

        int sampleSize = Math.min(imageWidth / imageViewWidth, imageHeight / imageViewHeight);
        textView2.append("\nsampleSize = " + sampleSize);
        options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        imageView.setImageBitmap(bitmap);

        deleteButton.setEnabled(true);
        fullsizeButton.setEnabled(true);
        galleryButton.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
