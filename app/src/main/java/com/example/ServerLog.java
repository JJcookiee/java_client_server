package com.example;
import java.util.ArrayList;

public class ServerLog {
    protected String timestamp;
    protected ArrayList<Client> ClientList;
    protected ArrayList<Message> messageHistory;

    public ServerLog(String timestamp, ArrayList<Client> ClientList, ArrayList<Message> messageHistory) {
        this.timestamp = timestamp;
        this.ClientList = ClientList;
        this.messageHistory = messageHistory;
    }

    protected String getTimestamp() { return timestamp; }
    protected ArrayList<Client> getClientList() { return ClientList; }
    protected ArrayList<Message> getMessageHistory() { return messageHistory; }

    protected void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    protected void addClient(Client client) { this.ClientList.add(client); }
    protected void addMessage(Message message) { this.messageHistory.add(message); }

    public void sortClients() {
        for (Message msg : messageHistory) {
            if (client.getSocketAddress() .equals(msg.getIp() + ":" + msg.getPort())) {
                active = true;
                ActiveClients.add(client.getUsername() + "#" + msg.getTag());
                break;
            }
        }
    }

    public String getHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<p>Timestamp: ").append(timestamp).append("</p>");
        
        html.append("<h2>Clients</h2>");
        if (ClientList.isEmpty()) {
            html.append("<p>No clients connected.</p>");
        } else {
            html.append("<ul>");
            for (Client client : ClientList) {
                html.append("<li>").append(client.getUsername()).append("#").append(client.getTag()).append("</li>");
            }
            html.append("</ul>");
        }

        html.append("<h2>Message History</h2>");
        if (messageHistory.isEmpty()) {
            html.append("<p>No messages yet.</p>");
        } else {
            html.append("<ul>");
            for (Message msg : messageHistory) {
                html.append("<li>[").append(msg.getTimestamp()).append("] ")
                    .append(msg.getUsername()).append("#").append(msg.getTag())
                    .append(": ").append(msg.getContent()).append("</li>");
            }
            html.append("</ul>");
        }
        return html.toString();
    }
}
