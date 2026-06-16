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
     * gets the socket address of the client based of the InetAddress and port from toServer. formatted like address:port
     * @return returns socket address based off InetAddress and Port
     */
    public String getSocketAddress() {
        return (toServer.getInetAddress().toString() + ":" + toServer.getPort());
    }

    /**
     * Client runnable
     * Contains threads for {@link WebHandler}, {@link InputHandler}, {@link ResponseHandler}
     * @throws Exception throws exception if the webhandler doesnt run
     */
    public void run() {
        synchronized(this) {
            this.runningThread = Thread.currentThread();
        }

        UIPort = getBrowserPort();
        webHandler = new WebHandler(UIPort, this.username, this);
        webHandler.setPage(this.username);
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
     * closes the toServer socket
     * @throws IOException throws exception if it cant close the socket
     */
    public void close() {
        try {
            toServer.close();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
        }
    }

    /**
     * outputs a message through the socket after checking for its intended receiver
     * @param message the message string
     * @param isWhisper boolean. tags are added to the end of the message. to whisper add '````*tag*' to the end of a message. if not whispered it auto adds '0000' to send to all
     * @version the old one?
     */
    public void sendMessage(String message, boolean isWhisper) {
        String receiver = isWhisper ? "0000" : "0000";

        if (message.contains("->") && message.contains(":")) {

            String[] parts = message.split(":", 2);
            String targetPart = parts[0].trim();
            if (targetPart.startsWith("->")) {
                receiver = targetPart.substring(2);
                message = parts[1].trim();
            }
        }
        sendMessage(message, receiver);
    }

    /**
     * sends a message to the rigth clients
     * @param message the message
     * @param receiver target audience
     */
    public void sendMessage(String message, String receiver) {
        String formattedMessage = username + ": " + message + "````" + receiver;

        try {
            OutputStream out = toServer.getOutputStream();
            out.write(formattedMessage.getBytes());
            out.flush();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
        }
    }

    /**
     * checks the message for banned words and commands
     * Calls the censorMessage function to filter banned words
     * Checks message for any commands, and executes them if so
     * @param message the message string
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
     * gets the input from the server
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
     * gets a port for the browser, iterates until it finds a free one
     * @return a valid port
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
     * checks if a port is free
     * @param port the port to be checked
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
