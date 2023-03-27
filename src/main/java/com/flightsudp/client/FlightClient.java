package com.flightsudp.client;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

public class FlightClient {

    public static void main(String[] args) throws IOException {
        // Check for correct command line arguments
        if (args.length != 2) {
            System.err.println("Usage: java FlightClient <serverAddress> <portNumber>");
            System.exit(1);
        }

        // Parse command line arguments
        String serverAddress = args[0];
        int serverPortNumber = Integer.parseInt(args[1]);

        // Create FlightClientController object for processing request and response
        FlightClientController controller = new FlightClientController();

        // Create socket and packet for sending and receiving data
        DatagramSocket socket = new DatagramSocket();

        // Loop to read input, send packet, receive response, and process and display response
        JSONObject userInput = new JSONObject();
        while (true) {
            try {
                userInput = getUserInput();
            }
            catch (Exception e) {
                socket.close();
                System.exit(0);
            }
            // Send packet to server
            JSONObject processedResponse = sendAndReceiveDatagramPacket(socket, controller, serverAddress, serverPortNumber, userInput);
            System.out.println("=".repeat(20) + "\nDisplaying Results\n");
            displayResults(processedResponse, 0);

            if (userInput.getString("function") == "monitorupdates") {
                // TODO keep socket open & keep printing results
                LocalTime expiryDate = LocalTime.parse(userInput.getString("expiryDate"));
                receiveDatagramPackets(socket, controller, expiryDate);
            }
        }
    }

    public static JSONObject getUserInput() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int choice = 0;
        JSONObject params = new JSONObject();
        JSONObject userInput = new JSONObject();

        while (choice<=0 || choice>=8) {
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
                    System.out.print("Enter expiry date i.e. 2022-04-01T12:00:00: ");
                    params.put("expiryDate", reader.readLine());
                    break;
                }
                case 5: {
                    userInput.put("function", "listFlights");
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
                    throw new Exception("Exit System");
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

        userInput = getUserChoiceOnPacketLoss(userInput);
        System.out.println("=".repeat(20));
        return userInput;
    }

    public static JSONObject getUserChoiceOnPacketLoss (JSONObject userInput) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Simulate packet loss from client to server? Enter Y/N: ");
        if (Objects.equals(reader.readLine(), "Y")) {
            userInput.put("packetLossClientToServer", Boolean.TRUE);
            userInput.put("packetLossServerToClient", Boolean.FALSE);
        } else {
            userInput.put("packetLossClientToServer", Boolean.FALSE);
            System.out.print("Simulate packet loss from server to client? Enter Y/N: ");
            if (Objects.equals(reader.readLine(), "Y") || Objects.equals(reader.readLine(), "y")) {
                userInput.put("packetLossServerToClient", Boolean.TRUE);
            } else {
                userInput.put("packetLossServerToClient", Boolean.FALSE);
            }
        }

        return userInput;
    }

    public static JSONObject sendAndReceiveDatagramPacket(DatagramSocket socket, FlightClientController controller, String serverAddress, int serverPortNumber, JSONObject userInput) throws SocketException, UnknownHostException {
        InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
        byte[] responseBytes = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        // Generate request bytes using FlightClientController object
        JSONObject requestJSON = controller.obtainUUID(userInput);
        byte[] requestBytes = controller.generateRequest(requestJSON);

        try {
            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverInetAddress, serverPortNumber);
            socket.send(requestPacket);
            System.out.println("Sending packet");
            socket.setSoTimeout(5000);

            // Receive response packet
            // If no response received within 5s, resend packet
            while(true) {
                try {
                    socket.receive(responsePacket);
                    break;
                } catch (SocketTimeoutException e) {
                    // Resend request packet after timeout
                    System.out.println("No response received, resending packet...");
                    requestJSON = getUserChoiceOnPacketLoss(requestJSON);

                    requestBytes = controller.generateRequest(requestJSON);
                    requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverInetAddress, serverPortNumber);

                    System.out.println("Sending packet");
                    socket.send(requestPacket);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        JSONObject processedResponse = new JSONObject();
        try {
            // Process response using FlightClientController object
            processedResponse = controller.processResponse(responsePacket);
        } catch (Exception e) {
            System.out.println(e);
        }

        return processedResponse;
    }

    public static void receiveDatagramPackets(DatagramSocket socket, FlightClientController controller, LocalTime expiryDate) {
        byte[] responseBytes = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        while(LocalTime.now().isBefore(expiryDate)) {
            try {
                socket.receive(responsePacket);
                JSONObject processedResponse = new JSONObject();
                try {
                    // Process response using FlightClientController object
                    processedResponse = controller.processResponse(responsePacket);
                } catch (Exception e) {
                    System.out.println(e);
                }

                System.out.println("=".repeat(20)+ "\nUpdate Received\n");
                displayResults(processedResponse, 0);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
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
