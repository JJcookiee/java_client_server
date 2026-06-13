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

                if (response == null || response.isEmpty()) {
                    continue;
                }

                if (response.equals(lastResponse)) {
                    continue;
                }

                String jsonBody;
                if (response.startsWith("{")) {
                    jsonBody = response;
                } else {
                    String[] parts = response.split("\r\n\r\n|\n\n", 2);
                    jsonBody = parts.length > 1 ? parts[1].trim() : "";
                }
                
                if (jsonBody.isEmpty() || !jsonBody.startsWith("{")) {
                    System.out.println("Skipping non-JSON response: " + response.substring(0, Math.min(50, response.length())));
                    continue;
                }
                
                lastResponse = response;
                
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
