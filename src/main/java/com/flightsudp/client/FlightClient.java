package com.flightsudp.client;

import java.io.*;
import java.net.*;

public class FlightClient {
    public static void main(String[] args) throws IOException {
        // Check for correct command line arguments
        if (args.length != 2) {
            System.err.println("Usage: java FlightClient <serverAddress> <portNumber>");
            System.exit(1);
        }

        // TODO: SONGYUN include semantics flag in command line args (at-most-once, at-least-once)
        // TODO: SONGYUN simulate packet loss

        // Parse command line arguments
        String serverAddress = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // Create socket and packet for sending and receiving data
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        // Create FlightClientController object for processing input and responses
        FlightClientController controller = new FlightClientController();

        // Loop to read input, send packet, receive response, and process and display response
        while (true) {
            // Read input from user
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter input: ");
            String input = reader.readLine();

            if (input == "quit") {
                break;
            }

            try {
                // Process input using FlightClientController object and send packet to server
                String processedInput = controller.processInput(input);
                sendData = processedInput.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverInetAddress, portNumber);
                socket.send(sendPacket);

                //TODO: SONGYUN timeout on response

                // Receive response packet and process response using FlightClientController object
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                try {
                    String processedResponse = controller.processResponse(receivePacket);
                    // Display processed response
                    // TODO: SONGYUN prettify response display
                    System.out.println("Response: " + processedResponse);

                } catch (Exception e) {
                    System.out.println(e);
                }

            } catch (Exception e){
                System.out.println(e);
            }
        }

        System.out.println("Exiting...");
        socket.close();
    }
}
