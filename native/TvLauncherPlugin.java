package com.mikhaell.tvlauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "TvLauncher")
public class TvLauncherPlugin extends Plugin {

    private boolean accessibilityOn() {
        String s = Settings.Secure.getString(getContext().getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return s != null && s.contains(getContext().getPackageName() + "/");
    }

    // ---------- controle pelo celular ----------

    @PluginMethod
    public void remoteInfo(PluginCall call) {
        com.getcapacitor.JSObject r = new com.getcapacitor.JSObject();
        String ip = localIp();
        r.put("ip", ip == null ? "" : ip);
        r.put("url", ip == null ? "" : "http://" + ip + ":" + RemoteServer.PORT);
        r.put("port", RemoteServer.PORT);
        r.put("name", android.os.Build.MODEL);
        r.put("running", RemoteServer.isRunning());
        r.put("error", RemoteServer.lastError());
        call.resolve(r);
    }

    private static String localIp() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> ifs =
                java.net.NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                java.net.NetworkInterface n = ifs.nextElement();
                if (!n.isUp() || n.isLoopback() || n.isVirtual()) continue;
                java.util.Enumeration<java.net.InetAddress> addrs = n.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    java.net.InetAddress a = addrs.nextElement();
                    if (a instanceof java.net.Inet4Address && a.isSiteLocalAddress()) {
                        return a.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    @PluginMethod
    public void remapStatus(PluginCall call) {
        android.content.SharedPreferences p = RemapService.prefs(getContext());
        com.getcapacitor.JSObject r = new com.getcapacitor.JSObject();
        r.put("enabled", accessibilityOn());
        r.put("running", RemapService.running);
        r.put("learning", p.getBoolean("learning", false));
        r.put("keycode", p.getInt("keycode", -1));
        call.resolve(r);
    }

    @PluginMethod
    public void startLearning(PluginCall call) {
        RemapService.prefs(getContext()).edit()
            .putBoolean("learning", true)
            .putString("packages", call.getString("packages", ""))
            .apply();
        call.resolve();
    }

    @PluginMethod
    public void clearRemap(PluginCall call) {
        RemapService.prefs(getContext()).edit().remove("keycode").putBoolean("learning", false).apply();
        call.resolve();
    }

    @PluginMethod
    public void open(PluginCall call) {
        String[] pkgs = call.getString("packages", "").split(",");
        PackageManager pm = getContext().getPackageManager();
        for (String pkg : pkgs) {
            pkg = pkg.trim();
            if (pkg.isEmpty()) continue;
            Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (i == null) i = pm.getLaunchIntentForPackage(pkg);
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
                call.resolve();
                return;
            }
        }
        call.reject("not_installed");
    }

    @PluginMethod
    public void openSettings(PluginCall call) {
        String[] actions = { call.getString("action", Settings.ACTION_SETTINGS), Settings.ACTION_SETTINGS };
        for (String a : actions) {
            try {
                Intent i = new Intent(a);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
                call.resolve();
                return;
            } catch (Exception ignored) {}
        }
        call.reject("unavailable");
    }
}
