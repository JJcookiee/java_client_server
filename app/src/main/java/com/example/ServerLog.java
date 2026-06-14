package com.example;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerLog {
    protected String timestamp;
    protected List<Client> ClientList = new ArrayList<>();
    protected Map<String, List<Message>> ActiveClients = new HashMap<>();
    protected ArrayList<Message> messageHistory;
    protected ArrayList<Socket> clientSockets;

    public ServerLog(String timestamp, ArrayList<Message> messageHistory, ArrayList<Socket> clientSockets) {
        this.timestamp = timestamp;
        this.messageHistory = messageHistory;
        this.clientSockets = clientSockets;
    }

    protected String getTimestamp() { return timestamp; }
    protected Map<String, List<Message>> getActiveClients() { return ActiveClients; }
    protected ArrayList<Message> getMessageHistory() { return messageHistory; }

    protected void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    protected void addClient(Client client) { this.ClientList.add(client); }
    protected void addMessage(Message message) { this.messageHistory.add(message); }

    public void sortClients() {
        ActiveClients.clear();
        for (Message msg : messageHistory) {
            String socketAddress = msg.getIp() + ":" + msg.getPort();
            ActiveClients
                .computeIfAbsent(socketAddress, k -> new ArrayList<>())
                .add(msg);
        }   
    }

    public String getHTML() {
        sortClients();
        StringBuilder html = new StringBuilder();
        html.append("<p>Timestamp: ").append(timestamp).append("</p>");
        html.append("<p>Number of Active Clients: ").append(ActiveClients.size()).append("</p>");
        html.append("<h2>Active Clients</h2>");
        html.append("<ul>");
        for (Map.Entry<String, List<Message>> entry : ActiveClients.entrySet()) {
            html.append("<li>").append(entry.getKey()).append("</li>");
            html.append("<ul>");
            for (Message msg : entry.getValue()) {
                html.append("<li>").append(msg.toString()).append("</li>");
            }
            html.append("</ul>");
        }
        html.append("</ul>");
        html.append("<h2>Message History</h2>");
        html.append("<ul>");
        for (Message msg : messageHistory) {
            html.append("<li>").append(msg.toString()).append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    public String getString() {
        sortClients();
        StringBuilder str = new StringBuilder();
        str.append("Timestamp: ").append(timestamp).append("\n");
        str.append("Number of Active Clients: ").append(ActiveClients.size()).append("\n");
        str.append("Active Clients:\n");
        for (Map.Entry<String, List<Message>> entry : ActiveClients.entrySet()) {
            str.append(entry.getKey()).append("\n");
            for (Message msg : entry.getValue()) {
                str.append("  ").append(msg.toString()).append("\n");
            }
        }
        str.append("Message History:\n");
        for (Message msg : messageHistory) {
            str.append(msg.toString()).append("\n");
        }
        return str.toString();
    }
}
