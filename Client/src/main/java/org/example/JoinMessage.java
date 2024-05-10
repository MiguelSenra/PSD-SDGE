package org.example;

import java.io.Serializable;

public class JoinMessage extends Message implements Serializable {
    private Editor user;

    public JoinMessage(Editor user) {
        super();
        this.user = user;
    }
    public Editor getUser() {
        return user;
    }


}
