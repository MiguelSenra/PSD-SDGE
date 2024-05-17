package org.example;

import java.io.Serializable;
import java.util.Map;

public class File_CRDT implements Serializable {
    private Map<String, Integer> vv;
    private String hash;

    File_CRDT(Map<String, Integer> vv, String hash) {
        this.vv = vv;
        this.hash = hash;
    }

    public Map<String, Integer> getVv() {
        return vv;
    }

    public String getHash() {
        return hash;
    }

    public void setVv(Map<String, Integer> vv) {
        this.vv = vv;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

}
