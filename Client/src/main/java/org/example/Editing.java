package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.ericsson.otp.erlang.*;
import com.google.protobuf.ByteString;
import inc.FileDownloadRequest;
import inc.FileUploadRequest;
import inc.RemoveFileRequest;
import inc.Rx3FileServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;


public class Editing {

    private ArrayList<Editor> members;

    private ArrayList<Zone> dataServers;

    private static final int BATCH_SIZE = 1024;

    ChatClock chatClock;

    AlbumCRDT albumCRDT;

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
            getDataServers();

            Thread replyThread = new Thread(() -> {
                try (ZContext context1 = new ZContext()) {
                    this.socket1 = context1.createSocket(SocketType.ROUTER);
                    this.socket1.setIdentity(username.getBytes(ZMQ.CHARSET));
                    this.socket1.bind("tcp://localhost:" + portNUmber);
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

            Editor self=new Editor();
            for (int i = 0; i < this.members.size(); i++) {
                if (this.members.get(i).getuser().equals(username)) {
                    self = new Editor(members.get(i));
                }
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

    public void getDataServers() {
        try {
            // Conecta ao servidor
            try (SocketChannel ss1 = SocketChannel.open(new InetSocketAddress((int) 12345))) {
                try {
                    // Monta a mensagem a ser enviada para o servidor
                    OtpErlangObject[] tuple = new OtpErlangObject[]{
                            new OtpErlangAtom("get_servers"),
                    };
                    OtpErlangTuple message = new OtpErlangTuple(tuple);
                    ByteBuffer bb = ByteBuffer.wrap(Sistema.tupleToBytes(message));

                    // Envia a mensagem para o servidor
                    ss1.write(bb);
                    bb.clear();

                    int bytesRead;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while ((bytesRead = ss1.read(bb)) != -1) {
                        bb.flip();
                        byte[] receivedBytes = new byte[bb.remaining()];
                        bb.get(receivedBytes);
                        baos.write(receivedBytes);
                        bb.clear();
                    }

                    // Processa a resposta do servidor
                    byte[] receivedBytes = baos.toByteArray();
                    // Concatenar os bytes recebidos em um único array
                    OtpErlangTuple response = Sistema.bytesToTuple(receivedBytes);
                    OtpErlangObject[] fields = response.elements();

                    OtpErlangObject firstField = fields[0];
                    if (firstField instanceof OtpErlangList) {

                        // Converter a lista em um ArrayList
                        OtpErlangList lista = ((OtpErlangList) firstField);
                        ArrayList<Zone> servers = new ArrayList<>();
                        // Iterar sobre os elementos da lista Erlang
                        for (OtpErlangObject element : lista.elements()) {
                            if (element instanceof OtpErlangTuple) {
                                OtpErlangTuple new_tuple = (OtpErlangTuple) element;
                                OtpErlangObject[] fields1 = new_tuple.elements();
                                String ip = ((OtpErlangString) fields1[0]).stringValue();
                                long portLong = ((OtpErlangLong) fields1[1]).longValue();
                                int port=(int) portLong;
                                String hash = ((OtpErlangString) fields1[2]).stringValue();
                                servers.add(new Zone(ip, port, hash));
                            }
                        }
                        this.dataServers=servers;
                    }
                    else {
                        throw new Exception("Não foi possível obter uma resposta");
                    }
                } catch (Exception e) {
                    // Trata exceções durante o processamento da mensagem
                    System.out.println("Erro durante o processamento da mensagem: " + e.getMessage());
                }
            } catch (IOException e) {
                // Trata exceções de conexão com o servidor
                System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            }
        } catch (Exception e) {
            // Trata exceções gerais
            System.err.println("Erro geral: " + e.getMessage());
        }
    }

    public boolean addFileDataServer(String hash, String filePath) {
        byte[] buffer = new byte[BATCH_SIZE];
        final boolean[] flag={true};
        final boolean[] error={false};
        while(flag[0]) {
            flag[0]=false;
            final int[] seqNum = {1};
            Zone dataServer = getDataServer(hash);
            var channel = ManagedChannelBuilder.forAddress(dataServer.getIp(), dataServer.getPort())
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
                                        .setSeqNum(seqNum[0])
                                        .setChunk(ByteString.copyFrom(buffer))
                                        .setSsaKey(hash)
                                        .build();
                                seqNum[0]++;
                                emitter.onNext(request);
                            }
                        })).map(response -> {
                            if (response.getMessage().equals("No autorization")) {
                                flag[0]=true;
                                error[0]=true;
                                getDataServers();
                                return "";
                            }
                            else if (response.getMessage().equals("File exists")) {

                                System.out.println("O ficheiro já existe no servidor de dados.");
                                error[0]=true;
                                return "";
                            }
                            else {
                                System.out.println(response);
                                return "File ack: " + response.getSize() + " bytes";
                            }
                        })
                        .blockingSubscribe(
                                System.out::println,
                                error1 -> System.err.println("Erro ao fazer upload do arquivo: " + error1.getMessage())
                        );

                if (!error[0]) {
                    System.out.println("Ficheiro guardado com sucesso.");
                }

            } catch (IOException e) {
                System.err.println("Erro ao abrir arquivo: " + e.getMessage());
            }
        }
        return error[0];
    }

    public Zone getDataServer(String hash) {
            for (Zone z : this.dataServers) {
                int val = z.getHash().compareTo(hash);
                if (val<0) {
                    return z;
                }
            }
            return this.dataServers.get(0);
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
                boolean op_error=addFileDataServer(hash, filePath);
                if (!op_error) {
                    State_CRDT_Message msg = this.albumCRDT.addFile(nomeFile, hash);
                    this.sendMessage(msg, false);
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                System.out.println("Erro ao calcular o hash do ficheiro.");
                return;
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("O arquivo " + filePath + " não existe.");
        }
    }

    public void  removeFileDataServer(String hash) {
        final boolean[] flag = {true};
        while (flag[0]) {
            flag[0] = false;
            Zone dataServer = getDataServer(hash);
            var channel = ManagedChannelBuilder.forAddress(dataServer.getIp(), dataServer.getPort())
                    .usePlaintext()
                    .build();
            var stub = Rx3FileServiceGrpc.newRxStub(channel);

            RemoveFileRequest request = RemoveFileRequest.newBuilder()
                .setHash(hash)
                .build();

            stub.removeFile(Flowable.just(request))
                .map(response -> {
                    if (response.getMessage().equals("No autorization")) {
                        getDataServers();
                        flag[0] = true;
                        return "";
                    }
                    else{
                        return "O ficheiro foi removido com sucesso";
                    }
                })
                .blockingSubscribe(System.out::println,
                        error1 -> System.err.println("Erro ao remover o ficheiro: " + error1.getMessage())
                );
        }
    }

    public void removeFile(String nomeFile) {
        if (this.albumCRDT.containsFile(nomeFile))  {
            String hash = this.albumCRDT.getHashFile(nomeFile);
            try {
                removeFileDataServer(hash);
                State_CRDT_Message msg= this.albumCRDT.removeFile(nomeFile);
                this.sendMessage(msg,false);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro ao remover o ficheiro");
            }
        }
        else {
            System.out.println("O ficheiro " + nomeFile + " não existe.");
        }
    }

    public void downloadFileDataServer(String hash, String filePath) {
        final boolean[] flag = {true};
        while (flag[0]) {
            flag[0]= false;
            Zone dataServer = getDataServer(hash);
            var channel = ManagedChannelBuilder.forAddress(dataServer.getIp(), dataServer.getPort())
                    .usePlaintext()
                    .build();
            var stub = Rx3FileServiceGrpc.newRxStub(channel);

            FileDownloadRequest request = FileDownloadRequest.newBuilder()
                    .setSsaKey(hash)
                    .build();

            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                // Submeter a solicitação de download e processar as respostas
                stub.download(Flowable.just(request))
                    .map(response -> {
                        if (response.getMessage().equals("No autorization")) {
                            flag[0] = true;
                            getDataServers();
                            return Flowable.empty() ;
                        }
                        else {
                            try {
                                System.out.println(response);
                                // Escrever os bytes recebidos no arquivo
                                fileOutputStream.write(response.getChunk().toByteArray());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return Flowable.empty();
                        }
                    })
                    .blockingSubscribe(System.out::println);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Ficheiro descarregado com sucesso.");
    }

    public void downloadFile(String nomeFile, String filePath) {
        if (this.albumCRDT.containsFile(nomeFile))  {
            String hash = this.albumCRDT.getHashFile(nomeFile);
            try {
                downloadFileDataServer(hash,filePath);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro ao remover o ficheiro");
            }
        }
        else {
            System.out.println("O ficheiro " + nomeFile + " não existe.");
        }
    }

    public void infoAlbum() {
        Map<String,Object> album = this.albumCRDT.Album_Send();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<String, Object> entry : album.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ");
            Object value = entry.getValue();
            if (value instanceof Map) {
                sb.append("{\n");
                for (Map.Entry<?, ?> subEntry : ((Map<?, ?>) value).entrySet()) {
                    sb.append("    ").append(subEntry.getKey()).append(": ").append(subEntry.getValue()).append(",\n");
                }
                sb.append("  }");
            } else if (value instanceof Collection) {
                sb.append("[\n");
                for (Object item : (Collection<?>) value) {
                    sb.append("    ").append(item).append(",\n");
                }
                sb.append("  ]");
            } else {
                sb.append(value);
            }
            sb.append(",\n");
        }
        sb.append("}");
        System.out.println(sb.toString());
    }


}
