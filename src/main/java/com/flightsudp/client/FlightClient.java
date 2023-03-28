package com.flightsudp.client;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.flightsudp.server.data.DateTimeString;
import org.json.JSONArray;
import org.json.JSONObject;

public class FlightClient {

    final static String DATE_FORMAT = "yyyy-MM-ddTHH:mm:ss";
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
            } catch (Exception e) {
                socket.close();
                System.exit(0);
            }

            try {
                // Send packet to server
                JSONObject processedResponse = sendAndReceiveDatagramPacket(socket, controller, serverAddress, serverPortNumber, userInput);
                System.out.println("=".repeat(20) + "\nDisplaying Results\n");
                System.out.println(processedResponse.toString());
            } catch (Exception e) {
                System.out.println("=".repeat(20) + "\nDisplaying Results\n");
                System.out.println("ERROR: " + e.getMessage());
            }

            // displayResults(processedResponse, 0);

//            2023-12-12T12:00:00

            if (userInput.getString("function") == "monitorupdates") {
                // TODO keep socket open & keep printing results
                JSONObject data = userInput.getJSONObject("data");
                LocalDateTime expiryDate = new DateTimeString(data.getString("expiryDate")).getLocalDateTime();
                receiveDatagramPackets(socket, controller, expiryDate);
            }
        }
    }

    public static JSONObject getUserInput() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int choice = 0;
        JSONObject params = new JSONObject();
        JSONObject userInput = new JSONObject();

        while (choice <= 0 || choice >= 8) {
            // Read input from user
            System.out.println("=".repeat(40) + "\n1: Find flight by source and destination\n" +
                    "2: Find flight by flightID\n3: Reserve Flight\n4: Monitor Flight\n5: List Flights\n" +
                    "6: Cancel Flight\n7: Quit");
            System.out.print("Enter your choice: ");
            try {
                choice = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input, please try again...");
                continue;
            }

            switch (choice) {
                case 1: {
                    userInput.put("function", "flightids");
                    while (true) {
                        System.out.print("Enter source: ");
                        String source = reader.readLine();
                        System.out.print("Enter destination: ");
                        String destination = reader.readLine();
                        if (!source.isEmpty() && !destination.isEmpty()) {
                            params.put("source", source);
                            params.put("destination", destination);
                            break;
                        } else {
                            System.out.println("Invalid input, please try again...");
                        }
                    }
                    break;
                }
                case 2: {
                    userInput.put("function", "flightdetails");
                    while (true) {
                        System.out.print("Enter flight ID: ");
                        String flightID = reader.readLine();
                        Boolean invalidInput;
                        try {
                            invalidInput = Boolean.FALSE;
                            Integer.parseInt(flightID);
                        } catch (NumberFormatException ex) {
                            invalidInput = Boolean.TRUE;
                        }
                        if (!flightID.isEmpty() && !invalidInput) {
                            params.put("flightid", flightID);
                            break;
                        } else {
                            System.out.println("Invalid input, please try again...");
                        }
                    }
                    break;
                }
                case 3: {
                    userInput.put("function", "flightreservation");
                    while (true) {
                        System.out.print("Enter flight ID: ");
                        String flightID = reader.readLine();
                        System.out.print("Enter number of seats to reserve: ");
                        String numSeats = reader.readLine();
                        Boolean invalidInput;
                        try {
                            invalidInput = Boolean.FALSE;
                            Integer.parseInt(flightID);
                            Integer.parseInt(numSeats);
                        } catch (NumberFormatException ex) {
                            invalidInput = Boolean.TRUE;
                        }

                        if (!flightID.isEmpty() && !numSeats.isEmpty() && !invalidInput) {
                            params.put("flightid", flightID);
                            params.put("numSeats", numSeats);
                            break;
                        } else {
                            System.out.println("Invalid input, please try again...");
                        }
                    }
                    break;
                }
                case 4: {
                    userInput.put("function", "monitorupdates");
                    while (true) {
                        System.out.print("Enter flight ID: ");
                        String flightID = reader.readLine();
                        System.out.print("Enter expiry date i.e. 2022-04-01T12:00:00: ");
                        String expiryDate = reader.readLine();

                        Boolean invalidInput;
                        Boolean invalidDate;
                        try {
                            invalidInput = Boolean.FALSE;
                            invalidDate = Boolean.FALSE;
                            Integer.parseInt(flightID);
                            LocalDateTime localDateTime = new DateTimeString(expiryDate).getLocalDateTime();
                            // SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
                            // Date date = formatter.parse(expiryDate);
                        } catch (Exception e) {
                            invalidInput = Boolean.TRUE;
                            invalidDate = Boolean.TRUE;
                        }
                        if (!flightID.isEmpty() && !expiryDate.isEmpty() && !invalidDate && !invalidInput) {
                            params.put("flightid", flightID);
                            params.put("expiryDate", expiryDate);
                            break;
                        } else {
                            System.out.println("Invalid input, please try again...");
                        }
                    }
                    break;
                }
                case 5: {
                    userInput.put("function", "listFlights");
                    break;
                }
                case 6: {
                    userInput.put("function", "flightcancellation");
                    while (true) {
                        System.out.print("Enter flight ID: ");
                        String flightID = reader.readLine();
                        System.out.print("Enter number of seats to cancel: ");
                        String numSeats = reader.readLine();
                        Boolean invalidInput;
                        try {
                            invalidInput = Boolean.FALSE;
                            Integer.parseInt(flightID);
                            Integer.parseInt(numSeats);
                        } catch (NumberFormatException ex) {
                            invalidInput = Boolean.TRUE;
                        }

                        if (!flightID.isEmpty() && !numSeats.isEmpty() && !invalidInput) {
                            params.put("flightid", flightID);
                            params.put("numSeats", numSeats);
                            break;
                        } else {
                            System.out.println("Invalid input, please try again...");
                        }
                    }
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

    public static JSONObject getUserChoiceOnPacketLoss(JSONObject userInput) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            System.out.print("Simulate packet loss from client to server? Enter Y/N: ");
            String choice = reader.readLine();
            if (Objects.equals(choice, "Y") || Objects.equals(choice, "y")) {
                userInput.put("packetLossClientToServer", Boolean.TRUE);
                userInput.put("packetLossServerToClient", Boolean.FALSE);
                return userInput;

            } else if (Objects.equals(choice, "N") || Objects.equals(choice, "n")) {
                userInput.put("packetLossClientToServer", Boolean.FALSE);
                while(true) {
                    System.out.print("Simulate packet loss from server to client? Enter Y/N: ");
                    choice = reader.readLine();
                    if (Objects.equals(choice, "Y") || Objects.equals(choice, "y")) {
                        userInput.put("packetLossServerToClient", Boolean.TRUE);
                        return userInput;
                    } else if (Objects.equals(choice, "N") || Objects.equals(choice, "n")) {
                        userInput.put("packetLossServerToClient", Boolean.FALSE);
                        return userInput;
                    } else {
                        System.out.println("Invalid input, please try again...");
                    }
                }

            } else {
                System.out.println("Invalid input, please try again...");
            }
        }
    }

    public static JSONObject sendAndReceiveDatagramPacket(DatagramSocket socket, FlightClientController controller, String serverAddress, int serverPortNumber, JSONObject userInput) throws Exception {
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
            while (true) {
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
            throw e;
        }

        JSONObject processedResponse = new JSONObject();
        try {
            // Process response using FlightClientController object
            processedResponse = controller.processResponse(responsePacket);
        } catch (Exception e) {
            throw e;
        }

        return processedResponse;
    }

    public static void receiveDatagramPackets(DatagramSocket socket, FlightClientController controller, LocalDateTime expiryDate) {
        byte[] responseBytes = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        while (LocalDateTime.now().isBefore(expiryDate)) {
            try {
                socket.receive(responsePacket);
                JSONObject processedResponse = new JSONObject();
                try {
                    // Process response using FlightClientController object
                    processedResponse = controller.processResponse(responsePacket);
                } catch (Exception e) {
                    System.out.println(e);
                }

                System.out.println("=".repeat(20) + "\nUpdate Received\n");
                displayResults(processedResponse, 0);
            } catch (Exception e) {
                // System.out.println(e);
            }
        }
    }

    public static void displayResults(JSONObject jsonObj, int nest) {
        for (String key : jsonObj.keySet()) {
            System.out.println(" ".repeat(2 * (nest)) + key);

            if (jsonObj.get(key) instanceof JSONArray) {
                JSONArray value = jsonObj.getJSONArray(key);
                for (int i = 0; i < value.length(); i++) {
                    JSONObject objectInArray = value.getJSONObject(i);
                    String[] elementNames = JSONObject.getNames(objectInArray);
                    for (int j = elementNames.length - 1; j > 0; j--) {
                        String elementName = elementNames[j];
                        String elementValue = String.valueOf(objectInArray.get(elementName));
                        System.out.print(" ".repeat(2 * (nest + 1)) + elementName + ": ");
                        System.out.println(elementValue);
                    }
                    System.out.println();
                }
            }
            //for nested objects iteration if required
            else if (jsonObj.get(key) instanceof JSONObject) {
                JSONObject value = jsonObj.getJSONObject(key);
                displayResults(value, nest + 1);
            } else
                System.out.println(" ".repeat(2 * (nest + 1)) + jsonObj.get(key));
        }
    }
}

