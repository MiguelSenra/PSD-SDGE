package org.example;

import java.util.Map;

public class Message {
    private String src;
    private String type;
    private Body body;

    public Message(String type, Body body) {
        this.type = type;
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public String getSrc() {
        return src;
    }

    public Body getBody() {
        return body;
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

