package org.example;

import java.io.Serializable;

public class Editor implements Comparable<Editor>, Serializable {
    private String ip;
    private int port;
    private String user;

    public Editor() {
        this.ip = "";
        this.port = 0;
        this.user = "";
    }

    public Editor(String ip, int port, String user) {
        this.ip = ip;
        this.port = port;
        this.user = user;
    }

    public Editor(Editor editor) {
        this.ip = editor.getIp();
        this.port = editor.getPort();
        this.user = editor.getuser();
    }

    @Override
    public int compareTo(Editor o) {
        return this.user.compareTo(o.getuser());
    }


    // Getters and setters if needed
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getuser() {
        return user;
    }

    public void setuser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Editor{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", user=" + user +
                '}';
    }
}

