package bio.digi.bpucontrol;

public enum APIRequests {
	// incoming messages
	SENDLINE("user.request.serial.sendLine"),
    SWITCHHV("user.request.bpu.switchHV"),
	SWITCHAC("user.request.bpu.switchAC"),
	SETVOLTAGE("user.request.bpu.setVoltageControl"),
	SETCHANNELS("user.request.bpu.setChannelState"),
	
	// messages to send:
    BPUOUTPUT("device.message.bpu.output"),
    STATECHANGE("device.message.bpu.state"),
	// error messages to send:
	DEVICEERROR("device.error"),
	JSONBODYERROR("json.error");
	
 	String key;
	APIRequests(String key) {
		this.key = key;
	}
	public String toString() {
	    return this.key;
	}
}
