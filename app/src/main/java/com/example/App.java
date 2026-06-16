package com.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * App entry point
 */
public final class App {
    private App() {
    }

    /**default port is 8080 */
    public static int port = 8080;
    /**8000 is a reserved port */
    public static List<String> reservedPorts = Arrays.asList("8000");
    public static String address = "localhost";
    /**
     * main
     * @param args command line arguments - 'sever' to start the server as admin, no arguement starts the client as a user
     * @throws Exception throws an error if the server doesnt start
     * @throws IOException throws an error if the client doesn't start
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int port = getPort(scanner);
        if (args.length > 0) {
            if (args[0].equals("server")) {
                try {
                    new Server(port, scanner).run();
                } catch (Exception e) {
                    FileHandler.Debug(e.getMessage());
                }
            }
        } else {
            try {
                String username = getUser(scanner);
                new Client(address, port, username, scanner).run();
            } catch (IOException e) {
                FileHandler.Debug(e.getMessage());
            }
        }
    }

    /**
     * asks the user for a port number and checks whether its valid, calls itself if its not
     * @param scanner scanner to read user input
     * @return returns a valid port number
     */
    public static int getPort(Scanner scanner) {
        System.out.print("Enter port number:");
        String portScan = scanner.nextLine();

        /**
         * default port is 8080
         * 8000 is reserved
         */
        if (portScan.isEmpty()) {
            System.out.println("\rUsing default port: " + port);
        } else if(Arrays.asList(reservedPorts).contains(portScan)) {
            System.out.println("That port is reserved");
            port = getPort(scanner);
        } else {
            System.out.println("Using port: " + portScan);
            port = Integer.parseInt(portScan);
        }
        return port;
    }

    /**
     * gets an address from the user and checks its a real ip with regex. if given no address it assumes the default 'localhost'
     * @param scanner gets user input
     * @return ip address
     */
    public static String getAddress(Scanner scanner){
        System.out.print("Enter port number:");
        String ipScan = scanner.nextLine();
        String binaryRegex = "(\\d{1,2}|(0|1)\\" + "d{2}|2[0-4]\\d|25[0-5])";
        String ipRegex = binaryRegex + "\\." + binaryRegex + "\\." + binaryRegex + "\\." + binaryRegex;
        Pattern p = Pattern.compile(ipRegex);

        if (ipScan.isEmpty()) {
            System.out.println("\rUsing default address: " + address);
        } else if (!p.matcher(ipScan).matches()) {
            address = getAddress(scanner);
        } else {
            address = ipScan;
        }
        return address;
    }
    /**
     * asks the user for a username
     * @param scanner scanner for user input
     * @return returns the username, or annonymous if they user doesn't give one
     */
    public static String getUser(Scanner scanner) {
        System.out.print("Enter username:");
        String userScan = scanner.nextLine();
        String username = "anonymous";
        if (!userScan.isEmpty()) {
            username = userScan;
            System.out.println("Using username: " + username);
        } else {
            System.out.println("Continuing as: " + username);
        }
        return username;
    }
}
