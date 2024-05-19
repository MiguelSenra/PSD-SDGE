package org.example;


public class Zone implements Comparable<Zone>{
    private String ip;
    private int port;
    private String hash;

    public Zone(String ip, int port, String hash) {
        this.ip = ip;
        this.port = port;
        this.hash = hash;
    }

    @Override
    public int compareTo(Zone o) {
        return this.hash.compareTo(o.getHash());
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", hash=" + hash +
                '}';
    }
}
