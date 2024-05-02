package org.example;

import com.ericsson.otp.erlang.*;
import com.google.common.hash.Hashing;
import inc.Rx3FileServiceGrpc;
import inc.TransferDataNewServerRequest;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.rxjava3.core.Flowable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.repeat;
import static org.example.Client_Upload.bytesToTuple;
import static org.example.Client_Upload.tupleToBytes;

public class DataServer1 {
    private SocketChannel ss;

    private static final String ip = "localhost";
    private static final int portNumber = 12346;

    private ArrayList<ZoneLimits> limits;

    private List<ZoneLimits> zoneLimits;
    public static void main(String[] args) throws Exception {

        String input1 = "Hello, World!_a";
        String input2 = "Hello, World!_b";
        String input3 = "Hello, World!_c";
        String input4 = "hello_";
        String input5 = "ola_b";
        String input6 = "ola_c";


        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final String hashBytes1 = Hashing.sha256()
                .hashString(input1, StandardCharsets.UTF_8)
                .toString();
        final String hashBytes2 = Hashing.sha256()
                .hashString(input2, StandardCharsets.UTF_8)
                .toString();
        final String hashBytes3 = Hashing.sha256()
                .hashString(input3, StandardCharsets.UTF_8)
                .toString();
        final String hashBytes4 = Hashing.sha256()
                .hashString(input4, StandardCharsets.UTF_8)
                .toString();
        final String hashBytes5 = Hashing.sha256()
                .hashString(input5, StandardCharsets.UTF_8)
                .toString();
        final String hashBytes6 = Hashing.sha256()
                .hashString(input6, StandardCharsets.UTF_8)
                .toString();


        ArrayList<Zone> zones= new ArrayList<>();
        zones.add(new Zone("localhost", 12346, hashBytes1));
        zones.add(new Zone("localhost", 12346, hashBytes2));
        zones.add(new Zone("localhost", 12346, hashBytes3));
        //zones.add(new Zone("localhost4", 12347, hashBytes4));
        //zones.add(new Zone("localhost5", 12347, hashBytes5));
        //zones.add(new Zone("localhost6", 12347, hashBytes6));
        Arrays.sort(zones.toArray());
        System.out.println(zones.toString());

        DataServer1 server = new DataServer1();
        //ArrayList<Zone> servers=server.OpenSession();
        System.out.println("aqui");
        ArrayList<Zone> vizinhos = server.getVizinhos(zones);
        System.out.println(vizinhos.toString());
        server.limits=server.getLimits(zones);
        System.out.println(server.limits.toString());
        System.out.println("aqui1");
        for (Zone z : vizinhos) {
            System.out.println("aqu2");
            server.GetData(z);
        }System.out.println("ativo");
        server.GRPCServer();
    }

    public ArrayList<Zone> getVizinhos(ArrayList<Zone> servers) {
        ArrayList<Zone> vizinhos = new ArrayList<>();
        Boolean add =false;
        for (Zone z : servers) {
            if (add && (!z.getIp().equals(this.ip) ||  z.getPort()!=this.portNumber)){
                System.out.println(z.toString());
                vizinhos.add(z);
                add=false;
            }
            else if (z.getIp().equals(this.ip) && z.getPort()==this.portNumber) {
                System.out.println(z.toString());
                add=true;
            }
        }
        //fecho do ciclo
        if (add  && (!servers.get(0).getIp().equals(this.ip) || servers.get(0).getPort()!=this.portNumber)) {
            vizinhos.add(servers.get(0));
        }
        return vizinhos;
    }

    public static String calculateMinimumSHA256() {
        // The minimum SHA-256 hash is a byte array filled with zeros
        return repeat("0", 64);
    }

    public static String calculateMaximumSHA256() {
        return repeat("f", 64);
    }

    public ArrayList<ZoneLimits> getLimits(ArrayList<Zone> servers) {
        ArrayList<ZoneLimits> limits = new ArrayList<>();
        String hash_before = calculateMinimumSHA256();
        for (Zone z : servers) {
            if (z.getIp().equals(this.ip) && z.getPort()==this.portNumber) {
                ZoneLimits zl = new ZoneLimits(hash_before, z.getHash());
                limits.add(zl);
            }
            hash_before = z.getHash();
        }
        //fecho do ciclo
        if (servers.get(0).getIp().equals(this.ip) && servers.get(0).getPort()==this.portNumber) {
            limits.add(new ZoneLimits(hash_before, calculateMaximumSHA256()));
        }
        return limits;
    }
    public ArrayList<Zone> OpenSession() throws Exception {
        this.ss = SocketChannel.open(new InetSocketAddress(portNumber));
        String input = "Hello, World!_a";

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());
        try {
            OtpErlangObject[] tuple = new OtpErlangObject[]{
                    new OtpErlangAtom("new_data_server"),
                    new OtpErlangTuple(new OtpErlangObject[]{
                            new OtpErlangString("localhost:"+portNumber),
                            new OtpErlangBinary(hashBytes)
                    })
            };
            OtpErlangTuple message = new OtpErlangTuple(tuple);
            ByteBuffer bb = ByteBuffer.wrap(tupleToBytes(message));
            ss.write(bb);

            bb.clear();
            while (ss.read(bb) > 0) {
                bb.flip();
                byte[] receivedBytes = new byte[bb.remaining()];
                bb.get(receivedBytes);
                OtpErlangTuple response = bytesToTuple(receivedBytes);
                OtpErlangObject[] fields = response.elements();

                OtpErlangObject firstField = fields[0];
                if (firstField instanceof OtpErlangList) {
                    // Converter a lista em um ArrayList
                    OtpErlangList lista = ((OtpErlangList) firstField);
                    ArrayList<String> arrayList = new ArrayList<>();

                    // Iterar sobre os elementos da lista Erlang
                    for (OtpErlangObject element : lista.elements()) {
                        // Converter cada elemento para uma string Java e adicionar ao ArrayList
                        String javaElement = ((OtpErlangString) element).stringValue();
                        arrayList.add(javaElement);
                    }
                    //return arrayList;
                    return new ArrayList<>();
                }
            }
            throw new Exception("Não foi possível obter uma resposta");

        }catch(Exception e){
            System.out.println("Não consegui escrever no socket" + e.getMessage());
        }
        this.ss.close();
        return new ArrayList<>();
    }

    public void GetData(Zone z) {
        var channel = ManagedChannelBuilder.forAddress(z.getIp(), z.getPort())
                .usePlaintext()
                .build();
        var stub = Rx3FileServiceGrpc.newRxStub(channel);
        TransferDataNewServerRequest request = TransferDataNewServerRequest.newBuilder()
                .setSsaKey(z.getHash())
                .build();
    System.out.println("aqui");
        final FileOutputStream[] fileOutputStream = {null};
        // Submeter a solicitação de download e processar as respostas
        stub.transferDataNewServer(Flowable.just(request))
                .map(response -> {
                    try {
                        if (response.getSeqNum()==1) {
                            fileOutputStream[0] = new FileOutputStream("ola"+response.getFileName());
                        }
                        // Escrever os bytes recebidos no arquivo
                        fileOutputStream[0].write(response.getChunk().toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Flowable.empty();
                })
                .blockingSubscribe(System.out::println);
    }
    public void GRPCServer() throws Exception {
        io.grpc.Server server = io.grpc.ServerBuilder.forPort(this.portNumber)
                .addService(new Service(limits))
                .build();
        server.start();
        server.awaitTermination();
    }
}
