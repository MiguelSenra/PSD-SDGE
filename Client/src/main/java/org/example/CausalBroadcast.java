package org.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
public class CausalBroadcast {

    private Map<String, Integer> vv = new HashMap<>();
    //static Set<String> delivered = new HashSet<>();
    private Set<Message> pending = new HashSet<>();

    private String username;

    public void incClock() {
        vv.put(username, vv.getOrDefault(username, 0) + 1);
    }

    public Map<String,Integer> getVv() {
        return new HashMap<>(vv);
    }

    public boolean canDeliver(Message msg) {
        String src = msg.getSrc();
        Map<Integer, Integer> vvm = msg.getBody().getVv();
        if (vv.get(src) + 1 == vvm.get(src)) {
            for (String key : vv.keySet()) {
                if (!key.equals(src) && vvm.get(key) > vv.getOrDefault(key, 0)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Message verificaMensagensAtrasadas() {
        for (Message msg : pending) {
            if (canDeliver(msg)) {
                pending.remove(msg);
                return msg;
            }
        }
        return null;
    }

    public void deliverMessage(Message msg) {
        String src = msg.getSrc();
        //delivered.add(msg.getBody().getMessage());
        vv.put(src, vv.get(src) + 1);
        Message nextMsg;
        while ((nextMsg = verificaMensagensAtrasadas()) != null) {
            //delivered.add(nextMsg.getBody().getMessage());
            vv.put(nextMsg.getSrc(), vv.get(nextMsg.getSrc()) + 1);
        }
    }
    /*
    static void broadcast() {
        for (Integer i : node_ids()) {
            if (!i.equals(node_id())) {
                send(i, new Body(), null); // Assuming Body is a class with appropriate fields
            }
        }
    }

    public void fwdMsg(Message msg) {
        if (canDeliver(msg)) {
            deliverMessage(msg);
        } else {
            pending.add(msg);
        }
    }
    /* 
    public static void main(String[] args) {
        receive(); // Assuming receive method is defined somewhere
    }
}
*/