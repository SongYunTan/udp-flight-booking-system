package com.flightsudp.server;

import java.net.*;
import java.io.*;

public class FlightServer {
    public static void main(String[] args) throws IOException {
        // Check for correct command line arguments
        if (args.length != 1) {
            System.err.println("Usage: java FlightServer <portNumber>");
            System.exit(1);
        }

        // Parse command line argument
        int portNumber = Integer.parseInt(args[0]);

        // Create socket and packet for receiving and sending data
        DatagramSocket socket = new DatagramSocket(portNumber);
        
        System.out.println("Server started at port " + args[0]);
        
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        // Create FlightServerController object for processing input and generating responses
        FlightServerController controller = new FlightServerController();

        // Loop to receive and process incoming packets and send responses
        while (true) {
            // Receive incoming packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String input = new String(receivePacket.getData()).trim();

            if (input == "shutdown") {
                break;
            }

            try {
                InetAddress clientAddress = receivePacket.getAddress();
                Integer clientPort = receivePacket.getPort();
                String response = controller.processInput(input, clientAddress, clientPort);

                // Convert response to bytes and send response packet back to client
                sendData = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                socket.send(sendPacket);
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        System.out.println("Server exiting....");
        socket.close();
    }
}

