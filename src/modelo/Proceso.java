/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import java.util.concurrent.Semaphore;
/**
 *
 * @author 58412
 */
public class Proceso extends Thread{
    
    private int pid;
    private String name;
    private TipoProceso type;
    private int totalInstructions;
    private int left;
    private int priority;
    private int pc;
    private int mar;
    private int ioEntry;
    private int ioService;
    private EstadoProceso status = EstadoProceso.NUEVO;
    private int arrivalCiclo = -1;
    private int startCiclo = -1;
    private int completionCiclo = -1;
    private int tiempoEsperaAcumulado = 0;
    private int tiempoCpuAcumulado = 0;
    private Semaphore paso = new Semaphore(0);
    private Semaphore finPaso = new Semaphore(0);
    private volatile boolean terminar = false;
    private volatile ProcesoEvento ultimoEvento = new ProcesoEvento(ProcesoEvento.Tipo.NINGUNO, this, 0);

    public Proceso(int pid, String name, TipoProceso type, int totalInstrucciones) {
        this.pid = pid;
        this.name = name;
        this.type = type;
        this.totalInstructions = Math.max(1, totalInstrucciones);
        this.left = this.totalInstructions;
        setName("P-" + pid + "-" + name);
        setDaemon(true);
    }

    public void run() {
        while (true) {
            try { 
                paso.acquire(); 
            } 
            catch (InterruptedException e) { 
                return; 
            }
            if (terminar) { 
                finPaso.release(); 
                return; 
            }
            ejecutarUnPaso();
            finPaso.release();
        }
    }

    private void ejecutarUnPaso() {
        if (status == EstadoProceso.BLOQUEADO || status == EstadoProceso.SUSPENDIDO || status == EstadoProceso.TERMINADO) {
            ultimoEvento = new ProcesoEvento(ProcesoEvento.Tipo.NINGUNO, this, 0);
            return;
        }
        
        status = EstadoProceso.EJECUCION;
        pc++;
        mar = pc;
        left--;
        tiempoCpuAcumulado++;
        
        if (left <= 0) {
            status = EstadoProceso.TERMINADO;
            ultimoEvento = new ProcesoEvento(ProcesoEvento.Tipo.TERMINADO, this, 0);
            return;
        }
        
        if (type == TipoProceso.IO_BOUND && ioEntry > 0 && (pc % ioEntry == 0)) {
            status = EstadoProceso.BLOQUEADO;
            ultimoEvento = new ProcesoEvento(ProcesoEvento.Tipo.BLOQUEADO, this, Math.max(1, ioService));
            return;
        }
        
        status = EstadoProceso.LISTO;
        ultimoEvento = new ProcesoEvento(ProcesoEvento.Tipo.NINGUNO, this, 0);
    }

    public void solicitarPaso() { 
        paso.release(); 
    }
    
    public void esperarFinPaso() {
        try { 
            finPaso.acquire(); 
        }catch 
            (InterruptedException ignored) {}
    }
    
    public ProcesoEvento getUltimoEvento() { 
        return ultimoEvento; 
    }

    public void tickEsperando() { 
        if (status == EstadoProceso.LISTO || status == EstadoProceso.NUEVO) 
            tiempoEsperaAcumulado++; 
    }
    
    public void marcarInicioSiCorresponde(int ciclo) { 
        if (startCiclo < 0) startCiclo = ciclo; 
    }
    
    public void marcarCompletion(int ciclo) { 
        completionCiclo = ciclo; terminar = true; 
        paso.release(); 
    }

    public int getPid() { 
        return pid; 
    }
    
    public String getNombre() { 
        return name; 
    }
    
    public TipoProceso getTipo() { 
        return type; 
    }
    
    public void setTipo(TipoProceso t) { 
        this.type = t; 
    }
    
    public int getTotalInstrucciones() { 
        return totalInstructions; 
    }
    
    public int getRestantes() { 
        return left; 
    }
    public int getPrioridad() { 
        return priority; 
    }
    
    public void setPrioridad(int p) { 
        this.priority = p; 
    }
    
    public int getPc() { 
        return pc; 
    }
    
    public int getMar() { 
        return mar; 
    }

    public int getIoEntry() {
        return ioEntry;
    }

    public int getIoService() {
        return ioService;
    }
    
    public void setIoEntry(int v) { 
        this.ioEntry = Math.max(0, v); 
    }
    
    public void setIoService(int v) { 
        this.ioService = Math.max(0, v); 
    }

    public EstadoProceso getEstado() { 
        return status; 
    }
    
    public void setEstado(EstadoProceso e) { 
        this.status = e; 
    }

    public int getArrivalCiclo() { 
        return arrivalCiclo; 
    }
    
    public void setArrivalCiclo(int c) { 
        this.arrivalCiclo = c; 
    }
    
    public int getStartCiclo() { 
        return startCiclo; 
    }
    
    public int getCompletionCiclo() { 
        return completionCiclo; 
    }
    
    public int getTiempoEsperaAcumulado() { 
        return tiempoEsperaAcumulado; 
    }
    
    public int getTiempoCpuAcumulado() { 
        return tiempoCpuAcumulado;
    }
}
