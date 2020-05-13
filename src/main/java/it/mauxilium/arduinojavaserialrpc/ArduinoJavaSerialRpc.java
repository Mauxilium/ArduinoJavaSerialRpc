//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import it.mauxilium.arduinojavaserialrpc.businesslogic.UsbHandler;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcActionFailsException;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcInitializationError;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.TooManyListenersException;
import java.util.logging.Logger;

/**
 * This class implements a bidirectional communication with Arduino card in form
 * of RPC (Remote Procedure Call) and RMI (Remote Method Invocation) through the serial port.<br>
 * Due to resource limitations of Arduino card, only a few fixed signatures are available.<br>
 * <br>
 * <b>Java to Arduino (RPC)</b><br>
 * A Java program that includes ArduinoJavaSerialRpc can call a function inside an
 * Arduino sketch if:<br>
 * <blockquote>
 * <ol>
 * <li>The sketch includes the ArduinoSerialRpc library: "<code>#include &lt;ArduinoSerialRpc.h&gt;</code>"</li>
 * <li>The sketch function is registered using the "registerArduinoAction" method.</li>
 * <li>The registered function have one of the following signatures:<br>
 * <ul>
 * <li>void <i>methodName</i>();</li>
 * <li>int <i>methodName</i>(int arg1, int arg2);</li>
 * <li>float <i>methodName</i>(float arg);</li>
 * <li>String <i>methodName</i>(String arg);</li>
 * </ul>
 * </li>
 * </ol>
 * </blockquote>
 * <br>
 * For example, a legal call could be:<br>
 * <blockquote><code>libraryInstance.executeAction("writeAction", 1811, 1118);</code></blockquote>
 * <br>
 * <b>Arduino to Java (RMI)</b><br>
 * An Arduino sketch can call a Java method without any registration, if:
 * <br>
 * <blockquote>
 * <ol>
 * <li>The sketch includes: "<code>#include &lt;ArduinoSerialRpc.h&gt;</code>".</li>
 * <li>The required method is part of a class which extends ArduinoJavaSerialRpc.</li>
 * <li>The method signature is one of the following:<br>
 * <ul>
 * <li>void <i>methodName</i>();</li>
 * <li>Integer <i>methodName</i>(Integer arg1, Integer arg2);</li>
 * <li>Float <i>methodName</i>(Float arg);</li>
 * <li>String <i>methodName</i>(String arg);</li>
 * </ul></li>
 * </ol>
 * </blockquote><br>
 * <br>
 * <b>PROJECT NOTE:</b><br>
 * Any Java project which uses ArduinoJavaSerialRpc must include also the RxTx library
 * (a copy of it may be found into: ArduinoJavaSerialRpc/RxTx/*).<br>
 * <b>Warning</b>: Missing the RxTx inclusion produces an execution error like:
 * <b>gnu/io/NoSuchPortException</b><br>
 * <br>
 * <b>EXECUTION NOTE:</b><br>
 * In order to execute a Java program which includes ArduinoJavaSerialRpc, the command
 * line must includes a driver link, like:<br>
 * <b>java -Djava.library.path=RxTx\mfz-rxtx-2.2-20081207-win-x64 -cp target/test-classes;target/classes;RxTx/mfz-rxtx-2.2-20081207-win-x64/RXTXcomm.jar integration.java.IntegrationTest COM5 9600</b>
 */
public class ArduinoJavaSerialRpc {

    static {
        System.out.println("Powered by ArduinoJavaSerialRpc from www.mauxilium.it");
        Logger.getGlobal().info("Powered by ArduinoJavaSerialRpc from www.mauxilium.it");
    }

    /**
     * Default port to use when the Operative System is Mac OS.
     * <br>
     * Assigned value: MAC_OS_DEFAULT_PORT = "/dev/tty.usbserial-A9007UX1"
     */
    public static final String MAC_OS_DEFAULT_PORT = "/dev/tty.usbserial-A9007UX1";
    /**
     * Default port to use when the Platform is Raspberry PI.
     * <br>
     * Assigned value: RASPBERRY_PI_DEFAULT_PORT = "/dev/ttyACM0"
     */
    public static final String RASPBERRY_PI_DEFAULT_PORT = "/dev/ttyACM0";
    /**
     * Default port to use when the Operative System is Linux.
     * <br>
     * Assigned value: LINUX_DEFAULT_PORT = "/dev/ttyUSB0"
     */
    public static final String LINUX_DEFAULT_PORT = "/dev/ttyUSB0";
    /**
     * Default port to use when the Operative System is Windows.
     * <br>
     * Assigned value: WINDOWS_DEFAULT_PORT = "COM5"
     */
    public static final String WINDOWS_DEFAULT_PORT = "COM5";

    /**
     * Port COM1, to be used when the Operative System is Windows.
     */
    public static final String WINDOWS_USB_1 = "COM1";
    /**
     * Port COM2, to be used when the Operative System is Windows.
     */
    public static final String WINDOWS_USB_2 = "COM2";
    /**
     * Port COM3, to be used when the Operative System is Windows.
     */
    public static final String WINDOWS_USB_3 = "COM3";
    /**
     * Port COM4, to be used when the Operative System is Windows.
     */
    public static final String WINDOWS_USB_4 = "COM4";
    /**
     * Port COM5, to be used when the Operative System is Windows.
     */
    public static final String WINDOWS_USB_5 = "COM5";
    /**
     * Port COM6, to be used when the Operative System is Windows.
     */
    public static final String WINDOWS_USB_6 = "COM6";

    public static final int DATA_RATE_300 = 300;
    public static final int DATA_RATE_600 = 600;
    public static final int DATA_RATE_1200 = 1200;
    public static final int DATA_RATE_2400 = 2400;
    public static final int DATA_RATE_4800 = 4800;
    public static final int DATA_RATE_9600 = 9600;
    public static final int DATA_RATE_14400 = 14400;
    public static final int DATA_RATE_19200 = 19200;
    public static final int DATA_RATE_28800 = 28800;
    public static final int DATA_RATE_38400 = 38400;
    public static final int DATA_RATE_57600 = 57600;
    public static final int DATA_RATE_115200 = 115200;

    private final UsbHandler usbHandler;

    /**
     * Creates a connector to Arduino card.<br>
     * The constructor requires two parameter:<br>
     * The port name, which syntax depends from the operating system in use.<br>
     * The baud rate, which depends from the sketch setup().<br>
     * Frequently used values are declared in the PORTs constants (like
     * LINUX_DEFAULT_PORT) and DATA_RATE constants.
     * <br>
     * NOTE:<br>
     * This constructor instantiates the resource only, to establish a physical
     * connection with the card a "connect()" invocation is required.
     *
     * @param portName The name of connection port; i.e. "COM1";
     * @param baudRate The value for Serial port speed;
     */
    public ArduinoJavaSerialRpc(final String portName, final int baudRate) {
        usbHandler = new UsbHandler(portName, baudRate);
    }

    /**
     * Returns the used USB port name
     * @return one of the legal values like "/dev/ttyUSB0" or "COM4"
     */
    public String getPortName() {
        return usbHandler.getPortName();
    }

    /**
     * Returns the used baud rate
     * @return one of the legal values like DATA_RATE_300 or DATA_RATE_9600
     */
    public int getBaudRate() {
        return usbHandler.getBaudRate();
    }

    /**
     * Creates a connection with the Arduino card.<br>
     * After this calls the USB port is locked and no other programs can use it.<br>
     * In order to release the USB port a "disconnect()" call is required.
     *
     * @throws ArduinoRpcInitializationError In any case of connection or
     * initialization error (i.e. wrong connection port specified).
     */
    public void connect() throws ArduinoRpcInitializationError {
        // TODO check if it is strongly required or not
        //        // The next line is for Raspberry Pi and gets us into the while loop and was suggested here:
        //        //      http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
        //        if (isRaspberryPi()) {
        //            System.setProperty("gnu.io.rxtx.SerialPorts", RASPBERRY_PI_DEFAULT_PORT);
        //        }

        try {
            usbHandler.initialize(this);
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException |
                IOException | TooManyListenersException ex) {
            throw new ArduinoRpcInitializationError(ex);
        }
    }

    /**
     * Release the serial port connected to the Arduino card.<br>
     * To establish a new connection a "connect()" call is required.
     *
     * @throws IOException In any case of closing errors.
     */
    public void disconnect() throws IOException {
        usbHandler.disconnect();
    }

    /**
     * Discover the available serial ports in system.
     *
     * @return A list of available serial ports (free and used too).
     */
    public static String[] portScanner() {
        return UsbHandler.portScanner();
    }

    /**
     * Returns the card identification declared into the sketch
     * It is the string used as argument of ArduinoSerialRpc constructor into the sketch.
     * @return the registered card identification name
     * @throws ArduinoRpcActionFailsException In any case of communication or function execution error.
     */
    public String getCardName() throws ArduinoRpcActionFailsException {
        return usbHandler.getCardName();
    }

    /**
     * Executes a function (of Arduino sketch) with signature: void <i>actionName</i>();.<br>
     *
     * @param actionName The name of Arduino's function to call.
     * @throws ArduinoRpcActionFailsException In any case of communication or function execution error.
     */
    public void executeRemoteAction(final String actionName) throws ArduinoRpcActionFailsException {
        usbHandler.executeAction(actionName);
    }

    /**
     * Executes a function (of Arduino sketch) with signature: int <i>actionName</i>(int arg1, int arg2);
     * <br><br>
     * For example:<br>
     * <br>
     * <blockquote>
     * <code>
     * ArduinoSerialRpc rpc;<br>
     * <br>
     * void setup() {
     * </code>
     * <blockquote>
     * pinMode(2, OUTPUT);<br>
     * pinMode(3, OUTPUT);<br>
     * rpc.registerArduinoFunction("setLightIntensity", setBrightness);
     * </blockquote>
     * }<br>
     * <br>
     * // Callable method from Java program.<br>
     * // Java executes: libraryInstance.executeAction("setLightIntensity", 2, 132);<br>
     * boolean setBrightness(int pin, int value) {
     * <blockquote>
     * if ((pin == 2) || (pin == 3)) {
     * <blockquote>
     * analogWrite(pin, value);<br>
     * return true;
     * </blockquote>
     * } else {
     * <blockquote>
     * return false;
     * </blockquote>
     * }
     * </blockquote>
     * }<br></blockquote>
     *
     * @param actionName The name of function to call, in Arduino sketch.
     * @param arg1 First value to send.
     * @param arg2 Second value to send.
     * @return The result of called function;
     * @throws ArduinoRpcActionFailsException In any case of communication or function execution error.
     */
    public Integer executeRemoteAction(final String actionName, final int arg1, final int arg2)
            throws ArduinoRpcActionFailsException {
        return usbHandler.executeAction(actionName, arg1, arg2);
    }

    /**
     * Executes a function (of Arduino sketch) with signature: string <i>actionName</i>(string);
     * <br><br>
     *
     * @param actionName The name of function to call, in Arduino sketch.
     * @param argument The parameter to send
     * @return The result of called function
     * @throws ArduinoRpcActionFailsException In any case of communication or method execution error.
     */
    public String executeRemoteAction(final String actionName, final String argument) throws ArduinoRpcActionFailsException {
        return usbHandler.executeAction(actionName, argument);
    }

    /**
     * Executes a method (of Arduino sketch) with signature: float <i>actionName</i>(float);
     * <br><br>
     *
     * @param actionName The name of function to call, in Arduino sketch.
     * @param argument The parameter to send
     * @return The result of called function
     * @throws ArduinoRpcActionFailsException In any case of communication or function execution error
     */
    public float executeRemoteAction(final String actionName, final float argument) throws ArduinoRpcActionFailsException {
        return usbHandler.executeAction(actionName, argument);
    }

    /**
     * Overridable function called when an exception occurs receiving a message from Arduino.
     *
     * @param ex The occurred exception
     */
    public void handlerReceivingException(final Exception ex) {
        System.err.println("Error handling Arduino message: " + ex.toString());
    }

    /**
     * Discovers and executes a method of the extending class.
     *
     * @param actionToDo the name of method to be called
     * @throws ArduinoRpcActionFailsException generated in case of execution errors
     */
    public void executeLocalAction(final String actionToDo) throws ArduinoRpcActionFailsException {
        Method howToRun;
        try {
            howToRun = this.getClass().getMethod(actionToDo);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new ArduinoRpcActionFailsException(ex);
        }
        try {
            howToRun.invoke(this);
        } catch (IllegalAccessException | IllegalArgumentException |
                InvocationTargetException ex) {
            throw new ArduinoRpcActionFailsException(ex);
        }
    }

    /**
     * Discovers and executes a method of the extending class.
     *
     * @param actionToDo the name of method to be called
     * @param arg1 the first input parameter
     * @param arg2 the second input parameter
     * @throws ArduinoRpcActionFailsException generated in case of execution errors
     */
    public void executeLocalAction(final String actionToDo, final int arg1, final int arg2) throws ArduinoRpcActionFailsException {
        Method howToRun;
        try {
            howToRun = this.getClass().getMethod(actionToDo, Integer.class, Integer.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new ArduinoRpcActionFailsException(ex);
        }
        try {
            howToRun.invoke(this, arg1, arg2);
        } catch (IllegalAccessException | IllegalArgumentException |
                InvocationTargetException ex) {
            throw new ArduinoRpcActionFailsException(ex);
        }
    }

    /**
     * Discovers and executes a method of the extending class.
     *
     * @param actionToDo the name of method to be called
     * @param arg the input parameter
     * @throws ArduinoRpcActionFailsException generated in case of execution errors
     */
    public void executeLocalAction(final String actionToDo, final String arg) throws ArduinoRpcActionFailsException {
        Method howToRun;
        try {
            howToRun = this.getClass().getMethod(actionToDo, String.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new ArduinoRpcActionFailsException(ex);
        }
        try {
            howToRun.invoke(this, arg);
        } catch (IllegalAccessException | IllegalArgumentException |
                InvocationTargetException ex) {
            throw new ArduinoRpcActionFailsException(ex);
        }
    }

    /**
     * Discovers and executes a method of the extending class.
     *
     * @param actionToDo the name of method to be called
     * @param arg the input parameter
     * @throws ArduinoRpcActionFailsException generated in case of execution errors
     */
    public void executeLocalAction(final String actionToDo, final float arg) throws ArduinoRpcActionFailsException {
        Method howToRun;
        try {
            howToRun = this.getClass().getMethod(actionToDo, Float.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new ArduinoRpcActionFailsException(ex);
        }
        try {
            howToRun.invoke(this, arg);
        } catch (IllegalAccessException | IllegalArgumentException |
                InvocationTargetException ex) {
            throw new ArduinoRpcActionFailsException(ex);
        }
    }

    // TODO restore it in case of required use in connect() above
    //    private boolean isRaspberryPi() {
    //        return System.getProperty("os.name").toLowerCase().contains("raspbian");
    //    }
}
