package BPUControl;

import java.io.IOException;
import java.util.Arrays;
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
    private boolean logCommands = false;
    private boolean logOutput = false;
	private volatile boolean stop = false;
    private volatile boolean topElectrode = true;
    private volatile byte[] ChannelState;
	
	private byte[] dataRead;
    private String dataStr;

    private final SerialComManager scm;
    public String output = "";
    private long comPortHandle;
    private String status;
    DATABITS databits = DATABITS.DB_8;
	STOPBITS stopbits = STOPBITS.SB_1;
    PARITY parity = PARITY.P_NONE;
    BAUDRATE baudrate = BAUDRATE.B9600;
    
    public BPUSerial() throws IOException {
    	this.scm = new SerialComManager();
    }
    public void stopRunning() {
    	System.out.println("stopping thread");
    	this.stop = true;
    }
    public void openComPort(String portName) throws SerialComException {
    	comPortHandle = scm.openComPort(portName, true, true, true);
        scm.configureComPortData(comPortHandle, databits, stopbits, parity, baudrate, 0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {}
    }
    
    public void closeComPort() throws SerialComException {
    	scm.closeComPort(comPortHandle);
    }
    /*
     * enable this to print output of BPU to the console (default false)
     */
    public void setOutputLogging(boolean enable) {
    	this.logOutput = enable;
    }
    /* 
     * enable this to print commands sent to the BPU to the console (default false)
     */
    public void setCommandLogging(boolean enable) {
    	this.logCommands = enable;
    }
    /* 
     * enable this when you have a top layer on the biochip (default true)
     */
    public void setTopElectrode(boolean topElectrode) throws SerialComException {
    	this.topElectrode = topElectrode;
    	if(ChannelState.length > 0) {
    		this.UpdateHV(ChannelState);
    	}
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
	public void UpdateHV(byte[] state) throws SerialComException
	{
		byte[] values = Arrays.copyOf(state, 8);

		if (topElectrode)
			values[7] |= 128;

		String cmd = "hvset " + HelperMethods.bytesToHex(values);
		WriteLine(cmd);
	}
	public void WriteLine(String input) throws SerialComException {
		WriteLine(input, logCommands);
	}
    public void WriteLine(String input, boolean log) throws SerialComException {
    	if(log) {
    		System.out.println("Sending serial command: " + input);
    	}
        scm.writeString(comPortHandle, input + "\n", 0);
    }
    
    public String LastOutput() {
    	String[] outputList = output.split("\n");
    	try {
    		return outputList[outputList.length - 1];
    	} catch (Exception e) {
    		return "no output";
    	}
    }
    @Override
    public void run() {
    	String updateStatus = "";
    	int outputCounter = 0;
        while(!stop) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                if(stop) {
                	return;
                }
            }
            try {
                dataRead = scm.readBytes(comPortHandle, 10);
                if(dataRead != null) {
                    dataStr = new String(dataRead);
                    output += dataStr;
                }
            } catch (SerialComException e) {
                updateStatus = e.getExceptionMsg();
                this.stopRunning();
        		e.printStackTrace();

            } 
            if(!updateStatus.equals(status)) {
            	status = updateStatus;
                System.out.println(status);
            }
			if (logOutput) {
	            String[] outputLines = output.split("\n");
            	while(outputCounter < outputLines.length - 1) {
            		System.out.print(outputCounter + ": " + outputLines[outputCounter] + "\n");
            		outputCounter++;
            	}
			}
        }
        System.out.println("finished running");
    }

    public static String[] ListAvailablePorts() throws IOException {
        SerialComManager scm = new SerialComManager();
        return scm.listAvailableComPorts();
	}
    /*
     * for testing purposes only
     */
    public static void main(String[] args) {
    	BPUSerial bpu = null;
    	try {
			String[] ports = ListAvailablePorts();
			String port = ports[0];
			System.out.println("example application started with BPU on port = " + port);
            bpu = new BPUSerial();
            bpu.openComPort(port);
            Thread bpuThread = new Thread(bpu);
            bpuThread.start();
        	try {
        	    Thread.sleep(1000);
        	} catch(InterruptedException ex) {
        	    Thread.currentThread().interrupt();
        	}
        	bpu.SetAC(true);
        	bpu.EnableLog(true);
        	bpu.SetDigipotState(111);
        	bpu.UpdateHV(new byte[] {121,(byte)232});
        	bpu.UpdateHV(new byte[] {1,3,7,15});
        	bpu.SetAC(false);
        	bpu.UpdateHV(new byte[] {121, 43, 45, 56});
        	try {
        	    Thread.sleep(4000);
            	bpu.stopRunning();
            	System.out.print(bpu.output);
        	} catch(InterruptedException ex) {
        	    Thread.currentThread().interrupt();
        	}
        	bpu.stopRunning();
            System.out.println("finished application");
    	} catch (Exception e) {
    		bpu.stopRunning();
            System.out.println(e.getMessage());
            e.printStackTrace();
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
