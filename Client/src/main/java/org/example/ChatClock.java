package org.example;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatClock {

    ArrayList<ChatMessage> pending = new ArrayList<>();
    private HashMap<String, Integer> v_clock;
    private String username;

    public void insereRelogio(String username) {
        v_clock.put(username,0);
    }

    public void updateRelogio(int clock) {
        v_clock.put(username,clock);
    }

    public int myClock() {
        return v_clock.get(this.username);
    }

    public ChatClock(String username) {
        this.username = username;
        this.v_clock = new HashMap<>();
        this.insereRelogio(username);
    }

    public HashMap<String, Integer> getV_clock() {
        return new HashMap<>(v_clock);
    }

    public void setV_clock(HashMap<String, Integer> v_clock) {
        this.v_clock = v_clock;
    }

    public void incClock(String username) {
        v_clock.put(username, v_clock.getOrDefault(username, 0) + 1);
    }

    public ChatMessage ChatMessage(String message) {
        incClock(username);
        return new ChatMessage(username,message, getV_clock());
    }

    public boolean canDeliver(ChatMessage msg) {
        System.out.println("Relogio recebido "+msg.getVectorClock());
        System.out.println("Relogio local "+v_clock);
        if (msg.getSrc().equals(username)) {
            return true;
        }
        HashMap<String, Integer> vector_received = msg.getVectorClock();
        if (this.v_clock.getOrDefault(msg.src, 0) + 1 == vector_received.get(msg.src)) {
            for (String key : vector_received.keySet()) {
                if (!key.equals(msg.src) && vector_received.getOrDefault(key, 0) > this.v_clock.getOrDefault(key, 0)) {
                    pending.add(msg);
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public ChatMessage verificaMensagensAtrasadas() {
        for (ChatMessage msg : pending) {
            if (canDeliver(msg)) {
                pending.remove(msg);
                return msg;
            }
        }
        return null;
    }
    
    public void deliverMessage(ChatMessage msg) {
        if (msg.getSrc().equals(username)) {
            System.out.println("[" + msg.getSrc() + "]: " + msg.getText());
        }
        else {
            String src = msg.getSrc();
            v_clock.put(src, v_clock.getOrDefault(src, 0) + 1);
            System.out.println("[" + msg.getSrc() + "]: " + msg.getText());
            ChatMessage nextMsg;
            while ((nextMsg = verificaMensagensAtrasadas()) != null) {
                v_clock.put(nextMsg.getSrc(), v_clock.get(nextMsg.getSrc()) + 1);
            }
        }
    }

    public void processMessage(ChatMessage msg) {
        if (canDeliver(msg)) {
            deliverMessage(msg);
        } else {
            pending.add(msg);
        }
    }
}
