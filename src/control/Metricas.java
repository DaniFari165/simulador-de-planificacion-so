/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author Daniel Fariña
 */
public final class Metricas {
    private final int ventana;
    private final int serieMax;

    private int ciclo;
    private int ocupadosVentana;
    private final int[] cpuOcupadaHist;
    private int idxCpu;
    private int sumaCpu;

    private final int[] terminadosHist;
    private int idxTerm;
    private int sumaTerm;

    private final int[] respHist;
    private int idxResp;
    private int sumaResp;
    private int cntResp;

    private final double[] serieUso;
    private final double[] serieThroughput;
    private final double[] serieRespProm;
    private int lenSerie;

    public Metricas(int ventanaMovil, int largoSerie) {
        this.ventana = Math.max(1, ventanaMovil);
        this.serieMax = Math.max(10, largoSerie);
        this.cpuOcupadaHist = new int[ventana];
        this.terminadosHist = new int[ventana];
        this.respHist = new int[ventana];
        this.serieUso = new double[serieMax];
        this.serieThroughput = new double[serieMax];
        this.serieRespProm = new double[serieMax];
        this.ciclo = 0;
        this.idxCpu = 0;
        this.idxTerm = 0;
        this.idxResp = 0;
        this.sumaCpu = 0;
        this.sumaTerm = 0;
        this.sumaResp = 0;
        this.cntResp = 0;
        this.lenSerie = 0;
    }

    public void onTick(boolean cpuOcupada) {
        ciclo++;
        int inc = cpuOcupada ? 1 : 0;
        sumaCpu -= cpuOcupadaHist[idxCpu];
        cpuOcupadaHist[idxCpu] = inc;
        sumaCpu += inc;
        idxCpu = (idxCpu + 1) % ventana;

        int termCero = 0;
        sumaTerm -= terminadosHist[idxTerm];
        terminadosHist[idxTerm] = termCero;
        sumaTerm += termCero;
        idxTerm = (idxTerm + 1) % ventana;

        sumaResp -= respHist[idxResp];
        respHist[idxResp] = 0;
        idxResp = (idxResp + 1) % ventana;
        if (cntResp > 0 && sumaResp == 0) cntResp = 0;

        double uso = 100.0 * sumaCpu / ventana;
        double thr = (double) sumaTerm;
        double resp = cntResp == 0 ? 0.0 : ((double) sumaResp / cntResp);

        appendSerie(uso, thr, resp);
    }

    public void onProcesoTerminado(int respuesta) {
        sumaTerm -= terminadosHist[(idxTerm + ventana - 1) % ventana];
        terminadosHist[(idxTerm + ventana - 1) % ventana] += 1;
        sumaTerm += 1;

        int pos = (idxResp + ventana - 1) % ventana;
        sumaResp -= respHist[pos];
        respHist[pos] += Math.max(0, respuesta);
        sumaResp += Math.max(0, respuesta);
        cntResp++;
    }

    private void appendSerie(double uso, double thr, double resp) {
        if (lenSerie < serieMax) {
            serieUso[lenSerie] = uso;
            serieThroughput[lenSerie] = thr;
            serieRespProm[lenSerie] = resp;
            lenSerie++;
        } else {
            for (int i = 1; i < serieMax; i++) {
                serieUso[i - 1] = serieUso[i];
                serieThroughput[i - 1] = serieThroughput[i];
                serieRespProm[i - 1] = serieRespProm[i];
            }
            serieUso[serieMax - 1] = uso;
            serieThroughput[serieMax - 1] = thr;
            serieRespProm[serieMax - 1] = resp;
        }
    }

    public double[] getUso() {
        int n = lenSerie;
        double[] out = new double[n];
        for (int i = 0; i < n; i++) out[i] = serieUso[i];
        return out;
    }

    public double[] getThroughput() {
        int n = lenSerie;
        double[] out = new double[n];
        for (int i = 0; i < n; i++) out[i] = serieThroughput[i];
        return out;
    }

    public double[] getRespProm() {
        int n = lenSerie;
        double[] out = new double[n];
        for (int i = 0; i < n; i++) out[i] = serieRespProm[i];
        return out;
    }
}
