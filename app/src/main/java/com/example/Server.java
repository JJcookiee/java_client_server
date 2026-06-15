package com.example;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    protected int serverPort = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);
    protected ArrayList<Message> messageCache = new ArrayList<>();
    protected ArrayList<String> clientTags = new ArrayList<>();
    protected ArrayList<OutputStream> clientOutputStreams = new ArrayList<>();
    protected ArrayList<Socket> clientSockets = new ArrayList<>();
    protected ServerLog serverLog = null;
    protected Logger logger = null;
    protected Scanner scanner;

    public Server(int port, Scanner scanner) {
        this.serverPort = port;
        this.scanner = scanner;

        this.threadPool.execute(new InputHandler(this));
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();

        LocalTime time = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        serverLog = new ServerLog(time.toString(), messageCache, clientSockets);
        logger = new Logger(serverLog);
        logger.startLogging();

        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
                clientSockets.add(clientSocket);
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    break;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }

            this.threadPool.execute(
                new ServerRunnable(
                    clientSocket, messageCache, clientTags, clientOutputStreams, serverLog));
        }
        logger.stopLogging();
        this.threadPool.shutdown();
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + serverPort, e);
        }
    }

    public void checkCommand(String c) {
        String[] parts = c.split(" ", 2);
        String command = parts[0];
        String argument = parts.length > 1 ? parts[1] : null;
        switch (command) {
            case "stop" -> {
                System.out.println("Stopping server...");
                this.stop();
            }
            case "start" -> {
                if (this.isStopped()) {
                    serverPort = argument != null ? Integer.parseInt(argument) : App.getPort(scanner);
                    System.out.println("Starting server...");
                    this.run();
                } else {
                    System.out.println("Server is already running. Type stop to stop the server.");
                }
            }
            case "status" -> {
                System.out.println("Server is running on port: " + serverPort);
                System.out.println("Connected clients: " + clientSockets.size());
            }
            case "log" -> {
                System.out.println(serverLog.getString());
            }
            case "ls" -> {
                File folder = new File("files");
                File[] files = folder.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        System.out.println(file.getName());
                    }
                }
            }
            case "cat" -> {
                if (argument != null) {
                    File file = new File("files/" + argument);
                    if (file.exists() && file.isFile()) {
                        try (Scanner fileScanner = new Scanner(file)) {
                            while (fileScanner.hasNextLine()) {
                                System.out.println(fileScanner.nextLine());
                            }
                        } catch (IOException e) {
                            System.out.println("Error reading file: " + e.getMessage());
                        }
                    } else {
                        System.out.println("File not found: " + argument);
                    }
                } else {
                    System.out.println("Please specify a filename. e.g: cat <filename>");
                }
            }
            default -> {
                System.out.println("Unknown command: " + command);
                System.out.println("Available commands: stop, status");
            }
        }
    }
}