package org.example;

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
}

