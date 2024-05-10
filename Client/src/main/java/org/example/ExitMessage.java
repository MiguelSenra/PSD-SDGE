package org.example;

import java.io.Serializable;

public class ExitMessage extends Message implements Serializable {
    private Editor user;

    public ExitMessage(Editor user) {
        super();
        this.user = user;
    }
    public Editor getUser() {
        return user;
    }
}
