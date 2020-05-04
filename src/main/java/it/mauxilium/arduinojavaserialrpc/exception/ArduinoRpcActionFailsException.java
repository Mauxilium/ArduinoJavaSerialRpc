//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.exception;

/**
 */
public class ArduinoRpcActionFailsException extends ArduinoRpcException {

    public ArduinoRpcActionFailsException(final String errorTxt) {
        super(errorTxt);
    }

    public ArduinoRpcActionFailsException(final Exception exc) {
        super(exc);
    }

    public ArduinoRpcActionFailsException(final String errorTxt, final Exception exc) {
        super(errorTxt, exc);
    }

}
