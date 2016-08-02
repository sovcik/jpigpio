package tests;

import jpigpio.JPigpio;
import jpigpio.PigpioException;
import jpigpio.PigpioSocket;
import jpigpio.Utils;

/**
 * Simple tester for TTL to RS485 converter allowing Raspberry Pi communication using RS-485 serial bus
 */
public class Test_RS485 {
    public static void main(String args[]) {
        System.out.println("Test_RS485");
        Test_RS485 app = new Test_RS485();
        app.run();
    }

    public void run() {

        String host = "pigpiod-host";
        String serialPort = "/dev/ttyAMA0";
        int baudRate = 115200;
        int tx485ControlGpio = 18;   // gpio pin to which RS485 TX control is connected

        int handle;
        long startTime = System.currentTimeMillis();
        int i = 1;

        try {
            System.out.println("Opening Pigpio.");
            JPigpio pigpio = new PigpioSocket(host, 8888);
            //JPigpio pigpio = new Pigpio();

            pigpio.gpioInitialize();
            Utils.addShutdown(pigpio);

            System.out.println("Opening serial port "+serialPort);
            handle = pigpio.serialOpen(serialPort, baudRate, 0);
            pigpio.gpioSetMode(tx485ControlGpio, JPigpio.PI_OUTPUT);
            pigpio.gpioWrite(tx485ControlGpio, JPigpio.PI_HIGH); // set Raspberry Pi to transmit mode

            System.out.println("Going to send incrementing numbers for the next 20 seconds");
            while (System.currentTimeMillis() - startTime < 20000){

                Thread.sleep(1000);
                //pigpio.gpioWrite(tx485ControlGpio, JPigpio.PI_HIGH);
                pigpio.serialWrite(handle, (""+i+"\n\r").getBytes());
                //pigpio.gpioWrite(tx485ControlGpio, JPigpio.PI_LOW);
                System.out.println("SENT: "+i);
                i++;

            }

            pigpio.serialClose(handle);
            pigpio.gpioTerminate();

        } catch (PigpioException |InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Test Serial Completed.");
    }


}
