package com.mikhaell.tvlauncher;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class RemapService extends AccessibilityService {
    static final String PREFS = "remap";
    static boolean running = false;
    // Servico ativo: permite Voltar/Inicio em qualquer app, nao so na tela inicial.
    public static RemapService instance = null;

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    protected void onServiceConnected() {
        running = true;
        instance = this;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        running = false;
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        running = false;
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    private static boolean isNavKey(int k) {
        return k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN
            || k == KeyEvent.KEYCODE_DPAD_LEFT || k == KeyEvent.KEYCODE_DPAD_RIGHT
            || k == KeyEvent.KEYCODE_DPAD_CENTER || k == KeyEvent.KEYCODE_ENTER
            || k == KeyEvent.KEYCODE_BACK || k == KeyEvent.KEYCODE_HOME
            || k == KeyEvent.KEYCODE_VOLUME_UP || k == KeyEvent.KEYCODE_VOLUME_DOWN
            || k == KeyEvent.KEYCODE_POWER;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent e) {
        SharedPreferences p = prefs(this);
        int code = e.getKeyCode();

        if (p.getBoolean("learning", false)) {
            if (isNavKey(code)) return false;
            if (e.getAction() == KeyEvent.ACTION_UP) {
                p.edit().putInt("keycode", code).putBoolean("learning", false).apply();
            }
            return true;
        }

        int target = p.getInt("keycode", -1);
        if (target == -1 || code != target) return false;
        if (e.getAction() == KeyEvent.ACTION_UP) {
            PackageManager pm = getPackageManager();
            for (String pkg : p.getString("packages", "").split(",")) {
                pkg = pkg.trim();
                if (pkg.isEmpty()) continue;
                Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
                if (i == null) i = pm.getLaunchIntentForPackage(pkg);
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    break;
                }
            }
        }
        return true;
    }
}
