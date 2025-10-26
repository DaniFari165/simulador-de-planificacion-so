/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io;

import java.io.*;
/**
 *
 * @author Daniel Fariña
 */
public final class SimConfigJSON {
    public static final String RUTA = "config/simulacion.json";

    public static int cargarDuracion(int defecto) {
        String s = leer(RUTA);
        if (s == null || s.trim().isEmpty()) return defecto;
        String t = s.replaceAll("\\s+", "");
        int i = t.indexOf("\"duracion\":");
        if (i < 0) return defecto;
        i += 11;
        int j = i;
        while (j < t.length() && Character.isDigit(t.charAt(j))) j++;
        try { return Integer.parseInt(t.substring(i, j)); } catch (Exception e) { return defecto; }
    }

    public static void guardarDuracion(int ms) {
        asegurarDirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(RUTA))) {
            pw.println("{\"duracion\":" + ms + "}");
        } catch (IOException ignored) {}
    }

    private static void asegurarDirs() {
        File dir = new File("config");
        if (!dir.exists()) dir.mkdirs();
    }

    private static String leer(String ruta) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        } catch (IOException e) { return null; }
        return sb.toString();
    }

    private SimConfigJSON() {}
}