/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpsnet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author hackr6
 */
public class Px implements Runnable {
    
    public static HashMap<String, MsgType> typeDict;
    public static LinkedBlockingQueue<String []> parseQueue;
    public static String [] tacFrame = null;
    public static long counter = 0;
    private static BufferedWriter writer = null;

    enum MsgType {

        GLL,
        VTG,
        GSV,
        GSA,
        GGA,
        RMC
    }

    Px () {
        typeDict = new HashMap<> ();

        typeDict.put("$GPGLL", MsgType.GLL);
        typeDict.put("$GPVTG", MsgType.VTG);
        typeDict.put("$GPGSV", MsgType.GSV);
        typeDict.put("$GPGSA", MsgType.GSA);
        typeDict.put("$GPGGA", MsgType.GGA);
        typeDict.put("$GPRMC", MsgType.RMC);

        parseQueue = new LinkedBlockingQueue <> ();

        tacFrame = new String [8];
    }

    @Override
    public void run () {

        while(StateMachine.connected) {
            try {
                parseMsg(Px.parseQueue.poll(1,TimeUnit.NANOSECONDS));
            } catch (Exception e) {}
        }

    }

    public static void parseMsg(String [] str) {

        MsgType type;

        // Determine Message Type and call function:
        if (typeDict.containsKey(str[0])) {
            type = Px.typeDict.get(str[0]);
        } else {
            return;
        }
        
        switch (type) {

            case GLL:
                parseGLL(str);
                break;

            case VTG:
                parseVTG(str);
                break;

            case GSV:
                parseGSV(str);
                break;

            case GSA:
                parseGSA(str);
                break;

            case GGA:
                parseGGA(str);
                break;

            case RMC:
                parseRMC(str);
                break;

            default:
                break;

            }

        if (StateMachine.tac && StateMachine.fix) {
            counter++;
            if(counter % 10 == 0){
                parseTAC(); 
            }

            if (counter % 1000 == 0){
                GPSNetDisplay.resultTextField.setText("");
                counter = 0;
            }
        }
    }

    private static void parseGLL (String [] str) {

        // Check if the option is enabled and create string
        if(StateMachine.gll){

            // Check to see if the data is blank, if so, break out.
            //if (str[1] == "") { return; }

            // Determine latitude and longitude components
            String fixHrs  = "";
            String fixMins = "";
            String fixSecs = "";

            String lat_deg  = "";
            String lat_mins = "";

            String long_deg  = "";
            String long_mins = "";

            if ( !str[1].equals("") && !str[2].equals("") && !str[4].equals("")){
                fixHrs  = str[5].substring(0,2);
                fixMins = str[5].substring(2,4);
                fixSecs = str[5].substring(4);
    
                lat_deg  = str[1].substring(0,2);
                lat_mins = str[1].substring(2);
    
                long_deg  = str[3].substring(0,3);
                long_mins = str[3].substring(3);
            }

            // Determine Data Status
            String dataStatus = (str[6].equals("A")) ? "Active" : "Void";

            //Checksum Mode
            String chksmMode = "";

            if      (str[7].charAt(0) == 'A') { chksmMode = "Autonomous"; }
            else if (str[7].charAt(0) == 'D') { chksmMode = "Differential"; }
            else if (str[7].charAt(0) == 'E') { chksmMode = "Estimated/Dead Reckoning"; }
            else if (str[7].charAt(0) == 'M') { chksmMode = "Manual Input"; }
            else if (str[7].charAt(0) == 'S') { chksmMode = "Simulator"; }
            else                                    { chksmMode = "Data Not Valid"; }

            // Craft and show parsed message!
            String gllMsg = "";

            gllMsg += String.format("\nMessage Type\t| Geographic Longitude and Latitude");
            gllMsg += String.format("\nFix Time\t| %s:%s:%s UTC", fixHrs, fixMins, fixSecs);
            gllMsg += String.format("\nLatitude\t| %s degrees %s minutes %s", lat_deg, lat_mins, str[2]);
            gllMsg += String.format("\nLongitude\t| %s degrees %s minutes %s", long_deg, long_mins, str[4]);
            gllMsg += String.format("\nData Status\t| %s", dataStatus);
            gllMsg += String.format("\nChksm Mode\t| %s", chksmMode);

            GPSNetDisplay.updateResultPane(gllMsg);

        } else {
            return;
        }
    }

    private static void parseVTG (String [] str) {

        // Check if the option is enabled and create string
        if(StateMachine.vtg){

            // Check to see if the data is blank, if so, break out.
            //if (str[1] == "") { return; }

            // Determine True Track, Magnetic Track, and Ground Speed
            String true_track = str[1];
            String mag_track = str[3];
            String gpseed_knots = str[5];
            String gpseed_kph = str[7];

            //Checksum Mode
            String chksmMode = "";

            if      (str[9].charAt(0) == 'A') { chksmMode = "Autonomous"; }
            else if (str[9].charAt(0) == 'D') { chksmMode = "Differential"; }
            else if (str[9].charAt(0) == 'E') { chksmMode = "Estimated/Dead Reckoning"; }
            else if (str[9].charAt(0) == 'M') { chksmMode = "Manual Input"; }
            else if (str[9].charAt(0) == 'S') { chksmMode = "Simulator"; }
            else                                   { chksmMode = "Data Not Valid"; }

            // Craft and show parsed message!
            String vtgMsg = "";

            vtgMsg += String.format("\nMessage Type\t| Vector Track and Ground Speed");
            vtgMsg += String.format("\nTrue Track\t| %s degrees", true_track);
            vtgMsg += String.format("\nMag. Track\t| %s degrees", mag_track);
            vtgMsg += String.format("\nSpeed (Naut.)\t| %s knots", gpseed_knots);
            vtgMsg += String.format("\nSpeed (Met.)\t| %s kilometers/hour", gpseed_kph);
            vtgMsg += String.format("\nChksm Mode\t| %s", chksmMode);

            GPSNetDisplay.updateResultPane(vtgMsg);

        } else {
            return;
        }
    }

    private static void parseGSV (String [] str) {

        // Check if the option is enabled and create string
        if(StateMachine.gsv){

            // Craft and show parsed message!
            String gsvMsg = "";
            String snr = "";

            gsvMsg += String.format("\nMessage Type\t| Detailed Satellite Link Data");
            gsvMsg += String.format("\nTot. Sen.\t| %s", str[1]);
            gsvMsg += String.format("\nCurr. Sen.\t| %s", str[2]);
            gsvMsg += String.format("\nSat.'s in View\t| %s", str[3]);
            gsvMsg += String.format("\nSat. 1 PRN\t| %s", str[4]);
            gsvMsg += String.format("\nElevation\t| %s degrees", str[5]);
            gsvMsg += String.format("\nAzimuth\t| %s degrees", str[6]);

            String [] pieces = str[7].split("[*]");
            snr = pieces[0];

            gsvMsg += String.format("\nSNR\t| %s dB", snr);

            if (str.length > 8) {

                gsvMsg += String.format("\nSat. 2 PRN\t| %s", str[8]);
                gsvMsg += String.format("\nElevation\t| %s degrees", str[9]);
                gsvMsg += String.format("\nAzimuth\t| %s degrees", str[10]);

                snr = "";
                pieces = str[11].split("[*]");
                snr = pieces[0];
    
                gsvMsg += String.format("\nSNR\t| %s dB", snr);

            }

            if (str.length > 12) {

                gsvMsg += String.format("\nSat. 3 PRN\t| %s", str[12]);
                gsvMsg += String.format("\nElevation\t| %s degrees", str[13]);
                gsvMsg += String.format("\nAzimuth\t| %s degrees", str[14]);
                
                snr = "";
                pieces = str[15].split("[*]");
                snr = pieces[0];
    
                gsvMsg += String.format("\nSNR\t| %s dB", snr);

            }

            if (str.length > 16) {

                gsvMsg += String.format("\nSat. 4 PRN\t| %s", str[16]);
                gsvMsg += String.format("\nElevation\t| %s degrees", str[17]);
                gsvMsg += String.format("\nAzimuth\t| %s degrees", str[18]);

                snr = "";
                pieces = str[19].split("[*]");
                snr = pieces[0];

                gsvMsg += String.format("\nSNR\t| %s dB", snr);

            }

            GPSNetDisplay.updateResultPane(gsvMsg);


        } else {
            return;
        }
    }

    private static void parseGSA (String [] str) {

        // Check if the option is enabled and create string
        if(StateMachine.gsa){

            // Craft and show parsed message!
            String gsaMsg = "";
            String fixType1 = (str[1].equals("A")) ? "Automatic" : "Manual";
            String fixType2 = "";

            if (str[2].equals("1")){
                fixType2 = "Not Available";
            } else if (str[2].equals("2")) {
                fixType2 = "2D";
            } else if (str[2].equals("3")) {
                fixType2 = "3D";
            }

            gsaMsg += String.format("\nMessage Type\t| Dilution of Precision Data");
            gsaMsg += String.format("\nMode 1\t| %s", fixType1);
            gsaMsg += String.format("\nFix Type\t| %s", fixType2);
            gsaMsg += String.format("\nPRN 1\t| %s", str[3]);
            gsaMsg += String.format("\nPRN 2\t| %s", str[4]);
            gsaMsg += String.format("\nPRN 3\t| %s", str[5]);
            gsaMsg += String.format("\nPRN 4\t| %s", str[6]);
            gsaMsg += String.format("\nPRN 5\t| %s", str[7]);
            gsaMsg += String.format("\nPRN 6\t| %s", str[8]);
            gsaMsg += String.format("\nPRN 7\t| %s", str[9]);
            gsaMsg += String.format("\nPRN 8\t| %s", str[10]);
            gsaMsg += String.format("\nPRN 9\t| %s", str[11]);
            gsaMsg += String.format("\nPRN 10\t| %s", str[12]);
            gsaMsg += String.format("\nPRN 11\t| %s", str[13]);
            gsaMsg += String.format("\nPRN 12\t| %s", str[14]);
            gsaMsg += String.format("\nPDOP\t| %s degrees", str[15]);
            gsaMsg += String.format("\nHDOP\t| %s degrees", str[16]);

            String [] pieces = str[17].split("[*]");

            gsaMsg += String.format("\nVDOP\t| %s degrees", pieces[0]);

            GPSNetDisplay.updateResultPane(gsaMsg);

        } else {
            return;
        }
    }

    private static void parseGGA (String [] str) {
        
        // Using primary message to check fix status
        if (str[2].equals("")) {
            StateMachine.fix = false;
        } else {
            StateMachine.fix = true;
        }

        // Fill TacFrame Fields As necessary
        tacFrame[0] = str[1];
        tacFrame[1] = str[7];
        tacFrame[2] = str[2];
        tacFrame[3] = str[4];
        tacFrame[4] = str[9];
        tacFrame[6] = str[3];
        tacFrame[7] = str[5];
        
        // Check if the option is enabled and create string
        if(StateMachine.gga){

            // Craft and show parsed message!
            String ggaMsg = "";

            String hrs  = "";
            String mins = "";
            String secs = "";

            String lat_deg  = "";
            String lat_mins = "";

            String long_deg  = "";
            String long_mins = "";

            if ( !str[1].equals("") && !str[2].equals("") && !str[4].equals("")){
                hrs  = str[1].substring(0,2);
                mins = str[1].substring(2,4);
                secs = str[1].substring(4);
    
                lat_deg  = str[2].substring(0,2);
                lat_mins = str[2].substring(2);
    
                long_deg  = str[4].substring(0,3);
                long_mins = str[4].substring(3);
               
            }
            
            String fix_quality = "";

            if (str[6].equals("0")){
                fix_quality = "Invalid";
            } else if (str[6].equals("1")) {
                fix_quality = "GPS";
            } else if (str[6].equals("2")) {
                fix_quality = "Differential GPS";
            } else if (str[6].equals("3")) {
                fix_quality = "N/A";
            } else if (str[6].equals("4")) {
                fix_quality = "RTK Fixed";
            } else if (str[6].equals("5")) {
                fix_quality = "RTK Float";
            } else if (str[6].equals("6")) {
                fix_quality = "INS Dead Reckoning";
            }

            ggaMsg += String.format("\nMessage Type\t| GPS Fix Data");
            ggaMsg += String.format("\nFix Time\t| %s:%s:%s UTC", hrs, mins, secs);
            ggaMsg += String.format("\nLatitude\t| %s degrees %s minutes %s", lat_deg, lat_mins, str[3]);
            ggaMsg += String.format("\nLongitude\t| %s degrees %s minutes %s", long_deg, long_mins, str[5]);
            ggaMsg += String.format("\nQuality Ind.\t| %s", fix_quality);
            ggaMsg += String.format("\nSat.'s Used\t| %s", str[7]);
            ggaMsg += String.format("\nHDOP\t| %s degrees", str[8]);
            ggaMsg += String.format("\nAltitude\t| %s meters (MSL)", str[9]);
            ggaMsg += String.format("\nGeoid Sep.\t| %s meters", str[11]);
            //ggaMsg += String.format("\nData Age\t| %s ", str[13]);


            GPSNetDisplay.updateResultPane(ggaMsg);

        } else {
            return;
        }
    }

    private static void parseRMC (String [] str) {

        // Use to fill tacFrame field as needed
        tacFrame[5] = str[7];

        // Check if the option is enabled and create string
        if(StateMachine.rmc){

            // Craft and show parsed message!
            String rmcMsg = "";

            String hrs  = "";
            String mins = "";
            String secs = "";

            String lat_deg  = "";
            String lat_mins = "";

            String long_deg  = "";
            String long_mins = "";

            if ( !str[1].equals("") && !str[2].equals("") && !str[4].equals("")){
                hrs  = str[1].substring(0,2);
                mins = str[1].substring(2,4);
                secs = str[1].substring(4);
    
                lat_deg  = str[3].substring(0,2);
                lat_mins = str[3].substring(2);
    
                long_deg  = str[5].substring(0,3);
                long_mins = str[5].substring(3);
            }

            String status = "";

            if (str[2].equals("A")) {
                status = "Active";
            } else {
                status = "Void";
            }
            
            rmcMsg += String.format("\nMessage Type\t| Recommended Minimun Data");
            rmcMsg += String.format("\nFix Time\t| %s:%s:%s UTC", hrs, mins, secs);
            rmcMsg += String.format("\nStatus\t| %s", status);
            rmcMsg += String.format("\nLatitude\t| %s degrees %s minutes %s", lat_deg, lat_mins, str[4]);
            rmcMsg += String.format("\nLongitude\t| %s degrees %s minutes %s", long_deg, long_mins, str[6]);
            rmcMsg += String.format("\nGroundspeed\t| %s knots", str[7]);
            rmcMsg += String.format("\nTrack Angle\t| %s degrees", str[8]);
            rmcMsg += String.format("\nDate\t| %s", str[9]);
            rmcMsg += String.format("\nMag. Variation\t| %s degrees", str[10]);

            GPSNetDisplay.updateResultPane(rmcMsg);

        } else {
            return;
        }
    }

    private static void parseTAC () {

        String tacMsg = "";

        String hrs  = tacFrame[0].substring(0,2);
        String mins = tacFrame[0].substring(2,4);
        String secs = tacFrame[0].substring(4);

        String lat_deg  = tacFrame[2].substring(0,2);
        String lat_mins = tacFrame[2].substring(2);

        String long_deg  = tacFrame[3].substring(0,3);
        String long_mins = tacFrame[3].substring(3);


        tacMsg += String.format("\nMessage Type\t| Tactical Frame");
        tacMsg += String.format("\nTime\t| %s:%s:%s UTC", hrs, mins, secs);
        tacMsg += String.format("\nFixes\t| %s", tacFrame[1]);
        tacMsg += String.format("\nLatitude\t| %s deg. %s min. %s", lat_deg, lat_mins, tacFrame[6]);
        tacMsg += String.format("\nLongitude\t| %s deg. %s min. %s", long_deg, long_mins, tacFrame[7]);
        tacMsg += String.format("\nAltitude\t| %s m.", tacFrame[4]);
        tacMsg += String.format("\nGroundspeed\t| %s knots", tacFrame[5]);

        GPSNetDisplay.updateResultPane(tacMsg);

        if (StateMachine.logging) {
            if(writer == null) {

                String timeStamp = new SimpleDateFormat("yy.MM.dd.HH.mm.ss").format(new Date());
                String name = String.format("./%s_log.csv", timeStamp);
                
                try {
                    writer = new BufferedWriter(new FileWriter(name, true));
                    writer.append("Time,Fixes,Latitude,Longitude,Altitude,Groundspeed\n");
                } catch (IOException ex) { GPSNetDisplay.logCheckBox.setSelected(false); return; }

                
            }    

            // Latitude & Long to Degrees
            double lat = Double.parseDouble(lat_deg) + Double.parseDouble(lat_mins)/60;
            double longit = Double.parseDouble(long_deg) + Double.parseDouble(long_mins)/60;

            if (tacFrame[6].equals("S")) { lat = -1*lat; }
            if (tacFrame[7].equals("W")) { longit = -1*longit; }

            String logmsg = String.format("%s:%s:%s,%s,%.8f,%.8f,%s,%s\n", hrs, mins, secs, tacFrame[1], lat, longit, tacFrame[4],tacFrame[5]);

            try {
                writer.append(logmsg);
            } catch (IOException ex) { }
                
                
        } else {
            if (writer != null) {
                
                try {
                    writer.close();
                } catch (IOException e) {}
                
                writer = null;
            }
        }
            
    }
}