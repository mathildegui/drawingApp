package com.mathilde.drawingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar_top;
    private Toolbar mToolbar_bottom;
    private FloatingActionButton mFab;

    private CustomView mCustomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCustomView     = (CustomView) findViewById(R.id.custom_view);
        mToolbar_top    = (Toolbar) findViewById(R.id.toolbar);
        mToolbar_bottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        setSupportActionBar(mToolbar_top);

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
        }
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
