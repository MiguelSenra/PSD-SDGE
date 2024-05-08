package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlbumCRDT {
    private Map<String, Integer> vv;
    private Map <String,Object> album;

    private String username;

    public AlbumCRDT( Map <String,Object> album,String username) {
        this.vv = new HashMap<>();
        this.vv.put(username, 0);
        this.album = album;
    }
    public void incClock() {
        vv.put(username, vv.getOrDefault(username, 0) + 1);
    }

    public Map<String, Integer> getVv() {
        return new HashMap<>(vv);
    }

    public Map <String,Object> getAlbum() {
        return new HashMap<>(album);
    }

    public void setMessage(Map <String,Object> album) {
        this.album = album;
    }

    public  Message1 addUser(String nome) {
        Map<String,Map<String,Integer>> membros=(Map<String,Map<String,Integer>>) this.album.get("membros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        membros.put(nome,clocks);
        album.put("membros",membros);
        Message1 msg= new Message1(clocks,getAlbum());
        return msg;
    }

    public  Message1 removeUser(String nome) {
        Map<String,Map<String,Integer>> membros=(Map<String,Map<String,Integer>>) this.album.get("membros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        if (membros.containsKey(nome)) {
            membros.remove(nome);
        }
        album.put("membros",membros);
        Message1 msg= new Message1(clocks,getAlbum());
        return msg;
    }

    public void newState(Message1 message) {
        // Compara os relógios vetoriais
        int cmp = message.compareVectorClocks(this.vv,message.getVv());
        if (cmp == -1) {
            this.album = message.getAlbum();
        } else if (cmp == 0) {
            Message1 msg =message.mergeStates(this);
            this.vv= msg.getVv();
            this.album= msg.getAlbum();
        }
    }

    public HashMap<String,Object> Album_Send() {
        HashMap<String,Object> album= new HashMap<>();
        album.put("ficheiros",new HashMap<String,String>());
        HashMap<String,Map<String,Integer>> membros= (HashMap<String,Map<String,Integer>>) this.album.get("membros");
        album.put("membros",new ArrayList<String>(membros.keySet()));
        return album;
    }
}

