package com.example;

/**
 * Message class
 */
public class Message {
    protected String content;
    protected String username;
    protected String tag;
    protected String timestamp;
    protected String port;
    protected String ip;
    protected String reciever;
    
    /**
     * Message constructor
     * @param content
     * @param username
     * @param tag
     * @param timestamp
     * @param port
     * @param ip
     * @param reciever
     */
    public Message(String content, String username, String tag,String timestamp, String port, String ip, String reciever) {
        this.content = content;
        this.username = username;
        this.tag = tag;
        this.timestamp = timestamp;
        this.port = port;
        this.ip = ip;
        this.reciever = reciever;
    }

    public String getContent() { return content; }
    public String getUsername() { return username; }
    public String getTag() { return tag; }
    public String getTimestamp() { return timestamp; }
    public String getPort() { return port; }
    public String getIp() { return ip; }
    public String getReciever() { return reciever; }

    /**
     * converts message to formatted string
     */
    @Override
    public String toString() {
        return username + "#" + tag + " [" + timestamp + "] (" + ip + ":" + port + ") -> #" + reciever + ": " + content;
    }
}
