package com.example;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;


public class ResponseHandler implements Runnable {
    protected Client client;
    protected String response;

    public ResponseHandler(Client client, String response) {
        this.client = client;
        this.response = response;
    }

    public void run() {
        String lastResponse = null;
        while(true) {           
            try {
                Thread.sleep(1000);
                response = client.getPage();

                if (response.equals(lastResponse) || response == null) {
                    break;
                }
                String jsonBody;
                if (response.startsWith("{")) {
                    jsonBody = response;
                } else {
                    String[] parts = response.split("\r\n\r\n|\n\n", 2);
                    jsonBody = parts.length > 1 ? parts[1].trim() : "";
                }
                
                lastResponse = response;
                response = null;
                
                JSONObject jsonResponse = new JSONObject(jsonBody);
                System.out.println("json response: "+jsonResponse.toString());//debug
                client.webHandler.setPage(jsonResponse, client.username);
            } catch (Exception e) {
                System.out.println("Error parsing response(or interruption): " + response);
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.out.println("ResponseHandler interrupted, stopping thread.");
                break;
            }
        }
    }
}
