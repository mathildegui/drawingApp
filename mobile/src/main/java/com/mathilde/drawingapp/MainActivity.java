package com.mathilde.drawingapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.Bind;
import utils.Helper;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_CAT = MainActivity.class.getSimpleName();
    private final static int COMPRESS_QUALITY = 85;

    @Bind(R.id.toolbar) Toolbar mToolbar_top;
    @Bind(R.id.custom_view) CustomView mCustomView;
    @Bind(R.id.toolbar_bottom) Toolbar mToolbar_bottom;
    //@Bind(R.id.fab) FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        Helper.init(this);
        setSupportActionBar(mToolbar_top);

        mCustomView.setSaveEnabled(true);

        mToolbar_bottom.inflateMenu(R.menu.menu_drawing);
        mToolbar_bottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                handleDrawingIconTouched(menuItem.getItemId());
                return false;
            }
        });

        /*mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToolbar_bottom.setVisibility(View.VISIBLE);
                mFab.setVisibility(View.GONE);
            }
        });*/
    }

    private void handleDrawingIconTouched(int itemId) {
        switch(itemId) {
            case R.id.action_delete:
                deleteDialog();
                break;
            case R.id.action_undo:
                mCustomView.undo();
                break;
            case R.id.action_redo:
                mCustomView.redo();
                break;
            case R.id.action_share:
                shareImage();
                break;
            case R.id.action_save:
                saveDrawing();
                break;
        }
    }
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void shareImage() {
        mCustomView.setDrawingCacheEnabled(true);
        mCustomView.invalidate();

        if(mCustomView.getDrawingCache() == null) {
            Log.e(LOG_CAT,"Unable to get drawing cache ");
        }

        Bitmap b = mCustomView.getDrawingCache();

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream outputStream;
        File file = new File(path, "android_drawing_app.png");

        file.getParentFile().mkdirs();

        try {
            file.createNewFile();
        } catch(Exception e) {
            Log.e(LOG_CAT, e.getCause() + e.getMessage());
        }
        if (file.exists ()) file.delete ();
        try {
            outputStream = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch(Exception e) {
            Log.e(LOG_CAT, e.getCause() + e.getMessage());
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType("image/png");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share image"));
    }

    private void saveDrawing() {
        String path = Environment.getExternalStorageDirectory().toString();
        path += "/" + getString(R.string.app_name);
        File dir = new File(path);
        mCustomView.setDrawingCacheEnabled(true);

        String imgTitle = "Drawing_" + System.currentTimeMillis() + ".png";
        String imgSaved = MediaStore.Images.Media.insertImage(getContentResolver(), mCustomView.getDrawingCache(), imgTitle, "drawing to save");

        try {
            if(!dir.isDirectory() || !dir.exists()) {
                dir.mkdirs();
            }

            mCustomView.setDrawingCacheEnabled(true);
            File file = new File(path, imgTitle);
            FileOutputStream fOut = new FileOutputStream(file);
            Bitmap bitmap = mCustomView.getDrawingCache();
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, fOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Uh Oh!");
            alert.setMessage("Oops! Image could not be saved. Do you have enough space in your device?1");
            alert.setPositiveButton("OK", null);
            alert.show();
        }

        if(imgSaved!=null){
            Toast savedToast = Toast.makeText(getApplicationContext(),
                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
            savedToast.show();
            Log.d("imgSaved", imgSaved);
        }

        mCustomView.destroyDrawingCache();
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
        return super.onOptionsItemSelected(item);
    }

    private void deleteDialog() {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(R.string.delete_drawing);
        deleteDialog.setMessage(R.string.new_drawing_warning);
        deleteDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCustomView.eraseAll();
                dialog.dismiss();
            }
        });
        deleteDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        deleteDialog.show();
    }
}
