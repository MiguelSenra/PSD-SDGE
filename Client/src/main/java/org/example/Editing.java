package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;
import inc.FileUploadRequest;
import inc.Rx3FileServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Editing {

    //private Scanner scanner;

    private ArrayList<Editor> members;

    private static final int BATCH_SIZE = 1024;

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
                                    //System.out.println(msg.getClock());
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

    public static String calculateSHA256(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(filePath);
             DigestInputStream dis = new DigestInputStream(fis, md)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = dis.read(buffer)) != -1) {
                // Atualiza o Digest com os bytes lidos
                md.update(buffer, 0, bytesRead);
            }
        }

        // Converte o hash para uma representação hexadecimal
        byte[] hashBytes = md.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public findDataServer()

    public void addFileDataServer(String hash, String filePath) {
        byte[] buffer = new byte[BATCH_SIZE];
        var channel = ManagedChannelBuilder.forAddress("localhost", 12346)
                .usePlaintext()
                .build();
        var stub = Rx3FileServiceGrpc.newRxStub(channel);


        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            stub.upload(Flowable.generate(emitter -> {
                        int numBytesRead = fileInputStream.read(buffer);
                        if (numBytesRead == -1) {
                            emitter.onComplete();
                        } else {
                            FileUploadRequest request = FileUploadRequest.newBuilder()
                                    .setChunk(ByteString.copyFrom(buffer))
                                    .setSsaKey(Hashing.sha256().hashString(hash, java.nio.charset.StandardCharsets.UTF_8).toString())
                                    .build();
                            emitter.onNext(request);
                        }
                    })).map(n -> "Result: " + n)
                    .blockingSubscribe(
                            // Lambda para processar resultados
                            System.out::println,
                            // Lambda para tratar erros
                            error -> System.err.println("Erro ao fazer upload do arquivo: " + error.getMessage())
                    );

        } catch (IOException e) {
            System.err.println("Erro ao abrir arquivo: " + e.getMessage());
        }
    }

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    public void addFile(String nomeFile, String filePath) {
        if (fileExists(filePath)) {
            String hash = null;
            try {
                hash = calculateSHA256(filePath);
                addFileDataServer(hash, filePath);
                State_CRDT_Message msg = this.albumCRDT.addFile(nomeFile, hash);
                this.sendMessage(msg, false);
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                System.out.println("Erro ao calcular o hash do ficheiro.");
                return;
            }
        } else {
            System.out.println("O arquivo " + filePath + " não existe.");
        }
    }

    public void removeFile(String nomeFile) {
        State_CRDT_Message msg= this.albumCRDT.removeFile(nomeFile);
        this.sendMessage(msg,false);
    }


}
