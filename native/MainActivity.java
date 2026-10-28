package com.mikhaell.tvlauncher;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(TvLauncherPlugin.class);
        super.onCreate(savedInstanceState);
    }

    // A home screen must never close with the Back button.
    @Override
    public void onBackPressed() {}
}
