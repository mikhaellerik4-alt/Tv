package com.mikhaell.tvlauncher;

import android.content.Intent;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(TvLauncherPlugin.class);
        super.onCreate(savedInstanceState);
        RemoteServer.setActivity(this);
        startService(new Intent(this, RemoteService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        RemoteServer.setActivity(this);
    }

    @Override
    public void onDestroy() {
        RemoteServer.setActivity(null);
        super.onDestroy();
    }

    // A home screen must never close with the Back button.
    @Override
    public void onBackPressed() {}
}
