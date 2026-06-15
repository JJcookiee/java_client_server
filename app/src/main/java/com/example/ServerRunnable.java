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
    protected ServerLog serverLog = null;

    public ServerRunnable(Socket clientSocket, ArrayList<Message> messageCache, ArrayList<String> clientTags, ArrayList<OutputStream> clientOutputStreams, ServerLog serverLog) {
        this.clientSocket = clientSocket;
        this.messageCache = messageCache;
        this.clientTags = clientTags;
        this.clientOutputStreams = clientOutputStreams;
        this.serverLog = serverLog;
    }

    @Override
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

                if(textMessage.startsWith("GET ") || textMessage.startsWith("POST ")) {
                    StringBuilder page = new StringBuilder();
                    page.append("<html><body>");
                    page.append("<p>Server is running.</p>");
                    page.append(serverLog.getHTML());
                    page.append("</body></html>");
                    String responseBody = page.toString();
                    byte[] body = responseBody.getBytes();
                    output.write(("""
                                  HTTP/1.1 200 OK\r
                                  Content-Type: text/html; charset=UTF-8\r
                                  Content-Length: """ + body.length + "\r\n" +
                                "Connection: keep-alive\r\n" +
                                "\r\n").getBytes());
                    output.write(body);
                    output.flush();
                } else {
                    String[] parts = textMessage.split(": ", 2);
                    String username = parts[0];
                    String[] messageParts = parts[1].split("````", 2);
                    String reciever = messageParts[1];
                    String content = messageParts.length > 1 ? messageParts[0] : "";
                    Message newMessage = new Message(
                        content,
                        username, 
                        clientTag.toString(), 
                        time.toString(), 
                        Integer.toString(clientSocket.getPort()), 
                        clientSocket.getInetAddress().toString(), 
                        reciever);
                    messageCache.add(newMessage);
                    System.out.println();//debug
                    FileHandler.Debug("message cache: " + messageCache.toString());
                    ArrayList<Message> clientCache = getClientCache(reciever);
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("messages", clientCache);
                    String responseBody = jsonResponse.toString();
                    byte[] body = responseBody.getBytes();
                    
                    synchronized(clientOutputStreams) {
                        ArrayList<OutputStream> streamsToRemove = new ArrayList<>();
                        for (OutputStream out : clientOutputStreams) {
                            try {
                                out.write(("""
                                           HTTP/1.1 200 OK\r
                                           Content-Type: application/json\r
                                           Content-Length: """ + body.length + "\r\n" +
                                            "Connection: keep-alive\r\n" +
                                            "\r\n").getBytes());
                                out.write(body);
                                out.flush();
                            } catch (IOException e) {
                                streamsToRemove.add(out);
                                FileHandler.Debug(e.getMessage());
                            }
                        }
                        for (OutputStream out : streamsToRemove) {
                            clientOutputStreams.remove(out);
                        }
                    }
                }
                // System.out.println(textMessage);
                // System.out.println("Sent response: " + responseBody);
            }
            
            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
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
        if (clientTag.equals("0000")) {
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
