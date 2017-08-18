/**
 * 
 */
package bio.digi.bpucontrol;

import com.serialpundit.core.SerialComException;

/**
 * @author xor
 *
 */
public class BPUControlVirtualDevice implements Runnable, BPUControl {
	private volatile boolean stop = false;
	BPUCallbacks callbacks;
    public BPUControlVirtualDevice(BPUCallbacks callbacks) {
    	this.callbacks = callbacks;
    }

	/**
	 * 
	 */
	public BPUControlVirtualDevice() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#stopRunning()
	 */
	@Override
	public void stopRunning() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#switchOutputLogging(boolean)
	 */
	@Override
	public void switchOutputLogging(boolean enable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#toggleOutputLogging(boolean)
	 */
	@Override
	public void toggleOutputLogging(boolean enable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#switchCommandLogging(boolean)
	 */
	@Override
	public void switchCommandLogging(boolean enable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#toggleCommandLogging(boolean)
	 */
	@Override
	public void toggleCommandLogging(boolean enable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#switchTopElectrode(boolean)
	 */
	@Override
	public void switchTopElectrode(boolean enabled) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#toggleTopElectrode(boolean)
	 */
	@Override
	public void toggleTopElectrode(boolean enabled) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#setTargetVoltage(int)
	 */
	@Override
	public void setTargetVoltage(int voltage) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#switchHighVoltage(java.lang.Boolean)
	 */
	@Override
	public void switchHighVoltage(Boolean enable) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#toggleHighVoltage(java.lang.Boolean)
	 */
	@Override
	public void toggleHighVoltage(Boolean enable) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#switchVoltageLog(java.lang.Boolean)
	 */
	@Override
	public void switchVoltageLog(Boolean enable) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#toggleVoltageLog(java.lang.Boolean)
	 */
	@Override
	public void toggleVoltageLog(Boolean enable) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#switchAC(java.lang.Boolean)
	 */
	@Override
	public void switchAC(Boolean enable) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#toggleAC(java.lang.Boolean)
	 */
	@Override
	public void toggleAC(Boolean enable) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#updateChannels(byte[])
	 */
	@Override
	public void updateChannels(byte[] state) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#updateChannels(java.lang.String)
	 */
	@Override
	public void updateChannels(String hex) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#sendLine(java.lang.String)
	 */
	@Override
	public void sendLine(String input) throws SerialComException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#checkVersion()
	 */
	@Override
	public Boolean checkVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#getState(bio.digi.bpucontrol.BPUMessage)
	 */
	@Override
	public String getState(BPUMessage M) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see bio.digi.bpucontrol.BPUControl#run()
	 */
	@Override
	public void run() {
        while(!stop) {
            try {
                Thread.sleep(1000);
                this.callbacks.outputReceived("Virtual device received output");
            } catch (InterruptedException e1) {
                if(stop) {
                	return;
                }
            }
        }
        System.out.println("finished running");

	}

}
