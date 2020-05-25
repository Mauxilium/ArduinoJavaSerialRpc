//
// ╔════════════════════════╗
//   Author: Gabriele Maris 
//   Project: Mauxilium     
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package it.mauxilium.arduinojavaserialrpc.businesslogic;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import it.mauxilium.arduinojavaserialrpc.ArduinoJavaSerialRpc;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcJavaFailsException;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcCardException;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 */
class UsbReceiverAgent extends Thread implements SerialPortEventListener {

    private static final String CMD_PREAMBLE = "MArC_cmd";
    private static final String RESULT_PREAMBLE = "MArC_res";
    private static final String ERROR_PREAMBLE = "MArC_err";
    private static final String MESSAGE_PREAMBLE = "MArC_msg";

    private final UsbHandler usbH;
    private final BufferedReader input;
    private final ArduinoJavaSerialRpc controller;
    private Object callingResult;
    private final Object receiverWaitingResultLock = new Object();

    public UsbReceiverAgent(final UsbHandler usbHandler, final BufferedReader in, final ArduinoJavaSerialRpc ctrl) {
        usbH = usbHandler;
        input = in;
        controller = ctrl;
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
        if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                handleReceivingData();
            } catch (IOException | ArduinoRpcJavaFailsException | ArduinoRpcCardException ex) {
                if ("Underlying input stream returned zero bytes".equals(ex.getMessage()) == false) {
                    controller.handlerReceivingException(ex);
                    callingResult = null;
                    synchronized (receiverWaitingResultLock) {
                        receiverWaitingResultLock.notifyAll();
                    }
                } else {
                    System.out.println("Serial event exception: " + ex.toString());
                }
            }
        } else {
            assert (spe.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY):
                    "Serial Output buffer empty";
        }
    }

    private void handleReceivingData() throws IOException, ArduinoRpcJavaFailsException, ArduinoRpcCardException {
        if (input.ready()) {
            String receivedPreamble = input.readLine();
            switch (receivedPreamble) {
                case CMD_PREAMBLE:
                    ProtocolFromArduino.receiveCommand(input, controller);
                    break;
                case RESULT_PREAMBLE:
                    parsingResult();
                    break;
                case ERROR_PREAMBLE:
                    throw new ArduinoRpcCardException(input.readLine());
                case MESSAGE_PREAMBLE:
                    System.out.println("Arduino message: " + input.readLine());
                    break;
                default:
                    if (receivedPreamble.isEmpty() == false) {
                        System.out.println("Ignoring fragmented command: "+receivedPreamble);
                    }
            }
        }
    }

    void parsingResult() throws IOException {
        String argType = input.readLine();
        switch (argType.charAt(0)) {
            case UsbHandler.VOID_ARG_PREAMBLE:
                callingResult = null;
                break;
            case UsbHandler.INT_ARG_PREAMBLE:
                callingResult = Integer.parseInt(input.readLine());
                break;
            case UsbHandler.FLOAT_ARG_PREAMBLE:
                callingResult = Float.parseFloat(input.readLine());
                break;
            case UsbHandler.STRING_ARG_PREAMBLE:
                callingResult = input.readLine();
                break;
            default:
                throw new IOException("Not supported received data type: " + argType);
        }

        synchronized (receiverWaitingResultLock) {
            receiverWaitingResultLock.notifyAll();
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (receiverWaitingResultLock) {
                try {
                    receiverWaitingResultLock.wait();
                } catch (InterruptedException ignored) {
                }
            }

            usbH.setIncomingResult(callingResult);
        }
    }

}
