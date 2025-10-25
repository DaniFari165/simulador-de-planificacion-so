/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import java.util.function.BiConsumer;
import estructuras.*;
import modelo.*;
import planificador.*;
import io.*;

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

    private IODevice ioDev = new IODevice(this);

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
            colaBloqueados.decrementarUnCiclo();
            ciclosTotales++;
            if (!cpu.estaLibre()) ciclosCpuOcupada++;
            forzarSuspensionSiOverflow();
        };
        reloj.addListener(tick);
        ioDev.start();
        EventLog.get().log("Kernel iniciado; planificador=" + nombrePlanificador());
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
            EventLog.get().log("ADMIT pid=" + p.getPid());
        }
    }

    private void reactivarSuspendidosSiHayMemoria() {
        Proceso[] sbListos = colaSuspendidos.avanzarUnCicloYReactivarSiCorresponde();
        for (int i = 0; i < sbListos.length; i++) {
            Proceso p = sbListos[i];
            if (procesosEnMemoria() < capacidadMemoria) {
                p.setEstado(EstadoProceso.LISTO);
                colaListos.encolar(p);
                EventLog.get().log("RESUME pid=" + p.getPid());
            } else {
                colaSuspendidos.suspenderListo(p);
            }
        }
        while (procesosEnMemoria() < capacidadMemoria) {
            Proceso p = colaSuspendidos.reactivarListo();
            if (p == null) break;
            colaListos.encolar(p);
            EventLog.get().log("RESUME pid=" + p.getPid());
        }
    }

    private void forzarSuspensionSiOverflow() {
        while (procesosEnMemoria() > capacidadMemoria && !colaListos.esVacia()) {
            Proceso victima = colaListos.desencolar();
            colaSuspendidos.suspenderListo(victima);
            EventLog.get().log("SUSPEND pid=" + victima.getPid());
        }
    }

    public void setPlanificadorFCFS() {
        this.planificador = new FCFS();
    }
    public void setPlanificadorRR(int quantum) {
        this.planificador = new RoundRobin(quantum);
    }
    public void setPlanificadorSPN() {
        this.planificador = new SPN();
    }
    public void setPlanificadorSRT() {
        this.planificador = new SRT();
    }
    public void setPlanificadorHRRN() {
        this.planificador = new HRRN();
    }
    public void setPlanificadorFeedback() {
        this.planificador = new Feedback();
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
                colaBloqueados.decrementarUnCiclo();
                ciclosTotales++;
                if (!cpu.estaLibre()) ciclosCpuOcupada++;
                forzarSuspensionSiOverflow();
            };
            reloj.addListener(tick);
            reloj.iniciar();
            if (!ioDev.isAlive()) {
                ioDev = new IODevice(this);
                ioDev.start();
            }
        }
    }

    public void pausarOContinuar() {
        if (!reloj.isAlive()) {
            return;
        }
        if (reloj.isPausado()) {
            reloj.continuar();
        } else {
            reloj.pausar();
        }
    }

    public void detener() {
        if (reloj != null && reloj.isAlive()) {
            reloj.detener();
        }
        if (ioDev != null) {
            ioDev.detener();
        }
        EventLog.get().log("Kernel detenido");
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
            colaBloqueados.decrementarUnCiclo();
            ciclosTotales++;
            if (!cpu.estaLibre()) ciclosCpuOcupada++;
            forzarSuspensionSiOverflow();
        };
        reloj.addListener(tick);
        ioDev = new IODevice(this);
        ioDev.start();
        EventLog.get().log("Kernel reiniciado; planificador=" + nombrePlanificador());
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
        EventLog.get().log("CREATE pid=" + p.getPid() + " tipo=" + p.getTipo().name() + " total=" + totalInstr + " prio=" + prioridad);
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
        EventLog.get().log("DISPATCH pid=" + p.getPid());
        cpu.asignar(p);
    }

    public ProcesoEvento ejecutarCPU() {
        ProcesoEvento ev = cpu.tick();
        if (ev.getTipo() == ProcesoEvento.Tipo.TERMINADO) {
            Proceso fin = ev.getProceso();
            fin.marcarCompletion(getCicloActual());
            procesosCompletados++;
            EventLog.get().log("EXIT pid=" + fin.getPid());
            colaTerminados.encolar(fin);
            return ev;
        }
        if (ev.getTipo() == ProcesoEvento.Tipo.BLOQUEADO) {
            EventLog.get().log("BLOCK pid=" + ev.getProceso().getPid() + " io=" + ev.getIoEsperaCiclos());
            colaBloqueados.bloquear(ev.getProceso(), ev.getIoEsperaCiclos());
            ioDev.encolarIO(ev.getProceso(), ev.getIoEsperaCiclos(), getDuracionCiclo());
            return ev;
        }
        return ev;
    }

    public Proceso preemptarCPU() {
        Proceso prev = cpu.preempt();
        if (prev != null) {
            EventLog.get().log("PREEMPT pid=" + prev.getPid());
        }
        return prev;
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
        if (ev.getTipo() == ProcesoEvento.Tipo.TERMINADO) {
            colaTerminados.encolar(ev.getProceso());
        } else if (ev.getTipo() == ProcesoEvento.Tipo.BLOQUEADO) {
            EventLog.get().log("BLOCK pid=" + ev.getProceso().getPid() + " io=" + ev.getIoEsperaCiclos());
            colaBloqueados.bloquear(ev.getProceso(), ev.getIoEsperaCiclos());
            ioDev.encolarIO(ev.getProceso(), ev.getIoEsperaCiclos(), getDuracionCiclo());
        }
    }

    public void liberarBloqueadosAListos() {
        colaBloqueados.decrementarUnCiclo();
    }

    public void ioCompleto(Proceso p) {
        if (p == null) return;
        colaBloqueados.liberarPorPid(p.getPid());
        if (procesosEnMemoria() < capacidadMemoria) {
            p.setEstado(EstadoProceso.LISTO);
            colaListos.encolar(p);
            EventLog.get().log("UNBLOCK pid=" + p.getPid());
        } else {
            colaSuspendidos.suspenderListo(p);
            EventLog.get().log("UNBLOCK->SUSP pid=" + p.getPid());
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