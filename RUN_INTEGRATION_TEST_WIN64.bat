echo off
rem
rem ╔════════════════════════╗
rem   Author: Gabriele Maris
rem   Project: Mauxilium
rem ╚════════════════════════╝
rem  Copyright 2020 Gabriele Maris
rem
rem Before to run this Java program, please install the sketch ArduinoJavaSerialRpc\src\test\java\integration\sketch\sketch.ino into your Arduino Card
rem

java -Djava.library.path=RxTx\mfz-rxtx-2.2-20081207-win-x64 -cp target/test-classes;target/classes;RxTx/mfz-rxtx-2.2-20081207-win-x64/RXTXcomm.jar integration.java.IntegrationTest COM5 9600

pause