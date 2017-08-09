package bio.digi.bpucontrol;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import com.serialpundit.core.SerialComException;

public class WebConnector {
	String host = "";
	String email = "";
	String password = "";
	String device = "";
	String serialPort = null;
	SocketAPI socket = null;
	BPUControl bpu = new BPUControlSerial();

	private void connect() {
		if (!host.endsWith("/")) {
			host = host + "/";
		}
		if (!host.startsWith("http")) {
			host = "http://" + host;
		}
		try {
			socket = new SocketAPI(host, email, password, device, bpu);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	static void listports() {
		String[] ports;
		try {
			ports = BPUControlSerial.listAvailablePorts();
			System.out.println("listing usb ports with available hardware:");
			if (ports.length == 0)
				System.out.println("no available hardware");
			else
				for (String port : ports) {
					System.out.println(port);
				}
		} catch (IOException e) {
			System.err.println("an error occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Webconnector app");
		String[] serialPorts = null;
		try {
			serialPorts = BPUControlSerial.listAvailablePorts();
		} catch (IOException e) {
			System.err.println("error trying to find ports");
		}
		if (args[0].equals("--list-ports")) {
			listports();
		} else {
			WebConnector wc = new WebConnector();
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--host") && i < args.length) {
					wc.host = args[i + 1];
				}
				if (args[i].equals("--email") && i < args.length) {
					wc.email = args[i + 1];
				}
				if (args[i].equals("--password") && i < args.length) {
					wc.password = args[i + 1];
				}
				if (args[i].equals("--device") && i < args.length) {
					wc.device = args[i + 1];
				}
				if (args[i].equals("--serialport") && i < args.length) {
					wc.serialPort = args[i + 1];
				}
			}
			// the following part is to find, select and connect to an available usb port.
			if (wc.serialPort != null) {
				if (!Arrays.asList(serialPorts).contains(wc.serialPort)) {
					System.out.println("No device at requested port " + wc.serialPort + "available");
					wc.serialPort = null;
				}
			} else {
				if (serialPorts != null && serialPorts.length == 0) {
					System.out.println("No connected serial devices available");
				} else {
					System.out.println("choosing port " + serialPorts[0] + " as device port");
					wc.serialPort = serialPorts[0];
				}
			}
			if (wc.serialPort != null) {
				try {
					((BPUControlSerial) wc.bpu).openComPort(wc.serialPort);
				} catch (SerialComException e) {
					System.err.println("Error opening com port: " + e.getMessage());
				}
			}

			if (wc.host.length() > 0) {
				wc.connect();
			}
		}
	}
}
