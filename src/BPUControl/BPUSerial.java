package BPUControl;

import java.io.IOException;
import java.util.List;
import java.lang.Math;
import java.util.concurrent.atomic.AtomicBoolean;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.util.SerialComUtil;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;
/**
 * @author Frido Emans
 *
 */
public final class BPUSerial implements Runnable {
	private byte[] dataRead;
    private String dataStr;

    private final SerialComManager scm;
    private String output = "";
    private long comPortHandle;
    private String status;
    private final SignalExit exitTrigger;
    DATABITS databits = DATABITS.DB_8;
	STOPBITS stopbits = STOPBITS.SB_1;
    PARITY parity = PARITY.P_NONE;
    BAUDRATE baudrate = BAUDRATE.B9600;
    
    public BPUSerial(SignalExit exitTrigger) throws IOException {
    	this.scm = new SerialComManager();
        this.exitTrigger = exitTrigger;
    }

    public void openComPort(String portName) throws SerialComException {
    	comPortHandle = scm.openComPort(portName, true, true, true);
        scm.configureComPortData(comPortHandle, databits, stopbits, parity, baudrate, 0);
        this.run();
    }
    
    public void closeComPort() throws SerialComException {
    	scm.closeComPort(comPortHandle);
    }
    public void SetDigipotState(int val) throws SerialComException {
    	WriteLine("pot " + Math.max(0, 127 - val));
	}
    public void EnableHighVoltage(Boolean enable) throws SerialComException {
		WriteLine("hvgen " + (enable ? "1" : "0"));
	}
    public void EnableLog(Boolean enable) throws SerialComException {
		WriteLine("log " + (enable ? "1" : "0"));
	}
	public void SetAC(Boolean enable) throws SerialComException {
		WriteLine("polac " + (enable ? "1" : "0"));
	}
	public void UpdateHV(byte[] state, Boolean topElectrode) throws SerialComException
	{
		byte[] values = state.clone();

		// Ground plane bit
		if (topElectrode)
			values[7] |= 128;

		String cmd = "hvset " + HelperMethods.bytesToHex(values);
		WriteLine(cmd);
	}

    public void WriteLine(String input) throws SerialComException {
        scm.writeString(comPortHandle, input + "\n", 0);
    }
    @Override
    public void run() {
        while(exitTrigger.isExitTriggered() == false) {
        	String updateStatus = "";
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                if(exitTrigger.isExitTriggered() == true) {
                    return;
                }
            }
            try {
                dataRead = scm.readBytes(comPortHandle, 10);
                if(dataRead != null) {
                    dataStr = new String(dataRead);
                    output += dataStr;
                    System.out.println(dataStr);
                }
            } catch (SerialComException e) {
                updateStatus = e.getExceptionMsg();
                this.exitTrigger.setExitTrigger(true);
        		e.printStackTrace();

            } catch (Exception e) {
                updateStatus = e.getMessage();
                this.exitTrigger.setExitTrigger(true);
        		e.printStackTrace();
            }
            if(!updateStatus.equals(status)) {
            	status = updateStatus;
                System.out.println(status);
            }
        }
    }

    public static String[] ListAvailablePorts() throws IOException {
        SerialComManager scm = new SerialComManager();
        return scm.listAvailableComPorts();
	}
    /*
     * for testing purposes only
     */
    public static void main(String[] args) {
    	try {
			String[] ports = ListAvailablePorts();
			String port = ports[0];
			System.out.println("application started, port = " + port);
			SignalExit signalExit = new SignalExit(false);
            BPUSerial bpu = new BPUSerial(signalExit);
            bpu.openComPort(port);
            System.out.println(bpu.comPortHandle);
            
    	} catch (IOException e) {
            System.out.println(e.getMessage());
    		System.out.print(e.getStackTrace());
		} 
    }
    private static class HelperMethods {
    	/*
	     * from http://stackoverflow.com/a/9855338/2380702
	     */
	    public static String bytesToHex(byte[] bytes) {
		    final char[] hexArray = "0123456789ABCDEF".toCharArray();
	        char[] hexChars = new char[bytes.length * 2];
	        for ( int j = 0; j < bytes.length; j++ ) {
	            int v = bytes[j] & 0xFF;
	            hexChars[j * 2] = hexArray[v >>> 4];
	            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	        }
	        return new String(hexChars);
	    }
    }
}

/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */


final class SignalExit {

    private volatile boolean exit;

    public SignalExit(boolean exit) {
        this.exit = exit;
    }

    public void setExitTrigger(boolean exit) {
        this.exit = exit;
    }

    public boolean isExitTriggered() {
        return exit;
    }
}
