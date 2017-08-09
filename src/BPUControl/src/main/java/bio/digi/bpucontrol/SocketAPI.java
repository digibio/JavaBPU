package bio.digi.bpucontrol;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.serialpundit.core.SerialComException;

import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.emitter.Emitter.Listener;

public class SocketAPI {
	private BPUControl bpu;
	private String url; 
	private String userEmail; 
	private String userPassword;
	private String deviceName;
	private Socket socket;
	
	private final Map<APIRequests, Listener> bindings = new HashMap<APIRequests, Listener>(); 
	private void setBindings() {
		bindings.put(APIRequests.SENDLINE, sendLine());
		bindings.put(APIRequests.SETCHANNELS, setChannels());
		bindings.put(APIRequests.SWITCHAC, switchAC());
	}
	
	public SocketAPI(
			String url, 
			String userEmail, 
			String userPassword, 
			String deviceName, 
			BPUControl bpu) throws URISyntaxException {
		System.out.println("connecting to " + url);
		this.bpu = bpu;
		this.url = url;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
		this.deviceName = deviceName;
		this.userEmail = userEmail;
        this.deviceName = deviceName;
        setBindings();
        Options options = new IO.Options();
        // no options
        socket = IO.socket(this.url + "bpu", options);
        
        socket.on(Socket.EVENT_CONNECT_ERROR, socketError("Socket connection error"));
        socket.on(Socket.EVENT_ERROR, socketError("Socket error"));
        socket.on(Socket.EVENT_DISCONNECT, socketDisconnect(0)); 
        socket.on(Socket.EVENT_MESSAGE, (args) -> System.out.println("Server message: " + StringUtils.join(Arrays.asList(args), ", ")));
        socket.on(Socket.EVENT_CONNECT, socketConnect());
        socket.connect();
	}

	private Listener socketConnect() {
		return new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				System.out.println("Socket connected; authenticating...");
				socket.once("authenticated", new Emitter.Listener() {
					@Override
					public void call(Object... args) {
						System.out.println("Socket authentication successful");
						bindApiCalls();
					}
				});
				JSONObject authCredentials = new JSONObject();
				authCredentials
					.put("useremail", userEmail)
					.put("userpassword", userPassword)
					.put("devicename", deviceName);
				socket.emit("authenticate", authCredentials);
			}
		};
	}
	private Listener socketDisconnect(int status) {
		return (args) -> {
			System.out.println("Socket disconnected; going to reconnect");
		};
	}
	private Listener socketError(String status) {
		return (args) -> {
			System.out.println(status + " occurred: " + StringUtils.join(Arrays.asList(args), ", "));
		};
	}
	private Listener sendLine() {
		return (args) -> {
			JSONObject obj = (JSONObject)args[0];
			try {
				System.out.print("SocketAPI received sendline");
				String line = obj.getString("line");
				System.out.println(": " + line);
				bpu.sendLine(line);
			} catch (SerialComException e) {
				emit(APIRequests.DEVICEERROR, e.getMessage());
			} catch (JSONException e) {
				emit(APIRequests.JSONBODYERROR, e.getMessage());
			}
		};
	}
	private Listener setChannels() {
		return (args) -> {
			JSONObject obj = (JSONObject)args[0];
			try {
				String hexstate = obj.getString("hexstate");
				System.out.println("SocketAPI received channel update:" + hexstate);
				bpu.updateChannels(hexstate);
			} catch (SerialComException e) {
				emit(APIRequests.DEVICEERROR, e.getMessage());
			} catch (JSONException e) {
				emit(APIRequests.JSONBODYERROR, e.getMessage());
			}
		};
	}
	private Listener switchAC() {
		return (args) -> {
			JSONObject obj = (JSONObject)args[0];
			try {
				Boolean state = obj.getBoolean("state");
				System.out.println("SocketAPI received AC state:" + state);
				bpu.switchAC(state);
			} catch (SerialComException e) {
				emit(APIRequests.DEVICEERROR, e.getMessage());
			} catch (JSONException e) {
				emit(APIRequests.JSONBODYERROR, e.getMessage());
			}
		};
	}
	private void emit(APIRequests message, Object body) {
		socket.emit(message.toString(), body);
	}
	private void bindApiCalls() {
		System.out.println("Binding api calls");
		for(Map.Entry<APIRequests, Listener> binding : bindings.entrySet()) {
			socket.on(binding.getKey().key, binding.getValue());
		}
	}
}
