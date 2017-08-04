package bio.digi.bpucontrol;
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
 * version 0.1.0
 * 
 * license: MIT
 *
 */

public final class BPUControlSerial implements Runnable, BPUControl {
	
	private final String API_VERSION = "2.0.1";
	private final String LINEBREAK = "\n";
	
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
    
    private Map<APIMessage, String> BPUState = new EnumMap<APIMessage, String>(APIMessage.class);
    
    private void interpretOutput(String output) {
    	for(APIMessage M : APIMessage.values()) {
    		if(output.startsWith(M.message)) {
    			String value;
    			if(M == APIMessage.HV_REPORTED) {
    				value = output.substring(output.indexOf(";")).trim();
    			}
    			else if(M == APIMessage.VIN_REPORTED) {
    				value = output.substring(M.message.length(), output.lastIndexOf(" "));
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
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#stopRunning()
	 */
    @Override
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
     * 
     * previously: setOutputLogging
     */
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#toggleOutputLogging(boolean)
	 */
    @Override
	public void toggleOutputLogging(boolean enable) {
    	this.logOutput = enable;
    }
    /* 
     * enable this to print commands sent to the BPU to the console (default false)
     * 
     * previously: setCommandLogging
     */
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#toggleCommandLogging(boolean)
	 */
    @Override
	public void toggleCommandLogging(boolean enable) {
    	this.logCommands = enable;
    }
    /* 
     * enable this when you have a top layer on the biochip (default true)
     * 
     * previously: setTopElectrode
     */
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#toggleTopElectrode(boolean)
	 */
    @Override
	public void toggleTopElectrode(boolean enabled) throws SerialComException {
    	this.topElectrode = enabled;
    	if(ChannelState.length > 0) {
    		this.updateChannels(ChannelState);
    	}
    }
    
    /*
     * send signal to set the voltage; use number between 0 and 127
     * 
     * previously: SetDigiPotState
     */
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#setVoltageControl(int)
	 */
    @Override
	public void setTargetVoltage(int voltage) throws SerialComException {
    	sendLine("hv vol " + voltage);
	}
    /*
     * switch HV on and off in the BPU
     * 
     * previously: EnableHighVoltage
     */
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#toggleHighVoltage(java.lang.Boolean)
	 */
    @Override
	public void toggleHighVoltage(Boolean enable) throws SerialComException {
		sendLine("hv gen " + (enable ? "1" : "0"));
	}
	
	
    /*
     * switch status reports of BPU of the meaured voltage
     * 
     * previously: EnableLog
     */
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#toggleVoltageLog(java.lang.Boolean)
	 */
    @Override
	public void toggleVoltageLog(Boolean enable) throws SerialComException {
		sendLine("log " + (enable ? "1" : "0"));
	}
    /*
     * switch AC on and off
     * 
     * previously called: SetAC
     */
	/* (non-Javadoc)
	 * @see BPUControl.BPUControl#toggleAC(java.lang.Boolean)
	 */
	@Override
	public void toggleAC(Boolean enable) throws SerialComException {
		sendLine("polac " + (enable ? "1" : "0"));
	}
	/*
	 * select which channels of the device get switched on and off using byte array
	 * 
	 * previously called: UpdateHV
	 */
	/* (non-Javadoc)
	 * @see BPUControl.BPUControl#updateChannels(byte[])
	 */
	@Override
	public void updateChannels(byte[] state) throws SerialComException
	{
//		byte[] values = Arrays.copyOf(state, 16);
		updateChannels(HelperMethods.bytesToHex(state));
	}
	/*
	 * select which channels of the device get switched on and off using a 	hexadecimal number
	 */
	/* (non-Javadoc)
	 * @see BPUControl.BPUControl#updateChannels(java.lang.String)
	 */
	@Override
	public void updateChannels(String hex) throws SerialComException {
		String cmd = "hvset " + hex;
		sendLine(cmd);
	}
	/*
	 * write a line through the serial to the BPU
	 * 
	 * previously called: WriteLine
	 */
	/* (non-Javadoc)
	 * @see BPUControl.BPUControl#sendLine(java.lang.String)
	 */
	@Override
	public void sendLine(String input) throws SerialComException {
		if(logCommands) {
    		System.out.println("Sending serial command: " + input);
    	}
		sendLine(input);
	}
    /*
     * check recorded version of BPU output against local constant VERSION
     * 
     * if no such record exists, return null
     */
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#checkVersion()
	 */
    @Override
	public Boolean checkVersion() {
    	if(getState(Message.API_VERSION) == null) return null;
    	return getState(Message.API_VERSION).equals(this.API_VERSION);
    }
    /*
     * retrieve recorded state from BPU, indexed by the enum Message;
     * 
     * if no such state message has been recorded, return null
     */
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#getState(BPUControl.BPUSerial.Message)
	 */
    @Override
	public String getState(Message M) {
    	return this.BPUState.get(M);
    }
    /* previously called LastOutput */
    public String lastOutput() {
    	String[] outputList = output.split(LINEBREAK);
    	try {
    		return outputList[outputList.length - 1];
    	} catch (Exception e) {
    		return "no output";
    	}
    }
    
    /* (non-Javadoc)
	 * @see BPUControl.BPUControl#run()
	 */
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

    /* 
     * list ports with usb devices connected
     * 
     * previously called: ListAvailablePorts
     */
    public static String[] listAvailablePorts() throws IOException {
        SerialComManager scm = new SerialComManager();
        return scm.listAvailableComPorts();
	}
    /*
     * for testing purposes only
     */
    public static void main(String[] args) {
    	BPUSerial bpu = null;
    	try {
			String[] ports = listAvailablePorts();
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
        	bpu.toggleAC(true);
        	
        	bpu.toggleOutputLogging(true);
			bpu.toggleHighVoltage(true);
			bpu.setTargetVoltage(111);
        	bpu.updateChannels(new byte[] {121,(byte)232});
        	bpu.updateChannels(new byte[] {1,3,7,15});
//        	bpu.SetAC(false);
        	bpu.updateChannels(new byte[] {121, 43, 45, 56});
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
