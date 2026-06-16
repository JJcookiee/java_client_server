package com.example;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServerLog class
 */
public class ServerLog {
    protected String timestamp;
    protected List<Client> ClientList = new ArrayList<>();
    protected Map<String, List<Message>> ActiveClients = new HashMap<>();
    protected ArrayList<Message> messageHistory;
    protected ArrayList<Socket> clientSockets;

    /**
     * ServerLog constructor
     * @param timestamp
     * @param messageHistory
     * @param clientSockets
     */
    public ServerLog(String timestamp, ArrayList<Message> messageHistory, ArrayList<Socket> clientSockets) {
        this.timestamp = timestamp;
        this.messageHistory = messageHistory;
        this.clientSockets = clientSockets;
    }

    protected String getTimestamp() { return timestamp; }
    protected Map<String, List<Message>> getActiveClients() { return new HashMap<>(ActiveClients); }
    protected ArrayList<Message> getMessageHistory() { return new ArrayList<>(messageHistory); }

    protected void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    protected void addClient(Client client) { ClientList.add(client); }

    /**
     * Connects active clients to the messages they've sent
     */
    public void sortClients() {
        ActiveClients.clear();
        for (Message msg : messageHistory) {
            String socketAddress = msg.getIp() + ":" + msg.getPort();
            ActiveClients
                .computeIfAbsent(socketAddress, k -> new ArrayList<>())
                .add(msg);
        }
    }

    /**
     * gets the current serverLog as html, containing the timestamp, the number of active clients, the active clients and their related messages, and the whole message history of the server
     * @return html
     */
    public String getHTML() {
        sortClients();
        StringBuilder html = new StringBuilder();
        html.append("<p>Timestamp: ").append(timestamp).append("</p>");
        html.append("<p>Number of Active Clients: ").append(ActiveClients.size()).append("</p>");
        html.append("<br><p>Active Clients</p>");
        html.append("<ul>");
        for (Map.Entry<String, List<Message>> entry : ActiveClients.entrySet()) {
            Message msg = entry.getValue().get(entry.getValue().size() - 1);
            String nameTag = msg.getUsername() + "#" + msg.getTag();
            html.append("<li>").append(entry.getKey()).append(nameTag).append("</li>");
        }
        html.append("</ul>");
        html.append("<br><p>Message History</p>");
        html.append("<ul>");
        for (Message msg : messageHistory) {
            html.append("<li>").append(msg.toString()).append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    /**
     * gets the current serverLog as a string, including the timestamp, the server up time, the number of active clients and their messages, all the messages
     * @return string
     */
    public String getString() {
        StringBuilder str = new StringBuilder();
        try {
            sortClients();
            LocalTime time = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            Duration duration = Duration.between(LocalTime.parse(timestamp), time).truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            String upTime = String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart());
            str.append("Server Log\n");
            str.append("Server start: ").append(timestamp).append("\n");
            str.append("Up time: ").append(upTime).append("\n");
            str.append("Number of Active Clients: ").append(ActiveClients.size()).append("\n");
            str.append("Active Client logs:\n");
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
        } catch (Exception e) {
            FileHandler.Debug("Error in getString: " + e.getMessage());
        }
        return str.toString();
    }
}
