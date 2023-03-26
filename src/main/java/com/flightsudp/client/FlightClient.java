package com.flightsudp.client;

import java.io.*;
import java.net.*;
import java.util.Objects;

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

        while (true) {
            JSONObject userInput = getUserInput();
            // Create FlightClientController object for processing request and response
            FlightClientController controller = new FlightClientController();
            // Generate request bytes using FlightClientController object
            byte[] requestBytes = controller.generateRequest(userInput);
            // Send packet to server
            DatagramPacket responsePacket = sendAndReceiveDatagramPacket(serverAddress, serverPortNumber, requestBytes, userInput.getString("semantics"));
            // Process response using FlightClientController object
            JSONObject processedResponse = null;
            try {
                processedResponse = controller.processResponse(responsePacket);
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("=".repeat(20) + "\nDisplaying Results\n");
            displayResults(processedResponse, 0);
        }
    }

    public static JSONObject getUserInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int choice = 0;
        JSONObject params = new JSONObject();
        JSONObject userInput = new JSONObject();

        while (choice<=0 || choice>=9) {
            // Read input from user
            System.out.println("=".repeat(40) + "\n1: Find flight by source and destination\n" +
                    "2: Find flight by flightID\n3: Reserve Flight\n4: Monitor Flight\n5: Get Cheaper Flights\n" +
                    "6: Cancel Flight\n7: Quit");
            System.out.print("Enter your choice: ");
            choice = Integer.parseInt(reader.readLine());

            switch (choice) {
                case 1: {
                    userInput.put("function", "flightids");
                    System.out.print("Enter source: ");
                    params.put("source", reader.readLine());
                    System.out.print("Enter destination: ");
                    params.put("destination", reader.readLine());
                    break;
                }
                case 2: {
                    userInput.put("function", "flightdetails");
                    System.out.print("Enter flight ID: ");
                    params.put("flightid", reader.readLine());
                    break;
                }
                case 3: {
                    userInput.put("function", "flightreservation");
                    System.out.print("Enter flight ID: ");
                    params.put("flightid", reader.readLine());
                    System.out.print("Enter number of seats to reserve: ");
                    params.put("numSeats", reader.readLine());
                    break;
                }
                case 4: {
                    userInput.put("function", "monitorupdates");
                    System.out.print("Enter flight ID: ");
                    params.put("flightid", reader.readLine());
                    System.out.print("Enter monitor duration: ");
                    params.put("duration", reader.readLine());
                    break;
                }
                case 5: {
                    userInput.put("function", "getcheaper");
                    System.out.print("Enter airfare: ");
                    params.put("airfare", reader.readLine());
                    break;
                }
                case 6: {
                    userInput.put("function", "flightcancellation");
                    System.out.print("Enter flight ID: ");
                    params.put("flightid", reader.readLine());
                    System.out.print("Enter number of seats to cancel: ");
                    params.put("numSeats", reader.readLine());
                    break;
                }
                case 7: {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
                case 8: {
                    userInput.put("function", "listFlights");
                    break;
                }
                default: {
                    System.out.println("Invalid number, please try again...");
                }
            }
        }

        userInput.put("data", params);

        System.out.print("=".repeat(20) + "\n1: at-least-once\n2: at-most-once\nEnter invocation semantics: ");
        if (Objects.equals(reader.readLine(), "1")) {
            userInput.put("semantics", "AT-LEAST-ONCE");
        } else {
            userInput.put("semantics", "AT-MOST-ONCE");
        }

        System.out.print("Simulate packet loss? Enter Y/N: ");
        if (Objects.equals(reader.readLine(), "Y")) {
            userInput.put("packetLoss", Boolean.TRUE);
        } else {
            userInput.put("packetLoss", Boolean.FALSE);
        }
        System.out.println("=".repeat(20));
        return userInput;
    }

    public static DatagramPacket sendAndReceiveDatagramPacket(String serverAddress, int serverPortNumber, byte[] requestBytes, String semantics) throws SocketException, UnknownHostException {
        // Create socket and packet for sending and receiving data
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
        byte[] responseBytes = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);;

        try {
            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverInetAddress, serverPortNumber);
            socket.send(requestPacket);

            System.out.println("Sending packet");

            socket.setSoTimeout(5000);

            // Receive response packet
            // If no response received within 5s and semantics is at-least-once, resend packet
            while(true) {
                try {
                    socket.receive(responsePacket);
                    break;
                } catch (SocketTimeoutException e) {
                    if (semantics == "AT-LEAST-ONCE") {
                        // Resend request packet after timeout
                        socket.send(requestPacket);
                        System.out.println("Sending packet");
                    } else {
                        System.out.println("No response received from server");
                        return null;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        socket.close();
        return responsePacket;
    }

    public static void displayResults(JSONObject jsonObj, int nest) {
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
                displayResults(value, nest + 1);
            }
            else
                System.out.println(" ".repeat(2*(nest+1)) + jsonObj.get(key));
        }
    }
}
