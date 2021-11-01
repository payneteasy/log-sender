package com.payneteasy.telnet;

import java.io.IOException;
import java.net.*;


public class TelnetServerApplication {

    public static void tcp() {
//        ServerSocket
    }
    public static void main(String[] args) throws IOException {

        DatagramSocket socket = new DatagramSocket(8400);
        String message = "<event>\n" +
                "\t<active>search</active>\n" +
                "\t<type>ack</type>\n" +
                "\t<id>20001</id>\n" +
                "\t<ip>10.0.2.127</ip>\n" +
                "\t<mac>B8:E8:56:2B:0F:E6</mac>\n" +
                "</event>";
        byte[] bytes = message.getBytes();
        socket.send(new DatagramPacket(bytes, bytes.length, new InetSocketAddress("10.0.2.159", 8400)));
        socket.close();
    }
}
