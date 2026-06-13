package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONObject;

public class ServerRunnable implements Runnable {
    protected Socket clientSocket = null;
    protected ArrayList<Message> messageCache = null;
    protected ArrayList<String> clientTags = null;
    protected String clientTag = "0000";
    protected ArrayList<OutputStream> clientOutputStreams = null;

    public ServerRunnable(Socket clientSocket, ArrayList<Message> messageCache, ArrayList<String> clientTags, ArrayList<OutputStream> clientOutputStreams) {
        this.clientSocket = clientSocket;
        this.messageCache = messageCache;
        this.clientTags = clientTags;
        this.clientOutputStreams = clientOutputStreams;
    }

    public void run() {
        try {     
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            synchronized (clientOutputStreams) {
                clientOutputStreams.add(output);
            }

            clientTag = getClientTag();
            
            while (true) {
                byte[] buffer = new byte[1024];
                int bytesRead = input.read(buffer);
                
                if (bytesRead == -1) {
                    break;
                }
                
                String textMessage = new String(buffer, 0, bytesRead).trim();
                LocalTime time = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);

                String responseBody = "";
                StringBuilder page = new StringBuilder();

                String contentType = "";

                if(textMessage.startsWith("GET ") || textMessage.startsWith("POST ")) {
                    page.append("<html><body><p>Server is running.</p></body></html>");
                    if (messageCache.isEmpty()) {
                        page.append("<p>No messages yet.</p");
                    } else {
                        page.append("<p>Message history:</p><ul>");
                        for (Message msg : messageCache) {
                            page.append("<li>[" + msg.timestamp + "]" + msg.username + "#" + msg.tag + ": " + msg.content + "</li>");
                        }
                        page.append("</ul>");
                    }
                    responseBody = page.toString();
                    contentType = "text/html; charset=UTF-8";
                } else {
                    String[] parts = textMessage.split(": ", 2);
                    String username = parts[0];
                    String[] messageParts = parts[1].split("````", 2);
                    String reciever = messageParts[1];
                    String content = messageParts.length > 1 ? messageParts[0] : "";
                    messageCache.add(
                        new Message(
                            content,
                            username,
                            clientTag.toString(),
                            time.toString(),
                            Integer.toString(clientSocket.getPort()),
                            clientSocket.getInetAddress().toString(),
                            reciever
                        ));//most of this stuff is useless but we can log it i guess?
                    System.out.println("message cache: " + messageCache.toString());//debug
                    ArrayList<Message> clientCache = getClientCache(reciever);
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("messages", clientCache);
                    responseBody = jsonResponse.toString();
                    contentType = "application/json";
                }

                byte[] body = responseBody.getBytes();
                synchronized(clientOutputStreams) {
                    try {
                        for (OutputStream out : clientOutputStreams) {
                        out.write(("HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + contentType + "\r\n" +
                                    "Content-Length: " + body.length + "\r\n" +
                                    "Connection: keep-alive\r\n" +
                                    "\r\n").getBytes());
                        out.write(body);
                        out.flush();
                    }
                    } catch (Exception e) {
                        System.out.println("Broadcast error: ");
                        e.printStackTrace();
                        clientOutputStreams.remove(output);
                    }
                    
                }
                System.out.println(textMessage);
                System.out.println("Sent response: " + responseBody);
            }
            
            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
    
    public ArrayList<Message> getClientCache(String clientTag) {
        ArrayList<Message> clientMessageCache = new ArrayList<>();
        for (Message msg : messageCache) {
            if (msg.reciever.equals(clientTag) || msg.reciever.equals("0000")) {
                clientMessageCache.add(msg);
            }
        }
        return clientMessageCache;
    }

    public String getClientTag() {
        if (clientTag == "0000") {
            clientTag = setClientTag();
        }
        return clientTag;
    }

    public String setClientTag() {
        Random rand = new Random();
        int tag = rand.nextInt(10000);
        String clientTag = String.format("%04d", tag);

        if (clientTags.contains(clientTag)) {
            setClientTag();
        } else {
            clientTags.add(clientTag);
        }
        return clientTag;
    }
}
