/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import modelo.TipoProceso;
import modelo.Proceso;
import io.Config;
import io.EventLog;
import control.Kernel;
import io.SimConfigJSON;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;


/**
 *
 * @author Daniel Fariña
 */
public class VentanaPrincipal extends JFrame {
    private final Kernel kernel;

    private final JComboBox<String> cboPolitica = new JComboBox<>(new String[]{"FCFS","SPN","SRT","RR","HRRN","Feedback"});
    private final JSpinner spQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 1000, 1));
    private final JSpinner spDuracion = new JSpinner(new SpinnerNumberModel(500, 10, 5000, 10));
    private final JSpinner spCapacidad = new JSpinner(new SpinnerNumberModel(4, 1, 64, 1));
    private final JButton btnIniciar = new JButton("Iniciar");
    private final JButton btnPausar = new JButton("Pausar/Continuar");
    private final JButton btnDetener = new JButton("Detener");
    private final JButton btnCrear = new JButton("Crear Proceso");

    private final JLabel lblAlgoritmo = new JLabel("-");
    private final JLabel lblCiclo = new JLabel("0");
    private final JLabel lblActual = new JLabel("-");
    private final JLabel lblPC = new JLabel("-");
    private final JLabel lblMAR = new JLabel("-");
    private final JLabel lblModo = new JLabel("-");

    private final DefaultTableModel modeloNuevos = new DefaultTableModel(new Object[]{"PID","Nombre","Tipo","Total","Prioridad"}, 0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable tblNuevos = new JTable(modeloNuevos);

    private final DefaultTableModel modeloListos = new DefaultTableModel(new Object[]{"PID","Nombre","Tipo","Restantes","Total","Prioridad"}, 0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable tblListos = new JTable(modeloListos);

    private final DefaultTableModel modeloBloq = new DefaultTableModel(new Object[]{"PID","Nombre","Tipo","Restantes","Total","Prioridad","EsperaIO"}, 0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable tblBloqueados = new JTable(modeloBloq);

    private final DefaultTableModel modeloSusp = new DefaultTableModel(new Object[]{"PID","Nombre","Tipo","SL/SB","Restantes","Total","Prioridad","IO restante"}, 0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable tblSuspendidos = new JTable(modeloSusp);

    private final DefaultTableModel modeloTerminados = new DefaultTableModel(new Object[]{"PID","Nombre","Turnaround","Respuesta","Espera","CPU"}, 0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable tblTerminados = new JTable(modeloTerminados);

    private final JTextArea txtLog = new JTextArea();
    private Timer timer;

    private JSplitPane splitTop;
    private JSplitPane splitBottom;
    private JSplitPane splitCenter;

    public VentanaPrincipal() {
        this(new Kernel());
    }

    public VentanaPrincipal(Kernel kernel) {
        this.kernel = kernel;
        setTitle("Simulador de Planificación");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1220, 760));
        setLayout(new BorderLayout());
        Config.cargar();
        construirUI();
        inicializarValores();
        wireEventos();
        iniciarRefresco();
        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> {
            if (splitTop != null) splitTop.setDividerLocation(0.5);
            if (splitBottom != null) splitBottom.setDividerLocation(0.5);
            if (splitCenter != null) splitCenter.setDividerLocation(0.5);
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) { guardarConfig(); }
        });
    }

    private void construirUI() {
        add(crearPanelControles(), BorderLayout.NORTH);
        add(crearPanelCentro(), BorderLayout.CENTER);
        add(crearPanelLog(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelControles() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Controles"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5,5,5,5);
        gc.gridx=0; gc.gridy=0; p.add(new JLabel("Política:"), gc);
        gc.gridx=1; p.add(cboPolitica, gc);
        gc.gridx=2; p.add(new JLabel("Quantum:"), gc);
        gc.gridx=3; p.add(spQuantum, gc);
        gc.gridx=4; p.add(new JLabel("Duración ciclo (ms):"), gc);
        gc.gridx=5; p.add(spDuracion, gc);
        gc.gridx=6; p.add(new JLabel("Capacidad:"), gc);
        gc.gridx=7; p.add(spCapacidad, gc);
        gc.gridx=8; p.add(btnIniciar, gc);
        gc.gridx=9; p.add(btnPausar, gc);
        gc.gridx=10; p.add(btnDetener, gc);
        gc.gridx=11; p.add(btnCrear, gc);
        return p;
    }

    private JPanel crearPanelCPU() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("CPU / Estado"));
        p.setPreferredSize(new Dimension(260, 200));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,10,6,10);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx=0; gc.gridy=0; p.add(new JLabel("Algoritmo:"), gc);
        gc.gridx=1; p.add(lblAlgoritmo, gc);
        gc.gridx=0; gc.gridy=1; p.add(new JLabel("Ciclo global:"), gc);
        gc.gridx=1; p.add(lblCiclo, gc);
        gc.gridx=0; gc.gridy=2; p.add(new JLabel("Proceso actual:"), gc);
        gc.gridx=1; p.add(lblActual, gc);
        gc.gridx=0; gc.gridy=3; p.add(new JLabel("PC:"), gc);
        gc.gridx=1; p.add(lblPC, gc);
        gc.gridx=0; gc.gridy=4; p.add(new JLabel("MAR:"), gc);
        gc.gridx=1; p.add(lblMAR, gc);
        gc.gridx=0; gc.gridy=5; p.add(new JLabel("Modo:"), gc);
        gc.gridx=1; p.add(lblModo, gc);
        return p;
    }

    private JPanel crearPanelNuevos() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Nuevos"));
        tblNuevos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNuevos.setFillsViewportHeight(true);
        p.add(new JScrollPane(tblNuevos), BorderLayout.CENTER);
        return p;
    }

    private JSplitPane crearColumnaIzquierda() {
        JPanel cpu = crearPanelCPU();
        JPanel nuevos = crearPanelNuevos();
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cpu, nuevos);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.67);
        split.setDividerLocation(0.3);
        cpu.setMinimumSize(new Dimension(260, 180));
        nuevos.setMinimumSize(new Dimension(260, 220));
        split.setPreferredSize(new Dimension(450, 1));
        return split;
    }

    private JSplitPane crearSplitTop() {
        tblListos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBloqueados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblListos.setFillsViewportHeight(true);
        tblBloqueados.setFillsViewportHeight(true);
        JPanel left = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new BorderLayout());
        left.setBorder(BorderFactory.createTitledBorder("Listos"));
        right.setBorder(BorderFactory.createTitledBorder("Bloqueados"));
        left.add(new JScrollPane(tblListos), BorderLayout.CENTER);
        right.add(new JScrollPane(tblBloqueados), BorderLayout.CENTER);
        splitTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitTop.setContinuousLayout(true);
        splitTop.setResizeWeight(0.5);
        splitTop.setDividerLocation(0.5);
        return splitTop;
    }

    private JSplitPane crearSplitBottom() {
        tblSuspendidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTerminados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSuspendidos.setFillsViewportHeight(true);
        tblTerminados.setFillsViewportHeight(true);
        JPanel left = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new BorderLayout());
        left.setBorder(BorderFactory.createTitledBorder("Suspendidos"));
        right.setBorder(BorderFactory.createTitledBorder("Terminados"));
        left.add(new JScrollPane(tblSuspendidos), BorderLayout.CENTER);
        right.add(new JScrollPane(tblTerminados), BorderLayout.CENTER);
        splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitBottom.setContinuousLayout(true);
        splitBottom.setResizeWeight(0.5);
        splitBottom.setDividerLocation(0.5);
        return splitBottom;
    }

    private JSplitPane crearSplitCentro() {
        splitCenter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearSplitTop(), crearSplitBottom());
        splitCenter.setContinuousLayout(true);
        splitCenter.setResizeWeight(0.5);
        splitCenter.setDividerLocation(0.5);
        return splitCenter;
    }

    private JPanel crearPanelCentro() {
        JPanel center = new JPanel(new BorderLayout());
        center.add(crearColumnaIzquierda(), BorderLayout.WEST);
        center.add(crearSplitCentro(), BorderLayout.CENTER);
        return center;
    }

    private JScrollPane crearPanelLog() {
        txtLog.setEditable(false);
        txtLog.setRows(7);
        JScrollPane sp = new JScrollPane(txtLog);
        sp.setBorder(BorderFactory.createTitledBorder("Logs del planificador"));
        return sp;
    }

    private void inicializarValores() {
        int durJson = SimConfigJSON.cargarDuracion(Config.DURACION_CICLO_MS);
        spDuracion.setValue(durJson);
        spCapacidad.setValue(Config.CAPACIDAD);
        spQuantum.setValue(Config.QUANTUM);
        seleccionarPolitica(Config.POLITICA);
        kernel.setDuracionCiclo((Integer) spDuracion.getValue());
        kernel.setCapacidadMemoria((Integer) spCapacidad.getValue());
        kernel.setQuantumSiRR((Integer) spQuantum.getValue());
        lblAlgoritmo.setText(kernel.nombrePlanificador());
        lblCiclo.setText(String.valueOf(kernel.getCicloActual()));
    }

    private void seleccionarPolitica(String nombre) {
        if (nombre == null) nombre = "FCFS";
        if (nombre.equals("FCFS")) kernel.setPlanificadorFCFS();
        else if (nombre.equals("SPN")) kernel.setPlanificadorSPN();
        else if (nombre.equals("SRT")) kernel.setPlanificadorSRT();
        else if (nombre.equals("RR")) kernel.setPlanificadorRR((Integer) spQuantum.getValue());
        else if (nombre.equals("HRRN")) kernel.setPlanificadorHRRN();
        else if (nombre.equals("Feedback")) kernel.setPlanificadorFeedback();
        cboPolitica.setSelectedItem(nombre);
        lblAlgoritmo.setText(kernel.nombrePlanificador());
    }

    private void wireEventos() {
        cboPolitica.addActionListener(e -> {
            String v = (String) cboPolitica.getSelectedItem();
            if (v == null) return;
            seleccionarPolitica(v);
        });
        spQuantum.addChangeListener(e -> kernel.setQuantumSiRR((Integer) spQuantum.getValue()));
        spDuracion.addChangeListener(e -> {
            int v = (Integer) spDuracion.getValue();
            kernel.setDuracionCiclo(v);
            SimConfigJSON.guardarDuracion(v);
        });
        spCapacidad.addChangeListener(e -> kernel.setCapacidadMemoria((Integer) spCapacidad.getValue()));
        btnIniciar.addActionListener(e -> kernel.iniciar());
        btnPausar.addActionListener(e -> kernel.pausarOContinuar());
        btnDetener.addActionListener(e -> kernel.detener());
        btnCrear.addActionListener(e -> mostrarDialogoCrearProceso());
    }

    private void iniciarRefresco() {
        int ms = Config.UI_REFRESH_MS <= 0 ? 200 : Config.UI_REFRESH_MS;
        timer = new Timer(ms, e -> refrescar());
        timer.start();
    }

    private void refrescar() {
        lblAlgoritmo.setText(kernel.nombrePlanificador());
        lblCiclo.setText(String.valueOf(kernel.getCicloActual()));
        Proceso actual = kernel.getProcesoActual();
        if (actual != null) {
            lblActual.setText(textoProceso(actual));
            lblPC.setText(String.valueOf(actual.getPc()));
            lblMAR.setText(String.valueOf(actual.getMar()));
            lblModo.setText("Usuario PID " + actual.getPid());
        } else {
            lblActual.setText("-");
            lblPC.setText("-");
            lblMAR.setText("-");
            lblModo.setText("SO");
        }
        cargarNuevos();
        cargarListos();
        cargarBloqueados();
        cargarSuspendidos();
        cargarTerminados();
        String[] lines = EventLog.get().dump();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) sb.append(lines[i]).append('\n');
        txtLog.setText(sb.toString());
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void cargarNuevos() {
        modeloNuevos.setRowCount(0);
        Proceso[] arr = kernel.snapshotNuevos();
        for (int i = 0; i < arr.length; i++) {
            Proceso p = arr[i];
            modeloNuevos.addRow(new Object[]{
                p.getPid(),
                p.getNombre(),
                p.getTipo()==TipoProceso.CPU_BOUND?"CPU":"IO",
                p.getTotalInstrucciones(),
                p.getPrioridad()
            });
        }
    }

    private void cargarListos() {
        modeloListos.setRowCount(0);
        Proceso[] arr = kernel.snapshotListos();
        for (int i = 0; i < arr.length; i++) {
            Proceso p = arr[i];
            modeloListos.addRow(new Object[]{
                p.getPid(),
                p.getNombre(),
                p.getTipo()==TipoProceso.CPU_BOUND?"CPU":"IO",
                p.getRestantes(),
                p.getTotalInstrucciones(),
                p.getPrioridad()
            });
        }
    }

    private void cargarBloqueados() {
        modeloBloq.setRowCount(0);
        Object[][] rows = kernel.snapshotBloqueadosTable();
        for (int i = 0; i < rows.length; i++) modeloBloq.addRow(rows[i]);
    }

    private void cargarSuspendidos() {
        modeloSusp.setRowCount(0);
        Object[][] rows = kernel.snapshotSuspendidosTable();
        for (int i = 0; i < rows.length; i++) modeloSusp.addRow(rows[i]);
    }

    private void cargarTerminados() {
        modeloTerminados.setRowCount(0);
        Proceso[] arr = kernel.snapshotTerminados();
        for (int i = 0; i < arr.length; i++) {
            Proceso p = arr[i];
            int resp = (p.getStartCiclo()>=0 && p.getArrivalCiclo()>=0)?(p.getStartCiclo()-p.getArrivalCiclo()):-1;
            int turn = (p.getCompletionCiclo()>=0 && p.getArrivalCiclo()>=0)?(p.getCompletionCiclo()-p.getArrivalCiclo()):-1;
            modeloTerminados.addRow(new Object[]{
                p.getPid(),
                p.getNombre(),
                turn,
                resp,
                p.getTiempoEsperaAcumulado(),
                p.getTiempoCpuAcumulado()
            });
        }
    }

    private String textoProceso(Proceso p) {
        return "PID " + p.getPid() + " · " + p.getNombre() + " · " + (p.getTipo()==TipoProceso.CPU_BOUND?"CPU":"IO") + " · " + p.getRestantes() + "/" + p.getTotalInstrucciones();
    }

    private void mostrarDialogoCrearProceso() {
        JTextField txtNombre = new JTextField("P" + System.currentTimeMillis()%1000);
        JComboBox<String> cboTipo = new JComboBox<>(new String[]{"CPU","IO"});
        JSpinner spInstr = new JSpinner(new SpinnerNumberModel(20,1,10000,1));
        JSpinner spPrio = new JSpinner(new SpinnerNumberModel(5,0,100,1));
        JSpinner spIoCada = new JSpinner(new SpinnerNumberModel(5,1,1000,1));
        JSpinner spIoServ = new JSpinner(new SpinnerNumberModel(3,1,1000,1));
        JPanel f = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.gridx=0; gc.gridy=0; f.add(new JLabel("Nombre:"), gc);
        gc.gridx=1; f.add(txtNombre, gc);
        gc.gridx=0; gc.gridy=1; f.add(new JLabel("Tipo:"), gc);
        gc.gridx=1; f.add(cboTipo, gc);
        gc.gridx=0; gc.gridy=2; f.add(new JLabel("Instrucciones:"), gc);
        gc.gridx=1; f.add(spInstr, gc);
        gc.gridx=0; gc.gridy=3; f.add(new JLabel("Prioridad:"), gc);
        gc.gridx=1; f.add(spPrio, gc);
        gc.gridx=0; gc.gridy=4; f.add(new JLabel("IO cada (ciclos):"), gc);
        gc.gridx=1; f.add(spIoCada, gc);
        gc.gridx=0; gc.gridy=5; f.add(new JLabel("IO servicio (ciclos):"), gc);
        gc.gridx=1; f.add(spIoServ, gc);
        cboTipo.addActionListener(e -> {
            boolean io = "IO".equals(cboTipo.getSelectedItem());
            spIoCada.setEnabled(io);
            spIoServ.setEnabled(io);
        });
        cboTipo.setSelectedIndex(0);
        spIoCada.setEnabled(false);
        spIoServ.setEnabled(false);
        int r = JOptionPane.showConfirmDialog(this, f, "Crear Proceso", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim().length()==0 ? ("P" + System.currentTimeMillis()%1000) : txtNombre.getText().trim();
            boolean esIO = "IO".equals(cboTipo.getSelectedItem());
            TipoProceso tipo = esIO ? TipoProceso.IO_BOUND : TipoProceso.CPU_BOUND;
            int total = (Integer) spInstr.getValue();
            int pr = (Integer) spPrio.getValue();
            Proceso p = kernel.crearProceso(nombre, tipo, total, pr);
            if (esIO) {
                p.setIoEntry((Integer) spIoCada.getValue());
                p.setIoService((Integer) spIoServ.getValue());
            }
        }
    }

    private void guardarConfig() {
        Config.DURACION_CICLO_MS = (Integer) spDuracion.getValue();
        Config.CAPACIDAD = (Integer) spCapacidad.getValue();
        Config.QUANTUM = (Integer) spQuantum.getValue();
        Object sel = cboPolitica.getSelectedItem();
        Config.POLITICA = sel == null ? "FCFS" : sel.toString();
        Config.guardar();
        SimConfigJSON.guardarDuracion((Integer) spDuracion.getValue());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
