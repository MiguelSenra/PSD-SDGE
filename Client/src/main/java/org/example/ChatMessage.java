package org.example;

import java.io.Serializable;
import java.util.HashMap;

public class ChatMessage extends Message implements Serializable {
    public String text;

    public String src;

    public HashMap<String, Integer> vectorClock;

    public ChatMessage(String src, String text, HashMap<String,Integer> vectorClock) {
        super();
        this.src= src;
        this.text = text;
        this.vectorClock=vectorClock;
    }

    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }

    public HashMap<String, Integer> getVectorClock() {
        return new HashMap<>(vectorClock);
    }

    public String getSrc() {
        return src;
    }
}
