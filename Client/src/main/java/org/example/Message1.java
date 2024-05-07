package org.example;

import java.io.Serializable;
import java.util.Map;

public class Message1 implements Serializable {
    private Map<String , Integer> vv;
    private Map <String,Object> album;

    public Message1(Map<String, Integer> vv, Map <String,Object> album) {
        this.vv = vv;
        this.album = album;
    }

    public Map<String, Integer> getVv() {
        return vv;
    }

    public Map <String,Object> getAlbum() {
        return album;
    }

    public void setMessage(Map <String,Object> message) {
        this.album = message;
    }
    public void setVv(Map<String, Integer> vv) {
        this.vv = vv;
    }
}

