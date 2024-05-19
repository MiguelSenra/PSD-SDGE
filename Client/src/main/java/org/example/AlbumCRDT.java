package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AlbumCRDT {
    private Map<String, Integer> vv;
    private Map <String,Object> album;
    private final String username;

    private ReentrantLock lock_clock = new ReentrantLock();
    private ReentrantLock lock_album = new ReentrantLock();

    public AlbumCRDT( Map <String,Object> album,String username) {
        this.vv = new HashMap<>();
        this.vv.put(username, 0);
        this.username= username;
        this.album = album;
    }
    public void incClock() {
        lock_clock.lock();
        vv.put(username, vv.getOrDefault(username, 0) + 1);
        lock_clock.unlock();
    }

    public Map<String, Integer> getVv() {
        lock_clock.lock();
        HashMap<String,Integer> clock= new HashMap<>(vv);
        lock_clock.unlock();
        return clock;
    }

    public Map <String,Object> getAlbum() {
        lock_album.lock();
        HashMap<String,Object> map= new HashMap<>(album);
        lock_album.unlock();
        return map;
    }

    public  State_CRDT_Message addFile(String nameFile, String hash) {
        lock_clock.lock();
        lock_album.lock();
        System.out.println("nameFile: "+nameFile+" hash: "+hash);
        Map<String,File_CRDT> ficheiros=(Map<String,File_CRDT>) this.album.get("ficheiros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        File_CRDT file= new File_CRDT(clocks,hash);
        ficheiros.put(nameFile,file);
        this.album.put("ficheiros",ficheiros);
        System.out.println(this.album.toString());
        State_CRDT_Message crdt_msg= new State_CRDT_Message(clocks,getAlbum());
        lock_album.unlock();
        lock_clock.unlock();
        return crdt_msg;
    }

    public  State_CRDT_Message removeFile(String nameFile) {
        lock_clock.lock();
        lock_album.lock();
        Map<String,File_CRDT> ficheiros=(Map<String,File_CRDT>) this.album.get("ficheiros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        ficheiros.remove(nameFile);
        this.album.put("ficheiros",ficheiros);
        State_CRDT_Message crdt_msg= new State_CRDT_Message(clocks,getAlbum());
        lock_album.unlock();
        lock_clock.unlock();
        return crdt_msg;
    }

    public  State_CRDT_Message addUser(String nome) {
        lock_clock.lock();
        lock_album.lock();
        Map<String,Map<String,Integer>> membros=(Map<String,Map<String,Integer>>) this.album.get("membros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        membros.put(nome,clocks);
        album.put("membros",membros);
        State_CRDT_Message crdt_msg= new State_CRDT_Message(clocks,getAlbum());
        lock_album.unlock();
        lock_clock.unlock();
        return crdt_msg;
    }

    public State_CRDT_Message actualState() {
        lock_clock.lock();
        lock_album.lock();
        State_CRDT_Message crdt_msg= new State_CRDT_Message(this.getVv(),this.getAlbum());
        lock_album.unlock();
        lock_clock.unlock();
        return crdt_msg;

    }

    private boolean containsFile(String nome) {
        Map<String,File_CRDT> ficheiros=(Map<String,File_CRDT>) this.album.get("ficheiros");
        return ficheiros.containsKey(nome);
    }


    public String getHashFile (String nome) {
            lock_album.lock();
            String res=null;
            if (containsFile(nome)) {
                Map<String, File_CRDT> ficheiros = (Map<String, File_CRDT>) this.album.get("ficheiros");
                res= ficheiros.get(nome).getHash();
            }
            lock_album.unlock();
            return res;
    }

    public  State_CRDT_Message removeUser(String nome) {
        lock_clock.lock();
        lock_album.lock();
        Map<String,Map<String,Integer>> membros=(Map<String,Map<String,Integer>>) this.album.get("membros");
        this.incClock();
        Map<String,Integer> clocks= this.getVv();
        membros.remove(nome);
        album.put("membros",membros);
        State_CRDT_Message crdt_msg= new State_CRDT_Message(clocks,getAlbum());
        lock_album.unlock();
        lock_clock.unlock();
        return crdt_msg;
    }

    public void newState(State_CRDT_Message message) {
        // Compara os relógios vetoriais
        lock_clock.lock();
        lock_album.lock();
        int cmp = message.compareVectorClocks(this.vv,message.getVv());
        if (cmp == -1) {
            this.album = message.getAlbum();
        } else if (cmp == 0) {
            State_CRDT_Message msg =message.mergeStates(this);
            this.vv= msg.getVv();
            this.album= msg.getAlbum();
        }
        lock_album.unlock();
        lock_clock.unlock();
    }

    public HashMap<String,Object> Album_Send() {
        lock_clock.lock();
        lock_album.lock();
        HashMap<String,Object> album= new HashMap<>();
        HashMap<String,String> ficheiros = new HashMap<>();
        for (Map.Entry<String,File_CRDT> entry: ((Map<String,File_CRDT>) this.album.get("ficheiros")).entrySet()) {
            ficheiros.put(entry.getKey(),entry.getValue().getHash());
        }
        album.put("ficheiros",ficheiros);
        HashMap<String,Map<String,Integer>> membros= (HashMap<String,Map<String,Integer>>) this.album.get("membros");
        album.put("membros",new ArrayList<String>(membros.keySet()));
        lock_album.unlock();
        lock_clock.unlock();
        return album;
    }
}

