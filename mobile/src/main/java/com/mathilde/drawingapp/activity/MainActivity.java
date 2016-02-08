package com.mathilde.drawingapp.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mathilde.drawingapp.R;
import com.mathilde.drawingapp.fragment.BrushSizeChooserFragment;
import com.mathilde.drawingapp.utils.Helper;
import com.mathilde.drawingapp.view.CustomView;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import butterknife.Bind;
import com.mathilde.drawingapp.listener.OnNewBrushSizeSelectedListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_CAT               = MainActivity.class.getSimpleName();
    private static final int COMPRESS_QUALITY         = 85;
    private static String[] PERMISSIONS_STORAGE       = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private ColorPicker cp;
    private int selectedColorRGB;

    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.custom_view) CustomView mCustomView;
    @Bind(R.id.action_redo) FloatingActionButton mSubRedo;
    @Bind(R.id.action_save) FloatingActionButton mSubSave;
    @Bind(R.id.action_undo) FloatingActionButton mSubUndo;
    @Bind(R.id.action_color) FloatingActionButton mSubColor;
    @Bind(R.id.action_erase) FloatingActionButton mSubErase;
    @Bind(R.id.action_share) FloatingActionButton mSubShare;
    @Bind(R.id.action_delete) FloatingActionButton mSubDelete;
    @Bind(R.id.action_brush_size) FloatingActionButton mSubBrushSize;

    public static void checkStoragePermissions(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void initClick() {
        mFab.setOnClickListener(this);
        mSubUndo.setOnClickListener(this);
        mSubRedo.setOnClickListener(this);
        mSubSave.setOnClickListener(this);
        mSubShare.setOnClickListener(this);
        mSubColor.setOnClickListener(this);
        mSubErase.setOnClickListener(this);
        mSubDelete.setOnClickListener(this);
        mSubBrushSize.setOnClickListener(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkStoragePermissions(this);
        Helper.init(this);
        initClick();
        this.mCustomView.setSaveEnabled(true);
    }

    private void setVisibility(int visibility) {
        mSubUndo.setVisibility(visibility);
        mSubRedo.setVisibility(visibility);
        mSubSave.setVisibility(visibility);
        mSubShare.setVisibility(visibility);
        mSubColor.setVisibility(visibility);
        mSubErase.setVisibility(visibility);
        mSubDelete.setVisibility(visibility);
        mSubBrushSize.setVisibility(visibility);
    }

    private void brushSizePicker() {
        BrushSizeChooserFragment brushDialog = BrushSizeChooserFragment.newInstance((int) this.mCustomView.getLastBrushSize());
        brushDialog.setOnNewBrushSizeSelectedListener(new OnNewBrushSizeSelectedListener() {
            public void OnNewBrushSizeSelected(float newBrushSize) {
                mCustomView.setBrushSize(newBrushSize);
                mCustomView.setLastBrushSize(newBrushSize);
            }
        });
        brushDialog.show(getSupportFragmentManager(), "Dialog");
    }

    private void changeColor() {
        getRGBFromHexa(this.mCustomView.getColor());
        this.cp.show();
        (cp.findViewById(com.pes.androidmaterialcolorpickerdialog.R.id.okColorButton)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectedColorRGB = cp.getColor();
                mCustomView.updateColor(selectedColorRGB);
                cp.dismiss();
            }
        });
    }

    private void getRGBFromHexa(int s) {
        Object[] objArr = new Object[REQUEST_EXTERNAL_STORAGE];
        objArr[0] = 16777215 & s;
        int color = (int) Long.parseLong(String.format("%06X", objArr), 16);
        this.cp   = new ColorPicker(this, (color >> 16) & 255, (color >> 8) & 255, (color) & 255);
    }

    private void shareImage() {
        mCustomView.setDrawingCacheEnabled(true);
        mCustomView.invalidate();
        if (mCustomView.getDrawingCache() == null) {
            Log.e(LOG_CAT, "Unable to get drawing cache ");
        }
        Bitmap b  = this.mCustomView.getDrawingCache();
        File file = new File(Environment.getExternalStorageDirectory().toString(), "android_drawing_app.png");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (Exception e) {
            Log.e(LOG_CAT, e.getCause() + e.getMessage());
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            OutputStream outputStream = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e2) {
            Log.e(LOG_CAT, e2.getCause() + e2.getMessage());
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction("android.intent.action.SEND");
        shareIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(file));
        shareIntent.setType("image/png");
        shareIntent.addFlags(REQUEST_EXTERNAL_STORAGE);
        startActivity(Intent.createChooser(shareIntent, "Share image"));
    }

    private void saveDrawing() {
        String path = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name);
        File dir = new File(path);
        this.mCustomView.setDrawingCacheEnabled(true);
        String imgTitle = "Drawing_" + System.currentTimeMillis() + ".png";
        String imgSaved = MediaStore.Images.Media.insertImage(getContentResolver(), this.mCustomView.getDrawingCache(), imgTitle, "drawing to save");
        try {
            if (!(dir.isDirectory() && dir.exists())) {
                dir.mkdirs();
            }
            this.mCustomView.setDrawingCacheEnabled(true);
            this.mCustomView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, new FileOutputStream(new File(path, imgTitle)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Builder alert = new Builder(this);
            alert.setTitle("Uh Oh!");
            alert.setMessage("Oops! Image could not be saved. Do you have enough space in your device?1");
            alert.setPositiveButton("OK", null);
            alert.show();
        }
        if (imgSaved != null) {
            Toast.makeText(this, "Drawing saved to Gallery!", Toast.LENGTH_SHORT).show();
        }
        this.mCustomView.destroyDrawingCache();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void deleteDialog() {
        Builder deleteDialog = new Builder(this);
        deleteDialog.setTitle(R.string.delete_drawing);
        deleteDialog.setMessage(R.string.new_drawing_warning);
        deleteDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.mCustomView.eraseAll();
                dialog.dismiss();
            }
        });
        deleteDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        deleteDialog.show();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_erase:
                int color = 0;
                Drawable background = this.mCustomView.getBackground();
                if (background instanceof ColorDrawable) {
                    color = ((ColorDrawable) background).getColor();
                }
                this.mCustomView.updateColor(color);
                return;
            case R.id.action_delete:
                deleteDialog();
                return;
            case R.id.action_undo:
                this.mCustomView.undo();
                return;
            case R.id.action_redo:
                this.mCustomView.redo();
                return;
            case R.id.action_color:
                changeColor();
                return;
            case R.id.action_brush_size:
                brushSizePicker();
                return;
            case R.id.action_save:
                saveDrawing();
                return;
            case R.id.action_share:
                shareImage();
                return;
            case R.id.fab:
                if (this.mSubUndo.getVisibility() == View.GONE) {
                    setVisibility(View.VISIBLE);
                    return;
                } else {
                    setVisibility(View.GONE);
                    return;
                }
            default:
                return;
        }
    }
}