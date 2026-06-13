package com.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebHandler implements HttpHandler {
    
    protected static int port = 8000;
    protected static PageConfig config = new PageConfig();
    protected static Client client;

    protected WebHandler(int port, String page, Client client) {
        this.port = port;
        this.config.setPage(page);
        this.client = client;
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new WebHandler(port, config.getPage(), client));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:" + port + "/");
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:" + port));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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

            String response = "Message sent!";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        } else {
            String response = this.config.getPage();
            response += "\n\n<form method='POST' action='/'>" +
                    "<input type='text' name='message' placeholder='Type a message...' required>" +
                    "<button type='submit'>Send</button>" +
                    "</form>";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
    }
    
    public void setPage(JSONObject jsonResponse, String username) {
        StringBuilder page = new StringBuilder();
        page.append("<!DOCTYPE html>");
        page.append("<html>");
        page.append("<head><title>Messages for " + username + "</title></head>");
        page.append("<body><h1>Messages for " + username + "</h1>");

        if (jsonResponse.getJSONArray("messages").isEmpty()) {
            page.append("<p>No messages yet</p>");
        } else {
            for (Object msg : jsonResponse.getJSONArray("messages")) {
                JSONObject message = (JSONObject) msg;
                page.append("<a>[" + message.getString("timestamp") + "]" + message.getString("username") + "#" + message.getString("tag") + ": " + message.getString("content") + "</a><br>");
                System.out.println(page.toString());//debug
            }
        }

        page.append("</body>");
        page.append("</html>");

        this.config.setPage(page.toString());
    }
}