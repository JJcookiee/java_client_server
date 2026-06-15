package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    
    protected Socket toServer;
    protected String username = "anonymous";
    protected int UIPort = 8000;
    protected WebHandler webHandler;
    protected Scanner scanner;
    protected Thread runningThread = null;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(2);

    Client(String serverSocket, int port, String username, Scanner scanner) throws IOException {
        toServer = new Socket(serverSocket, port);
        this.username = username;
        this.scanner = scanner;
    }

    public String getSocketAddress() {
        return (toServer.getInetAddress().toString() + ":" + toServer.getPort());
    }

    public void run() {
        synchronized(this) {
            this.runningThread = Thread.currentThread();
        }

        UIPort = getBrowserPort();
        webHandler = new WebHandler(UIPort, this.username, this);
        try {
            webHandler.main(new String[0]);
        } catch (Exception e) {
            FileHandler.Debug(e.getMessage());
        }
        
        this.threadPool.execute(
            new InputHandler(
                this
            ));

        this.threadPool.execute(
            new ResponseHandler(
                this,
                null
            ));
    }

    public void close() {
        try {
            toServer.close();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
        }
    }

    public void sendMessage(String message, boolean isWhisper) {
        if (!isWhisper) {
            message = message + "````" + "0000";
        }

        message = username + ": " + message;

        try {
            OutputStream out = toServer.getOutputStream();
            out.write(message.getBytes());
            out.flush();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
        }
    }

    public void checkMessage(String message) {
        if (message.startsWith("/")) {
            String[] parts = message.split(" ");
            switch (parts[0]) {
                case "/exit":
                    close();
                    System.exit(0);
                    break;
                case "/help":
                    System.out.println("Available commands:\n/exit - Exit the application\n/help - Show this help message\n/msg <tag> <message> - Send a private message to a user with the specified tag. e.g. /msg 1234 Hello there!");
                    break;
                case "/msg":
                    if (parts.length < 3) {
                        System.out.println("Usage: /msg <tag> <message>");
                    } else {
                        String targetUser = parts[1];
                        String privateMessage = String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length));
                        sendMessage("->" + targetUser + ": " + privateMessage + "````" + targetUser, true);
                    }
                    break;
                default:
                    System.out.println("Unknown command. Type /help for a list of commands.");
            }
        } else {
            sendMessage(message, false);
        }   
    }

    public String getPage() {
        try {
            InputStream in = toServer.getInputStream();
            byte[] buffer = new byte[8192];
            StringBuilder pageBuilder = new StringBuilder();
            int bytesRead;
            
            while((bytesRead = in.read(buffer)) != -1) {
                String page = new String(buffer, 0, bytesRead);
                //System.out.println("Raw page response: " + page);//debug
                pageBuilder.append(page);
                if (in.available() == 0) {
                    break; 
                }

            }

            //SSystem.out.println("Raw response: " + new String(buffer, 0, bytesRead));//debug
            //in.close();
            return pageBuilder.toString();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
            return null;
        }
    }
    
    public int getBrowserPort() {
        while (true) {
            if (isPortFree(UIPort)) {
                return UIPort;
            }
            UIPort++;
        }
    }

    private boolean isPortFree(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            FileHandler.Debug(e.getMessage());
            return false;
        }
    }
}
