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

        // Parse command line arguments
        String serverAddress = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // Create socket and packet for sending and receiving data
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
        byte[] sendData = new byte[65535];
        byte[] receiveData = new byte[65535];

        // Create FlightClientController object for processing input and responses
        FlightClientController controller = new FlightClientController();

        // Loop to read input, send packet, receive response, and process and display response
        while (true) {
            // Read input from user
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter input: ");
            String input = reader.readLine();

            // Process input using FlightClientController object
            Byte[] processedInput = controller.processInput(input);
            
            UUID uuid = 

            // Convert processed input to bytes and send packet to server
            DatagramPacket sendPacket = new DatagramPacket(processedInput, processedInput.length, serverInetAddress, portNumber);
            socket.send(sendPacket);

            // Receive response packet and process response using FlightClientController object
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData());
            String processedResponse = controller.processResponse(response);

            // Display processed response
            System.out.println("Response: " + processedResponse);
        }
    }
}
