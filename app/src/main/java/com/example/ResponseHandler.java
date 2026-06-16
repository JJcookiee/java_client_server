package com.example;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ResponseHandler class
 */
public class ResponseHandler implements Runnable {
    protected Client client;
    protected String response;

    /**
     * ResponseHandler constructor
     * @param client
     * @param response
     */
    public ResponseHandler(Client client, String response) {
        this.client = client;
        this.response = response;
    }

    /**
     * checks the input each second, if its null or the same as last time it skips
     * if the response isnt a JSON it also skips
     * @exception InterruptedExecption thread interrupted
     * @exception JSONExecption cannot parse resposne
     */
    @Override
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
                    FileHandler.Debug("Skipping non-JSON response: " + response.substring(0, Math.min(50, response.length())));
                    continue;
                }
                
                lastResponse = response;
                
                JSONObject jsonResponse = new JSONObject(jsonBody);
                FileHandler.Debug("json response: " + jsonResponse.toString());
                client.webHandler.setPage(jsonResponse, client.username);
            } catch (InterruptedException | JSONException e) {
                FileHandler.Debug("Error parsing response(or interruption): " + response + e.getMessage());
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                FileHandler.Debug("ResponseHandler interrupted, stopping thread." + e.getMessage());
                break;
            }
        }
    }
}
