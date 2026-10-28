package com.mikhaell.tvlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.KeyEvent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// Servidor HTTP minimo para o celular controlar a TV pela rede do Wi-Fi.
// A propria TV serve a pagina do controle (assets/remote.html), entao o celular
// so precisa abrir o endereco mostrado na tela - nada para instalar.
public class RemoteServer {
    public static final int PORT = 8355;

    private static volatile boolean started = false;
    private static volatile String lastError = "";
    private static Context appCtx;
    private static Activity activity;
    private static byte[] cachedPage = null;

    private static final String OK = "{\"ok\":true}";

    static void setActivity(Activity a) {
        activity = a;
    }

    public static boolean isRunning() {
        return started;
    }

    public static String lastError() {
        return lastError;
    }

    public static void start(Context ctx) {
        if (started) return;
        synchronized (RemoteServer.class) {
            if (started) return;
            appCtx = ctx.getApplicationContext();
            started = true;
            Thread t = new Thread(new Runnable() {
                public void run() {
                    ServerSocket ss = null;
                    try {
                        ss = new ServerSocket(PORT);
                        while (true) {
                            final Socket s = ss.accept();
                            Thread w = new Thread(new Runnable() {
                                public void run() {
                                    handle(s);
                                }
                            });
                            w.setDaemon(true);
                            w.start();
                        }
                    } catch (Exception e) {
                        started = false;
                        lastError = e.toString();
                    } finally {
                        try {
                            if (ss != null) ss.close();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }, "tv-remote");
            t.setDaemon(true);
            t.start();
        }
    }

    // ---------- atendimento ----------

    private static void handle(Socket s) {
        try {
            s.setSoTimeout(15000);
            InputStream in = s.getInputStream();
            String line = readLine(in);
            if (line == null || line.isEmpty()) {
                s.close();
                return;
            }
            String[] parts = line.split(" ");
            String method = parts[0];
            String path = parts.length > 1 ? parts[1] : "/";
            int q = path.indexOf('?');
            if (q >= 0) path = path.substring(0, q);

            int clen = 0;
            String h;
            while ((h = readLine(in)) != null && !h.isEmpty()) {
                if (h.toLowerCase().startsWith("content-length:")) {
                    try {
                        clen = Integer.parseInt(h.substring(15).trim());
                    } catch (Exception ignored) {
                    }
                }
            }

            byte[] body = new byte[0];
            if (clen > 0) {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n = 0;
                while (n < clen) {
                    int r = in.read(buf, 0, Math.min(buf.length, clen - n));
                    if (r < 0) break;
                    bo.write(buf, 0, r);
                    n += r;
                }
                body = bo.toByteArray();
            }

            if (method.equals("OPTIONS")) {
                send(s, 204, "text/plain", new byte[0]);
                return;
            }
            if (method.equals("GET") && path.equals("/")) {
                send(s, 200, "text/html; charset=utf-8", page());
                return;
            }
            if (method.equals("GET") && path.equals("/info")) {
                send(s, 200, "application/json; charset=utf-8",
                    ("{\"ok\":true,\"name\":\"" + esc(Build.MODEL) + "\",\"port\":" + PORT + "}")
                        .getBytes("UTF-8"));
                return;
            }
            if (method.equals("POST") && path.equals("/command")) {
                send(s, 200, "application/json; charset=utf-8",
                    command(new String(body, "UTF-8")).getBytes("UTF-8"));
                return;
            }
            send(s, 404, "application/json; charset=utf-8",
                "{\"ok\":false,\"error\":\"not_found\"}".getBytes("UTF-8"));
        } catch (Exception e) {
            try {
                s.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static void send(Socket s, int code, String type, byte[] body) throws Exception {
        String status;
        if (code == 200) status = "200 OK";
        else if (code == 204) status = "204 No Content";
        else if (code == 404) status = "404 Not Found";
        else status = code + " Error";
        String head = "HTTP/1.1 " + status + "\r\n"
            + "Content-Type: " + type + "\r\n"
            + "Content-Length: " + body.length + "\r\n"
            + "Access-Control-Allow-Origin: *\r\n"
            + "Access-Control-Allow-Headers: content-type\r\n"
            + "Access-Control-Allow-Methods: GET,POST,OPTIONS\r\n"
            + "Cache-Control: no-store\r\n"
            + "Connection: close\r\n\r\n";
        OutputStream o = s.getOutputStream();
        o.write(head.getBytes("UTF-8"));
        o.write(body);
        o.flush();
        s.close();
    }

    private static String readLine(InputStream in) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            if (b != '\r') bo.write(b);
        }
        if (b == -1 && bo.size() == 0) return null;
        return bo.toString("UTF-8");
    }

    private static byte[] page() {
        if (cachedPage != null) return cachedPage;
        try {
            InputStream in = appCtx.getAssets().open("remote.html");
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) > 0) bo.write(buf, 0, r);
            in.close();
            cachedPage = bo.toByteArray();
            return cachedPage;
        } catch (Exception e) {
            lastError = "assets/remote.html: " + e;
            return "<!doctype html><meta charset=utf-8><h3>remote.html ausente no APK</h3>".getBytes();
        }
    }

    // ---------- comandos ----------

    private static String command(String body) {
        String action = jstr(body, "action");
        if (action == null) return err("no_action");
        try {
            if (action.equals("ping")) return OK;

            if (action.equals("key")) {
                int k = jnum(body, "keycode", -1);
                if (k < 0) return err("bad_key");
                return press(k) ? OK : err("launcher_closed");
            }
            if (action.equals("back")) {
                back();
                return OK;
            }
            if (action.equals("home")) {
                home();
                return OK;
            }
            if (action.equals("volume")) {
                return volume(jstr(body, "dir")) ? OK : err("volume");
            }
            if (action.equals("media")) {
                return media(jnum(body, "keycode", -1)) ? OK : err("media");
            }
            if (action.equals("open")) {
                return openPackages(jstr(body, "packages"));
            }
            if (action.equals("settings")) {
                return openSettings(jstr(body, "setting"));
            }
        } catch (Exception e) {
            return err("failed");
        }
        return err("unknown_action");
    }

    private static boolean press(int code) {
        final Activity a = activity;
        if (a == null) return false;
        a.runOnUiThread(new Runnable() {
            public void run() {
                long t = SystemClock.uptimeMillis();
                a.dispatchKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN, code, 0));
                a.dispatchKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_UP, code, 0));
            }
        });
        return true;
    }

    private static void back() {
        RemapService acc = RemapService.instance;
        if (acc != null) {
            acc.performGlobalAction(2); // GLOBAL_ACTION_BACK
            return;
        }
        press(KeyEvent.KEYCODE_BACK);
    }

    private static void home() {
        RemapService acc = RemapService.instance;
        if (acc != null) {
            acc.performGlobalAction(3); // GLOBAL_ACTION_HOME
            return;
        }
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appCtx.startActivity(i);
        } catch (Exception ignored) {
        }
    }

    private static boolean volume(String dir) {
        AudioManager am = (AudioManager) appCtx.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return false;
        int adj;
        if (dir == null || dir.equals("up")) adj = AudioManager.ADJUST_RAISE;
        else if (dir.equals("down")) adj = AudioManager.ADJUST_LOWER;
        else if (dir.equals("mute")) adj = AudioManager.ADJUST_TOGGLE_MUTE;
        else return false;
        try {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, adj, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean media(int code) {
        if (code < 0) return false;
        AudioManager am = (AudioManager) appCtx.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return false;
        try {
            long t = SystemClock.uptimeMillis();
            am.dispatchMediaKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN, code, 0));
            am.dispatchMediaKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_UP, code, 0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String openPackages(String pkgs) {
        if (pkgs == null || pkgs.trim().isEmpty()) return err("no_packages");
        PackageManager pm = appCtx.getPackageManager();
        for (String pkg : pkgs.split(",")) {
            pkg = pkg.trim();
            if (pkg.isEmpty()) continue;
            Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (i == null) i = pm.getLaunchIntentForPackage(pkg);
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appCtx.startActivity(i);
                return OK;
            }
        }
        return err("not_installed");
    }

    private static String openSettings(String action) {
        String[] actions = { action, Settings.ACTION_SETTINGS };
        for (String a : actions) {
            if (a == null || a.isEmpty()) continue;
            try {
                Intent i = new Intent(a);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appCtx.startActivity(i);
                return OK;
            } catch (Exception ignored) {
            }
        }
        return err("unavailable");
    }

    // ---------- util ----------

    private static String err(String m) {
        return "{\"ok\":false,\"error\":\"" + m + "\"}";
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String jstr(String b, String key) {
        int i = b.indexOf("\"" + key + "\"");
        if (i < 0) return null;
        i = b.indexOf(':', i);
        if (i < 0) return null;
        int a = b.indexOf('"', i + 1);
        if (a < 0) return null;
        int z = b.indexOf('"', a + 1);
        if (z < 0) return null;
        return b.substring(a + 1, z);
    }

    private static int jnum(String b, String key, int def) {
        int i = b.indexOf("\"" + key + "\"");
        if (i < 0) return def;
        i = b.indexOf(':', i);
        if (i < 0) return def;
        StringBuilder sb = new StringBuilder();
        for (int j = i + 1; j < b.length(); j++) {
            char c = b.charAt(j);
            if (c == '-' || (c >= '0' && c <= '9')) sb.append(c);
            else if (sb.length() > 0) break;
        }
        if (sb.length() == 0) return def;
        try {
            return Integer.parseInt(sb.toString());
        } catch (Exception e) {
            return def;
        }
    }
}
