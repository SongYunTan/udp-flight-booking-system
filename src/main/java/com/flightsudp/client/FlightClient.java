package com.flightsudp.client;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class FlightClient {
    enum Semantics{
        AT_LEAST_ONCE,
        AT_MOST_ONCE
    }

    public static void main(String[] args) throws IOException {
        // Check for correct command line arguments
        if (args.length != 2) {
            System.err.println("Usage: java FlightClient <serverAddress> <portNumber>");
            System.exit(1);
        }

        // Parse command line arguments
        String serverAddress = args[0];
        int serverPortNumber = Integer.parseInt(args[1]);

        // Loop to read input, send packet, receive response, and process and display response
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            // Read input from user
            System.out.println("1: Find flight by source and destination\n2: Find flight by flightID\n" +
                    "3: Reserve Flight\n4: Monitor Flight\n5: Get Cheaper Flights\n" +
                    "6: Cancel Flight\n7: Quit");
            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(reader.readLine());

            String functionName = "";
            JSONObject params = new JSONObject();

            switch (choice) {
                case 1: {
                    functionName = "flightids";
                    System.out.print("Enter source: ");
                    params.put("source", reader.readLine());
                    System.out.print("Enter destination: ");
                    params.put("destination", reader.readLine());
                    break;
                }
                case 2: {
                    functionName = "flightdetails";
                    System.out.print("Enter flight ID: ");
                    params.put("flightid", reader.readLine());
                    break;
                }
                case 3: {
                    functionName = "flightreservation";
                    System.out.print("Enter flight ID: ");
                    params.put("flightid", reader.readLine());
                    System.out.print("Enter number of seats to reserve: ");
                    params.put("numSeats", reader.readLine());
                    break;
                }
                case 4: {
                    functionName = "monitorupdates";
                    System.out.print("Enter flight ID: ");
                    params.put("flightid", reader.readLine());
                    System.out.print("Enter monitor duration: ");
                    params.put("duration", reader.readLine());
                    break;
                }
                case 5: {
                    functionName = "getcheaper";
                    System.out.print("Enter airfare: ");
                    params.put("airfare", reader.readLine());
                    break;
                }
                case 6: {
                    functionName = "flightcancellation";
                    System.out.print("Enter flight ID: ");
                    params.put("flightid", reader.readLine());
                    System.out.print("Enter number of seats to cancel: ");
                    params.put("numSeats", reader.readLine());
                    break;
                }
                case 7: {
                    System.out.println("Exiting...");
                    break;
                }
                case 8: {
                    functionName= "listFlights";
                    break;
                }
                default: {
                    System.out.println("Invalid number, please try again...");
                    continue;
                }
            }

            if (choice == 7) {
                break;
            }

            Semantics semantics;
            System.out.print("1: at-least-once\n2: at-most-once\nEnter invocation semantics: ");
            if (Objects.equals(reader.readLine(), "1")) {
                semantics = Semantics.AT_LEAST_ONCE;
            } else {
                semantics = Semantics.AT_MOST_ONCE;
            }


            Boolean packetLoss;
            System.out.print("Simulate packet loss? Enter Y/N: ");
            if (Objects.equals(reader.readLine(), "Y")) {
                packetLoss = Boolean.TRUE;
            } else {
                packetLoss = Boolean.FALSE;
            }

            JSONObject processedResponse = sendAndReceiveDatagramPacket(serverAddress, serverPortNumber, functionName, params, semantics, packetLoss);
            // Display processed response
            printJsonObject(processedResponse, 0);
            // System.out.println("Response: " + processedResponse);
        }
    }

    public static void printJsonObject(JSONObject jsonObj, int nest) {
        for (String key : jsonObj.keySet()) {
            System.out.println(" ".repeat(2*(nest)) + key);

            if (jsonObj.get(key) instanceof JSONArray) {
                JSONArray value = jsonObj.getJSONArray(key);
                for(int i = 0; i < value.length(); i++)
                {
                    JSONObject objectInArray = value.getJSONObject(i);
                    String[] elementNames = JSONObject.getNames(objectInArray);
                    for (int j = elementNames.length-1; j > 0; j--)
                    {
                        String elementName = elementNames[j];
                        String elementValue = String.valueOf(objectInArray.get(elementName));
                        System.out.print(" ".repeat(2*(nest+1)) + elementName + ": ");
                        System.out.println(elementValue);
                    }
                    System.out.println();
                }
            }
            //for nested objects iteration if required
            else if (jsonObj.get(key) instanceof JSONObject) {
                JSONObject value = jsonObj.getJSONObject(key);
                printJsonObject(value, nest + 1);
            }
            else
                System.out.println(" ".repeat(2*(nest+1)) + jsonObj.get(key));
        }
    }

    public static JSONObject sendAndReceiveDatagramPacket(String serverAddress, int serverPortNumber, String functionName, JSONObject params, Semantics semantics, Boolean packetLoss) throws SocketException, UnknownHostException {
        // Create socket and packet for sending and receiving data
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
        byte[] requestBytes = new byte[1024];
        byte[] responseBytes = new byte[1024];

        // Create FlightClientController object for processing request and response
        FlightClientController controller = new FlightClientController();
        JSONObject processedResponse = null;

        try {
            // Generate request bytes using FlightClientController object and send packet to server
            requestBytes = controller.generateRequest(functionName, params, semantics, packetLoss);
            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverInetAddress, serverPortNumber);
            socket.send(requestPacket);

            System.out.println("Sending packet");

            socket.setSoTimeout(5000);

            // Receive response packet
            DatagramPacket responsePacket;

            while(true) {
                responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
                try {
                    socket.receive(responsePacket);
                    break;
                } catch (SocketTimeoutException e) {
                    if (semantics == Semantics.AT_LEAST_ONCE) {
                        // Resend request packet after timeout
                        socket.send(requestPacket);
                        System.out.println("Sending packet");
                    } else {
                        System.out.println("No response received from server");
                        return null;
                    }
                }
            }

            // Process response using FlightClientController object
            try {
                processedResponse = controller.processResponse(responsePacket);
            } catch (Exception e) {
                System.out.println(e);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        socket.close();
        return processedResponse;
    }
}
