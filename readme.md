# JavaBPU

Serial API version 2.0.1

class: 
`src/BPUControl/BPUSerial.java`

library to control the BPU through serial channel

binary included for arduino nano

## TODO / in progress:

* implement an interface to use data listener callback
* add unit tests to test class
* implement an enum to standardize the commands to send through the serial

## operation:

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
