package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlbumCRDT {
    private Map<String, Integer> vv;
    private Map <String,Object> album;

    private final String username;

    public AlbumCRDT( Map <String,Object> album,String username) {
        this.vv = new HashMap<>();
        this.vv.put(username, 0);
        this.username= username;
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

    public void setAlbum(Map <String,Object> album) {
        this.album = album;
    }

    public  State_CRDT_Message addFile(String nameFile, String hash) {
        System.out.println("nameFile: "+nameFile+" hash: "+hash);
        Map<String,File_CRDT> ficheiros=(Map<String,File_CRDT>) this.album.get("ficheiros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        File_CRDT file= new File_CRDT(clocks,hash);
        ficheiros.put(nameFile,file);
        this.album.put("ficheiros",ficheiros);
        System.out.println(this.album.toString());
        return new State_CRDT_Message(clocks,getAlbum());
    }

    public  State_CRDT_Message removeFile(String nameFile) {
        Map<String,File_CRDT> ficheiros=(Map<String,File_CRDT>) this.album.get("ficheiros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        ficheiros.remove(nameFile);
        this.album.put("ficheiros",ficheiros);
        return new State_CRDT_Message(clocks,getAlbum());
    }

    public  State_CRDT_Message addUser(String nome) {
        Map<String,Map<String,Integer>> membros=(Map<String,Map<String,Integer>>) this.album.get("membros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        membros.put(nome,clocks);
        album.put("membros",membros);
        return new State_CRDT_Message(clocks,getAlbum());
    }

    public State_CRDT_Message actualState() {
        return new State_CRDT_Message(this.getVv(),this.getAlbum());
    }

    public boolean containsFile(String nome) {
        Map<String,File_CRDT> ficheiros=(Map<String,File_CRDT>) this.album.get("ficheiros");
        return ficheiros.containsKey(nome);
    }


    public String getHashFile (String nome) {
            Map<String,File_CRDT> ficheiros=(Map<String,File_CRDT>) this.album.get("ficheiros");
            return ficheiros.get(nome).getHash();
    }

    public  State_CRDT_Message removeUser(String nome) {
        Map<String,Map<String,Integer>> membros=(Map<String,Map<String,Integer>>) this.album.get("membros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        membros.remove(nome);
        album.put("membros",membros);
        return new State_CRDT_Message(clocks,getAlbum());
    }

    public void newState(State_CRDT_Message message) {
        // Compara os relógios vetoriais
        int cmp = message.compareVectorClocks(this.vv,message.getVv());
        if (cmp == -1) {
            this.album = message.getAlbum();
        } else if (cmp == 0) {
            State_CRDT_Message msg =message.mergeStates(this);
            this.vv= msg.getVv();
            this.album= msg.getAlbum();
        }
    }

    public HashMap<String,Object> Album_Send() {
        HashMap<String,Object> album= new HashMap<>();
        HashMap<String,String> ficheiros = new HashMap<>();
        for (Map.Entry<String,File_CRDT> entry: ((Map<String,File_CRDT>) this.album.get("ficheiros")).entrySet()) {
            ficheiros.put(entry.getKey(),entry.getValue().getHash());
        }
        album.put("ficheiros",ficheiros);
        HashMap<String,Map<String,Integer>> membros= (HashMap<String,Map<String,Integer>>) this.album.get("membros");
        album.put("membros",new ArrayList<String>(membros.keySet()));

        return album;
    }
}

