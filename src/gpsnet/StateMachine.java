/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpsnet;

import java.awt.Color;

/**
 *
 * @author hackr6
 */


public class StateMachine implements Runnable {

    public static boolean connected = false;
    public static boolean logging   = false;
    public static boolean fix       = false;
    public static boolean tac       = false;
    public static boolean gll       = false;
    public static boolean vtg       = false;
    public static boolean gsv       = false;
    public static boolean gsa       = false;
    public static boolean gga       = false;
    public static boolean rmc       = false;

    @Override
    public void run() {
        
        while (connected) {

            if (GPSNetDisplay.gllCheckBox.isSelected()){
                gll = true;
            } else {
                gll = false;
            }

            if (GPSNetDisplay.vtgCheckBox.isSelected()){
                vtg = true;
            } else {
                vtg = false;
            }

            if (GPSNetDisplay.gsvCheckBox.isSelected()){
                gsv = true;
            } else {
                gsv = false;
            }
            
            if (GPSNetDisplay.gsaCheckBox.isSelected()){
                gsa = true;
            } else {
                gsa = false;
            }

            if (GPSNetDisplay.ggaCheckBox.isSelected()){
                gga = true;
            } else {
                gga = false;
            }

            if (GPSNetDisplay.rmcCheckBox.isSelected()){
                rmc = true;
            } else {
                rmc = false;
            }
            
            if (fix) {
                GPSNetDisplay.fixStatusText.setText("Fix Status | Locked");
            } else {
                GPSNetDisplay.fixStatusText.setText("Fix Status | None");
            }

            if (GPSNetDisplay.tacCheckBox.isSelected()){
                tac = true;
            } else {
                tac = false;
            }
            
            if (GPSNetDisplay.logCheckBox.isSelected()) {
                logging = true;
            } else {
                logging = false;
            }

            try{
                Thread.sleep(50L);
            } catch (InterruptedException exc) {}
        }

    }
    
}
