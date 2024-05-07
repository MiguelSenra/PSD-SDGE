package org.example;

import java.util.Map;

public class Message {
    private int id;
    private int src;
    private Body body;

    public Message(int src, Body body) {
        this.src = src;
        this.body = body;
    }

    public int getSrc() {
        return src;
    }

    public Body getBody() {
        return body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

class Body {
    private Map<Integer, Integer> vv;
    private String message;

    public Body(Map<Integer, Integer> vv, String message) {
        this.vv = vv;
        this.message = message;
    }

    public Map<Integer, Integer> getVv() {
        return vv;
    }

    public String getMessage() {
        return message;
    }
}

