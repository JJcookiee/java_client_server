package com.example;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
    /**
     * main
     * @param args command line arguments - 'sever' to start the server as admin, no arguement starts the client
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
                new Client("localhost", port, username, scanner).run();
            } catch (Exception e) {
                FileHandler.Debug(e.getMessage());
            }
        }
    }

    /**
     * getPort
     * @param scanner scanner
     * @return returns the port number
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
     * Username input
     * @param scanner scanner
     * @return returns the username
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
