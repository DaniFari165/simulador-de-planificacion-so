/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Daniel Fariña
 */
public final class Config {

    public static int DURACION_CICLO_MS = 500;
    public static int QUANTUM = 3;
    public static int CAPACIDAD = 4;
    public static String POLITICA = "FCFS";
    public static int UI_REFRESH_MS = 200;

    public static String PROCESOS_JSON = "config/procesos.json";

    private static String rutaConfig() {
        String home = System.getProperty("user.home");
        if (home == null || home.length() == 0) home = ".";
        return home + File.separator + ".simso.conf";
    }

    private static void asegurarDirs() {
        File cfgDir = new File("config");
        if (!cfgDir.exists()) cfgDir.mkdirs();
        File logsDir = new File("logs");
        if (!logsDir.exists()) logsDir.mkdirs();
    }

    public static void cargar() {
        asegurarDirs();
        File f = new File(rutaConfig());
        if (!f.exists()) return;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String k = line.substring(0, eq).trim();
                String v = line.substring(eq + 1).trim();

                if (k.equals("duracion")) DURACION_CICLO_MS = parseInt(v, DURACION_CICLO_MS);
                else if (k.equals("quantum")) QUANTUM = parseInt(v, QUANTUM);
                else if (k.equals("capacidad")) CAPACIDAD = parseInt(v, CAPACIDAD);
                else if (k.equals("politica")) POLITICA = v.length() == 0 ? POLITICA : v;
                else if (k.equals("ui_refresh")) UI_REFRESH_MS = parseInt(v, UI_REFRESH_MS);
                else if (k.equals("procesos_json")) PROCESOS_JSON = v.length() == 0 ? PROCESOS_JSON : v;
            }
        } catch (IOException ignored) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored2) {}
            }
        }
    }

    public static void guardar() {
        asegurarDirs();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(rutaConfig()));
            bw.write("duracion=" + DURACION_CICLO_MS); bw.newLine();
            bw.write("quantum=" + QUANTUM); bw.newLine();
            bw.write("capacidad=" + CAPACIDAD); bw.newLine();
            bw.write("politica=" + POLITICA); bw.newLine();
            bw.write("ui_refresh=" + UI_REFRESH_MS); bw.newLine();
            bw.write("procesos_json=" + PROCESOS_JSON); bw.newLine();
        } catch (IOException ignored) {
        } finally {
            if (bw != null) {
                try { bw.close();
                } catch (IOException ignored2) {}
            }
        }
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private Config() {}
}