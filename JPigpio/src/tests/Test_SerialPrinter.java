package tests;

import jpigpio.JPigpio;
import jpigpio.PigpioException;
import jpigpio.PigpioSocket;
import jpigpio.Utils;

import java.util.Arrays;

/**
 * Simple test class to echo input to output.
 * To allow automated RX-TX testing, then if no input is received, then test sends test message
 *
 * Created by Jozef on 12.06.2016.
 */

public class Test_SerialPrinter {
    public static void main(String args[]) {
        System.out.println("Test_Serial");
        Test_SerialPrinter app = new Test_SerialPrinter();
        app.run();
    }

    public synchronized int getStatus(JPigpio pigpio, int handle){
        byte[] buff = new byte[] {27,118,0x04};
        int status = 99;
        try {

            System.out.println("Checking status...");
            pigpio.serialWrite(handle,buff);
            Thread.sleep(1);
            if (pigpio.serialDataAvailable(handle) > 0){
                status = pigpio.serialReadByte(handle);
                if ((status & 0x1) == 0) {
                    //TODO: handle status
                }
            }

        } catch (InterruptedException|PigpioException e) {
            System.out.println(e.toString());
        }
        return status;
    }

    public void run() {

        String host = "pigpiod-host";
        String serialPort = "/dev/ttyUSB1";
        int baudRate = 115200;

        int handle;
        int avail = 0;
        byte[] data;
        long startTime = System.currentTimeMillis();
        int counter = 0;
        int testC = 0;
        int status = 1;
        String rcvd;

        try {
            System.out.println("Opening Pigpio.");
            JPigpio pigpio = new PigpioSocket(host, 8888);
            //JPigpio pigpio = new Pigpio();

            pigpio.gpioInitialize();
            Utils.addShutdown(pigpio);

            System.out.println("Opening serial port "+serialPort);
            handle = pigpio.serialOpen(serialPort, baudRate, 0);

            System.out.println("Initializing printer ");
            pigpio.serialWrite(handle,new byte[] {27,64});

            status = getStatus(pigpio, handle);
            System.out.println("Status = "+Integer.toBinaryString(status));

            System.out.println("Enabling automatic status reporting ");
            pigpio.serialWrite(handle,new byte[] {29,97,2});

            System.out.println("PRINTING ");
            pigpio.serialWrite(handle, "Hello world! \r\n\r\n\r\n".getBytes());

            status = getStatus(pigpio, handle);
            System.out.println("Status = "+Integer.toBinaryString(status));

            System.out.println("Going to wait for 20 for printer status change");
            while (System.currentTimeMillis() - startTime < 20000) {

                //System.out.println(counter++);
                Thread.sleep(1);
                avail = pigpio.serialDataAvailable(handle);
                if (avail > 0) {
                    System.out.println("Status received from printer..."+avail);
                }
            }

            if (avail > 0){
                data = pigpio.serialRead(handle, avail);
                System.out.println("Status: "+Arrays.toString(data));
            }

            status = getStatus(pigpio, handle);
            System.out.println("Status = "+Integer.toBinaryString(status));


            pigpio.serialClose(handle);
            pigpio.gpioTerminate();

        } catch (PigpioException|InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Test Serial Printer Completed.");
    }
}