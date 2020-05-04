//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.businesslogic;

import it.mauxilium.arduinojavaserialrpc.ArduinoJavaSerialRpc;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcActionFailsException;

import java.io.BufferedReader;
import java.io.IOException;

class ProtocolFromArduino {

    public static void receiveCommand(final BufferedReader inChannel, final ArduinoJavaSerialRpc controller)
            throws IOException, ArduinoRpcActionFailsException {
        String cmdName = inChannel.readLine();
        String argType = inChannel.readLine();
        switch (argType.charAt(0)) {
            case UsbHandler.VOID_ARG_PREAMBLE:
                controller.executeLocalAction(cmdName);
                break;
            case UsbHandler.INT_INT_ARG_PREAMBLE:
                int arg1 = Integer.parseInt(inChannel.readLine());
                int arg2 = Integer.parseInt(inChannel.readLine());
                controller.executeLocalAction(cmdName, arg1, arg2);
                break;
            case UsbHandler.STRING_ARG_PREAMBLE:
                controller.executeLocalAction(cmdName, inChannel.readLine());
                break;
            case UsbHandler.FLOAT_ARG_PREAMBLE:
                float argF = Float.parseFloat(inChannel.readLine());
                controller.executeLocalAction(cmdName, argF);
                break;
            default:
                throw new ArduinoRpcActionFailsException(
                        "Error in Arduino request to execute: " + cmdName +
                        "; Not supported argument model: " + argType);
        }
    }
}
