# JavaBPU

class: 
`src/BPUControl/BPUSerial.java`

library to control the BPU through serial channel

## TODO / in progress:

* implement an interface to use data listener callback
* add API description and BPU firmware version
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
String digipotstate = bpu.getState(Message.DIGIPOTSTATE);
```



use `setOutputLogging` to log BPU output to the console

use `setCommandLogging` to log commands you send to the BPU

