package com.mikhaell.tvlauncher;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// Mantem o servidor do controle vivo enquanto o TV Home estiver aberto.
public class RemoteService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RemoteServer.start(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
