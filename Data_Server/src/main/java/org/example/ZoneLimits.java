package org.example;

import java.util.ArrayList;
import java.util.Arrays;

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

    public void setEndHash(String endHash) {
        this.endHash = endHash;
    }

    public String getStartHash() {
        return this.startHash;
    }

    public String getEndHash() {
        return this.endHash;
    }
}
