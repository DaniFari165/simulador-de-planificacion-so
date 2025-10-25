/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author 58412
 */
public final class EventLog {
    private static final EventLog INSTANCE = new EventLog();

    private final String[] buffer = new String[512];
    private int head = 0;
    private int count = 0;

    private volatile boolean toFile = true;
    private FileWriter out;

    private EventLog() {
        try {
            File dir = new File("logs");
            if (!dir.exists()) dir.mkdirs();
            out = new FileWriter(new File(dir, "scheduler.log"), true);
        } catch (IOException e) {
            toFile = false;
        }
    }

    public static EventLog get() {
        return INSTANCE;
    }

    public synchronized void log(String s) {
        long ts = System.currentTimeMillis();
        String line = "[" + ts + "] " + s;
        buffer[head] = line;
        head = (head + 1) % buffer.length;
        if (count < buffer.length) count++;
        if (toFile && out != null) {
            try {
                out.write(line);
                out.write(System.lineSeparator());
                out.flush();
            } catch (IOException ignored) { toFile = false; }
        }
    }

    public synchronized String[] dump() {
        String[] arr = new String[count];
        int idx = (head - count + buffer.length) % buffer.length;
        for (int i = 0; i < count; i++) {
            arr[i] = buffer[(idx + i) % buffer.length];
        }
        return arr;
    }
}

