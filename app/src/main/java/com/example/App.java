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

    public static int port = 8080;
    public static List<String> reservedPorts = Arrays.asList("8000");
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int port = getPort(scanner);
        if (args.length > 0) {
            if (args[0].equals("server")) {
                try {
                    new Server(port).run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\nServer started on port: " + port);
        } else {
            try {
                String username = getUser(scanner);
                new Client("localhost", port, username, scanner).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getPort(Scanner scanner) {
        System.out.print("Enter port number:");
        String portScan = scanner.nextLine();

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
