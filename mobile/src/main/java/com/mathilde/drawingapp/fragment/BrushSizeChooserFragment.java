package com.mathilde.drawingapp.fragment;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;

import com.mathilde.drawingapp.R;
import com.mathilde.drawingapp.listener.OnNewBrushSizeSelectedListener;

public class BrushSizeChooserFragment extends DialogFragment {
    public static final String BRUSH_SIZE = "current_brush_size";

    @Bind(R.id.text_view_max_value) TextView maxValue;
    @Bind(R.id.text_view_min_value) TextView minValue;
    @Bind(R.id.text_view_brush_size) TextView currentValue;
    @Bind(R.id.seek_bar_brush_size) SeekBar brushSizeSeekBar;

    private int mCurrentBrushSize;
    private OnNewBrushSizeSelectedListener mListener;

    public void setOnNewBrushSizeSelectedListener(OnNewBrushSizeSelectedListener listener) {
        this.mListener = listener;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundle();
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_brush_size_chooser, null);

        ButterKnife.bind(this, dialogView);
        if (dialogView != null) {
            this.minValue.setText(String.valueOf(getResources().getInteger(R.integer.min_size)));
            this.maxValue.setText(String.valueOf(getResources().getInteger(R.integer.max_size)));
            if (this.mCurrentBrushSize > 0) {
                this.currentValue.setText(getResources().getString(R.string.label_brush_size) + this.mCurrentBrushSize);
            }
            brushSizeSeekBar.setProgress(this.mCurrentBrushSize);
            brushSizeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                int progressChanged = 0;

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    this.progressChanged = progress;
                    BrushSizeChooserFragment.this.currentValue.setText(BrushSizeChooserFragment.this.getResources().getString(R.string.label_brush_size) + progress);
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    BrushSizeChooserFragment.this.mListener.OnNewBrushSizeSelected((float) this.progressChanged);
                }
            });
        }
        builder.setTitle("Choose the new brush size").setPositiveButton("Ok", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setView(dialogView);
        return builder.create();
    }

    public static BrushSizeChooserFragment newInstance(int size) {
        BrushSizeChooserFragment f = new BrushSizeChooserFragment();
        Bundle b = new Bundle();
        if (size > 0) {
            b.putInt(BRUSH_SIZE, size);
            f.setArguments(b);
        }
        return f;
    }

    private void getBundle() {
        Bundle b = getArguments();
        if (b != null && b.containsKey(BRUSH_SIZE)) {
            int brushSize = b.getInt(BRUSH_SIZE, 0);
            if (brushSize != 0) {
                this.mCurrentBrushSize = brushSize;
            }
        }
    }
}