//
// ╔════════════════════════╗
//   Author: Gabriele Maris
//   Project: Mauxilium
// ╚════════════════════════╝
//  Copyright 2020 Gabriele Maris
package unit;

import it.mauxilium.arduinojavaserialrpc.ArduinoJavaSerialRpc;
import it.mauxilium.arduinojavaserialrpc.businesslogic.UsbHandler;
import it.mauxilium.arduinojavaserialrpc.exception.ArduinoRpcActionFailsException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ArduinoJavaSerialRpc.class, UsbHandler.class })
public class ArduinoJavaRpcTest {

    @Mock
    private UsbHandler handler;

    @Before
    public void setup() throws Exception {
        Mockito.when(handler.getPortName()).thenReturn("TestPort");
        Mockito.when(handler.getBaudRate()).thenReturn(9600);
        Mockito.when(handler.getCardName()).thenReturn("TestCard");
        PowerMockito.whenNew(UsbHandler.class)
                .withArguments(ArgumentMatchers.eq("TestPort"), ArgumentMatchers.eq(9600))
                .thenReturn(handler);
    }

    @Test
    public void getPortNameOk() {
        ArduinoJavaSerialRpc connector = new ArduinoJavaSerialRpc("TestPort", 9600);
        String portName = connector.getPortName();

        Assert.assertEquals("TestPort", portName);
        Mockito.verify(handler, VerificationModeFactory.atMost(1)).getPortName();
        Mockito.verify(handler, VerificationModeFactory.atLeast(1)).getPortName();
        Mockito.verifyNoMoreInteractions(handler);
    }

    @Test
    public void getBaudRate() {
        ArduinoJavaSerialRpc connector = new ArduinoJavaSerialRpc("TestPort", 9600);
        int rate = connector.getBaudRate();

        Assert.assertEquals(9600, rate);
        Mockito.verify(handler, VerificationModeFactory.atMost(1)).getBaudRate();
        Mockito.verify(handler, VerificationModeFactory.atLeast(1)).getBaudRate();
        Mockito.verifyNoMoreInteractions(handler);
    }

    @Test
    public void getCardNameOk() throws IOException, ArduinoRpcActionFailsException {
        ArduinoJavaSerialRpc connector = new ArduinoJavaSerialRpc("TestPort", 9600);
        String card = connector.getCardName();

        Assert.assertEquals("TestCard", card);
        Mockito.verify(handler, VerificationModeFactory.atMost(1)).getCardName();
        Mockito.verify(handler, VerificationModeFactory.atLeast(1)).getCardName();
        Mockito.verifyNoMoreInteractions(handler);
    }

    @Test
    public void getCardNameFails() throws IOException, ArduinoRpcActionFailsException {
        Mockito.when(handler.getCardName()).thenThrow(ArduinoRpcActionFailsException.class);

        ArduinoJavaSerialRpc connector = new ArduinoJavaSerialRpc("TestPort", 9600);
        try {
            connector.getCardName();
            Assert.fail("Expected MauxArConnectorActionFailsException exception");
        } catch (ArduinoRpcActionFailsException e) {
            Mockito.verify(handler, VerificationModeFactory.atMost(1)).getCardName();
            Mockito.verify(handler, VerificationModeFactory.atLeast(1)).getCardName();
            Mockito.verifyNoMoreInteractions(handler);
        }
    }
}
