package com.wanari.inifintemarker.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wanari.inifintemarker.R;
import com.wanari.inifintemarker.model.Constants;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private ProgressBar mapLoaderPB;
    private TextView markerCountTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapLoaderPB = (ProgressBar) findViewById(R.id.mapLoader);
        markerCountTV = (TextView) findViewById(R.id.markerCount);
    }

    public void showLoader() {
        if (mapLoaderPB != null) {
            mapLoaderPB.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoader() {
        if (mapLoaderPB != null) {
            mapLoaderPB.setVisibility(View.GONE);
        }
    }

    public void updateShownMarkerCount(int count) {
        if (markerCountTV != null) {
            markerCountTV.setText(getString(R.string.marker_counter, count, Constants.GENERATED_MARKER_COUNT));
        }
    }

}
