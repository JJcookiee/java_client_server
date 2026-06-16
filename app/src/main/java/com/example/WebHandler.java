package com.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * WebHandler class
 */
public class WebHandler implements HttpHandler {
    
    protected static int port = 8000;
    protected static PageConfig config = new PageConfig();
    protected static Client client;

    /**
     * WebHandler constructor
     * @param port
     * @param page
     * @param client
     */
    protected WebHandler(int port, String page, Client client) {
        this.port = port;
        this.config.setPage(page);
        this.client = client;
    }

    /**
     * starts the http server
     * has commented out function to open the browser for client ui
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new WebHandler(port, config.getPage(), client));
        server.setExecutor(null);
        server.start();
        System.out.println("Web UI started at http://localhost:" + port + "/");
        // try {
        //     java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:" + port));
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    /**
     * handles the client ui
     * @param exchange the http variable
     * @throws IOException throws exception if the exchange handler doesnt work
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        /**
         * outputs the change to the messages div only
         */
        if (path.equals("/messages")) {

            String page = config.getPage();

            int start = page.indexOf("<div id='messages'>");
            int end = page.indexOf("</div>", start + 18);

            String messagesOnly = "";

            if (start != -1 && end != -1) {
                messagesOnly = page.substring(start, end + 6);
            } else {
                messagesOnly = "<div id='messages'>No message</div>";
            }

            byte[] bytes = messagesOnly.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream out = exchange.getResponseBody();
            out.write(bytes);
            out.close();
            return;
        }

        /**
         * Recieves input from ui POST form
         */
        String method = exchange.getRequestMethod();
        if (method.equalsIgnoreCase("POST")) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String[] params = body.split("&");
            String message = "";
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals("message")) {
                    message = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    break;
                }
            }
            if (!message.isEmpty()) {
                client.sendMessage(message, false);
            }

            exchange.getResponseHeaders().add("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
            return;
        }
        String response = this.config.getPage();
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
    
    /**
     * creates the html for the client ui
     * @param jsonResponse the response from the getPage function
     * @param username the clients username
     */
    public void setPage(JSONObject jsonResponse, String username) {
        StringBuilder page = new StringBuilder();
        page.append("<!DOCTYPE html>");
        page.append("<html>");
        page.append("<head><title>Messages for " + username + "</title></head>");
        page.append("<body><h4>Messages for " + username + "</h4>");
        page.append("<div id='messages'>");

        String toReceiver = "";
        if (jsonResponse.getJSONArray("messages").isEmpty()) {
            page.append("<p>No messages yet</p>");
        } else {
            for (Object msg : jsonResponse.getJSONArray("messages")) {
                JSONObject message = (JSONObject) msg;
                if(!message.getString("receiver").equals("0000")) {
                    toReceiver = "->#" + message.getString("receiver") + ": ";
                }
                page.append(
                    "<a>[" + 
                    message.getString("timestamp") + "]" + 
                    message.getString("username") + "#" + 
                    message.getString("tag") + ": " + 
                    toReceiver + 
                    message.getString("content") + 
                    "</a><br>"
                );
            }
        }

        page.append("</div>");

        page.append("<form method='POST' action='/'>");
        page.append("<input type='text' name='message' placeholder='Type a message...' required autofocus>");
        page.append("<button type='submit'>Send</button>");
        page.append("</form>");

        /**
         * embedded js so the messages can be updated, and refresh each second
         */
        page.append("""
        <script>
        async function updateMessages() {
            try {
                const res = await fetch("/messages");
                const html = await res.text();

                const messagesDiv = document.getElementById("messages");
                if (messagesDiv) {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, "text/html");
                    const newMessages = doc.getElementById("messages");
                    if (newMessages) {
                        messagesDiv.innerHTML = newMessages.innerHTML;
                    }
                }
            } catch (e) {
                console.log("update failed", e);
            }
        }

        setInterval(updateMessages, 1000);

        updateMessages();

        document.querySelector('input[name="message"]').focus();
        </script>
        """);

        page.append("</body>");
        page.append("</html>");

        this.config.setPage(page.toString());
    }

    public void setPage(String username) {
        JSONObject emptyJson = new JSONObject();
        emptyJson.put("messages", new org.json.JSONArray());
        setPage(emptyJson, username);
    }
}