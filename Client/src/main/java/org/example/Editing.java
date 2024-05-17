package org.example;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Editing {

    //private Scanner scanner;

    private ArrayList<Editor> members;
    //private Map<String,Object> album;

    ChatClock chatClock;

    AlbumCRDT albumCRDT;

    //private CausalBroadcast cb= new CausalBroadcast();
     String username;

     String albumName;

    ZContext context;

    ZMQ.Socket socket;
    ZMQ.Socket socket1;


    public Editing(String username,String AlbumName,long portNUmber, ArrayList<Editor> members, Map<String,Object> album) throws InterruptedException {
            this.albumCRDT=new AlbumCRDT(album,username);
            this.albumName=AlbumName;
            this.context = new ZContext();
            this.socket = context.createSocket(SocketType.ROUTER);
            this.username=username;
            this.members=members;
            this.chatClock = new ChatClock(username);

            Thread replyThread = new Thread(() -> {
                try (ZContext context1 = new ZContext()) {
                    this.socket1 = context1.createSocket(SocketType.ROUTER);
                    this.socket1.setIdentity(username.getBytes(ZMQ.CHARSET));
                    this.socket1.bind("tcp://localhost:" + portNUmber);
                    //CausalBroadcast causal = new CausalBroadcast();
                    //while (!Thread.currentThread().isInterrupted()) {
                    while (true) {
                        byte[] id = socket1.recv();
                        byte[] req = socket1.recv();
                        if (req != null) {
                            Message deserializedMessage = SerializationUtils.deserializeObject(req);
                            System.out.println();
                            System.out.println(deserializedMessage);
                            if(deserializedMessage != null) {
                                if (deserializedMessage instanceof State_CRDT_Message) {
                                    albumCRDT.newState((State_CRDT_Message) deserializedMessage);
                                } else if (deserializedMessage instanceof ChatMessage){
                                    ChatMessage msg = (ChatMessage) deserializedMessage;
                                    chatClock.processMessage(msg);
                                    //System.out.println("[" + new String(id, ZMQ.CHARSET) + "]: " + msg.getText());
                                }
                                else if (deserializedMessage instanceof JoinMessage) {
                                    JoinMessage msg = (JoinMessage) deserializedMessage;
                                    this.members.add(msg.getUser());
                                    socket.connect("tcp://localhost:" + msg.getUser().getPort());
                                    Thread.sleep(2000);
                                    sendMessage_User( new JoinResponse(chatClock.myClock()),msg.getUser().getuser());
                                }
                                else if (deserializedMessage instanceof JoinResponse) {
                                    JoinResponse msg = (JoinResponse) deserializedMessage;
                                    System.out.println("AQUIIIIIIIIIIIIIII "+msg.getClock());
                                    chatClock.updateRelogio(msg.getClock());
                                }
                                else if (deserializedMessage instanceof ExitMessage) {
                                    ExitMessage msg = (ExitMessage) deserializedMessage;
                                    this.members.remove(msg.getUser());
                                    if (msg.getUser().getuser().equals(username)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            replyThread.start();
            socket.setIdentity(username.getBytes(ZMQ.CHARSET));
            //this.socket.connect("tcp://localhost:" + 12346);

            Editor self=new Editor();
            for (int i = 0; i < this.members.size(); i++) {
                if (this.members.get(i).getuser().equals(username)) {
                    self = new Editor(members.get(i));
                }
                //System.out.println("tcp://localhost:" + this.members.get(i).getPort());
                this.socket.connect("tcp://localhost:" + this.members.get(i).getPort());
           }
            Thread.sleep(1000);
            sendMessage(new JoinMessage(self),false);
    }

    public void chat() {
        Scanner scanner = new Scanner(System.in);
            try {
                String message;
                while(scanner.hasNextLine() && !(message = scanner.nextLine()).equals("")) {// Aguarda pressionar Enter
                    sendMessage(chatClock.ChatMessage(message),true);
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                scanner.next();
                scanner.close();
            }
            //scanner.reset();
            //scanner.close();
        }

    public void sendMessage(Message msg,boolean idincluded) {
        byte[] data = SerializationUtils.serializeObject(msg);
        for (int i = 0; i < this.members.size(); i++) {
            if (!idincluded) {
                if (!this.members.get(i).getuser().equals(this.username)) {
                    socket.sendMore(this.members.get(i).getuser().getBytes(ZMQ.CHARSET));
                    socket.send(data, 0);
                    System.out.println(this.members.get(i).getuser());
                }
            }
            else {
                socket.sendMore(this.members.get(i).getuser().getBytes(ZMQ.CHARSET));
                socket.send(data, 0);
            }
        }
    }

    public void sendMessage_User(Message msg,String user) {
        byte[] data = SerializationUtils.serializeObject(msg);
        socket.sendMore(user.getBytes(ZMQ.CHARSET));
        socket.send(data, 0);
    }


    public Map<String,Object> startEdition() {
        System.out.println(members);
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getuser().equals(username)) {
                sendMessage(new ExitMessage(new Editor(members.get(i))),true);
                break;
            }
        }
        this.socket.close();
        this.context.close();
        return this.albumCRDT.Album_Send();
    }
    public Map<String,Object> TerminateEdition() {
        System.out.println(members);
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getuser().equals(username)) {
                sendMessage(new ExitMessage(new Editor(members.get(i))),true);
                break;
            }
        }
        this.socket.close();
        this.context.close();
        return this.albumCRDT.Album_Send();
    }
    public void addUser(String nome) {
        State_CRDT_Message msg=this.albumCRDT.addUser(nome);
        this.sendMessage(msg,false);
    }

    public void removeUser(String nome) {
        State_CRDT_Message msg=this.albumCRDT.removeUser(nome);
        this.sendMessage(msg,false);
    }

    public void addFile(String nomeFile, String hash) {
        State_CRDT_Message msg= this.albumCRDT.addFile(nomeFile,hash);

        this.sendMessage(msg,false);
    }

    public void removeFile(String nomeFile) {
        State_CRDT_Message msg= this.albumCRDT.removeFile(nomeFile);
        this.sendMessage(msg,false);
    }


}
