package com.example;

/**
 * InputHandler class
 */
public class InputHandler implements Runnable {
    protected Client client;
    protected Server server;
    protected boolean isClient;

    /**
     * InputHandler constructor for client
     * @param client
     */
    public InputHandler(Client client) {
        this.client = client;
        isClient = true;
    }

    /**
     * InputHandler constructor for server
     * @param server
     */
    public InputHandler(Server server) {
        this.server = server;
        this.isClient = false;
    }
    
    /**
     * InputHandler Runnable
     */
    @Override
    public void run() {
        while (true) { 
            try {
                if(isClient) {
                    String textMessage = client.scanner.nextLine();
                    client.checkMessage(textMessage);
                } else {
                    String command = server.scanner.nextLine();
                    server.checkCommand(command);
                }
            } catch (Exception e) {
                FileHandler.Debug("InputHandler interrupted, stopping thread: " + e.getMessage());
                break;
            }
        }
    }
}
