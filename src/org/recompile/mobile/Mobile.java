/*
	This file is part of FreeJ2ME.

	FreeJ2ME is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	FreeJ2ME is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with FreeJ2ME.  If not, see http://www.gnu.org/licenses/
*/
package org.recompile.mobile;

import org.recompile.freej2me.FreeJ2ME;
import org.recompile.freej2me.J2meSandBox;

import java.io.InputStream;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.m3g.Graphics3D;

/*

	Mobile

	Provides MobilePlatform access to mobile app

*/

public class Mobile {
    private MobilePlatform platform;

    private Display display;

    private Graphics3D graphics3d;

    private J2meSandBox j2meSandBox;

    public static boolean quiet = false;

    public static boolean nokia = false;

    public static boolean siemens = false;

    public static boolean motorola = false;

    public static boolean sound = true;

    //Standard keycodes
    public static final int KEY_NUM0 = Canvas.KEY_NUM0;  // 48
    public static final int KEY_NUM1 = Canvas.KEY_NUM1;  // 49
    public static final int KEY_NUM2 = Canvas.KEY_NUM2;  // 50
    public static final int KEY_NUM3 = Canvas.KEY_NUM3;  // 51
    public static final int KEY_NUM4 = Canvas.KEY_NUM4;  // 52
    public static final int KEY_NUM5 = Canvas.KEY_NUM5;  // 53
    public static final int KEY_NUM6 = Canvas.KEY_NUM6;  // 54
    public static final int KEY_NUM7 = Canvas.KEY_NUM7;  // 55
    public static final int KEY_NUM8 = Canvas.KEY_NUM8;  // 56
    public static final int KEY_NUM9 = Canvas.KEY_NUM9;  // 57
    public static final int KEY_STAR = Canvas.KEY_STAR;  // 42
    public static final int KEY_POUND = Canvas.KEY_POUND; // 35
    public static final int GAME_UP = Canvas.UP;     // 1
    public static final int GAME_DOWN = Canvas.DOWN;   // 6
    public static final int GAME_LEFT = Canvas.LEFT;   // 2
    public static final int GAME_RIGHT = Canvas.RIGHT;  // 5
    public static final int GAME_FIRE = Canvas.FIRE;   // 8
    public static final int GAME_A = Canvas.GAME_A; // 9
    public static final int GAME_B = Canvas.GAME_B; // 10
    public static final int GAME_C = Canvas.GAME_C; // 11
    public static final int GAME_D = Canvas.GAME_D; // 12

    //Nokia-specific keycodes
    public static final int NOKIA_UP = -1; // KEY_UP_ARROW = -1;
    public static final int NOKIA_DOWN = -2; // KEY_DOWN_ARROW = -2;
    public static final int NOKIA_LEFT = -3; // KEY_LEFT_ARROW = -3;
    public static final int NOKIA_RIGHT = -4; // KEY_RIGHT_ARROW = -4;
    public static final int NOKIA_SOFT1 = -6; // KEY_SOFTKEY1 = -6;
    public static final int NOKIA_SOFT2 = -7; // KEY_SOFTKEY2 = -7;
    public static final int NOKIA_SOFT3 = -5; // KEY_SOFTKEY3 = -5;
    public static final int NOKIA_END = -11; // KEY_END = -11;
    public static final int NOKIA_SEND = -10; // KEY_SEND = -10;

    //Siemens-specific keycodes
    public static final int SIEMENS_UP = -59;
    public static final int SIEMENS_DOWN = -60;
    public static final int SIEMENS_LEFT = -61;
    public static final int SIEMENS_RIGHT = -62;
    public static final int SIEMENS_SOFT1 = -1;
    public static final int SIEMENS_SOFT2 = -4;
    public static final int SIEMENS_FIRE = -26;

    //Motorola-specific keycodes
    public static final int MOTOROLA_UP = -1;
    public static final int MOTOROLA_DOWN = -6;
    public static final int MOTOROLA_LEFT = -2;
    public static final int MOTOROLA_RIGHT = -5;
    public static final int MOTOROLA_SOFT1 = -21;
    public static final int MOTOROLA_SOFT2 = -22;
    public static final int MOTOROLA_FIRE = -20;


    public Mobile(J2meSandBox j2meSandBox) {
        this.j2meSandBox = j2meSandBox;
    }

    public J2meSandBox getJ2meSandBox() {
        return j2meSandBox;
    }

    public MobilePlatform getPlatform() {
        return platform;
    }

    public void setPlatform(MobilePlatform p) {
        platform = p;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display d) {
        display = d;
    }

    public Graphics3D getGraphics3D() {
        return graphics3d;
    }

    public void setGraphics3D(Graphics3D g) {
        graphics3d = g;
    }

    static public InputStream getResourceAsStream(Class c, String resource) {
        return FreeJ2ME.getMobile().platform.loader.getMIDletResourceAsStream(resource);
    }

    static public InputStream getMIDletResourceAsStream(String resource) {
        return FreeJ2ME.getMobile().platform.loader.getMIDletResourceAsStream(resource);
    }

    public static void log(String text) {
        if (!quiet) {
            System.out.println(text);
        }
    }

    // #region debug-point shared:m3g-runtime-stall
    public static String debugJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        String hex = Integer.toHexString(ch);
                        builder.append("\\u");
                        for (int j = hex.length(); j < 4; j++) {
                            builder.append('0');
                        }
                        builder.append(hex);
                    } else {
                        builder.append(ch);
                    }
                    break;
            }
        }
        return builder.toString();
    }

    public static final class DebugWatchScope {
        private final String hypothesisId;
        private final String location;
        private final long startMs;
        private final long watchdogMs;
        private volatile boolean completed;
        private volatile boolean watchdogFired;
        private volatile String stage;

        private DebugWatchScope(String hypothesisId, String location, long watchdogMs) {
            this.hypothesisId = hypothesisId;
            this.location = location;
            this.watchdogMs = watchdogMs;
            this.startMs = System.currentTimeMillis();
            this.stage = "begin";
        }
    }

    public static DebugWatchScope beginDebugWatchScope(final String hypothesisId, final String location, final long watchdogMs) {
        final DebugWatchScope scope = new DebugWatchScope(hypothesisId, location, watchdogMs);
        Thread watchdog = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(watchdogMs);
                    if (!scope.completed) {
                        scope.watchdogFired = true;
                        reportDebugEventNow(hypothesisId, location, "[DEBUG] scope still running", "{\"thread\":\""
                                + debugJson(Thread.currentThread().getName())
                                + "\",\"elapsedMs\":"
                                + (System.currentTimeMillis() - scope.startMs)
                                + ",\"watchdogMs\":"
                                + scope.watchdogMs
                                + ",\"stage\":\""
                                + debugJson(scope.stage)
                                + "\"}");
                    }
                } catch (Throwable ignored) {
                }
            }
        }, "debug-watchdog-" + hypothesisId);
        watchdog.setDaemon(true);
        watchdog.start();
        return scope;
    }

    public static void updateDebugWatchScope(DebugWatchScope scope, String stage) {
        if (scope != null) {
            scope.stage = stage;
        }
    }

    public static void finishDebugWatchScope(DebugWatchScope scope) {
        if (scope == null) {
            return;
        }
        scope.completed = true;
        if (scope.watchdogFired) {
            reportDebugEventNow(scope.hypothesisId, scope.location, "[DEBUG] scope end", "{\"thread\":\""
                    + debugJson(Thread.currentThread().getName())
                    + "\",\"totalMs\":"
                    + (System.currentTimeMillis() - scope.startMs)
                    + ",\"stage\":\""
                    + debugJson(scope.stage)
                    + "\"}");
        }
    }

    public static void reportDebugEvent(final String hypothesisId, final String location, final String msg, final String dataJson) {
        Thread reporter = new Thread(new Runnable() {
            public void run() {
                reportDebugEventNow(hypothesisId, location, msg, dataJson);
            }
        }, "debug-m3g-runtime-stall");
        reporter.setDaemon(true);
        reporter.start();
    }

    public static void reportDebugEventNow(final String hypothesisId, final String location, final String msg, final String dataJson) {
        try {
            postDebugEvent(hypothesisId, location, msg, dataJson);
        } catch (Throwable ignored) {
        }
    }

    private static void postDebugEvent(final String hypothesisId, final String location, final String msg, final String dataJson) {
        try {
            String debugServerUrl = "http://127.0.0.1:7777/event";
            String debugSessionId = "m3g-runtime-stall";
            try {
                java.io.File[] envFiles = new java.io.File[]{
                        new java.io.File("d:\\github\\j2me\\freej2me\\.dbg\\m3g-runtime-stall.env"),
                        new java.io.File(".dbg\\m3g-runtime-stall.env")
                };
                for (int i = 0; i < envFiles.length; i++) {
                    java.io.File envFile = envFiles[i];
                    if (envFile.isFile()) {
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile));
                        try {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("DEBUG_SERVER_URL=")) {
                                    debugServerUrl = line.substring("DEBUG_SERVER_URL=".length()).trim();
                                } else if (line.startsWith("DEBUG_SESSION_ID=")) {
                                    debugSessionId = line.substring("DEBUG_SESSION_ID=".length()).trim();
                                }
                            }
                        } finally {
                            reader.close();
                        }
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }
            String payload = "{\"sessionId\":\""
                    + debugJson(debugSessionId)
                    + "\",\"runId\":\"pre-fix\",\"hypothesisId\":\""
                    + debugJson(hypothesisId)
                    + "\",\"location\":\""
                    + debugJson(location)
                    + "\",\"msg\":\""
                    + debugJson(msg)
                    + "\",\"data\":"
                    + (dataJson == null ? "{}" : dataJson)
                    + ",\"ts\":"
                    + System.currentTimeMillis()
                    + "}";
            byte[] body = payload.getBytes("UTF-8");
            String[] candidateUrls = new String[]{
                    debugServerUrl,
                    "http://127.0.0.1:7779/event",
                    "http://127.0.0.1:7778/event",
                    "http://127.0.0.1:7777/event"
            };
            for (int i = 0; i < candidateUrls.length; i++) {
                java.net.HttpURLConnection connection = null;
                try {
                    connection = (java.net.HttpURLConnection) new java.net.URL(candidateUrls[i]).openConnection();
                    connection.setConnectTimeout(200);
                    connection.setReadTimeout(200);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    java.io.OutputStream output = connection.getOutputStream();
                    output.write(body);
                    output.close();
                    int responseCode = connection.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        break;
                    }
                } catch (Throwable ignored) {
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    // #endregion

    public void notifyDestroy() {
        if (display != null) {
            display.destroy();
        }
        if (platform.inputFrame != null) {
            platform.inputFrame.dispose();
        }
        if (j2meSandBox != null) {
            j2meSandBox.notifyDestroy();
        }
    }

    public int getLcdWidth() {
        return platform.lcdWidth;
    }

    public int getLcdHeight() {
        return platform.lcdHeight;
    }
}
