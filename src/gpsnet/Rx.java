/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpsnet;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author hackr6
 */
public class Rx {
    
    SerialPort connection = null;
    Px         parser     = null;
    
    byte [] rateCmd = {(byte) 0xB5, (byte) 0x62, (byte) 0x06, (byte) 0x03, (byte) 0xE8, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00 };

    Rx (String portName) {
    
        parser = new Px ();
        Thread parser_thread = new Thread(parser);
        parser_thread.start();

        connection = SerialPort.getCommPort(portName);
        
        connection.setBaudRate(Integer.parseInt(GPSNetDisplay.baudRateField.getText()));
        
        connection.setParity(SerialPort.NO_PARITY);
        connection.setNumDataBits(8);
        connection.setNumStopBits(1);
        
        connection.openPort ();
        
        GPSMsgHandler listener = new GPSMsgHandler ();
        
        connection.addDataListener (listener);
        
        }
    
    public void purge () {

        parser = null;
        connection.removeDataListener ();
        connection.closePort();
        }
    
    }

    

    class GPSMsgHandler implements SerialPortMessageListener {

        @Override
        public byte[] getMessageDelimiter() {
            return new byte[] { (byte) 0x0D, (byte) 0x0A };
        }

        @Override
        public boolean delimiterIndicatesEndOfMessage() {
            return true;
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent spe) {
            
            byte [] message = spe.getReceivedData();

            String str = new String(message, StandardCharsets.UTF_8);
            
            Px.parseQueue.offer(str.split(","));
        
        }
    }
