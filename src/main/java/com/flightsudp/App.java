package com.flightsudp;

import org.json.JSONObject;
import java.io.*;
import java.net.InetAddress;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try {
            InetAddress local = InetAddress.getLocalHost();
            System.out.println(local);
        } catch (Exception e) {
            //TODO: handle error
        }
    }
}
