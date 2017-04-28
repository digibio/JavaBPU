# JavaBPU

class: 
`src/BPUControl/BPUSerial.java`

library to control the BPU through serial channel

## TODO / in progress:

* create methods to store and return device status; right now everything is stored in a single output string
* implement an interface to use data listener callback
* add API description and BPU firmware version
* add unit tests to test class

## operation:

an example is in main() method; starting the project will attempt to connect to a bpu and send some commands to it.

Typically, a thread is started to take care of the output of the bpu in the background by writing:

```
bpu = new BPUSerial();
bpu.openComPort(port);
Thread bpuThread = new Thread(bpu);
bpuThread.start();
```

the public string `bpu.output` gathers all output from the device. (In next revision this output will be interpreted and data stored in publicly accessible variables).

use `setOutputLogging` to log BPU output to the console

use `setCommandLogging` to log commands you send to the BPU

