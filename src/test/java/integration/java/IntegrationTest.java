//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
//
// Integration test class used with the sketch/sketch.ino file
// It implements a series of Java to Arduino calls using all the available signatures.
// It implements also, a series of Arduino to Java calls.
// Any test phases are managed from here by the way of "Start", "Switch" and "Stop" calls.
//
package integration.java;

import it.mauxilium.arduinojavaserialrpc.ArduinoJavaSerialRpc;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcActionFailsException;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcInitializationError;

import java.io.IOException;

public class IntegrationTest {

    // String assigned to ArduinoRpc instance inside the sketch
    private final static String EXPECTED_SKETCH = "Full Tutorial Sketch (www.mauxilium.it)";

    // Values registered as placeholder during sketch setup (registerArduinoAction)
    private final static String FLOAT_CALL_PC_TO_ARDUINO = "FloatCallPcToArduino";
    private final static String STRING_CALL_PC_TO_ARDUINO = "StringCallPcToArduino";
    private final static String INTEGER_CALL_PC_TO_ARDUINO = "IntCallPcToArduino";

    private ArduinoRpc arduino;
    private String receivingReport = "";
    private int receivingCounter = 0;
    private final Object waitResultLock = new Object();

    public static void main(final String[] args) {
        if (args.length == 2) {
            try {
                new IntegrationTest().doIt(args[0], args[1]);
            } catch (NumberFormatException | IOException | ArduinoRpcActionFailsException | ArduinoRpcInitializationError ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        }
        System.out.println("\nPlease use: IntegrationTest 'port' 'baudRate'");
        System.out.println("I.e.: IntegrationTest COM5 9600");
    }

    void doIt(final String port, final String baudRate) throws IOException, ArduinoRpcActionFailsException, ArduinoRpcInitializationError {
        connectToArduinoCard(port, Integer.parseInt(baudRate));
        verifyConnectedCard();
        arduino.executeRemoteAction("Start");
        performJavaToArduinoTest();
        arduino.executeRemoteAction("Switch");
        waitReceivingTestCompleted();
        arduino.executeRemoteAction("Stop");
        evaluateTestResult();
        System.exit(0);
    }

    private void connectToArduinoCard(final String port, final int baudRate) throws ArduinoRpcInitializationError {
        arduino = new ArduinoRpc("COM5", 9600);
        arduino.connect();
    }

    private void verifyConnectedCard() throws IOException, ArduinoRpcActionFailsException {
        String cardName = arduino.getCardName();
        if (EXPECTED_SKETCH.equals(cardName) == false) {
            System.out.println("Invalid card. Found \""+cardName+"\" instead of expected \""+EXPECTED_SKETCH+"\"");
            System.exit(-1);
        } else {
            System.out.println("Connected to: "+cardName);
        }
    }

    private void performJavaToArduinoTest() {
        System.out.println("\nPc to Arduino test:");

        String report = "";
        for (int cicle=1; cicle < 4; cicle++) {
            report = report + performJavaToArduinoTest(cicle);
        }

        if (report.length() > 0) {
            System.out.println("Pc to Arduino test, FAILS:");
            System.out.println("\t"+report);
        } else {
            System.out.println("Pc to Arduino test, successfully done");
        }
    }

    private void waitReceivingTestCompleted() {
        System.out.println("\nArduino to Pc test:");
        synchronized (waitResultLock) {
            try {
                waitResultLock.wait(10_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void evaluateTestResult() {
        if (receivingReport.length() > 0) {
            System.out.println(receivingReport);
        } else {
            System.out.println("Arduino to Pc test, successfully done");
        }
    }

    String performJavaToArduinoTest(final int cicle) {
        String result = "";
        try {
            return sendExecution(cicle);
        } catch (ArduinoRpcActionFailsException e) {
            return e.getLocalizedMessage();
        }
    }

    String sendExecution(final int cicle) throws ArduinoRpcActionFailsException {
        System.out.println("\tExec "+ STRING_CALL_PC_TO_ARDUINO +" with index: "+cicle);
        final String stringExpected = String.valueOf(cicle) + String.valueOf(cicle) + String.valueOf(cicle);
        String stringResp = arduino.executeRemoteAction(STRING_CALL_PC_TO_ARDUINO, String.valueOf(cicle));
        System.out.println("\t\tResult: "+stringResp+"; Expected: "+stringExpected);

        System.out.println("\tExec "+ INTEGER_CALL_PC_TO_ARDUINO +" with index: "+cicle);
        final int intExpected = (cicle + 18)*cicle;
        int intResp = arduino.executeRemoteAction(INTEGER_CALL_PC_TO_ARDUINO, cicle, cicle + 18);
        System.out.println("\t\tResult: "+intResp+"; Expected: "+intExpected);

        System.out.println("\tExec "+ FLOAT_CALL_PC_TO_ARDUINO +" with index: "+cicle);
        final float floatExpected = (float)(3.1*cicle);
        float floatResp = arduino.executeRemoteAction(FLOAT_CALL_PC_TO_ARDUINO, (float)1.0*cicle);
        System.out.println("\t\tResult: "+floatResp+"; Expected: "+floatExpected);
        System.out.println("");

        String report = "";
        if (floatExpected != floatResp) {
            report += "Pc to Arduino Float method call fails. Expected "+floatExpected+"; Found "+floatResp+".\n";
        }

        if (!stringExpected.equals(stringResp)) {
            report += "Pc to Arduino String method call fails. Expected "+stringExpected+"; Found "+stringResp+".\n";
        }

        if (intExpected != intResp) {
            report += "Pc to Arduino Integer method call fails. Expected "+intExpected+"; Found "+intResp+".\n";
        }

        return report;
    }

    protected class ArduinoRpc extends ArduinoJavaSerialRpc {

        public int stringCallCounter = 0;
        public int floatCallCounter = 0;
        public int intCallCounter = 0;

        public ArduinoRpc(String portName, int baudRate) {
            super(portName, baudRate);
        }

        /**
         * Method called from Arduino
         * @param value a test value sent from Arduino card
         * @return a constant ignored inside the sketch
         */
        public String stringCallArduinoToPc(final String value) {
            receivingCounter++;
            stringCallCounter++;

            String expectedString = String.valueOf(13 * stringCallCounter);
            if (expectedString.equals(value) == false) {
                receivingReport += "stringCallArduinoToPc called from Arduino fails. Expected "+expectedString+"; Found "+value+".\n";
            } else {
                System.out.println("\tstringCallArduinoToPc called with value: "+value+"; expected: "+expectedString);
            }
            return "ok";
        }

        /**
         * Method called from Arduino
         * NOTE: it is strongly recommended to use "FLOAT" instead of "float" in the signature.
         * @param value a test value sent from Arduino card
         * @return a constant ignored inside the sketch
         */
        public float floatCallArduinoToPc(final Float value) {
            receivingCounter++;
            floatCallCounter++;

            float expectedFloat = (float)(18.11*floatCallCounter);
            if (expectedFloat != value) {
                receivingReport += "floatCallArduinoToPc called from Arduino fails. Expected "+expectedFloat+"; Found "+value+".\n";
            } else {
                System.out.println("\tfloatCallArduinoToPc called with value: "+value+"; expected: "+expectedFloat);
            }
            return (float)1.0;
        }

        /**
         * Method called from Arduino
         * NOTE: it is strongly recommended to use "Integer" instead of "int" in the signature.
         * @param value1 a test value sent from Arduino card
         * @param value2 a test value sent from Arduino card
         * @return a constant ignored inside the sketch
         */
        public int intCallArduinoToPc(final Integer value1, final Integer value2) {
            receivingCounter++;
            intCallCounter++;

            if (value1 != intCallCounter) {
                receivingReport += "intCallArduinoToPc called from Arduino fails. Expected first value "+intCallCounter+"; Found "+value1+".\n";
            }

            int expectedInt = 27*value1;
            if (value2 != expectedInt) {
                receivingReport += "intCallArduinoToPc called from Arduino fails. Expected second value "+expectedInt+"; Found "+value2+".\n";
            } else {
                System.out.println("\tintCallArduinoToPc called with value1: "+value1+"; expected: "+intCallCounter+
                        "; value2: "+value2+"; expected: "+expectedInt);
            }

            return 1;
        }

        /**
         * Method called from Arduino when the Arduino to Pc calls are done
         * When this method is called, the wait_receiving_test_completed method can be unlocked
         */
        public void arduinoEnds() {
            if (receivingCounter != 3*3) {
                receivingReport = receivingReport + "FAILED:  Received "+receivingCounter+" calls when expected is 9:";
                receivingReport = receivingReport + "stringCallJavaSide called "+arduino.stringCallCounter+" times instead 3";
                receivingReport = receivingReport + "floatCallJavaSide called "+arduino.floatCallCounter+" times instead 3";
                receivingReport = receivingReport + "intCallJavaSide called "+arduino.intCallCounter+" times instead 3";
            }

            synchronized (waitResultLock) {
                waitResultLock.notify();
            }
        }
    }
}
