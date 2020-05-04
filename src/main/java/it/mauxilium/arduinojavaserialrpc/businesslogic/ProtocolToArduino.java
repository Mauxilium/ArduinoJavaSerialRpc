//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.businesslogic;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class perform any physical command send to Arduino card. The required
 * sent are:
 * <ul>
 * <li>Send command without parmeter</li>
 * <li>Send command with two integer parameters</li>
 * <li>Send command with float parameter</li>
 * <li>Send command with String parameter</li>
 * </ul>
 */
public class ProtocolToArduino {

    /**
     * Sends a command model: void method();
     *
     * @param cmd Command name
     * @param output Sending channel
     * @throws IOException In case of any sending error
     */
    public static void sendCommand(final String cmd, final OutputStream output)
            throws IOException {
        String command = cmd.trim() + " " + UsbHandler.VOID_ARG_PREAMBLE;
        output.write(command.getBytes());
        output.flush();
    }

    /**
     * Sends a command model: int method(int, int);
     *
     * @param cmd Command name
     * @param arg1 First argument
     * @param arg2 Second Argument
     * @param output Sending channel
     * @throws IOException In case of any sending error
     */
    public static void sendCommand(final String cmd, final int arg1,
            final int arg2, final OutputStream output) throws IOException {
        String command = cmd.trim() + " " + UsbHandler.INT_INT_ARG_PREAMBLE + arg1 + "," + arg2;
        output.write(command.getBytes());
        output.flush();
    }

    /**
     * Sends a command model: float method(float);
     *
     * @param cmd Command name
     * @param arg1 Command argument
     * @param output Sending channel
     * @throws IOException In case of any sending error
     */
    public static void sendCommand(final String cmd, final float arg1,
            final OutputStream output) throws IOException {
        String command = cmd.trim() + " " + UsbHandler.FLOAT_ARG_PREAMBLE + arg1;
        output.write(command.getBytes());
        output.flush();
    }

    /**
     * Sends a command model: String method(String);
     *
     * @param cmd Command name
     * @param arg1 Command argument
     * @param output Sending channel
     * @throws IOException In case of any sending error
     */
    public static void sendCommand(final String cmd, final String arg1,
            final OutputStream output) throws IOException {
        String command = cmd.trim() + " " + UsbHandler.STRING_ARG_PREAMBLE + arg1;
        output.write(command.getBytes());
        output.flush();
    }
}
