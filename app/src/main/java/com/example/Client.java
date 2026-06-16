package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client class
 */
public class Client {
    
    protected Socket toServer;
    protected String username = "anonymous";
    protected int UIPort = 8000;
    protected WebHandler webHandler;
    protected Scanner scanner;
    protected Thread runningThread = null;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(2);
    protected WordFilter wordFilter = new WordFilter();

    /**
     * Client constructor
     * @param serverSocket
     * @param port
     * @param username
     * @param scanner
     * @throws IOException
     */
    Client(String serverSocket, int port, String username, Scanner scanner) throws IOException {
        toServer = new Socket(serverSocket, port);
        this.username = username;
        this.scanner = scanner;
    }

    /**
     * getSocketAddress
     * @return returns socket address based off InetAddress and Port
     */
    public String getSocketAddress() {
        return (toServer.getInetAddress().toString() + ":" + toServer.getPort());
    }

    /**
     * Client runnable
     * Contains threads for webHandler, InputHandler, ResponseHandler
     */
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

    /**
     * close
     * Closes socket
     */
    public void close() {
        try {
            toServer.close();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
        }
    }

    /**
     * sendMessage
     * @param message String
     * @param isWhisper boolean. tags are added to the end of the message. to whisper add '````*tag*' to the end of a message. if not whispered it auto adds '0000' to send to all
     */
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

    /**
     * checkMessage
     * Calls the censorMessage function to filter banned words
     * Checks message for any commands, and executes them if so
     * @param message String
     */
    public void checkMessage(String message) {
        message = wordFilter.censorMessage(message);
        if (message.startsWith("/")) {
            String[] parts = message.split(" ");
            switch (parts[0]) {
                case "/exit" -> {
                    close();
                    System.exit(0);
                }
                case "/help" -> System.out.println("Available commands:\n/exit - Exit the application\n/help - Show this help message\n/msg <tag> <message> - Send a private message to a user with the specified tag. e.g. /msg 1234 Hello there!");
                case "/msg" -> {
                    if (parts.length < 3) {
                        System.out.println("Usage: /msg <tag> <message>");
                    } else {
                        String targetUser = parts[1];
                        String privateMessage = String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length));
                        sendMessage("->" + targetUser + ": " + privateMessage + "````" + targetUser, true);
                    }
                }
                default -> System.out.println("Unknown command. Type /help for a list of commands.");
            }
        } else {
            sendMessage(message, false);
        }   
    }

    /**
     * getPage
     * @return returns page from input strems, returns null if it cant read the input stream
     * @exception IOexception input error
     */
    public String getPage() {
        try {
            InputStream in = toServer.getInputStream();
            byte[] buffer = new byte[8192];
            StringBuilder pageBuilder = new StringBuilder();
            int bytesRead;
            
            while((bytesRead = in.read(buffer)) != -1) {
                String page = new String(buffer, 0, bytesRead);
                pageBuilder.append(page);
                if (in.available() == 0) {
                    break; 
                }

            }
            return pageBuilder.toString();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
            return null;
        }
    }
    
    /**
     * getBrowserPort
     * iterates until it finds a free port
     * @return a port number for the client UI
     */
    public int getBrowserPort() {
        while (true) {
            if (isPortFree(UIPort)) {
                return UIPort;
            }
            UIPort++;
        }
    }

    /**
     * isPortFree
     * checks if a port is free
     * @param port
     * @return boolean, true if port is free, fasle if not
     */
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
