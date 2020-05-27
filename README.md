# ArduinoJavaSerialRpc

ArduinoJavaSerialRpc is the Java side of a serial communication library with Arduino Card.

The Arduino part of this communication is implemented into ArduinoSerialRpc repository.

The communication model is implemented in form of:
* Remote Method Invocation. Where Arduino calls a method declared into a Java class.
* Remote Procedure Call. Where a Java process call a function defined into the Arduino sketch.

The communication is a point to point model, performed through the serial (USB) port.
 
## Architecture

Tbdf

## Features

* Bidirectional communication
* Low Arduino resources required
* Easy to learn interface
* Flexible naming convention

# Getting Started

## Arduino Side Installation
It is required to:
 * download the Arduino library: [ArduinoSerialRpc](https://github.com/Mauxilium/ArduinoSerialRpc)
 * expand it into your "library" path of Arduino Ide,
  or follows the manual installation section of https://www.arduino.cc/en/guide/libraries
  
## Java Side Installation
For Maven projects, it is only required to include the following dependency in you pom
```xml
<properties>
    <arduinorpc.version>1.0.0</arduinorpc.version>
</properties>
    
<dependencies>
    <dependency>
        <groupId>it.mauxilium</groupId>
        <artifactId>ArduinoJavaSerialRpc</artifactId>
        <version>${arduinorpc.version}</version>
    </dependency>
</dependencies>
```

## Arduino Sketch basic example
```c++
#include <ArduinoSerialRpc.h>

// Creating the rpc agent you are totally free to assign
// your preferred name to the card
ArduinoSerialRpc rpc("MyTestCard");

void setup() {
  Serial.begin(9600); // or any other supported value

  // Java can use "callIt" string to force the execution of myMethod function
  rpc.registerArduinoAction("callIt", myMethod);
}

void myMethod() {
    // This method is called when the Java program
    // executes an executeRemoteMethod("callIt"); 
}

void loop() {
    // Here Arduino calls a method of the external Java program.
    // No registration is required
    rpc.executeRemoteAction("pingFromArduino");
}
```

## Java Class basic example
A simplified version of Java test program could be:
```java
class ArduinoRpc extends ArduinoJavaSerialRpc {
    public ArduinoRpc() {
        super("COM1", 9600);
    }

    public void pingFromArduino() {
        System.out.println("Arduino calls it now");
    }
}

class ArduinoTest {
    public static void main(final String[] args) {
        ArduinoRpc rpc = new ArduinoRpc();
        rpc.connect();
        rpc.executeRemoteAction("callIt");
        rpc.disconnect();
    }
} 
```
You can find a real complete use case in the source path:
* ArduinoJavaSerialRpc\src\test\java\integration\sketch
* ArduinoJavaSerialRpc\src\test\java\integration\java

### Build and run 
Java and Arduino communication is performed by the way of RxTx external library:
```
(from his readme)
RXTX binary builds provided by Mfizz Inc. (http://mfizz.com/).
Please see http://mfizz.com/oss/rxtx-for-java for more info.
```
A copy of 64Bit RxTx library is added to this git repository. 

In order to execute the integration example test, please follows this steps:
* Open the sketch ArduinoJavaSerialRpc\src\test\java\integration\sketch\sketch.ino
* Download it into your Arduino Card
* Execute the following commands
```bash
mvn clean test
java -Djava.library.path=RxTx\mfz-rxtx-2.2-20081207-win-x64 -cp target/test-classes;target/classes;RxTx/mfz-rxtx-2.2-20081207-win-x64/RXTXcomm.jar integration.java.IntegrationTest COM5 9600
```

### Next steps
* [ArduinoJavaSerialRpcTutorial](https://github.com/Mauxilium/ArduinoJavaSerialRpcTutorial) - A tutorial to discover a more complex use of library
* [ArduinoSpring](https://github.com/Mauxilium/ArduinoSpring) - A Spring library developed on top of ArduinoJavaSeriaRpc
* [www.mauxilium.it](http://www.mauxilium.it) - The reference site for my open source projects

