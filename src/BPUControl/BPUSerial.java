package BPUControl;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
	
	private final String API_VERSION = "2.0.1";
	private final String LINEBREAK = "\n";
	
	
	public enum Message {
		API_VERSION("API version: "), 
		BPU_VERSION("BPU version: "),
		AC_STATE("AC state: "), // AC has been switched on
		HV_ENABLED("HV enabled: "), // High voltage switched on
		PINSTATE("pin state: "), // high voltage pinout setting in binary
		HV_REPORTED("LOG "), // measured output voltage: only available when log is enabled
		VIN_REPORTED("LOG "), // measured input voltage: only available when log is enabled
		DIGIPOTSTATE("pot state: "); // state of the pot that sets the voltage
		private String message;
		private Message(String message) {
			this.message = message;
		}
		public String toString() {
			return this.message;
		}
	}
    
	private boolean logCommands = false;
    private boolean logOutput = false;
    private boolean logStore = false; // log whenever a logged value is stored as a variable
    
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
    
    private Map<Message, String> BPUState = new EnumMap<Message, String>(Message.class);
    
    private void interpretOutput(String output) {
    	for(Message M : Message.values()) {
    		if(output.startsWith(M.message)) {
    			String value;
    			if(M == Message.HV_REPORTED) {
    				value = output.substring(output.indexOf(";")).trim();
    			}
    			else if(M == Message.VIN_REPORTED) {
    				value = output.substring(M.message.length(), output.indexOf(";"));
    			} 
    			else value = output.substring(M.message.length()).trim();
    			BPUState.put(M, value);
    			if(logStore) System.out.println("Storing variable: " + M.name() + ": " + value);
    		}
    	}
    }
    
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
	/*
	 * WriteLine: write a line through the serial to the BPU
	 */
	public void WriteLine(String input) throws SerialComException {
		WriteLine(input, logCommands);
	}
	
    public void WriteLine(String input, boolean log) throws SerialComException {
    	if(log) {
    		System.out.println("Sending serial command: " + input);
    	}
        scm.writeString(comPortHandle, input + LINEBREAK, 0);
    }
    /*
     * check recorded version of BPU output against local constant VERSION
     * 
     * if no such record exists, return null
     */
    public Boolean checkVersion() {
    	if(getState(Message.API_VERSION) == null) return null;
    	return getState(Message.API_VERSION).equals(this.API_VERSION);
    }
    /*
     * retrieve recorded state from BPU, indexed by the enum Message;
     * 
     * if no such state message has been recorded, return null
     */
    public String getState(Message M) {
    	return this.BPUState.get(M);
    }
    public String LastOutput() {
    	String[] outputList = output.split(LINEBREAK);
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
            String[] outputLines = output.split(LINEBREAK);
        	while(outputCounter < outputLines.length - 1) {
        		interpretOutput(outputLines[outputCounter]);
        		if (logOutput) {
            		System.out.print(outputCounter + ": " + outputLines[outputCounter] + "\n");
            	}
        		outputCounter++;
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
//        	bpu.SetAC(false);
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
            if(!bpu.checkVersion()) {
            	System.out.println("Wrong firmware version detected: " + bpu.getState(Message.API_VERSION));
            }
            for(Message M : Message.values()) {
            	System.out.println(">>>"+M + ": " + bpu.getState(M) + "<<<");
            }
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
