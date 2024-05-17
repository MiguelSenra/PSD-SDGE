package org.example;


public class ZoneLimits {
    private String startHash;
    private String endHash;

    public ZoneLimits(String startHash, String endHash) {
        this.startHash = startHash;
        this.endHash = endHash;
    }

    public boolean keyInInterval(String key) {
        return key.compareTo(this.startHash) >= 0 && key.compareTo(this.endHash) <= 0;
    }

    public void setStartHash(String startHash) {
        this.startHash = startHash;
    }

}
