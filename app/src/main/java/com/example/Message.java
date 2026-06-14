    package com.example;

public class Message {
    protected String content;
    protected String username;
    protected String tag;
    protected String timestamp;
    protected String port;
    protected String ip;
    protected String reciever;
    
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
}
