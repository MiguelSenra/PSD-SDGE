package org.example;

import java.io.Serializable;

public class ChatMessage extends Message implements Serializable {
    public String text;
    public ChatMessage(String text) {
        super();
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
