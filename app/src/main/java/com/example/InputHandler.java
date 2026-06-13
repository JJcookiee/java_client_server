package com.example;

public class InputHandler implements Runnable {
    protected Client client;

    public InputHandler(Client client) {
        this.client = client;
    }
    
    
    public void run() {
        while (true) { 
            try {
                String textMessage = client.scanner.nextLine();
                client.checkMessage(textMessage);
            } catch (Exception e) {
                System.out.println("InputHandler interrupted, stopping thread: ");
                e.printStackTrace();
                break;
            }
        }
    }
}
