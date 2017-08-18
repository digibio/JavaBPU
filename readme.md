# JavaBPU

Collection of libraries and tools to use with the bpu device

## WebConnector

Software to open a connection to the web app. To build: 

- install gradle
- run `gradle build`

to run, there are a number of parameters. 

- `--serialport`: choose a port on the local machine. If omitted, the first one found is used. If `virtual` is used, a virtual device is used.
- `--host`: url of the web host that runs the server connecting the software. This includes the port.
- `--email`: email address of the user that has been registered with the web app
- `--password`: password of the user registered with the web app  
- `--device`: name of the device. This can be arbitrarily chosen.

 

## BPUSerial library:

Serial API version 2.1.0

class: 
`src/BPUControl/BPUSerial.java`

library to control the BPU through serial channel

an example is in main() method; starting the project will attempt to connect to a bpu and send some commands to it.

Typically, a thread is started to take care of the output of the bpu in the background by writing:

```
bpu = new BPUSerial();
bpu.openComPort(port);
Thread bpuThread = new Thread(bpu);
bpuThread.start();
```

the public string `bpu.output` gathers all output from the device. 

using the method `getState` a certain state can be retreived from the BPU; like the value of the digipot:

```
String voltage = bpu.getState(Message.VOLTAGECONTROLSTATE);
```

License: MIT
