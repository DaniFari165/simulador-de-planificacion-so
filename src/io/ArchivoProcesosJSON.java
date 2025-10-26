/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io;

import java.io.*;
import modelo.Proceso;
import modelo.TipoProceso;
/**
 *
 * @author Daniel Fariña
 */
public final class ArchivoProcesosJSON {

    public static final class ProcSpec {
        public String nombre;
        public TipoProceso tipo;
        public int total;
        public int prioridad;
        public int ioCada;
        public int ioServicio;
    }

    public static void guardar(String ruta, Proceso[] procesos) throws IOException {
        asegurarDirs(ruta);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(ruta));
            pw.println("[");
            for (int i = 0; i < procesos.length; i++) {
                Proceso p = procesos[i];
                String tipo = p.getTipo().name();
                String nombreEsc = escapeJson(p.getNombre());
                int ioCada = p.getIoEntry();
                int ioServ = p.getIoService();
                pw.print("  {");
                pw.print("\"nombre\":\"" + nombreEsc + "\",");
                pw.print("\"tipo\":\"" + tipo + "\",");
                pw.print("\"total\":" + p.getTotalInstrucciones() + ",");
                pw.print("\"prioridad\":" + p.getPrioridad() + ",");
                pw.print("\"ioCada\":" + ioCada + ",");
                pw.print("\"ioServicio\":" + ioServ);
                pw.print("}");
                if (i < procesos.length - 1) pw.println(","); else pw.println();
            }
            pw.println("]");
        } finally {
            if (pw != null) pw.close();
        }
    }

    public static ProcSpec[] cargar(String ruta) throws IOException {
        String json = leerTodo(ruta);
        json = json.trim();
        if (json.length() == 0 || json.equals("[]")) return new ProcSpec[0];
        if (json.charAt(0) == '[') json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        String[] objs = splitTopLevelObjects(json);
        ProcSpec[] out = new ProcSpec[objs.length];
        int n = 0;
        for (int i = 0; i < objs.length; i++) {
            String o = objs[i].trim();
            if (o.length() == 0) continue;
            if (o.charAt(0) == '{') o = o.substring(1);
            if (o.endsWith("}")) o = o.substring(0, o.length() - 1);
            ProcSpec s = parseObject(o);
            if (s != null) out[n++] = s;
        }
        if (n == out.length) return out;
        ProcSpec[] rec = new ProcSpec[n];
        for (int i = 0; i < n; i++) rec[i] = out[i];
        return rec;
    }

    private static ProcSpec parseObject(String body) {
        String[] kvs = splitFields(body);
        ProcSpec s = new ProcSpec();
        for (int i = 0; i < kvs.length; i++) {
            String kv = kvs[i];
            int c = kv.indexOf(':');
            if (c <= 0) continue;
            String key = stripQuotes(kv.substring(0, c).trim());
            String val = kv.substring(c + 1).trim();
            if (key.equals("nombre")) s.nombre = unescapeJson(stripQuotes(val));
            else if (key.equals("tipo")) s.tipo = parseTipo(stripQuotes(val));
            else if (key.equals("total")) s.total = parseInt(val, 10);
            else if (key.equals("prioridad")) s.prioridad = parseInt(val, 0);
            else if (key.equals("ioCada")) s.ioCada = parseInt(val, 0);
            else if (key.equals("ioServicio")) s.ioServicio = parseInt(val, 0);
        }
        if (s.nombre == null) s.nombre = "P" + (System.currentTimeMillis() % 1000);
        if (s.tipo == null) s.tipo = TipoProceso.CPU_BOUND;
        if (s.total <= 0) s.total = 10;
        if (s.prioridad < 0) s.prioridad = 0;
        if (s.ioCada < 0) s.ioCada = 0;
        if (s.ioServicio < 0) s.ioServicio = 0;
        return s;
    }

    private static String[] splitTopLevelObjects(String s) {
        int depth = 0;
        String[] parts = new String[Math.max(1, s.length() / 8)];
        int count = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '{') depth++;
            else if (ch == '}') depth--;
            else if (ch == ',' && depth == 0) {
                if (count == parts.length) parts = grow(parts);
                parts[count++] = s.substring(start, i);
                start = i + 1;
            }
        }
        if (start < s.length()) {
            if (count == parts.length) parts = grow(parts);
            parts[count++] = s.substring(start);
        }
        String[] out = new String[count];
        for (int i = 0; i < count; i++) out[i] = parts[i];
        return out;
    }

    private static String[] splitFields(String s) {
        int depth = 0;
        boolean inQ = false;
        String[] parts = new String[16];
        int count = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inQ = !inQ;
            else if (!inQ) {
                if (ch == '{' || ch == '[') depth++;
                else if (ch == '}' || ch == ']') depth--;
                else if (ch == ',' && depth == 0) {
                    if (count == parts.length) parts = grow(parts);
                    parts[count++] = s.substring(start, i);
                    start = i + 1;
                }
            }
        }
        if (start < s.length()) {
            if (count == parts.length) parts = grow(parts);
            parts[count++] = s.substring(start);
        }
        String[] out = new String[count];
        for (int i = 0; i < count; i++) out[i] = parts[i].trim();
        return out;
    }

    private static String stripQuotes(String s) {
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"')
            return s.substring(1, s.length() - 1);
        return s;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '"') { b.append('\\').append(c); }
            else if (c == '\n') b.append("\\n");
            else if (c == '\r') b.append("\\r");
            else if (c == '\t') b.append("\\t");
            else b.append(c);
        }
        return b.toString();
    }

    private static String unescapeJson(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(i + 1);
                if (n == 'n') { b.append('\n'); i++; }
                else if (n == 'r') { b.append('\r'); i++; }
                else if (n == 't') { b.append('\t'); i++; }
                else { b.append(n); i++; }
            } else b.append(c);
        }
        return b.toString();
    }

    private static String leerTodo(String ruta) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(ruta));
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        } finally {
            if (br != null) try { br.close(); } catch (IOException ignored) {}
        }
        return sb.toString();
    }

    private static String[] grow(String[] a) {
        String[] b = new String[a.length * 2];
        for (int i = 0; i < a.length; i++) b[i] = a[i];
        return b;
    }

    private static void asegurarDirs(String ruta) {
        File f = new File(ruta);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static TipoProceso parseTipo(String s) {
        String v = s.trim().toUpperCase();
        if (v.startsWith("IO")) return TipoProceso.IO_BOUND;
        return TipoProceso.CPU_BOUND;
    }

    private ArchivoProcesosJSON() {}
}
