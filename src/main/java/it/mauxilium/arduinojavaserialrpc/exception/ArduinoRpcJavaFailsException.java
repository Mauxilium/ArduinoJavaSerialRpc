//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.exception;

/**
 * This exception is raised any time an error occurs in the Java side
 */
public class ArduinoRpcJavaFailsException extends ArduinoRpcException {

    public ArduinoRpcJavaFailsException(final String errorTxt) {
        super(errorTxt);
    }

    public ArduinoRpcJavaFailsException(final Exception exc) {
        super(exc);
    }

    public ArduinoRpcJavaFailsException(final String errorTxt, final Exception exc) {
        super(errorTxt, exc);
    }

}
