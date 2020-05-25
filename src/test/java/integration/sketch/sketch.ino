//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
//
// Sketch to use with the IntegrationTest.java file
// It implements a series of Java to Arduino calls using all the available signatures.
// It implements also, a series of Arduino to Java calls.
// Any test phases are managed from the Java program by the way of "Start", "Switch" and "Stop" calls.
//
#include <ArduinoSerialRpc.h>

boolean testRunning = false;
boolean sendingTestActive = false;
int sendIndex = 0;

ArduinoSerialRpc rpc("Full Tutorial Sketch (www.mauxilium.it)");

void setup() {
  Serial.begin(9600);

  rpc.registerArduinoFunction("Start", testStart);
  rpc.registerArduinoFunction("Switch", testSwitch);
  rpc.registerArduinoFunction("Stop", testStop);

  rpc.registerArduinoFunction("FloatCallPcToArduino", floatCall);
  rpc.registerArduinoFunction("StringCallPcToArduino", stringCall);
  rpc.registerArduinoFunction("IntCallPcToArduino", intCall);
}

void serialEvent() {
  rpc.serialEventHandler();
}


void testStart() {
  testRunning = true;
}

void testSwitch() {
  sendingTestActive = true;
}

void testStop() {
  testRunning = false;
  sendingTestActive = false;
  sendIndex = 0;
}


float floatCall(float arg) {
  return 3.1*arg;
}

String stringCall(String arg) {
  return arg + arg + arg;
}

int intCall(int arg1, int arg2) {
  return arg1*arg2;
}


void loop() {
  delay(10);
  if (sendingTestActive) {
    if (++sendIndex < 4) {
      rpc.executeRemoteMethod("stringCallArduinoToPc", String(13*sendIndex));
      rpc.executeRemoteMethod("floatCallArduinoToPc", 18.11*sendIndex);
      rpc.executeRemoteMethod("intCallArduinoToPc", sendIndex, 27*sendIndex);
    } else {
      rpc.executeRemoteMethod("arduinoEnds");
      sendingTestActive = false;
    }
  }
}
