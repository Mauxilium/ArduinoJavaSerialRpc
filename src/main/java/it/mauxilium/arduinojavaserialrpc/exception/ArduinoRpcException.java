//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.exception;

/**
 */
public class ArduinoRpcException extends Exception {

    protected ArduinoRpcException(final String errorTxt) {
        super(errorTxt);
    }

    protected ArduinoRpcException(final Exception exc) {
        super(exc);
    }

    protected ArduinoRpcException(final String errorTxt, final Exception exc) {
        super(errorTxt, exc);
    }

}
