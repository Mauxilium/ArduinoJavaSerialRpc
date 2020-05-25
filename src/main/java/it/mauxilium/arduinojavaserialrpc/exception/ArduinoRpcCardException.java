//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.exception;

/**
 * This exception is raised when the Arduino sketch sends an error notification.
 */
public class ArduinoRpcCardException extends ArduinoRpcException {

    public ArduinoRpcCardException(final String errorTxt) {
        super(errorTxt);
    }

    public ArduinoRpcCardException(final Exception exc) {
        super(exc);
    }

    public ArduinoRpcCardException(final String errorTxt, final Exception exc) {
        super(errorTxt, exc);
    }

}
