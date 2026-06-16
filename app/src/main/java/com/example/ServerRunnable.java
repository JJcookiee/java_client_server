package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONObject;

/**
 * ServerRunnable class
 */
public class ServerRunnable implements Runnable {
    protected Socket clientSocket = null;
    protected String clientTag = "0000";
    protected Server server;

    /**
     * ServerRunnable constructor
     * @param clientSocket the socket needed
     * @param server the server to access all the lists off stuff that are needed over all runnables
     */
    public ServerRunnable(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    /**
     * Deals with input and output of server
     * Reads either the messages from client which it evaluates and turns into a json to send to each respective client to view thei messages
     * Or read the http get or post requests for the browser
     */
    @Override
    public void run() {
        try {     
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            clientTag = getClientTag();
            server.registerClient(clientSocket, clientTag, output);
            /**
             * Reads input and decides how to respond
             */
            while (true) {
                byte[] buffer = new byte[1024];
                int bytesRead = input.read(buffer);
                
                if (bytesRead == -1) {
                    break;
                }
                
                String textMessage = new String(buffer, 0, bytesRead).trim();

                /**
                 * Filters between browser input and client input
                 */
                if(textMessage.startsWith("GET ") || textMessage.startsWith("POST ")) {
                    /**
                     * outputs html for server ui
                     */
                    handleHTTP(textMessage, output);
                } else {
                    /**
                     * parses message from client
                     */
                    handleMessage(textMessage);
                }
            }          
            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            FileHandler.Debug(e.getMessage());
        }
    }
    
    /**
     * sorts which messages the client should be able to see
     * @param clientTag
     * @return the correct messages for them to see
     */
    public ArrayList<Message> getClientCache(String clientTag) {
        ArrayList<Message> clientMessageCache = new ArrayList<>();
        for (Message msg : server.messageCache) {
            if (msg.receiver.equals(clientTag) || msg.tag.equals(clientTag) || msg.receiver.equals("0000")) {
                clientMessageCache.add(msg);
            }
        }
        return clientMessageCache;
    }

    /**
     * gives the client a tag if it needs one
     * @return clientTag
     */
    public String getClientTag() {
        if (clientTag.equals("0000")) {
            clientTag = setClientTag();
        }
        return clientTag;
    }

    /**
     * creates new random tag 0001-9999 and adds it to a list of client tags
     * cannot be more than 10000 clients because returning null will throw an error
     * i think the server will break before that
     * @return clientTag
     */
    public String setClientTag() {
        Random rand = new Random();
        int tag = rand.nextInt(10000);
        String clientTag = String.format("%04d", tag);

        if (!server.clientTags.contains(clientTag)) {
            return clientTag;
        } else {
            clientTag = setClientTag();
        }
        return null;
    }

    /**
     * handles the http requests, just makes a big html string that gets output to the browser
     * @param request the http request from the browser
     * @param output the outstresam to the browser
     * @throws IOException throws error if output fails
     */
    public void handleHTTP(String request, OutputStream output) throws IOException {
        StringBuilder page = new StringBuilder();
        page.append("<html><body>");
        page.append("<p>Server is running.</p>");
        page.append(server.serverLog.getHTML());
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
    }

    /**
     * handles the messages sent. splits the message up into tis parts to create a message object to pass on
     * @param textMessage the message
     * @throws IOException throws if output fails
     */
    public void handleMessage(String textMessage) throws IOException {
        String[] parts = textMessage.split(": ", 2);
        String username = parts[0];
        String[] messageParts = parts[1].split("````", 2);
        String receiver = messageParts.length > 1 ? messageParts[1] : "0000";
        String content = messageParts.length > 1 ? messageParts[0] : "";

        if (content.startsWith("/msg") && content.contains(" ")) {
            String[] cmdParts = content.split(" ", 3);
            if (cmdParts.length >= 3) {
                receiver = cmdParts[1];
                content = cmdParts[2];
            }
        }

        LocalTime time = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);

        Message newMessage = new Message(
            content,
            username, 
            clientTag, 
            time.toString(), 
            Integer.toString(clientSocket.getPort()), 
            clientSocket.getInetAddress().toString(), 
            receiver);

        server.messageCache.add(newMessage);

        if(receiver.equals("0000")){
            sendToAll();
        } else {
            sendToOne(receiver);
        }
    }

    /**
     * makes a list of one whole singular message to give to sendToClients
     * @throws IOException can throw an error
     */
    public void sendToAll() throws IOException {
        ArrayList<Message> list = getClientCache(clientTag);
        sendToClients(list, server.clientOutputStreams);
    }

    /**
     * makes another list of messages and gets the outputstream for the specific client using its tag
     * @param tag where the message is going
     * @throws IOException throws errors sometimes
     */
    public void sendToOne(String tag) throws IOException {
        int rIndex = server.clientTags.indexOf(tag);
        int sIndex = server.clientSockets.indexOf(clientSocket);

        if (rIndex >= 0) {
            ArrayList<Message> list = getClientCache(clientTag);

            ArrayList<OutputStream> targetStreams = new ArrayList<>();
            targetStreams.add(server.clientOutputStreams.get(rIndex));
            targetStreams.add(server.clientOutputStreams.get(sIndex));

            sendToClients(list, targetStreams);
        }
    }

    /**
     * sends messages to output streams. turns it into a json first, and then back into a string, and then once again sends it as a json
     * @param messages the messages to be sent
     * @param outputs the outputstreams its sending to
     * @throws IOException
     */
    public void sendToClients(ArrayList<Message> messages, ArrayList<OutputStream> outputs) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("messages", messages);
        String responseBody = jsonResponse.toString();
        byte[] body = responseBody.getBytes();
        
        /**
         * Sends all clients the messageCache as a JSON
         */
        synchronized(server.clientOutputStreams) {
            ArrayList<OutputStream> streamsToRemove = new ArrayList<>();
            for (OutputStream out : outputs) {
                try {
                    out.write(("""
                                HTTP/1.1 200 OK\r
                                Content-Type: application/json\r
                                Content-Length: """ + body.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n").getBytes());
                    out.write(body);
                    out.flush();
                } catch (IOException e) {
                    streamsToRemove.add(out);
                    FileHandler.Debug(e.getMessage());
                }
            }
            server.clientOutputStreams.removeAll(streamsToRemove);
        }
    }
}
