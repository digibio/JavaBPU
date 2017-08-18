package bio.digi.bpucontrol;
enum BPUMessage {
	API_VERSION("API version: "), 
	BPU_VERSION("BPU version: "),
	AC_STATE("AC state: "), // AC has been switched on
	HV_ENABLED("HV enabled: "), // High voltage switched on
	PINSTATE("pin state: "), // high voltage pinout setting in binary
	HV_REPORTED("LOG "), // measured output voltage: only available when log is enabled
	VIN_REPORTED("LOG "), // measured input voltage: only available when log is enabled
	VOLTAGECONTROLSTATE("pot state: "); // state of the pot that sets the voltage
	String message;
	BPUMessage(String message) {
		this.message = message;
	}
	public String toString() {
		return this.message;
	}
}
