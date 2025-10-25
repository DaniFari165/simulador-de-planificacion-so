/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import java.util.function.BiConsumer;
import estructuras.*;
import modelo.*;
import planificador.*;

/**
 *
 * @author 58412
 */
public class Kernel {
    private RelojGlobal reloj = new RelojGlobal();
    private ColaProceso colaNuevos = new ColaProceso();
    private ColaProceso colaListos = new ColaProceso();
    private ColaProceso colaTerminados = new ColaProceso();
    private ColaBloqueados colaBloqueados = new ColaBloqueados();
    private ColaSuspendidos colaSuspendidos = new ColaSuspendidos();

    private final CPU cpu = new CPU();

    private Scheduler planificador = new FCFS();

    private int nextPid = 1;

    private long ciclosTotales = 0;
    private long ciclosCpuOcupada = 0;
    private long procesosCompletados = 0;

    private int capacidadMemoria = 4;

    public Kernel() {
        BiConsumer<Integer,Long> tick = (ciclo, ts) -> {
            if (cpu.estaLibre() && colaListos.esVacia() && colaBloqueados.esVacia() && colaNuevos.esVacia() && colaSuspendidos.esVacia()) {
                ciclosTotales++;
                return;
            }
            planificador.onTick(this);
            admitirNuevosSiHayMemoria();
            reactivarSuspendidosSiHayMemoria();
            acumularEsperaEnListos();
            ciclosTotales++;
            if (!cpu.estaLibre()) ciclosCpuOcupada++;
            forzarSuspensionSiOverflow();
        };
        reloj.addListener(tick);
    }

    public void addTickListener(BiConsumer<Integer,Long> l) {
        reloj.addListener(l);
    }

    private void acumularEsperaEnListos() {
        Proceso[] arr = colaListos.toArray();
        for (int i = 0; i < arr.length; i++) arr[i].tickEsperando();
    }

    private int procesosEnMemoria() {
        int enListos = colaListos.getSize();
        int enCPU = cpu.estaLibre() ? 0 : 1;
        int enBloq = colaBloqueados.getSize();
        return enListos + enCPU + enBloq;
    }

    private void admitirNuevosSiHayMemoria() {
        while (!colaNuevos.esVacia() && procesosEnMemoria() < capacidadMemoria) {
            Proceso p = colaNuevos.desencolar();
            p.setEstado(EstadoProceso.LISTO);
            colaListos.encolar(p);
        }
    }

    private void reactivarSuspendidosSiHayMemoria() {
        Proceso[] sbListos = colaSuspendidos.avanzarUnCicloYReactivarSiCorresponde();
        for (int i = 0; i < sbListos.length; i++) {
            Proceso p = sbListos[i];
            if (procesosEnMemoria() < capacidadMemoria) {
                p.setEstado(EstadoProceso.LISTO);
                colaListos.encolar(p);
            } else {
                colaSuspendidos.suspenderListo(p);
            }
        }
        while (procesosEnMemoria() < capacidadMemoria) {
            Proceso p = colaSuspendidos.reactivarListo();
            if (p == null) break;
            colaListos.encolar(p);
        }
    }

    private void forzarSuspensionSiOverflow() {
        while (procesosEnMemoria() > capacidadMemoria && !colaListos.esVacia()) {
            Proceso victima = colaListos.desencolar();
            colaSuspendidos.suspenderListo(victima);
        }
    }

    public void setPlanificadorFCFS() {
        this.planificador = new FCFS();
    }
    public void setPlanificadorRR(int quantum) {
        this.planificador = new RoundRobin(quantum);
    }
    public void setPlanificadorSJF() {
        this.planificador = new SJF();
    }
    public void setPlanificadorSRTF() {
        this.planificador = new SRTF();
    }
    public void setPlanificadorPrioridadNP() {
        this.planificador = new PrioridadNP();
    }
    public void setPlanificadorPrioridadP() {
        this.planificador = new PrioridadP();
    }
    public String nombrePlanificador() {
        return planificador.name();
    }
    public void setQuantumSiRR(int q) {
        if (planificador instanceof RoundRobin) ((RoundRobin)planificador).setQuantum(q);
    }

    public void iniciar() {
        if (!reloj.isAlive()) {
            reloj = new RelojGlobal();
            BiConsumer<Integer,Long> tick = (c, ts) -> {
                if (cpu.estaLibre() && colaListos.esVacia() && colaBloqueados.esVacia() && colaNuevos.esVacia() && colaSuspendidos.esVacia()) {
                    ciclosTotales++;
                    return;
                }
                planificador.onTick(this);
                admitirNuevosSiHayMemoria();
                reactivarSuspendidosSiHayMemoria();
                acumularEsperaEnListos();
                ciclosTotales++;
                if (!cpu.estaLibre()) ciclosCpuOcupada++;
                forzarSuspensionSiOverflow();
            };
            reloj.addListener(tick);
            reloj.iniciar();
        }
    }

    public void pausarOContinuar() {
        if (!reloj.isAlive()) 
            return;
        if (reloj.isPausado()) 
            reloj.continuar(); else reloj.pausar();
    }

    public void detener() {
        if (reloj != null && reloj.isAlive()) 
            reloj.detener();
    }
    public void setDuracionCiclo(int ms) { reloj.setDuracionCiclo(ms); }
    public int getDuracionCiclo() { return reloj.getDuracionCiclo(); }
    public int getCicloActual() { return reloj.getCicloActual(); }

    public void reiniciarSimulacion() {
        detener();
        colaNuevos = new ColaProceso();
        colaListos = new ColaProceso();
        colaTerminados = new ColaProceso();
        colaBloqueados = new ColaBloqueados();
        colaSuspendidos = new ColaSuspendidos();
        nextPid = 1;
        ciclosTotales = 0;
        ciclosCpuOcupada = 0;
        procesosCompletados = 0;
        if (!cpu.estaLibre()) cpu.liberar();
        reloj = new RelojGlobal();
        BiConsumer<Integer,Long> tick = (ciclo, ts) -> {
            if (cpu.estaLibre() && colaListos.esVacia() && colaBloqueados.esVacia() && colaNuevos.esVacia() && colaSuspendidos.esVacia()) {
                ciclosTotales++;
                return;
            }
            planificador.onTick(this);
            admitirNuevosSiHayMemoria();
            reactivarSuspendidosSiHayMemoria();
            acumularEsperaEnListos();
            ciclosTotales++;
            if (!cpu.estaLibre()) ciclosCpuOcupada++;
            forzarSuspensionSiOverflow();
        };
        reloj.addListener(tick);
    }

    public Proceso crearProceso(String nombre, TipoProceso tipo, int totalInstr) {
        return crearProceso(nombre, tipo, totalInstr, 0);
    }

    public Proceso crearProceso(String nombre, TipoProceso tipo, int totalInstr, int prioridad) {
        Proceso p = new Proceso(nextPid++, nombre, tipo, totalInstr);
        p.setPrioridad(prioridad);
        p.setEstado(EstadoProceso.NUEVO);
        p.setArrivalCiclo(getCicloActual());
        p.start();
        colaNuevos.encolar(p);
        return p;
    }

    public Proceso[] snapshotNuevos() {
        return colaNuevos.toArray();
    }
    public Proceso[] snapshotListos() {
        return colaListos.toArray();
    }
    public Proceso[] snapshotTerminados() {
        return colaTerminados.toArray();
    }
    public String[] snapshotBloqueadosStrings() {
        return colaBloqueados.toDisplayStrings();
    }
    public Object[][] snapshotBloqueadosTable() {
        return colaBloqueados.toTableData();
    }
    public Object[][] snapshotSuspendidosTable() {
        return colaSuspendidos.toTableData();
    }
    public Proceso getProcesoActual() {
        return cpu.getActual();
    }

    public boolean cpuLibre() {
        return cpu.estaLibre();
    }
    public boolean listosVacio() {
        return colaListos.esVacia();
    }

    public Proceso desencolarListo() {
        return colaListos.desencolar();
    }
    public void encolarListo(Proceso p) {
        colaListos.encolar(p);
    }

    public void asignarCPU(Proceso p) {
        p.marcarInicioSiCorresponde(getCicloActual());
        cpu.asignar(p);
    }

    public ProcesoEvento ejecutarCPU() {
        ProcesoEvento ev = cpu.tick();
        if (ev.getTipo() == ProcesoEvento.Tipo.TERMINADO) {
            Proceso fin = ev.getProceso();
            fin.marcarCompletion(getCicloActual());
            procesosCompletados++;
        }
        return ev;
    }

    public Proceso preemptarCPU() {
        return cpu.preempt();
    }

    public Proceso peekListoMinRestantes() {
        Proceso[] arr = colaListos.toArray();
        if (arr.length == 0) return null;
        Proceso best = arr[0];
        for (int i = 1; i < arr.length; i++) if (arr[i].getRestantes() < best.getRestantes()) best = arr[i];
        return best;
    }

    public Proceso desencolarListoMinTotal() {
        return colaListos.retirarMinPorTotal();
    }
    public Proceso desencolarListoMinRestantes() {
        return colaListos.retirarMinPorRestantes();
    }

    public Proceso peekListoMinPrioridad() {
        Proceso[] arr = colaListos.toArray();
        if (arr.length == 0) return null;
        Proceso best = arr[0];
        for (int i = 1; i < arr.length; i++) if (arr[i].getPrioridad() < best.getPrioridad()) best = arr[i];
        return best;
    }

    public Proceso desencolarListoMinPrioridad() { return colaListos.retirarMinPorPrioridad(); }

    public void manejarEvento(ProcesoEvento ev) {
        switch (ev.getTipo()) {
            case TERMINADO -> colaTerminados.encolar(ev.getProceso());
            case BLOQUEADO -> {
                if (procesosEnMemoria() < capacidadMemoria) {
                    colaBloqueados.bloquear(ev.getProceso(), ev.getIoEsperaCiclos());
                } else {
                    colaSuspendidos.suspenderBloqueado(ev.getProceso(), ev.getIoEsperaCiclos());
                }
            }
            case NINGUNO -> {}
        }
    }

    public void liberarBloqueadosAListos() {
        Proceso[] libres = colaBloqueados.avanzarUnCicloYLiberar();
        for (int i = 0; i < libres.length; i++) {
            Proceso p = libres[i];
            if (procesosEnMemoria() < capacidadMemoria) {
                colaListos.encolar(p);
            } else {
                colaSuspendidos.suspenderListo(p);
            }
        }
    }

    public long getCiclosTotales() {
        return ciclosTotales;
    }
    public long getCiclosCpuOcupada() {
        return ciclosCpuOcupada;
    }
    public long getProcesosCompletados() {
        return procesosCompletados;
    }

    public String obtenerResumenMetricas() {
        double usoCpu = (ciclosTotales == 0) ? 0.0 : (100.0 * ciclosCpuOcupada / ciclosTotales);
        return "ciclosTotales=" + ciclosTotales + ";cpuOcupada=" + ciclosCpuOcupada + ";usoCpu=" + String.format("%.2f", usoCpu) + "%;terminados=" + procesosCompletados;
    }

    public int getCapacidadMemoria() {
        return capacidadMemoria;
    }
    public void setCapacidadMemoria(int cap) {
        this.capacidadMemoria = Math.max(1, cap);
    }
}
