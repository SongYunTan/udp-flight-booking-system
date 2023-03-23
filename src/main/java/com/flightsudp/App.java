package com.flightsudp;

import java.io.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("Enter input: ");
            String input = reader.readLine();
        
        String[] tokens = input.split(" ");
        String functionName = tokens[0];
        JSONObject params = new JSONObject();
        for (int i = 1; i < tokens.length; i += 2) {
            String paramName = tokens[i];
            String paramValue = tokens[i+1];
            params.put(paramName, paramValue);
        }

        // Construct request JSON with function name and parameters
        JSONObject requestJson = new JSONObject();
        requestJson.put("function", functionName);
        requestJson.put("params", params);

        // Convert request JSON to byte array
        String requestData = requestJson.toString()

        System.out.println("Response: " + requestData);
    }
}
