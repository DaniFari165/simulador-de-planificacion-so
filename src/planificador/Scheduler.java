/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificador;

import control.Kernel;

/**
 *
 * @author 58412
 */
public interface Scheduler {
    void onTick(Kernel k);
    String name();
}
