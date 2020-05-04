//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.businesslogic;

import gnu.io.*;
import it.mauxilium.arduinojavaserialrpc.ArduinoJavaSerialRpc;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcActionFailsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.concurrent.Semaphore;

/**
 * Protocol manager for send and receive commands
 * Serialize and deserialize strings and binary values with the following schema:<br>
 * Commands:<br>
 * <ol>
 * <li>Row 1: Preamble (string)</li>
 * <li>Row 2: Command name (string)</li>
 * <li>Row 3: Signature code (char)</li>
 * <li>Row 4: First argument (string/binary)</li>
 * <li>Row 5: Optional second argument (string/binary)</li>
 * </ol>
 * <br>
 * Results:<br>
 * <ol>
 * <li>Row 1: Preamble (string)</li>
 * <li>Row 2: Data type (char)</li>
 * <li>Row 3: Value (string/binary)</li>
 * </ol>
 * <br>
 * Errors:<br>
 * <ol>
 * <li>Row 1: Preamble (string)</li>
 * <li>Row 2: Failed command name (string)</li>
 * <li>Row 3: Error message (string)</li>
 * </ol>
 */
public class UsbHandler {

    public static final char VOID_ARG_PREAMBLE = 'V';
    public static final char INT_ARG_PREAMBLE = 'I';
    public static final char INT_INT_ARG_PREAMBLE = 'H';
    public static final char STRING_ARG_PREAMBLE = 'S';
    public static final char FLOAT_ARG_PREAMBLE = 'F';

    SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyACM0", // Raspberry Pi
        "/dev/ttyUSB0", // Linux
        "COM4", // Windows
    };
    
    /**
     * The input stream from the port
     */
    private BufferedReader input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to stop while waiting opening port
     */
    private static final int TIME_OUT = 20000;

    private final String selectedPortName;

    private final int selectedBaudRate;

    private Object callingResult;

    private final Semaphore callingLock = new Semaphore(1, true);

    private final Object waitingResultLock = new Object();

    public UsbHandler(final String portName, final int portRate) {
        selectedPortName = portName;
        selectedBaudRate = portRate;
    }

    public void initialize(final ArduinoJavaSerialRpc ctrl)
            throws PortInUseException, UnsupportedCommOperationException,
            IOException, TooManyListenersException, NoSuchPortException {

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(selectedPortName);
        if (portIdentifier.isCurrentlyOwned()) {
            throw new PortInUseException();
        }

        // open serial port, and use class name for the appName.
        serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), TIME_OUT);

        // set port parameters
        serialPort.setSerialPortParams(selectedBaudRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        // open the streams
        input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        output = serialPort.getOutputStream();

        // add event listeners
        UsbReceiverAgent usbAgent = new UsbReceiverAgent(this, input, ctrl);
        serialPort.addEventListener(usbAgent);
        serialPort.notifyOnDataAvailable(true);
        usbAgent.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    public String getPortName() {
        return selectedPortName;
    }

    public int getBaudRate() {
        return selectedBaudRate;
    }

    public void disconnect() throws IOException {
        input.close();
        output.close();
        serialPort.removeEventListener();
        serialPort.close();
    }

    public static String[] portScanner() {
        ArrayList<String> availablePorts = new ArrayList<>();
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            availablePorts.add(currPortId.getName()+
                    " (" + currPortId.getPortType()+
                    "): owner: "+
                    currPortId.getCurrentOwner());
        }
        String[] resList = new String[availablePorts.size()];
        int index = 0;
        for (String item : availablePorts) {
            resList[index++] = item;
        }
        return resList;
    }

    /**
     * This should be called when you stop using the port. This will prevent
     * port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public String getCardName() throws ArduinoRpcActionFailsException {
        checkConnectionReady();
        callingLock();
        synchronized (waitingResultLock) {
            try {
                ProtocolToArduino.sendCommand("GetCardName", "", output);
            } catch (IOException ex) {
                callingRelease();
                throw new ArduinoRpcActionFailsException("Executing get card name", ex);
            }
            waitResult();
        }
        callingRelease();
        return (String) callingResult;
    }

    public void executeAction(final String commandName) throws ArduinoRpcActionFailsException {
        checkConnectionReady();
        callingLock();
        synchronized (waitingResultLock) {
            try {
                ProtocolToArduino.sendCommand(commandName, output);
            } catch (IOException ex) {
                callingRelease();
                throw new ArduinoRpcActionFailsException("Executing "+commandName, ex);
            }
            waitResult();
        }
        callingRelease();
    }

    public Integer executeAction(final String commandName, final int arg1, final int arg2)
            throws ArduinoRpcActionFailsException {
        checkConnectionReady();
        callingLock();
        synchronized (waitingResultLock) {
            try {
                ProtocolToArduino.sendCommand(commandName, arg1, arg2, output);
            } catch (IOException ex) {
                callingRelease();
                throw new ArduinoRpcActionFailsException("Executing "+commandName+"("+arg1+","+arg2+")", ex);
            }
            waitResult();
        }
        callingRelease();
        return (Integer) callingResult;
    }

    public String executeAction(final String commandName, final String argument) throws ArduinoRpcActionFailsException {
        checkConnectionReady();
        callingLock();
        synchronized (waitingResultLock) {
            try {
                ProtocolToArduino.sendCommand(commandName, argument, output);
            } catch (IOException ex) {
                callingRelease();
                throw new ArduinoRpcActionFailsException("Executing "+commandName+"("+argument+")", ex);
            }
            waitResult();
        }
        callingRelease();
        return (String) callingResult;
    }

    public float executeAction(final String commandName, final float argument) throws ArduinoRpcActionFailsException {
        checkConnectionReady();
        callingLock();
        synchronized (waitingResultLock) {
            try {
                ProtocolToArduino.sendCommand(commandName, argument, output);
            } catch (IOException ex) {
                callingRelease();
                throw new ArduinoRpcActionFailsException("Executing "+commandName+"("+argument+")", ex);
            }
            waitResult();
        }
        callingRelease();
        return (float) callingResult;
    }

    public void setIncomingResult(final Object result) {
        callingResult = result;
        synchronized (waitingResultLock) {
            waitingResultLock.notifyAll();
        }
    }

    private void checkConnectionReady() throws ArduinoRpcActionFailsException {
        if (output == null) {
            throw new ArduinoRpcActionFailsException("Arduino is not connected. Please use connect() before to perform Arduino activities.");
        }
    }

    private void callingLock() throws ArduinoRpcActionFailsException {
        try {
            callingLock.acquire();
        } catch (InterruptedException ex) {
            throw new ArduinoRpcActionFailsException("Reserving priority in calling Arduino function", ex);
        }
    }

    private void waitResult() throws ArduinoRpcActionFailsException {
        try {
            waitingResultLock.wait();
        } catch (InterruptedException ex) {
            throw new ArduinoRpcActionFailsException("Waiting result from called Arduino function", ex);
        }
    }

    private void callingRelease() {
        callingLock.release();
    }
}
