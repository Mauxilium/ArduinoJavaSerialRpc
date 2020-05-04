//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.exception;

/**
 */
public class ArduinoRpcInitializationError extends ArduinoRpcException {

    public ArduinoRpcInitializationError(final String errorTxt) {
        super(errorTxt);
    }

    public ArduinoRpcInitializationError(final Exception exc) {
        super(exc);
    }

    public ArduinoRpcInitializationError(final String errorTxt, final Exception exc) {
        super(errorTxt, exc);
    }

}
