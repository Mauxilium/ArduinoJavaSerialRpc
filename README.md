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
 * download the Arduino library from: xyz
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
        <artifactId>ArduinoJavaRpc</artifactId>
        <version>${arduinorpc.version}</version>
    </dependency>
</dependencies>
```

## Using it in Arduino Sketch
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

## Using in Java Class
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
In order to execute the integration example test, please follows this steps:
* Open the sketch ArduinoJavaSerialRpc\src\test\java\integration\sketch\sketch.ino
* Download it into your Arduino Card
* Execute the following commands (modifying the path as your system requires)
```bash
# mvn clean package
# java -Djava.library.path=...your path...\ArduinoJavaSerialRpc\RxTx\mfz-rxtx-2.2-20081207-win-x64 integrationTest.jar
```

### Next steps
* ArduinoJavaSerialRpc tutorial - A tutorial to discover a more complex use of library
* ArduinoSpring - The Spring library developed on top of ArduinoJavaSeriaRpc
* www.mauxilium.it - The reference site for my other projects
