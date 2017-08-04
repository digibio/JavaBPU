package bio.digi.bpucontrol;
import com.serialpundit.core.SerialComException;

public interface BPUControl {

	void stopRunning();

	/*
	 * enable this to print output of BPU to the console (default false)
	 */
	void toggleOutputLogging(boolean enable);

	/* 
	 * enable this to print commands sent to the BPU to the console (default false)
	 */
	void toggleCommandLogging(boolean enable);

	/* 
	 * enable this when you have a top layer on the biochip (default true)
	 */
	void toggleTopElectrode(boolean enabled) throws SerialComException;

	/*
	 * set the target voltage for the high voltage generator
	 */
	void setTargetVoltage(int voltage) throws SerialComException;

	/*
	 * switch HV on and off in the BPU
	 */
	void toggleHighVoltage(Boolean enable) throws SerialComException;
	
	/*
	 * switch status reports of BPU of the meaured voltage
	 */
	void toggleVoltageLog(Boolean enable) throws SerialComException;

	/*
	 * switch AC on and off
	 */
	void toggleAC(Boolean enable) throws SerialComException;

	/*
	 * select which channels of the device get switched on and off using byte array
	 */
	void updateChannels(byte[] state) throws SerialComException;

	/*
	 * select which channels of the device get switched on and off using a 	hexadecimal number
	 */
	void updateChannels(String hex) throws SerialComException;

	/*
	 * write a string to the BPU
	 */
	void sendLine(String input) throws SerialComException;

	/*
	 * check recorded version of BPU output against local constant VERSION
	 * 
	 * if no such record exists, return null
	 */
	Boolean checkVersion();

	/*
	 * retrieve recorded state from BPU, indexed by the enum Message;
	 * 
	 * if no such state message has been recorded, return null
	 */
	String getState(APIMessage M);

	void run();

}