package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ChatClock {

    ArrayList<ChatMessage> pending = new ArrayList<>();
    private HashMap<String, Integer> v_clock;
    private String username;

    private final ReentrantLock lock_clock = new ReentrantLock();
    private final ReentrantLock lock_pending = new ReentrantLock();

    public void insereRelogio(String username) {
        lock_clock.lock();
        v_clock.put(username,0);
        lock_clock.unlock();
    }

    public void updateRelogio(int clock) {
        lock_clock.lock();
        v_clock.put(username,clock);
        lock_clock.unlock();
    }

    public int myClock() {
        lock_clock.lock();
        int val=v_clock.get(this.username);
        lock_clock.unlock();
        return val;
    }

    public ChatClock(String username) {
        this.username = username;
        this.v_clock = new HashMap<>();
        this.insereRelogio(username);
    }

    public HashMap<String, Integer> getV_clock() {
        lock_clock.lock();
        HashMap<String,Integer> clock=new HashMap<>(v_clock);
        lock_clock.unlock();
        return clock;
    }

    public void incClock(String username) {
        lock_clock.lock();
        v_clock.put(username, v_clock.getOrDefault(username, 0) + 1);
        lock_clock.unlock();
    }

    public ChatMessage ChatMessage(String message) {
        lock_clock.lock();
        incClock(username);
        ChatMessage msg= new ChatMessage(username,message, getV_clock());
        lock_clock.unlock();
        return msg;
    }

    public boolean canDeliver(ChatMessage msg) {
        System.out.println("Relogio recebido "+msg.getVectorClock());
        System.out.println("Relogio local "+v_clock);
        if (msg.getSrc().equals(username)) {
            return true;
        }
        lock_clock.lock();
        HashMap<String, Integer> vector_received = msg.getVectorClock();
        boolean flag= true;
        if (this.v_clock.getOrDefault(msg.src, 0) + 1 == vector_received.get(msg.src)) {
            for (String key : vector_received.keySet()) {
                if (!key.equals(msg.src) && vector_received.getOrDefault(key, 0) > this.v_clock.getOrDefault(key, 0)) {
                    lock_pending.lock();
                    pending.add(msg);
                    lock_pending.unlock();
                    flag= false;
                    break;
                }
            }
        } else {
           flag= false;
        }
        lock_clock.unlock();
        return flag;
    }

    public ChatMessage verificaMensagensAtrasadas() {
        lock_pending.lock();
        ChatMessage msg_res=null;
        for (ChatMessage msg : pending) {
            if (canDeliver(msg)) {
                pending.remove(msg);
                msg_res= msg;
            }
        }
        lock_pending.unlock();
        return msg_res;
    }
    
    public void deliverMessage(ChatMessage msg) {
        if (msg.getSrc().equals(username)) {
            System.out.println("[" + msg.getSrc() + "]: " + msg.getText());
        }
        else {
            String src = msg.getSrc();
            lock_clock.lock();
            v_clock.put(src, v_clock.getOrDefault(src, 0) + 1);
            lock_clock.unlock();
            System.out.println("[" + msg.getSrc() + "]: " + msg.getText());
            ChatMessage nextMsg;
            while ((nextMsg = verificaMensagensAtrasadas()) != null) {
                lock_clock.lock();
                v_clock.put(nextMsg.getSrc(), v_clock.get(nextMsg.getSrc()) + 1);
                lock_clock.unlock();
            }
        }
    }

    public void processMessage(ChatMessage msg) {
        if (canDeliver(msg)) {
            deliverMessage(msg);
        } else {
            lock_pending.lock();
            pending.add(msg);
            lock_pending.unlock();
        }
    }
}
