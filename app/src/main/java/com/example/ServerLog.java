package com.example;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public class ServerLog {
    protected String timestamp;
    protected List<Client> ClientList = new ArrayList<>();
    protected Map<String, List<Message>> ActiveClients = new HashMap<>();
    protected ArrayList<Message> messageHistory;
    protected ArrayList<Socket> clientSockets;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ServerLog(String timestamp, ArrayList<Message> messageHistory, ArrayList<Socket> clientSockets) {
        this.timestamp = timestamp;
        this.messageHistory = messageHistory;
        this.clientSockets = clientSockets;
    }

    protected String getTimestamp() {
        lock.readLock().lock();
        try {
            return timestamp;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected Map<String, List<Message>> getActiveClients() { 
        lock.readLock().lock();
        try {
            return new HashMap<>(ActiveClients);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected ArrayList<Message> getMessageHistory() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(messageHistory);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void setTimestamp(String timestamp) {
        lock.writeLock().lock();
        try {
            this.timestamp = timestamp;
        } finally {
            lock.writeLock().unlock();
        }
    }
    protected void addClient(Client client) {
        lock.writeLock().lock();
        try {
            ClientList.add(client);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void sortClients() {
        lock.writeLock().lock();
        try {
            ActiveClients.clear();
            for (Message msg : messageHistory) {
                String socketAddress = msg.getIp() + ":" + msg.getPort();
                ActiveClients
                    .computeIfAbsent(socketAddress, k -> new ArrayList<>())
                    .add(msg);
            }   
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getHTML() {
        lock.readLock().lock();
        try {
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
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getString() {
        lock.readLock().lock();
        try {
            sortClients();
            StringBuilder str = new StringBuilder();
            LocalTime time = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            Duration upTime = Duration.between(LocalTime.parse(timestamp), time).truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            str.append("Server Log\n");
            str.append("Server start: ").append(timestamp).append("\n");
            str.append("Up time: ").append(upTime.toString()).append("\n");
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
            return str.toString();
        } finally {
            lock.readLock().unlock();
        }
        
    }
}
